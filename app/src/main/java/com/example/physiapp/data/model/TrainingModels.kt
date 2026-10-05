package com.example.physiapp.data.model

import androidx.compose.ui.graphics.Color
import com.example.physiapp.ui.theme.PatternCarry
import com.example.physiapp.ui.theme.PatternHinge
import com.example.physiapp.ui.theme.PatternPull
import com.example.physiapp.ui.theme.PatternPush
import com.example.physiapp.ui.theme.PatternRotation
import com.example.physiapp.ui.theme.PatternSquat

enum class MovementPattern(val label: String, val color: Color, val description: String) {
    SQUAT("Squat", PatternSquat, "Knee-dominant lower body flexion/extension with upright trunk"),
    HINGE("Hinge", PatternHinge, "Hip-dominant posterior chain extension with minimal knee flexion"),
    PUSH("Push", PatternPush, "Upper body pushing (horizontal bench/pushup or vertical overhead press)"),
    PULL("Pull", PatternPull, "Upper body pulling (horizontal rows or vertical pull-ups/pulldowns)"),
    CARRY("Carry", PatternCarry, "Loaded locomotion testing posture, grip, and core stabilization"),
    ROTATION("Rotate & Anti-Rotate", PatternRotation, "Transverse plane power, torsional bracing, and anti-rotation control")
}

data class ExerciseDef(
    val id: String,
    val name: String,
    val pattern: MovementPattern,
    val primaryMuscles: String,
    val equipment: String,
    val coachingCues: List<String>,
    val commonMistakes: List<String>,
    val regression: String,
    val progression: String,
    val gifUrl: String = ""
)

data class ExerciseSet(
    val setNumber: Int,
    var reps: Int = 10,
    var weightKg: Double = 0.0,
    var rpe: Int = 7, // Rate of Perceived Exertion (1-10)
    var isCompleted: Boolean = false
)

data class LoggedExercise(
    val exerciseId: String,
    val exerciseName: String,
    val pattern: MovementPattern,
    val sets: MutableList<ExerciseSet> = mutableListOf(),
    var notes: String = ""
)

data class WorkoutTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val estimatedDurationMinutes: Int,
    val patternsCovered: List<MovementPattern>,
    val defaultExerciseIds: List<String>,
    val isDeloadTemplate: Boolean = false,
    val physioFocus: String
)

data class LoggedWorkoutSession(
    val id: String,
    val templateId: String?,
    val title: String,
    val timestampMillis: Long,
    val durationMinutes: Int,
    val exercises: List<LoggedExercise>,
    val isDeload: Boolean,
    val sessionRpe: Int,
    val notes: String
)
