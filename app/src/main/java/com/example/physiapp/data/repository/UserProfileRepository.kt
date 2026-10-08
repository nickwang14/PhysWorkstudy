package com.example.physiapp.data.repository

import android.content.Context
import android.util.Log
import com.example.physiapp.R
import com.example.physiapp.data.firebase.OperationType
import com.example.physiapp.data.firebase.handleFirestoreError
import com.example.physiapp.data.model.LessonProgressDoc
import com.example.physiapp.data.model.UserProfile
import com.example.physiapp.data.model.WorkoutLogDoc
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "UserProfileRepository"

class UserProfileRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    /**
     * Convenience constructor resolving the database ID from string resources as required by guidelines.
     */
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Operation requires an authenticated user")
    }

    /**
     * Real-time flow observing user profile from /users/{userId}.
     */
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val docRef = db.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(
                    UserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    /**
     * Saves or merges user profile into /users/{userId}.
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val payload = profile.copy(userId = uid).toMap()
            docRef.set(payload, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Real-time flow observing user workout logs from /users/{userId}/workout_logs.
     */
    fun observeWorkoutLogs(userId: String): Flow<List<WorkoutLogDoc>> = callbackFlow {
        val collectionRef = db.collection("users").document(userId).collection("workout_logs")
        val listener = collectionRef
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, collectionRef.path)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val logs = snapshot.toObjects(
                        WorkoutLogDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    trySend(logs)
                } else {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Adds a new workout log to /users/{userId}/workout_logs/{logId}.
     */
    suspend fun logWorkout(workoutLog: WorkoutLogDoc): Result<Unit> {
        val uid = requireUserId()
        val id = if (workoutLog.logId.isBlank()) {
            "log_${System.currentTimeMillis()}"
        } else {
            workoutLog.logId
        }
        val docRef = db.collection("users").document(uid).collection("workout_logs").document(id)
        return try {
            val payload = workoutLog.copy(logId = id, userId = uid).toMap()
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Deletes a workout log entry.
     */
    suspend fun deleteWorkoutLog(logId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("workout_logs").document(logId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Saves or updates lesson completion status in Cloud Firestore at /users/{userId}/lesson_progress/{lessonId}.
     */
    suspend fun saveLessonProgress(lessonId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("lesson_progress").document(lessonId)
        return try {
            val progressDoc = LessonProgressDoc(
                lessonId = lessonId,
                userId = uid,
                completedAt = null
            )
            docRef.set(progressDoc.toMap(), SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Real-time flow observing completed lesson IDs from /users/{userId}/lesson_progress.
     */
    fun observeLessonProgress(userId: String): Flow<Set<String>> = callbackFlow {
        val collectionRef = db.collection("users").document(userId).collection("lesson_progress")
        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, collectionRef.path)
                trySend(emptySet())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val completedIds = snapshot.documents.mapNotNull { it.getString("lessonId") ?: it.id }.toSet()
                trySend(completedIds)
            } else {
                trySend(emptySet())
            }
        }
        awaitClose { listener.remove() }
    }
}
