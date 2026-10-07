package com.example.physiapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

/**
 * Firestore data model representing a user's logged workout session stored
 * under /users/{userId}/workout_logs/{logId}.
 */
data class WorkoutLogDoc(
    val logId: String = "",
    val userId: String = "",
    val workoutName: String = "",
    val splitDay: String = "",
    val durationMinutes: Long = 0L,
    val completedExercisesCount: Long = 0L,
    val totalExercisesCount: Long = 0L,
    val notes: String? = null,
    val completedAt: Timestamp? = null,
    val createdAt: Timestamp? = null
) {
    /**
     * Converts to Firestore write map using server timestamps and filtering nulls.
     */
    @Suppress("UNCHECKED_CAST")
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "logId" to logId,
            "userId" to userId,
            "workoutName" to workoutName,
            "splitDay" to splitDay,
            "durationMinutes" to durationMinutes,
            "completedExercisesCount" to completedExercisesCount,
            "totalExercisesCount" to totalExercisesCount,
            "notes" to notes,
            "completedAt" to (completedAt ?: FieldValue.serverTimestamp()),
            "createdAt" to (createdAt ?: FieldValue.serverTimestamp())
        )
        return map.filterValues { it != null } as Map<String, Any>
    }
}
