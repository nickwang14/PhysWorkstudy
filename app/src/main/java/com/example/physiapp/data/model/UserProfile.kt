package com.example.physiapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

/**
 * Firestore data model representing user account profile and training preferences.
 * All properties provide default values to support synthetic no-arg constructors.
 */
data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val trainingGoal: String = "Strength & Hypertrophy",
    val fitnessLevel: String = "Intermediate",
    val weeklyTargetWorkouts: Long = 4L,
    val favoriteSplit: String = "Upper / Lower (4-Day Hypertrophy)",
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    /**
     * Converts domain model to Firestore write map with server timestamps.
     * Filters out null values to comply with zero-trust security rules.
     */
    @Suppress("UNCHECKED_CAST")
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "trainingGoal" to trainingGoal,
            "fitnessLevel" to fitnessLevel,
            "weeklyTargetWorkouts" to weeklyTargetWorkouts,
            "favoriteSplit" to favoriteSplit,
            "weightKg" to weightKg,
            "heightCm" to heightCm,
            "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map.filterValues { it != null } as Map<String, Any>
    }
}
