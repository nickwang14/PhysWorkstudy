package com.example.physiapp.data.repository

import com.example.physiapp.data.model.ExerciseDef
import com.example.physiapp.data.model.ExerciseSet
import com.example.physiapp.data.model.LoggedExercise
import com.example.physiapp.data.model.LoggedWorkoutSession
import com.example.physiapp.data.model.WorkoutTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ActiveWorkoutSessionState(
    val id: String = UUID.randomUUID().toString(),
    val templateId: String? = null,
    val title: String = "Workout Session",
    val exercises: List<LoggedExercise> = emptyList(),
    val isDeload: Boolean = false,
    val startTimeMillis: Long = System.currentTimeMillis()
)

object ActiveWorkoutManager {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    private val _currentSession = MutableStateFlow<ActiveWorkoutSessionState?>(null)
    val currentSession: StateFlow<ActiveWorkoutSessionState?> = _currentSession.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _isMinimized = MutableStateFlow(false)
    val isMinimized: StateFlow<Boolean> = _isMinimized.asStateFlow()

    fun startWorkout(template: WorkoutTemplate?) {
        val initialExercises = mutableListOf<LoggedExercise>()
        val isDeload = template?.isDeloadTemplate ?: false
        val setCount = if (isDeload) 2 else 3

        if (template != null) {
            val defs = template.defaultExerciseIds.mapNotNull { ExerciseRepository.getExerciseById(it) }
            defs.forEach { def ->
                val sets = (1..setCount).map {
                    ExerciseSet(
                        setNumber = it,
                        reps = 10,
                        weightKg = 20.0,
                        rpe = if (isDeload) 5 else 7,
                        isCompleted = false
                    )
                }.toMutableList()
                initialExercises.add(
                    LoggedExercise(
                        exerciseId = def.id,
                        exerciseName = def.name,
                        pattern = def.pattern,
                        sets = sets
                    )
                )
            }
        } else {
            val def = ExerciseRepository.exercises.first()
            val sets = mutableListOf(
                ExerciseSet(1, 10, 20.0, 7, false),
                ExerciseSet(2, 10, 20.0, 7, false),
                ExerciseSet(3, 10, 20.0, 7, false)
            )
            initialExercises.add(
                LoggedExercise(
                    exerciseId = def.id,
                    exerciseName = def.name,
                    pattern = def.pattern,
                    sets = sets
                )
            )
        }

        _currentSession.value = ActiveWorkoutSessionState(
            id = UUID.randomUUID().toString(),
            templateId = template?.id,
            title = template?.title ?: "Custom Workout",
            exercises = initialExercises,
            isDeload = isDeload
        )
        _elapsedSeconds.value = 0
        _isMinimized.value = false
        resumeTimer()
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun resumeTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_isTimerRunning.value) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    fun togglePlayPause() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            resumeTimer()
        }
    }

    fun minimize() {
        _isMinimized.value = true
    }

    fun maximize() {
        _isMinimized.value = false
    }

    fun markSetCompletion(exerciseIndex: Int, setIndex: Int, isCompleted: Boolean) {
        val session = _currentSession.value ?: return
        val updatedExercises = session.exercises.mapIndexed { eIdx, ex ->
            if (eIdx == exerciseIndex) {
                val updatedSets = ex.sets.mapIndexed { sIdx, set ->
                    if (sIdx == setIndex) set.copy(isCompleted = isCompleted) else set
                }.toMutableList()
                ex.copy(sets = updatedSets)
            } else ex
        }
        _currentSession.value = session.copy(exercises = updatedExercises)
    }

    fun updateSetValues(exerciseIndex: Int, setIndex: Int, reps: Int, weightKg: Double, rpe: Int) {
        val session = _currentSession.value ?: return
        val updatedExercises = session.exercises.mapIndexed { eIdx, ex ->
            if (eIdx == exerciseIndex) {
                val updatedSets = ex.sets.mapIndexed { sIdx, set ->
                    if (sIdx == setIndex) set.copy(reps = reps, weightKg = weightKg, rpe = rpe) else set
                }.toMutableList()
                ex.copy(sets = updatedSets)
            } else ex
        }
        _currentSession.value = session.copy(exercises = updatedExercises)
    }

    fun addSetToExercise(exerciseIndex: Int) {
        val session = _currentSession.value ?: return
        val updatedExercises = session.exercises.mapIndexed { eIdx, ex ->
            if (eIdx == exerciseIndex) {
                val lastSet = ex.sets.lastOrNull()
                val nextNum = ex.sets.size + 1
                val newSet = ExerciseSet(
                    setNumber = nextNum,
                    reps = lastSet?.reps ?: 10,
                    weightKg = lastSet?.weightKg ?: 20.0,
                    rpe = lastSet?.rpe ?: 7,
                    isCompleted = false
                )
                val updatedSets = (ex.sets + newSet).toMutableList()
                ex.copy(sets = updatedSets)
            } else ex
        }
        _currentSession.value = session.copy(exercises = updatedExercises)
    }

    fun addExerciseToSession(def: ExerciseDef) {
        val session = _currentSession.value ?: return
        val newSets = mutableListOf(
            ExerciseSet(1, 10, 20.0, 7, false),
            ExerciseSet(2, 10, 20.0, 7, false),
            ExerciseSet(3, 10, 20.0, 7, false)
        )
        val newEx = LoggedExercise(
            exerciseId = def.id,
            exerciseName = def.name,
            pattern = def.pattern,
            sets = newSets
        )
        _currentSession.value = session.copy(exercises = session.exercises + newEx)
    }

    fun markAllSetsComplete() {
        val session = _currentSession.value ?: return
        val updatedExercises = session.exercises.map { ex ->
            val updatedSets = ex.sets.map { set ->
                set.copy(isCompleted = true)
            }.toMutableList()
            ex.copy(sets = updatedSets)
        }
        _currentSession.value = session.copy(exercises = updatedExercises)
    }

    fun finishSession(sessionRpe: Int, notes: String): LoggedWorkoutSession? {
        val session = _currentSession.value ?: return null
        pauseTimer()
        val durationMins = (_elapsedSeconds.value / 60).coerceAtLeast(1)

        val completedWorkout = LoggedWorkoutSession(
            id = session.id,
            templateId = session.templateId,
            title = session.title,
            timestampMillis = System.currentTimeMillis(),
            durationMinutes = durationMins,
            exercises = session.exercises,
            isDeload = session.isDeload,
            sessionRpe = sessionRpe,
            notes = notes
        )

        _currentSession.value = null
        _elapsedSeconds.value = 0
        _isMinimized.value = false
        return completedWorkout
    }

    fun cancelSession() {
        pauseTimer()
        _currentSession.value = null
        _elapsedSeconds.value = 0
        _isMinimized.value = false
    }

    /**
     * Allows user to immediately log a template as completed manually
     * without running the live stopwatch runner.
     */
    fun createManualCompletedSession(
        template: WorkoutTemplate,
        durationMinutes: Int = template.estimatedDurationMinutes,
        sessionRpe: Int = if (template.isDeloadTemplate) 5 else 7,
        notes: String = "Manually marked as completed."
    ): LoggedWorkoutSession {
        val defs = template.defaultExerciseIds.mapNotNull { ExerciseRepository.getExerciseById(it) }
        val setCount = if (template.isDeloadTemplate) 2 else 3
        val loggedExercises = defs.map { def ->
            val sets = (1..setCount).map {
                ExerciseSet(
                    setNumber = it,
                    reps = 10,
                    weightKg = 20.0,
                    rpe = sessionRpe,
                    isCompleted = true
                )
            }.toMutableList()
            LoggedExercise(
                exerciseId = def.id,
                exerciseName = def.name,
                pattern = def.pattern,
                sets = sets
            )
        }

        return LoggedWorkoutSession(
            id = UUID.randomUUID().toString(),
            templateId = template.id,
            title = template.title,
            timestampMillis = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            exercises = loggedExercises,
            isDeload = template.isDeloadTemplate,
            sessionRpe = sessionRpe,
            notes = notes
        )
    }
}
