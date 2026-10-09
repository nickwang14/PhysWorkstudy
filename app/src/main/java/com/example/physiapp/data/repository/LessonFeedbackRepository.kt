package com.example.physiapp.data.repository

import com.example.physiapp.data.model.LessonComment
import com.example.physiapp.data.model.LessonFeedbackContract
import com.example.physiapp.data.model.LessonFeedbackState
import com.example.physiapp.data.model.LessonReviewStatus
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/** Bound to one immutable account/lesson scope. Firestore rules remain the security boundary. */
@OptIn(ExperimentalCoroutinesApi::class)
class LessonFeedbackRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    val uid: String,
    val lessonId: String
) {
    private val boundUser: FirebaseUser? = auth.currentUser?.takeIf { it.uid == uid }
    private val version = AtomicLong()
    private data class Access(val version: Long, val admin: Boolean)
    private data class ScopeEvent(val version: Long, val failed: Boolean = false)
    private val activeAccess = AtomicReference<Access?>(null)
    private val failures = MutableSharedFlow<ScopeEvent>(extraBufferCapacity = 1)

    private fun accountMatches(): Boolean = boundUser != null && auth.currentUser === boundUser &&
        auth.currentUser?.uid == uid

    private fun validLesson(): Boolean = LessonFeedbackContract.isValidId(lessonId) &&
        CurriculumRepository.allLessons.any { it.id == lessonId }

    // No document path is constructed until the bundled individual lesson ID is validated.
    private fun lesson() = db.collection("curriculum_lessons").document(lessonId)

    /** Collect once per screen. Every token/auth event invalidates private data before claim lookup. */
    fun observe(): Flow<LessonFeedbackState> {
        if (!validLesson()) return flowOf(LessonFeedbackState(error = "Lesson feedback unavailable."))
        val authEvents = callbackFlow<ScopeEvent> {
            fun invalidate() {
                activeAccess.set(null)
                trySend(ScopeEvent(version.incrementAndGet()))
            }
            val authListener = FirebaseAuth.AuthStateListener { invalidate() }
            val tokenListener = FirebaseAuth.IdTokenListener { invalidate() }
            auth.addAuthStateListener(authListener)
            auth.addIdTokenListener(tokenListener)
            invalidate()
            awaitClose {
                activeAccess.set(null)
                auth.removeAuthStateListener(authListener)
                auth.removeIdTokenListener(tokenListener)
            }
        }.buffer(Channel.CONFLATED)

        return merge(authEvents, failures).transformLatest { event ->
            if (event.version != version.get()) return@transformLatest
            val scopeVersion = event.version
            emit(LessonFeedbackState(scopeVersion = scopeVersion))
            try {
                check(!event.failed)
                check(accountMatches())
                val token = boundUser!!.getIdToken(false).await()
                check(accountMatches() && version.get() == scopeVersion)
                val access = Access(scopeVersion, LessonFeedbackContract.isAdminClaim(token.claims["admin"]))
                activeAccess.set(access)
                emit(LessonFeedbackState(scopeVersion = scopeVersion, isAdmin = access.admin, ready = true))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                activeAccess.set(null)
                emit(LessonFeedbackState(scopeVersion = scopeVersion, error = "Lesson feedback unavailable. Sign in again or retry."))
            }
        }.flatMapLatest { scope ->
            val access = activeAccess.get()
            if (!scope.ready || access == null || access.version != scope.scopeVersion) {
                flowOf(scope.copy(ready = false, isAdmin = false))
            } else {
                observeDocuments(access)
            }
        }
    }

    private fun observeDocuments(access: Access): Flow<LessonFeedbackState> = callbackFlow {
        var comments: List<LessonComment> = emptyList()
        var status: LessonReviewStatus? = null
        var commentsLoaded = false
        var reviewLoaded = false
        var failed = false
        val registrations = mutableListOf<ListenerRegistration>()

        fun isCurrent() = !failed && accountMatches() && activeAccess.get() === access
        fun publish() {
            if (isCurrent()) trySend(LessonFeedbackState(
                scopeVersion = access.version,
                ready = commentsLoaded && reviewLoaded,
                isAdmin = access.admin,
                comments = if (commentsLoaded && reviewLoaded) comments else emptyList(),
                reviewStatus = status
            ))
        }
        fun failClosed() {
            failed = true
            activeAccess.compareAndSet(access, null)
            comments = emptyList()
            status = null
            registrations.forEach { it.remove() }
            // A new scope key also discards the editor draft and any pending UI result.
            trySend(LessonFeedbackState(scopeVersion = version.incrementAndGet(),
                error = "Lesson feedback unavailable. Retry when connected."))
        }

        trySend(LessonFeedbackState(scopeVersion = access.version))
        try {
            val commentsCollection = lesson().collection("comments")
            val query = if (access.admin) commentsCollection else commentsCollection.whereEqualTo("authorId", uid)
            registrations += query.limit(LessonFeedbackContract.COMMENT_LIMIT).addSnapshotListener { snapshot, error ->
                if (!isCurrent()) return@addSnapshotListener
                if (error != null || snapshot == null) {
                    failClosed()
                } else {
                    comments = snapshot.documents.mapNotNull { doc ->
                        val author = doc.get("authorId") as? String ?: return@mapNotNull null
                        val text = doc.get("text") as? String ?: return@mapNotNull null
                        if ((!access.admin && author != uid) || !LessonFeedbackContract.isValidComment(text)) {
                            return@mapNotNull null
                        }
                        LessonComment(doc.id, author, text, (doc.get("createdAt") as? Timestamp)?.toDate()?.time)
                    }.sortedWith(compareByDescending<LessonComment> { it.createdAtMillis ?: Long.MAX_VALUE }.thenBy { it.id })
                    commentsLoaded = true
                    publish()
                }
            }
            registrations += lesson().collection("review").document("status").addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (!isCurrent()) return@addSnapshotListener
                if (error != null || snapshot == null) {
                    failClosed()
                } else {
                    val incoming = LessonReviewStatus.fromWire(snapshot.get("status") as? String)
                    val pending = snapshot.metadata.hasPendingWrites()
                    if (!pending && snapshot.exists() && incoming == null) {
                        failClosed()
                    } else {
                        status = LessonFeedbackContract.confirmedReviewStatus(status, incoming, pending)
                        // Without a prior acknowledged snapshot, do not label pending data as "Not reviewed".
                        if (!pending) reviewLoaded = true
                        publish()
                    }
                }
            }
        } catch (_: Exception) {
            failClosed()
        }
        awaitClose {
            registrations.forEach { it.remove() }
            activeAccess.compareAndSet(access, null)
        }
    }

    private suspend fun checkedAccess(requireAdmin: Boolean = false): Access = try {
        check(validLesson() && accountMatches()) { "Lesson feedback unavailable." }
        val access = checkNotNull(activeAccess.get()) { "Lesson feedback unavailable." }
        val token = boundUser!!.getIdToken(false).await()
        check(accountMatches() && activeAccess.get() === access) { "Lesson feedback unavailable." }
        check(access.admin == LessonFeedbackContract.isAdminClaim(token.claims["admin"])) {
            "Access changed. Retry lesson feedback."
        }
        check(!requireAdmin || (access.admin && LessonFeedbackContract.isAdminClaim(token.claims["admin"]))) {
            "Administrator access required."
        }
        access
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        activeAccess.set(null)
        failures.tryEmit(ScopeEvent(version.incrementAndGet(), failed = true))
        throw IllegalStateException("Lesson feedback unavailable.")
    }

    /** Synchronous only: transaction callbacks can retry and cannot perform suspend claim lookups. */
    private fun validateTransactionAccess(access: Access, requireAdmin: Boolean = false) {
        check(validLesson() && accountMatches() && activeAccess.get() === access && version.get() == access.version) {
            "Lesson feedback unavailable."
        }
        check(!requireAdmin || access.admin) { "Administrator access required." }
    }

    private suspend fun write(action: suspend () -> Unit): Result<Unit> = try {
        val access = activeAccess.get()
        action()
        check(accountMatches() && access != null && activeAccess.get() === access) {
            "Lesson feedback scope changed."
        }
        Result.success(Unit)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        // Never propagate provider messages, comment text, or sensitive exception details to UI/logs.
        Result.failure(IllegalStateException("Unable to save lesson feedback. Check your connection and access."))
    }

    suspend fun addComment(text: String): Result<Unit> = write {
        val payload = LessonFeedbackContract.commentPayload(uid, text, FieldValue.serverTimestamp())
        val access = checkedAccess()
        val parent = lesson()
        // Allocate once, not on each callback retry.
        val reference = parent.collection("comments").document()
        db.runTransaction { transaction ->
            validateTransactionAccess(access)
            check(transaction.get(parent).exists()) { "Lesson feedback unavailable." }
            validateTransactionAccess(access)
            transaction.set(reference, payload)
            Unit
        }.await()
    }

    suspend fun deleteComment(commentId: String): Result<Unit> = write {
        require(LessonFeedbackContract.isValidId(commentId)) { "Invalid comment ID." }
        val access = checkedAccess()
        val reference = lesson().collection("comments").document(commentId)
        db.runTransaction { transaction ->
            validateTransactionAccess(access)
            val comment = transaction.get(reference)
            check(comment.exists()) { "Lesson feedback unavailable." }
            val author = checkNotNull(comment.get("authorId") as? String) { "Lesson feedback unavailable." }
            validateTransactionAccess(access, requireAdmin = author != uid)
            transaction.delete(reference)
            Unit
        }.await()
    }

    suspend fun setReviewStatus(status: LessonReviewStatus): Result<Unit> = write {
        val access = checkedAccess(requireAdmin = true)
        val parent = lesson()
        val reference = parent.collection("review").document("status")
        val payload = LessonFeedbackContract.reviewPayload(uid, status, FieldValue.serverTimestamp())
        // Reads make offline transactions fail rather than queue writes. Rules authorize the commit;
        // cancelling await (or changing auth after dispatch) cannot cancel an already dispatched commit.
        db.runTransaction { transaction ->
            validateTransactionAccess(access, requireAdmin = true)
            check(transaction.get(parent).exists()) { "Lesson feedback unavailable." }
            validateTransactionAccess(access, requireAdmin = true)
            transaction.set(reference, payload)
            Unit
        }.await()
    }
}