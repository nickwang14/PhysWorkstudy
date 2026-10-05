package com.example.physiapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiapp.data.model.ExerciseDef
import com.example.physiapp.data.model.ExerciseSet
import com.example.physiapp.data.model.LoggedExercise
import com.example.physiapp.data.model.LoggedWorkoutSession
import com.example.physiapp.data.model.MovementPattern
import com.example.physiapp.data.model.WorkoutTemplate
import com.example.physiapp.data.repository.ExerciseRepository
import com.example.physiapp.ui.components.RestTimerDialog
import com.example.physiapp.ui.theme.DeloadIndigo
import com.example.physiapp.ui.theme.TealPrimary
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    template: WorkoutTemplate?,
    weightUnit: String,
    onCancel: () -> Unit,
    onFinish: (LoggedWorkoutSession) -> Unit,
    modifier: Modifier = Modifier
) {
    // Setup initial exercises
    val loggedExercises = remember {
        val initial = mutableStateListOf<LoggedExercise>()
        if (template != null) {
            val defs = template.defaultExerciseIds.mapNotNull { ExerciseRepository.getExerciseById(it) }
            val setCount = if (template.isDeloadTemplate) 2 else 3
            defs.forEach { def ->
                val sets = (1..setCount).map {
                    ExerciseSet(setNumber = it, reps = 10, weightKg = 20.0, rpe = if (template.isDeloadTemplate) 5 else 7, isCompleted = false)
                }.toMutableList()
                initial.add(LoggedExercise(exerciseId = def.id, exerciseName = def.name, pattern = def.pattern, sets = sets))
            }
        } else {
            // Custom workout initial empty exercise
            val def = ExerciseRepository.exercises.first()
            val sets = mutableListOf(
                ExerciseSet(1, 10, 20.0, 7, false),
                ExerciseSet(2, 10, 20.0, 7, false),
                ExerciseSet(3, 10, 20.0, 7, false)
            )
            initial.add(LoggedExercise(exerciseId = def.id, exerciseName = def.name, pattern = def.pattern, sets = sets))
        }
        initial
    }

    var elapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds += 1
        }
    }

    var showRestTimer by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var selectedExerciseForCues by remember { mutableStateOf<ExerciseDef?>(null) }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val durationText = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = template?.title ?: "Custom Workout Session",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = "Timer: $durationText • ${if (template?.isDeloadTemplate == true) "Deload Session" else "Training"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TealPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancel, modifier = Modifier.testTag("workout_cancel_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel Workout")
                    }
                },
                actions = {
                    IconButton(onClick = { showRestTimer = true }, modifier = Modifier.testTag("open_rest_timer_button")) {
                        Icon(Icons.Default.Timer, contentDescription = "Rest Timer", tint = TealPrimary)
                    }
                    Button(
                        onClick = { showFinishDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("finish_workout_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (template?.isDeloadTemplate == true) DeloadIndigo else TealPrimary
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Focus banner
                if (template?.physioFocus != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(TealPrimary.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = template.physioFocus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Exercises
                itemsIndexed(loggedExercises) { exIndex, ex ->
                    val def = ExerciseRepository.getExerciseById(ex.exerciseId)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Exercise Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                            contentDescription = "Cues",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SET", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(40.dp))
                                Text("WEIGHT ($weightUnit)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(90.dp))
                                Text("REPS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(60.dp))
                                Text("RPE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(50.dp))
                                Text("DONE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(48.dp))
                            }

                            // Sets Rows
                            ex.sets.forEachIndexed { setIdx, set ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${set.setNumber}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier.width(40.dp)
                                    )

                                    // Weight Input
                                    var weightText by remember { mutableStateOf(if (set.weightKg > 0) set.weightKg.toString() else "0") }
                                    OutlinedTextField(
                                        value = weightText,
                                        onValueChange = {
                                            weightText = it
                                            set.weightKg = it.toDoubleOrNull() ?: 0.0
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.width(90.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )

                                    // Reps Input
                                    var repsText by remember { mutableStateOf(set.reps.toString()) }
                                    OutlinedTextField(
                                        value = repsText,
                                        onValueChange = {
                                            repsText = it
                                            set.reps = it.toIntOrNull() ?: 0
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.width(60.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )

                                    // RPE (Rate of Perceived Exertion)
                                    var rpeText by remember { mutableStateOf(set.rpe.toString()) }
                                    OutlinedTextField(
                                        value = rpeText,
                                        onValueChange = {
                                            rpeText = it
                                            set.rpe = (it.toIntOrNull() ?: 7).coerceIn(1, 10)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.width(50.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )

                                    // Completion Checkbox
                                    var isDone by remember { mutableStateOf(set.isCompleted) }
                                    Checkbox(
                                        checked = isDone,
                                        onCheckedChange = {
                                            isDone = it
                                            set.isCompleted = it
                                            if (it) {
                                                // Trigger rest timer prompt
                                                showRestTimer = true
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = TealPrimary),
                                        modifier = Modifier.testTag("set_checkbox_${ex.exerciseId}_$setIdx")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Add Set Button
                            OutlinedButton(
                                onClick = {
                                    val nextNum = ex.sets.size + 1
                                    val lastSet = ex.sets.lastOrNull()
                                    ex.sets.add(
                                        ExerciseSet(
                                            setNumber = nextNum,
                                            reps = lastSet?.reps ?: 10,
                                            weightKg = lastSet?.weightKg ?: 20.0,
                                            rpe = lastSet?.rpe ?: 7,
                                            isCompleted = false
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_set_${ex.exerciseId}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Set")
                            }
                        }
                    }
                }

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

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Rest Timer Modal
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
                    Text("Muscles: ${def.primaryMuscles}", style = MaterialTheme.typography.bodySmall)

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
            title = { Text("Select Exercise") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(ExerciseRepository.exercises.size) { idx ->
                        val exDef = ExerciseRepository.exercises[idx]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    loggedExercises.add(
                                        LoggedExercise(
                                            exerciseId = exDef.id,
                                            exerciseName = exDef.name,
                                            pattern = exDef.pattern,
                                            sets = mutableListOf(
                                                ExerciseSet(1, 10, 20.0, 7, false),
                                                ExerciseSet(2, 10, 20.0, 7, false)
                                            )
                                        )
                                    )
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
            },
            confirmButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) {
                    Text("Cancel")
                }
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
                        text = "Great job showing up! Consistency builds lasting adaptations. How did this session feel overall?",
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
                        placeholder = { Text("e.g., Felt strong in goblet squats, knee comfortable") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val session = LoggedWorkoutSession(
                            id = UUID.randomUUID().toString(),
                            templateId = template?.id,
                            title = template?.title ?: "Custom Workout",
                            timestampMillis = System.currentTimeMillis(),
                            durationMinutes = (elapsedSeconds / 60).coerceAtLeast(1),
                            exercises = loggedExercises.toList(),
                            isDeload = template?.isDeloadTemplate ?: false,
                            sessionRpe = overallRpe.roundToInt(),
                            notes = sessionNotes
                        )
                        showFinishDialog = false
                        onFinish(session)
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
}
