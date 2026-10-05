package com.example.physiapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiapp.data.model.ConsistencyBadge
import com.example.physiapp.data.model.DualTrackProgress
import com.example.physiapp.data.model.UserPreferences
import com.example.physiapp.ui.theme.AmberTertiary
import com.example.physiapp.ui.theme.DeloadIndigo
import com.example.physiapp.ui.theme.TealPrimary
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsAndBadgesDialog(
    preferences: UserPreferences,
    progress: DualTrackProgress,
    badges: List<ConsistencyBadge>,
    onDismiss: () -> Unit,
    onSavePreferences: (weeklyGoal: Int, weightUnit: String, goal: String, restTime: Int) -> Unit,
    onAdvanceDeloadWeek: () -> Unit
) {
    var weeklyGoal by remember { mutableFloatStateOf(preferences.weeklyGoalTarget.toFloat()) }
    var weightUnit by remember { mutableStateOf(preferences.weightUnit) }
    var trainingGoal by remember { mutableStateOf(preferences.trainingGoal) }
    var restTime by remember { mutableFloatStateOf(preferences.defaultRestTimeSeconds.toFloat()) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings & Milestones",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Consistency Milestones Section (PD-004)
                Text(
                    text = "CONSISTENCY ACHIEVEMENTS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "PhysiApp rewards showing up and deload compliance, never reckless intensity or load ego.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    badges.forEach { badge ->
                        val iconVector: ImageVector = when (badge.iconName) {
                            "school" -> Icons.Default.School
                            "local_fire_department" -> Icons.Default.LocalFireDepartment
                            "stars" -> Icons.Default.Stars
                            "check_circle" -> Icons.Default.CheckCircle
                            "spa" -> Icons.Default.Spa
                            else -> Icons.Default.FitnessCenter
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (badge.isEarned) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (badge.isEarned) AmberTertiary else MaterialTheme.colorScheme.outlineVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconVector,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = badge.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (badge.isEarned) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "EARNED",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                                color = AmberTertiary
                                            )
                                        }
                                    }
                                    Text(
                                        text = badge.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Weekly Goal Setting (min 2 per PD-001/PD-003)
                Text(
                    text = "TRAINING CADENCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Weekly Target", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${weeklyGoal.roundToInt()} workouts / week",
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                    Slider(
                        value = weeklyGoal,
                        onValueChange = { weeklyGoal = it },
                        valueRange = 2f..5f,
                        steps = 2,
                        modifier = Modifier.testTag("slider_weekly_goal")
                    )
                    Text(
                        text = "Minimum 2 workouts per week ensures neuromuscular retention.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Weight Unit
                Column {
                    Text("Weight Measurement Unit", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = weightUnit == "kg",
                            onClick = { weightUnit = "kg" },
                            label = { Text("Kilograms (kg)") }
                        )
                        FilterChip(
                            selected = weightUnit == "lbs",
                            onClick = { weightUnit = "lbs" },
                            label = { Text("Pounds (lbs)") }
                        )
                    }
                }

                // Deload Periodization Management
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DeloadIndigo.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Deload Cadence (PD-002)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeloadIndigo
                        )
                        Text(
                            text = "Currently in Week ${progress.currentWeekInCycle} of 6-week cycle. Week 6 is designated light recovery.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onAdvanceDeloadWeek,
                            modifier = Modifier.testTag("advance_cycle_button")
                        ) {
                            Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Advance Week in Cycle")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSavePreferences(
                        weeklyGoal.roundToInt(),
                        weightUnit,
                        trainingGoal,
                        restTime.roundToInt()
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_settings_button")
            ) {
                Text("Save Preferences")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
