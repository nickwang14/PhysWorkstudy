package com.example.physiapp.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.physiapp.data.model.ExerciseDef
import com.example.physiapp.data.model.LoggedWorkoutSession
import com.example.physiapp.data.repository.ActiveWorkoutManager
import com.example.physiapp.data.repository.ExerciseRepository
import com.example.physiapp.ui.components.ExerciseMotionPlayer
import com.example.physiapp.ui.components.RestTimerDialog
import com.example.physiapp.ui.theme.DeloadIndigo
import com.example.physiapp.ui.theme.TealPrimary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    weightUnit: String,
    onMinimize: () -> Unit,
    onFinish: (LoggedWorkoutSession) -> Unit,
    modifier: Modifier = Modifier
) {
    val session by ActiveWorkoutManager.currentSession.collectAsState()
    val isTimerRunning by ActiveWorkoutManager.isTimerRunning.collectAsState()
    val elapsedSeconds by ActiveWorkoutManager.elapsedSeconds.collectAsState()

    var showRestTimer by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showDiscardConfirmDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showExerciseDbBrowser by remember { mutableStateOf(false) }
    var selectedExerciseForCues by remember { mutableStateOf<ExerciseDef?>(null) }

    if (session == null) return

    val currentSession = session!!
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentSession.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isTimerRunning) "Time: $timeFormatted" else "Paused: $timeFormatted",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isTimerRunning) TealPrimary else Color(0xFFE65100)
                            )
                            if (currentSession.isDeload) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• Deload Week",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeloadIndigo
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    // Minimize to browse app button
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier.testTag("workout_minimize_button")
                    ) {
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = "Minimize Workout to Browse",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    // Pause / Resume Toggle
                    IconButton(
                        onClick = { ActiveWorkoutManager.togglePlayPause() },
                        modifier = Modifier.testTag("workout_pause_resume_button")
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isTimerRunning) "Pause" else "Resume",
                            tint = if (isTimerRunning) TealPrimary else Color(0xFFE65100)
                        )
                    }

                    // Rest Timer Icon
                    IconButton(
                        onClick = { showRestTimer = true },
                        modifier = Modifier.testTag("open_rest_timer_button")
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = "Rest Interval", tint = TealPrimary)
                    }

                    // Finish Button
                    Button(
                        onClick = { showFinishDialog = true },
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("finish_workout_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentSession.isDeload) DeloadIndigo else TealPrimary
                        )
                    ) {
                        Text("Finish")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Pause banner alert if paused
                if (!isTimerRunning) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Pause, contentDescription = null, tint = Color(0xFFE65100))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Workout is Paused",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFE65100)
                                    )
                                }

                                Button(
                                    onClick = { ActiveWorkoutManager.resumeTimer() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                                ) {
                                    Text("Resume")
                                }
                            }
                        }
                    }
                }

                // Global Action Row: Mark All Done & Minimize Cues
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { ActiveWorkoutManager.markAllSetsComplete() },
                            modifier = Modifier.testTag("mark_all_sets_complete_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark All Complete")
                        }

                        TextButton(
                            onClick = onMinimize,
                            modifier = Modifier.testTag("revisit_splits_button")
                        ) {
                            Text("Browse Splits & Guide ↗")
                        }
                    }
                }

                // Exercise Cards
                itemsIndexed(currentSession.exercises) { exIndex, ex ->
                    val def = ExerciseRepository.getExerciseById(ex.exerciseId)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(ex.pattern.color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = ex.exerciseName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (def != null) {
                                    IconButton(
                                        onClick = { selectedExerciseForCues = def },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = "Physio Cues",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Table Labels
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SET", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(36.dp))
                                Text("WEIGHT ($weightUnit)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(90.dp))
                                Text("REPS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(56.dp))
                                Text("RPE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(48.dp))
                                Text("DONE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(46.dp))
                            }

                            // Sets Rows
                            ex.sets.forEachIndexed { setIdx, set ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${set.setNumber}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier.width(36.dp)
                                    )

                                    // Weight
                                    var weightInput by remember(set.weightKg) {
                                        mutableStateOf(if (set.weightKg > 0) set.weightKg.toString() else "0")
                                    }
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = {
                                            weightInput = it
                                            val parsed = it.toDoubleOrNull() ?: set.weightKg
                                            ActiveWorkoutManager.updateSetValues(exIndex, setIdx, set.reps, parsed, set.rpe)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.width(90.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )

                                    // Reps
                                    var repsInput by remember(set.reps) { mutableStateOf(set.reps.toString()) }
                                    OutlinedTextField(
                                        value = repsInput,
                                        onValueChange = {
                                            repsInput = it
                                            val parsed = it.toIntOrNull() ?: set.reps
                                            ActiveWorkoutManager.updateSetValues(exIndex, setIdx, parsed, set.weightKg, set.rpe)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.width(56.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )

                                    // RPE
                                    var rpeInput by remember(set.rpe) { mutableStateOf(set.rpe.toString()) }
                                    OutlinedTextField(
                                        value = rpeInput,
                                        onValueChange = {
                                            rpeInput = it
                                            val parsed = (it.toIntOrNull() ?: set.rpe).coerceIn(1, 10)
                                            ActiveWorkoutManager.updateSetValues(exIndex, setIdx, set.reps, set.weightKg, parsed)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.width(48.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )

                                    // Complete Checkbox
                                    Checkbox(
                                        checked = set.isCompleted,
                                        onCheckedChange = { isChecked ->
                                            ActiveWorkoutManager.markSetCompletion(exIndex, setIdx, isChecked)
                                            if (isChecked) {
                                                showRestTimer = true
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = TealPrimary),
                                        modifier = Modifier.testTag("set_checkbox_${ex.exerciseId}_$setIdx")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Add Set
                            OutlinedButton(
                                onClick = { ActiveWorkoutManager.addSetToExercise(exIndex) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_set_button_${ex.exerciseId}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Set")
                            }
                        }
                    }
                }

                // Add Exercise Button
                item {
                    Button(
                        onClick = { showAddExerciseDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_exercise_to_workout_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Exercise to Session")
                    }
                }

                // Discard / Cancel button option
                item {
                    TextButton(
                        onClick = { showDiscardConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancel and Discard Workout")
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Rest Timer Dialog
    if (showRestTimer) {
        AlertDialog(
            onDismissRequest = { showRestTimer = false },
            confirmButton = {},
            text = {
                RestTimerDialog(
                    initialSeconds = 90,
                    onDismiss = { showRestTimer = false }
                )
            },
            containerColor = Color.Transparent
        )
    }

    // Physio Cues Dialog
    selectedExerciseForCues?.let { def ->
        AlertDialog(
            onDismissRequest = { selectedExerciseForCues = null },
            title = {
                Text(def.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Movement Pattern: ${def.pattern.label}", fontWeight = FontWeight.Bold, color = def.pattern.color)
                    Text("Target Muscles: ${def.primaryMuscles}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(4.dp))
                    ExerciseMotionPlayer(
                        exerciseName = def.name,
                        pattern = def.pattern,
                        gifUrl = def.gifUrl,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Physio Coaching Cues:", fontWeight = FontWeight.Bold)
                    def.coachingCues.forEach { cue ->
                        Text("• $cue", style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Common Compensations:", fontWeight = FontWeight.Bold)
                    def.commonMistakes.forEach { mistake ->
                        Text("• $mistake", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Regression: ${def.regression}", style = MaterialTheme.typography.bodySmall)
                    Text("Progression: ${def.progression}", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedExerciseForCues = null }) {
                    Text("Got It")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Add Exercise Dialog
    if (showAddExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            title = { Text("Select Exercise to Add") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            showAddExerciseDialog = false
                            showExerciseDbBrowser = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("browse_exercisedb_in_workout_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Search 11,000+ on ExerciseDB")
                    }

                    Text(
                        text = "Or choose from Core Movement Library:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyColumn(modifier = Modifier.height(260.dp)) {
                        items(ExerciseRepository.exercises.size) { idx ->
                            val exDef = ExerciseRepository.exercises[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ActiveWorkoutManager.addExerciseToSession(exDef)
                                        showAddExerciseDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(exDef.pattern.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(exDef.name, fontWeight = FontWeight.SemiBold)
                                    Text("${exDef.pattern.label} • ${exDef.equipment}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ExerciseDB Browser Dialog
    if (showExerciseDbBrowser) {
        ExerciseDbBrowserDialog(
            onDismiss = { showExerciseDbBrowser = false },
            onSelectExercise = { edbItem ->
                val def = edbItem.toExerciseDef()
                ExerciseRepository.registerExercise(def)
                ActiveWorkoutManager.addExerciseToSession(def)
                showExerciseDbBrowser = false
            }
        )
    }

    // Finish Workout Dialog (Rating RPE and consistency win)
    if (showFinishDialog) {
        var overallRpe by remember { mutableFloatStateOf(7f) }
        var sessionNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = {
                Text("Complete Workout Session", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Great work! Consistency builds long-term joint and musculoskeletal resilience. How did this session feel overall?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Session RPE (Effort)", fontWeight = FontWeight.Bold)
                            Text("${overallRpe.roundToInt()} / 10", color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = overallRpe,
                            onValueChange = { overallRpe = it },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                    }

                    OutlinedTextField(
                        value = sessionNotes,
                        onValueChange = { sessionNotes = it },
                        label = { Text("Session Notes (Optional)") },
                        placeholder = { Text("e.g., Felt strong in goblet squats, knees felt healthy") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sessionObj = ActiveWorkoutManager.finishSession(overallRpe.roundToInt(), sessionNotes)
                        showFinishDialog = false
                        if (sessionObj != null) {
                            onFinish(sessionObj)
                        }
                    },
                    modifier = Modifier.testTag("confirm_finish_workout_button")
                ) {
                    Text("Save & Log Workout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Keep Training")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Discard Confirm Dialog
    if (showDiscardConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmDialog = false },
            title = { Text("Discard Workout?") },
            text = {
                Text("Are you sure you want to discard this workout? You can also simply minimize it to browse splits and resume later.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardConfirmDialog = false
                        ActiveWorkoutManager.cancelSession()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDiscardConfirmDialog = false
                        onMinimize()
                    }
                ) {
                    Text("Minimize Instead")
                }
            }
        )
    }
}
