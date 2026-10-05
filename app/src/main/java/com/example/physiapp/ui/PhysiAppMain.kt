package com.example.physiapp.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiapp.data.model.ExerciseDef
import com.example.physiapp.data.model.WorkoutTemplate
import com.example.physiapp.data.repository.CurriculumRepository
import com.example.physiapp.data.repository.ExerciseRepository
import com.example.physiapp.data.repository.UserPreferencesRepository
import com.example.physiapp.ui.components.ReadinessDialog
import com.example.physiapp.ui.screens.ActiveWorkoutScreen
import com.example.physiapp.ui.screens.CurriculumScreen
import com.example.physiapp.ui.screens.LessonDetailScreen
import com.example.physiapp.ui.screens.ProgressionMapScreen
import com.example.physiapp.ui.screens.SettingsAndBadgesDialog
import com.example.physiapp.ui.screens.TodayScreen
import com.example.physiapp.ui.screens.WorkoutsScreen
import com.example.physiapp.ui.theme.AmberTertiary
import com.example.physiapp.ui.theme.DeloadIndigo
import com.example.physiapp.ui.theme.PhysiAppTheme
import com.example.physiapp.ui.theme.TealPrimary

enum class AppDestination(val label: String, val icon: ImageVector) {
    TODAY("Today", Icons.Default.Dashboard),
    JOURNEY("Journey", Icons.Default.AltRoute),
    WORKOUTS("Workouts", Icons.Default.FitnessCenter),
    CURRICULUM("Curriculum", Icons.AutoMirrored.Filled.MenuBook)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysiAppMain() {
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context) }

    val progress by repository.progressFlow.collectAsState()
    val preferences by repository.prefsFlow.collectAsState()
    val workoutHistory by repository.workoutHistoryFlow.collectAsState()

    var currentDestination by remember { mutableStateOf(AppDestination.TODAY) }

    // Active full-screen flows
    var activeWorkoutTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var isCustomWorkoutActive by remember { mutableStateOf(false) }
    var selectedLessonId by remember { mutableStateOf<String?>(null) }

    // Dialogs
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showReadinessDialog by remember { mutableStateOf(false) }
    var selectedExerciseDetails by remember { mutableStateOf<ExerciseDef?>(null) }

    // Back handling for nested sub-screens
    BackHandler(enabled = selectedLessonId != null || activeWorkoutTemplate != null || isCustomWorkoutActive) {
        if (selectedLessonId != null) {
            selectedLessonId = null
        } else if (activeWorkoutTemplate != null || isCustomWorkoutActive) {
            activeWorkoutTemplate = null
            isCustomWorkoutActive = false
        }
    }

    PhysiAppTheme {
        if (selectedLessonId != null) {
            val lesson = CurriculumRepository.getLessonById(selectedLessonId!!)
            if (lesson != null) {
                LessonDetailScreen(
                    lesson = lesson,
                    isAlreadyCompleted = progress.completedLessonIds.contains(lesson.id),
                    onBack = { selectedLessonId = null },
                    onCompleteLesson = {
                        repository.completeLesson(lesson.id)
                    }
                )
                return@PhysiAppTheme
            }
        }

        if (activeWorkoutTemplate != null || isCustomWorkoutActive) {
            ActiveWorkoutScreen(
                template = activeWorkoutTemplate,
                weightUnit = preferences.weightUnit,
                onCancel = {
                    activeWorkoutTemplate = null
                    isCustomWorkoutActive = false
                },
                onFinish = { session ->
                    repository.logWorkout(session)
                    activeWorkoutTemplate = null
                    isCustomWorkoutActive = false
                    currentDestination = AppDestination.WORKOUTS
                }
            )
            return@PhysiAppTheme
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PhysiApp",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // Daily Learning Streak Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AmberTertiary.copy(alpha = 0.12f))
                                .clickable { showSettingsDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("top_bar_learning_streak")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Daily Streak",
                                    tint = AmberTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${progress.dailyLearningStreak}d",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = AmberTertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Weekly Workout Goal Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(TealPrimary.copy(alpha = 0.12f))
                                .clickable { showSettingsDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("top_bar_weekly_workouts")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Weekly Workouts",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${progress.workoutsCompletedThisWeek}/${progress.weeklyGoalTarget}w",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TealPrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("settings_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings & Milestones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    AppDestination.entries.forEach { dest ->
                        NavigationBarItem(
                            selected = currentDestination == dest,
                            onClick = { currentDestination = dest },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TealPrimary,
                                selectedTextColor = TealPrimary,
                                indicatorColor = TealPrimary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_${dest.name.lowercase()}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentDestination) {
                    AppDestination.TODAY -> {
                        // Find first incomplete lesson for today's lesson spotlight
                        val todayLesson = CurriculumRepository.allLessons.find { !progress.completedLessonIds.contains(it.id) }
                            ?: CurriculumRepository.allLessons.firstOrNull()

                        val suggestedTemplate = if (progress.currentWeekInCycle == 6) {
                            ExerciseRepository.templates.find { it.isDeloadTemplate } ?: ExerciseRepository.templates.first()
                        } else {
                            ExerciseRepository.templates.first()
                        }

                        TodayScreen(
                            progress = progress,
                            todayLesson = todayLesson,
                            suggestedTemplate = suggestedTemplate,
                            workoutHistory = workoutHistory,
                            onOpenLesson = { selectedLessonId = it },
                            onStartWorkout = { activeWorkoutTemplate = it },
                            onOpenDeloadDetails = { showSettingsDialog = true },
                            onAcknowledgeDeload = { repository.acknowledgeDeload() },
                            onOpenReadinessDialog = { showReadinessDialog = true },
                            onLogCustomWorkout = { isCustomWorkoutActive = true }
                        )
                    }

                    AppDestination.JOURNEY -> {
                        ProgressionMapScreen(
                            chapters = CurriculumRepository.chapters,
                            allLessons = CurriculumRepository.allLessons,
                            progress = progress,
                            onSelectLesson = { selectedLessonId = it }
                        )
                    }

                    AppDestination.WORKOUTS -> {
                        WorkoutsScreen(
                            templates = ExerciseRepository.templates,
                            exercises = ExerciseRepository.exercises,
                            workoutHistory = workoutHistory,
                            progress = progress,
                            onStartTemplate = { activeWorkoutTemplate = it },
                            onStartCustomWorkout = { isCustomWorkoutActive = true },
                            onOpenExerciseDetails = { selectedExerciseDetails = it }
                        )
                    }

                    AppDestination.CURRICULUM -> {
                        CurriculumScreen(
                            chapters = CurriculumRepository.chapters,
                            allLessons = CurriculumRepository.allLessons,
                            progress = progress,
                            onOpenLesson = { selectedLessonId = it }
                        )
                    }
                }
            }
        }

        // Settings Dialog
        if (showSettingsDialog) {
            SettingsAndBadgesDialog(
                preferences = preferences,
                progress = progress,
                badges = repository.getBadges(),
                onDismiss = { showSettingsDialog = false },
                onSavePreferences = { weeklyGoal, weightUnit, goal, restTime ->
                    repository.updatePreferences(weeklyGoal, weightUnit, goal, restTime)
                },
                onAdvanceDeloadWeek = {
                    repository.advanceWeekCycle()
                }
            )
        }

        // Readiness Survey Dialog
        if (showReadinessDialog) {
            ReadinessDialog(
                onDismiss = { showReadinessDialog = false },
                onSubmit = { sleep, soreness, energy ->
                    repository.recordReadiness(sleep, soreness, energy)
                    showReadinessDialog = false
                }
            )
        }

        // Exercise Details Popup
        selectedExerciseDetails?.let { def ->
            AlertDialog(
                onDismissRequest = { selectedExerciseDetails = null },
                title = {
                    Text(
                        def.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(def.pattern.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${def.pattern.label} Pattern",
                                fontWeight = FontWeight.Bold,
                                color = def.pattern.color
                            )
                        }

                        Text("Equipment: ${def.equipment}", style = MaterialTheme.typography.bodySmall)
                        Text("Target: ${def.primaryMuscles}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Physio Coaching Cues:", fontWeight = FontWeight.Bold)
                        def.coachingCues.forEach { cue ->
                            Text("• $cue", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Common Mistakes to Avoid:", fontWeight = FontWeight.Bold)
                        def.commonMistakes.forEach { mistake ->
                            Text("• $mistake", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Regression: ${def.regression}", style = MaterialTheme.typography.bodySmall)
                        Text("Progression: ${def.progression}", style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedExerciseDetails = null }) {
                        Text("Close")
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
