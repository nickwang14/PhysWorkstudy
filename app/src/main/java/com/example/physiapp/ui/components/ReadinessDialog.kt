package com.example.physiapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ReadinessDialog(
    onDismiss: () -> Unit,
    onSubmit: (sleep: Int, soreness: Int, energy: Int) -> Unit
) {
    var sleep by remember { mutableFloatStateOf(4f) }
    var soreness by remember { mutableFloatStateOf(2f) }
    var energy by remember { mutableFloatStateOf(4f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Daily Readiness Check-In",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Help PhysiApp autoregulate your training stress and identify recovery needs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Sleep
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sleep Quality", style = MaterialTheme.typography.labelLarge)
                        Text("${sleep.roundToInt()} / 5", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = sleep,
                        onValueChange = { sleep = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.testTag("slider_sleep")
                    )
                }

                // Soreness
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Muscle Soreness", style = MaterialTheme.typography.labelLarge)
                        Text(
                            when (soreness.roundToInt()) {
                                1 -> "1 (None)"
                                2 -> "2 (Mild)"
                                3 -> "3 (Moderate)"
                                4 -> "4 (High)"
                                else -> "5 (Severe)"
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = soreness,
                        onValueChange = { soreness = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.testTag("slider_soreness")
                    )
                }

                // Energy
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Daily Energy", style = MaterialTheme.typography.labelLarge)
                        Text("${energy.roundToInt()} / 5", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = energy,
                        onValueChange = { energy = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.testTag("slider_energy")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(sleep.roundToInt(), soreness.roundToInt(), energy.roundToInt())
                },
                modifier = Modifier.testTag("submit_readiness_button")
            ) {
                Text("Save Check-In")
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
