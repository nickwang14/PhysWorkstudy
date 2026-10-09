package com.example.physiapp.data.model

enum class LessonReviewStatus(val wireValue: String, val label: String) {
    APPROVED("approved", "Approved"),
    REJECTED("rejected", "Rejected"),
    NEEDS_IMPROVEMENT("needs_improvement", "Needs improvement");

    companion object {
        fun fromWire(value: String?): LessonReviewStatus? = entries.find { it.wireValue == value }
    }
}

object LessonFeedbackContract {
    const val MAX_COMMENT_LENGTH = 2000
    const val COMMENT_LIMIT = 50L

    fun isValidId(id: String): Boolean = id.isNotBlank() && '/' !in id
    fun isValidComment(text: String): Boolean = text.isNotBlank() && text.length <= MAX_COMMENT_LENGTH
    fun isAdminClaim(value: Any?): Boolean = value == true

    fun confirmedReviewStatus(
        previous: LessonReviewStatus?,
        incoming: LessonReviewStatus?,
        hasPendingWrites: Boolean
    ): LessonReviewStatus? = if (hasPendingWrites) previous else incoming

    fun commentPayload(uid: String, text: String, serverTimestamp: Any): Map<String, Any> {
        require(uid.isNotBlank()) { "Authentication required." }
        require(isValidComment(text)) { "Enter a nonblank comment of at most 2000 characters." }
        return mapOf("authorId" to uid, "text" to text.trim(), "createdAt" to serverTimestamp)
    }

    fun reviewPayload(uid: String, status: LessonReviewStatus, serverTimestamp: Any): Map<String, Any> {
        require(uid.isNotBlank()) { "Authentication required." }
        return mapOf("status" to status.wireValue, "updatedBy" to uid, "updatedAt" to serverTimestamp)
    }
}

data class LessonComment(
    val id: String,
    val authorId: String,
    val text: String,
    val createdAtMillis: Long? = null
)

data class LessonFeedbackState(
    val scopeVersion: Long = 0,
    val ready: Boolean = false,
    val isAdmin: Boolean = false,
    val comments: List<LessonComment> = emptyList(),
    val reviewStatus: LessonReviewStatus? = null,
    val error: String? = null
)