package com.example.physiapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.physiapp.data.model.ExerciseDef
import com.example.physiapp.data.model.WorkoutTemplate
import com.example.physiapp.data.repository.ExerciseRepository
import com.example.physiapp.ui.components.ExerciseMotionPlayer
import com.example.physiapp.ui.theme.DeloadIndigo
import com.example.physiapp.ui.theme.TealPrimary
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutSplitReviewDialog(
    template: WorkoutTemplate,
    onDismiss: () -> Unit,
    onStartWorkout: (WorkoutTemplate) -> Unit,
    onManualComplete: (WorkoutTemplate, durationMinutes: Int, rpe: Int, notes: String) -> Unit,
    onOpenExerciseDetails: (ExerciseDef) -> Unit
) {
    var showManualCompletePrompt by remember { mutableStateOf(false) }
    val plannedExercises = remember(template) {
        template.defaultExerciseIds.mapNotNull { ExerciseRepository.getExerciseById(it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (template.isDeloadTemplate) "DELOAD SPLIT REVIEW" else "WORKOUT SPLIT REVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (template.isDeloadTemplate) DeloadIndigo else TealPrimary
                    )
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = template.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Physiological Focus",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = template.physioFocus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Estimated: ${template.estimatedDurationMinutes} mins • Sets: ${if (template.isDeloadTemplate) "2 per exercise (Deload)" else "3 per exercise"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Movement Patterns Covered:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        template.patternsCovered.forEach { pattern ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(pattern.color.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = pattern.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = pattern.color
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Programmed Exercises & Form Cues (${plannedExercises.size}):",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(plannedExercises) { ex ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planned_exercise_${ex.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(ex.pattern.color)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = ex.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${ex.pattern.label} • ${ex.equipment}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onOpenExerciseDetails(ex) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Full Exercise Details",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Programmed Exercise Animated Motion Player (Directly above cues)
                            Spacer(modifier = Modifier.height(10.dp))
                            ExerciseMotionPlayer(
                                exerciseName = ex.name,
                                pattern = ex.pattern,
                                gifUrl = ex.gifUrl,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Coaching Cues Box directly below the GIF
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Coaching Cues:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    ex.coachingCues.take(3).forEach { cue ->
                                        Text(
                                            text = "• $cue",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (ex.commonMistakes.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "• Avoid: ${ex.commonMistakes.first()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showManualCompletePrompt = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("split_manual_complete_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark Done")
                }

                Button(
                    onClick = {
                        onDismiss()
                        onStartWorkout(template)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (template.isDeloadTemplate) DeloadIndigo else TealPrimary
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("split_start_workout_button")
                ) {
                    Icon(
                        imageVector = if (template.isDeloadTemplate) Icons.Default.Spa else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Live")
                }
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(24.dp)
    )

    // Manual Completion Dialog
    if (showManualCompletePrompt) {
        var manualDuration by remember { mutableFloatStateOf(template.estimatedDurationMinutes.toFloat()) }
        var manualRpe by remember { mutableFloatStateOf(if (template.isDeloadTemplate) 5f else 7f) }

        AlertDialog(
            onDismissRequest = { showManualCompletePrompt = false },
            title = {
                Text("Mark Split as Completed", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Did you complete this workout split without running the timer? Record your session metrics below to update your weekly training goal.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimated Duration", style = MaterialTheme.typography.bodySmall)
                            Text("${manualDuration.roundToInt()} min", fontWeight = FontWeight.Bold, color = TealPrimary)
                        }
                        Slider(
                            value = manualDuration,
                            onValueChange = { manualDuration = it },
                            valueRange = 15f..90f,
                            steps = 14
                        )
                    }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Session RPE (Effort)", style = MaterialTheme.typography.bodySmall)
                            Text("${manualRpe.roundToInt()} / 10", fontWeight = FontWeight.Bold, color = TealPrimary)
                        }
                        Slider(
                            value = manualRpe,
                            onValueChange = { manualRpe = it },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showManualCompletePrompt = false
                        onDismiss()
                        onManualComplete(
                            template,
                            manualDuration.roundToInt(),
                            manualRpe.roundToInt(),
                            "Manually recorded workout split completion."
                        )
                    },
                    modifier = Modifier.testTag("confirm_manual_complete_button")
                ) {
                    Text("Confirm & Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualCompletePrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
