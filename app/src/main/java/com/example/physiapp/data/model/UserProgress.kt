package com.example.physiapp.data.model

data class ReadinessEntry(
    val epochDay: Long,
    val sleepQuality: Int, // 1 to 5
    val sorenessLevel: Int, // 1 to 5
    val energyLevel: Int, // 1 to 5
    val physioAdvice: String
)

data class ConsistencyBadge(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val iconName: String,
    val isEarned: Boolean,
    val earnedDateEpochDay: Long? = null
)

data class UserPreferences(
    val weeklyGoalTarget: Int = 3, // min 2
    val weightUnit: String = "kg",
    val trainingGoal: String = "Movement Foundations",
    val defaultRestTimeSeconds: Int = 90
)

data class DualTrackProgress(
    val dailyLearningStreak: Int = 1,
    val lastLearningEpochDay: Long = 0L,
    val completedLessonIds: Set<String> = emptySet(),
    val weeklyGoalTarget: Int = 3,
    val workoutsCompletedThisWeek: Int = 1,
    val weeklyStreakWeeks: Int = 2,
    val currentWeekInCycle: Int = 3, // 1 to 6 (week 6 is deload)
    val isDeloadAcknowledged: Boolean = false,
    val totalWorkoutsLogged: Int = 7,
    val totalLessonsCompleted: Int = 4,
    val lastReadinessCheckin: ReadinessEntry? = null
)
