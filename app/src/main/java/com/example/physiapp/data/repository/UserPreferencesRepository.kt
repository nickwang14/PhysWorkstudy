package com.example.physiapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.physiapp.data.model.ConsistencyBadge
import com.example.physiapp.data.model.DualTrackProgress
import com.example.physiapp.data.model.ExerciseSet
import com.example.physiapp.data.model.FavoriteLearningItems
import com.example.physiapp.data.model.LoggedExercise
import com.example.physiapp.data.model.LoggedWorkoutSession
import com.example.physiapp.data.model.MovementPattern
import com.example.physiapp.data.model.ReadinessEntry
import com.example.physiapp.data.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("physiapp_prefs", Context.MODE_PRIVATE)

    private val _progressFlow = MutableStateFlow(loadProgress())
    val progressFlow: StateFlow<DualTrackProgress> = _progressFlow.asStateFlow()

    private val _prefsFlow = MutableStateFlow(loadPreferences())
    val prefsFlow: StateFlow<UserPreferences> = _prefsFlow.asStateFlow()

    private val _workoutHistoryFlow = MutableStateFlow(loadWorkoutHistory())
    val workoutHistoryFlow: StateFlow<List<LoggedWorkoutSession>> = _workoutHistoryFlow.asStateFlow()

    private val _favoritesFlow = MutableStateFlow(loadFavorites())
    val favoritesFlow: StateFlow<FavoriteLearningItems> = _favoritesFlow.asStateFlow()

    private fun loadFavorites(): FavoriteLearningItems = FavoriteLearningItems(
        lessonIds = prefs.getStringSet("favorite_lesson_ids", emptySet())?.toSet() ?: emptySet(),
        optionalReadingIds = prefs.getStringSet("favorite_optional_reading_ids", emptySet())?.toSet() ?: emptySet()
    )

    fun toggleFavoriteLesson(lessonId: String) {
        val current = _favoritesFlow.value
        val updatedIds = toggleFavoriteId("favorite_lesson_ids", current.lessonIds, lessonId)
        _favoritesFlow.value = current.copy(lessonIds = updatedIds)
    }

    fun toggleFavoriteOptionalReading(readingId: String) {
        val current = _favoritesFlow.value
        val updatedIds = toggleFavoriteId("favorite_optional_reading_ids", current.optionalReadingIds, readingId)
        _favoritesFlow.value = current.copy(optionalReadingIds = updatedIds)
    }

    private fun toggleFavoriteId(key: String, currentIds: Set<String>, id: String): Set<String> {
        val updatedIds = if (id in currentIds) currentIds - id else currentIds + id
        prefs.edit().putStringSet(key, HashSet(updatedIds)).apply()
        return updatedIds
    }

    private fun loadPreferences(): UserPreferences {
        val target = prefs.getInt("weekly_goal_target", 3).coerceAtLeast(2)
        val unit = prefs.getString("weight_unit", "kg") ?: "kg"
        val goal = prefs.getString("training_goal", "Movement Foundations") ?: "Movement Foundations"
        val restTime = prefs.getInt("rest_time_seconds", 90)
        return UserPreferences(
            weeklyGoalTarget = target,
            weightUnit = unit,
            trainingGoal = goal,
            defaultRestTimeSeconds = restTime
        )
    }

    private fun loadProgress(): DualTrackProgress {
        val streak = prefs.getInt("daily_streak", 1)
        val lastEpochDay = prefs.getLong("last_learning_epoch_day", 0L)
        val completedLessons = prefs.getStringSet("completed_lessons", emptySet()) ?: emptySet()
        val weeklyTarget = prefs.getInt("weekly_goal_target", 3).coerceAtLeast(2)
        val completedThisWeek = prefs.getInt("workouts_completed_this_week", 1)
        val weeklyStreak = prefs.getInt("weekly_streak_weeks", 2)
        val weekInCycle = prefs.getInt("week_in_cycle", 3)
        val deloadAck = prefs.getBoolean("deload_acknowledged", false)
        val totalWorkouts = prefs.getInt("total_workouts", 6)
        val totalLessons = prefs.getInt("total_lessons", completedLessons.size.coerceAtLeast(2))

        val readinessDay = prefs.getLong("readiness_day", 0L)
        val lastReadiness = if (readinessDay > 0) {
            ReadinessEntry(
                epochDay = readinessDay,
                sleepQuality = prefs.getInt("readiness_sleep", 4),
                sorenessLevel = prefs.getInt("readiness_soreness", 2),
                energyLevel = prefs.getInt("readiness_energy", 4),
                physioAdvice = prefs.getString("readiness_advice", "Readiness optimal. Proceed with standard progression.") ?: ""
            )
        } else null

        return DualTrackProgress(
            dailyLearningStreak = streak,
            lastLearningEpochDay = lastEpochDay,
            completedLessonIds = completedLessons,
            weeklyGoalTarget = weeklyTarget,
            workoutsCompletedThisWeek = completedThisWeek,
            weeklyStreakWeeks = weeklyStreak,
            currentWeekInCycle = weekInCycle,
            isDeloadAcknowledged = deloadAck,
            totalWorkoutsLogged = totalWorkouts,
            totalLessonsCompleted = totalLessons,
            lastReadinessCheckin = lastReadiness
        )
    }

    private fun loadWorkoutHistory(): List<LoggedWorkoutSession> {
        val jsonStr = prefs.getString("workout_history_json", null)
        if (jsonStr.isNullOrEmpty()) {
            return defaultSampleHistory()
        }
        return try {
            val list = mutableListOf<LoggedWorkoutSession>()
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val exArray = obj.getJSONArray("exercises")
                val exList = mutableListOf<LoggedExercise>()
                for (j in 0 until exArray.length()) {
                    val exObj = exArray.getJSONObject(j)
                    val patternName = exObj.optString("pattern", "SQUAT")
                    val pattern = try { MovementPattern.valueOf(patternName) } catch (e: Exception) { MovementPattern.SQUAT }
                    val setsArray = exObj.getJSONArray("sets")
                    val setsList = mutableListOf<ExerciseSet>()
                    for (k in 0 until setsArray.length()) {
                        val sObj = setsArray.getJSONObject(k)
                        setsList.add(
                            ExerciseSet(
                                setNumber = sObj.optInt("setNumber", k + 1),
                                reps = sObj.optInt("reps", 10),
                                weightKg = sObj.optDouble("weightKg", 0.0),
                                rpe = sObj.optInt("rpe", 7),
                                isCompleted = sObj.optBoolean("isCompleted", true)
                            )
                        )
                    }
                    exList.add(
                        LoggedExercise(
                            exerciseId = exObj.optString("exerciseId", ""),
                            exerciseName = exObj.optString("exerciseName", "Exercise"),
                            pattern = pattern,
                            sets = setsList,
                            notes = exObj.optString("notes", "")
                        )
                    )
                }
                list.add(
                    LoggedWorkoutSession(
                        id = obj.getString("id"),
                        templateId = obj.optString("templateId", null),
                        title = obj.getString("title"),
                        timestampMillis = obj.getLong("timestampMillis"),
                        durationMinutes = obj.optInt("durationMinutes", 40),
                        exercises = exList,
                        isDeload = obj.optBoolean("isDeload", false),
                        sessionRpe = obj.optInt("sessionRpe", 7),
                        notes = obj.optString("notes", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            defaultSampleHistory()
        }
    }

    private fun defaultSampleHistory(): List<LoggedWorkoutSession> {
        val now = System.currentTimeMillis()
        val oneDay = TimeUnit.DAYS.toMillis(1)
        val ex1 = LoggedExercise(
            exerciseId = "sq-1",
            exerciseName = "Goblet Squat",
            pattern = MovementPattern.SQUAT,
            sets = mutableListOf(
                ExerciseSet(1, 10, 20.0, 7, true),
                ExerciseSet(2, 10, 20.0, 7, true),
                ExerciseSet(3, 8, 24.0, 8, true)
            )
        )
        val ex2 = LoggedExercise(
            exerciseId = "ps-1",
            exerciseName = "Dumbbell Flat Bench Press",
            pattern = MovementPattern.PUSH,
            sets = mutableListOf(
                ExerciseSet(1, 10, 18.0, 7, true),
                ExerciseSet(2, 10, 18.0, 7, true),
                ExerciseSet(3, 10, 18.0, 8, true)
            )
        )
        val ex3 = LoggedExercise(
            exerciseId = "pl-1",
            exerciseName = "Single-Arm Dumbbell Row",
            pattern = MovementPattern.PULL,
            sets = mutableListOf(
                ExerciseSet(1, 10, 20.0, 7, true),
                ExerciseSet(2, 10, 20.0, 7, true),
                ExerciseSet(3, 10, 20.0, 8, true)
            )
        )
        return listOf(
            LoggedWorkoutSession(
                id = "sample-1",
                templateId = "tmpl-fullbody-a",
                title = "Full Body Foundations A",
                timestampMillis = now - oneDay * 2,
                durationMinutes = 45,
                exercises = listOf(ex1, ex2, ex3),
                isDeload = false,
                sessionRpe = 7,
                notes = "Solid rhythm. Focused on tripod foot pressure and scapular retraction."
            )
        )
    }

    private fun currentEpochDay(): Long {
        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    }

    fun completeLesson(lessonId: String) {
        val current = _progressFlow.value
        val today = currentEpochDay()
        val updatedSet = current.completedLessonIds + lessonId

        val newStreak = if (current.lastLearningEpochDay == today) {
            current.dailyLearningStreak // already counted today
        } else if (current.lastLearningEpochDay == today - 1) {
            current.dailyLearningStreak + 1 // continued streak
        } else {
            1 // streak restarted or newly started
        }

        val updated = current.copy(
            dailyLearningStreak = newStreak,
            lastLearningEpochDay = today,
            completedLessonIds = updatedSet,
            totalLessonsCompleted = updatedSet.size
        )

        prefs.edit()
            .putInt("daily_streak", updated.dailyLearningStreak)
            .putLong("last_learning_epoch_day", updated.lastLearningEpochDay)
            .putStringSet("completed_lessons", updated.completedLessonIds)
            .putInt("total_lessons", updated.totalLessonsCompleted)
            .apply()

        _progressFlow.value = updated
    }

    fun logWorkout(session: LoggedWorkoutSession) {
        val current = _progressFlow.value
        val currentHistory = _workoutHistoryFlow.value.toMutableList()
        currentHistory.add(0, session) // most recent first

        val newCount = current.workoutsCompletedThisWeek + 1
        var newStreakWeeks = current.weeklyStreakWeeks
        if (newCount == current.weeklyGoalTarget) {
            newStreakWeeks += 1
        }

        // Deload cadence check: if at week 6 and logging a deload workout, advance to week 1 of next cycle!
        var nextWeekInCycle = current.currentWeekInCycle
        var deloadAck = current.isDeloadAcknowledged
        if (session.isDeload || current.currentWeekInCycle >= 6 && newCount >= current.weeklyGoalTarget) {
            nextWeekInCycle = 1
            deloadAck = false
        }

        val updated = current.copy(
            workoutsCompletedThisWeek = newCount,
            weeklyStreakWeeks = newStreakWeeks,
            currentWeekInCycle = nextWeekInCycle,
            isDeloadAcknowledged = deloadAck,
            totalWorkoutsLogged = current.totalWorkoutsLogged + 1
        )

        saveWorkoutHistory(currentHistory)

        prefs.edit()
            .putInt("workouts_completed_this_week", updated.workoutsCompletedThisWeek)
            .putInt("weekly_streak_weeks", updated.weeklyStreakWeeks)
            .putInt("week_in_cycle", updated.currentWeekInCycle)
            .putBoolean("deload_acknowledged", updated.isDeloadAcknowledged)
            .putInt("total_workouts", updated.totalWorkoutsLogged)
            .apply()

        _progressFlow.value = updated
        _workoutHistoryFlow.value = currentHistory
    }

    private fun saveWorkoutHistory(list: List<LoggedWorkoutSession>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("templateId", item.templateId ?: "")
            obj.put("title", item.title)
            obj.put("timestampMillis", item.timestampMillis)
            obj.put("durationMinutes", item.durationMinutes)
            obj.put("isDeload", item.isDeload)
            obj.put("sessionRpe", item.sessionRpe)
            obj.put("notes", item.notes)

            val exArray = JSONArray()
            for (ex in item.exercises) {
                val exObj = JSONObject()
                exObj.put("exerciseId", ex.exerciseId)
                exObj.put("exerciseName", ex.exerciseName)
                exObj.put("pattern", ex.pattern.name)
                exObj.put("notes", ex.notes)

                val setsArray = JSONArray()
                for (s in ex.sets) {
                    val sObj = JSONObject()
                    sObj.put("setNumber", s.setNumber)
                    sObj.put("reps", s.reps)
                    sObj.put("weightKg", s.weightKg)
                    sObj.put("rpe", s.rpe)
                    sObj.put("isCompleted", s.isCompleted)
                    setsArray.put(sObj)
                }
                exObj.put("sets", setsArray)
                exArray.put(exObj)
            }
            obj.put("exercises", exArray)
            array.put(obj)
        }
        prefs.edit().putString("workout_history_json", array.toString()).apply()
    }

    fun acknowledgeDeload() {
        val updated = _progressFlow.value.copy(isDeloadAcknowledged = true)
        prefs.edit().putBoolean("deload_acknowledged", true).apply()
        _progressFlow.value = updated
    }

    fun advanceWeekCycle() {
        val current = _progressFlow.value
        val nextWeek = if (current.currentWeekInCycle >= 6) 1 else current.currentWeekInCycle + 1
        val updated = current.copy(
            currentWeekInCycle = nextWeek,
            workoutsCompletedThisWeek = 0,
            isDeloadAcknowledged = false
        )
        prefs.edit()
            .putInt("week_in_cycle", nextWeek)
            .putInt("workouts_completed_this_week", 0)
            .putBoolean("deload_acknowledged", false)
            .apply()
        _progressFlow.value = updated
    }

    fun recordReadiness(sleep: Int, soreness: Int, energy: Int) {
        val today = currentEpochDay()
        val advice = when {
            soreness >= 4 -> "Elevated muscle soreness: Focus on mobility, hydration, and light active recovery."
            sleep <= 2 -> "Sub-optimal sleep: Moderate training volume and target RPE 6-7 to avoid central fatigue."
            energy >= 4 && soreness <= 2 -> "Readiness high: Prime condition for your programmed movement patterns."
            else -> "Steady readiness: Execute your session with clean form and controlled rest intervals."
        }
        val entry = ReadinessEntry(today, sleep, soreness, energy, advice)
        prefs.edit()
            .putLong("readiness_day", today)
            .putInt("readiness_sleep", sleep)
            .putInt("readiness_soreness", soreness)
            .putInt("readiness_energy", energy)
            .putString("readiness_advice", advice)
            .apply()

        _progressFlow.value = _progressFlow.value.copy(lastReadinessCheckin = entry)
    }

    fun updatePreferences(weeklyGoal: Int, weightUnit: String, goal: String, restTime: Int) {
        val clampedGoal = weeklyGoal.coerceAtLeast(2)
        prefs.edit()
            .putInt("weekly_goal_target", clampedGoal)
            .putString("weight_unit", weightUnit)
            .putString("training_goal", goal)
            .putInt("rest_time_seconds", restTime)
            .apply()

        _prefsFlow.value = UserPreferences(clampedGoal, weightUnit, goal, restTime)
        _progressFlow.value = _progressFlow.value.copy(weeklyGoalTarget = clampedGoal)
    }

    fun getBadges(): List<ConsistencyBadge> {
        val progress = _progressFlow.value
        return listOf(
            ConsistencyBadge(
                id = "badge-first-lesson",
                title = "Curiosity Sparked",
                subtitle = "First lesson completed",
                description = "Completed your first movement science micro-lesson and knowledge check.",
                iconName = "school",
                isEarned = progress.totalLessonsCompleted >= 1
            ),
            ConsistencyBadge(
                id = "badge-streak-3",
                title = "Daily Knowledge Habit",
                subtitle = "3-day learning streak",
                description = "Showed up 3 consecutive days to study evidence-based biomechanics.",
                iconName = "local_fire_department",
                isEarned = progress.dailyLearningStreak >= 3
            ),
            ConsistencyBadge(
                id = "badge-streak-7",
                title = "Seven Days of Science",
                subtitle = "7-day learning streak",
                description = "A full week of intentional daily learning habit.",
                iconName = "stars",
                isEarned = progress.dailyLearningStreak >= 7
            ),
            ConsistencyBadge(
                id = "badge-training-goal",
                title = "Weekly Foundation",
                subtitle = "Met weekly training target",
                description = "Achieved your weekly workout goal without sacrificing rest days.",
                iconName = "check_circle",
                isEarned = progress.workoutsCompletedThisWeek >= progress.weeklyGoalTarget || progress.weeklyStreakWeeks >= 1
            ),
            ConsistencyBadge(
                id = "badge-deload-master",
                title = "Smart Recovery Master",
                subtitle = "Deload week participant",
                description = "Acknowledged and embraced planned periodization for connective tissue remodeling.",
                iconName = "spa",
                isEarned = progress.currentWeekInCycle == 6 || progress.isDeloadAcknowledged
            ),
            ConsistencyBadge(
                id = "badge-pattern-balance",
                title = "Balanced Mover",
                subtitle = "5+ workouts logged",
                description = "Consistently trained across multiple movement patterns for balanced joint resilience.",
                iconName = "fitness_center",
                isEarned = progress.totalWorkoutsLogged >= 5
            )
        )
    }
}
