package com.example.physiapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.physiapp.ui.theme.DeloadIndigo

@Composable
fun DeloadBanner(
    currentWeekInCycle: Int,
    isAcknowledged: Boolean,
    onAcknowledge: () -> Unit,
    onStartDeloadWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDeloadWeek = currentWeekInCycle == 6

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("deload_banner_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeloadWeek) DeloadIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDeloadWeek) DeloadIndigo else MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDeloadWeek) Icons.Default.Spa else Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isDeloadWeek) "Week 6: Planned Deload & Recovery" else "Periodization Cycle: Week $currentWeekInCycle of 6",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDeloadWeek) DeloadIndigo else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDeloadWeek) "Active tissue remodeling phase" else "Deload scheduled after Week 5",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isDeloadWeek) {
                Text(
                    text = "Planned recovery is not taking time off; it is how tendons, joints, and your nervous system adapt and supercompensate. Volume is reduced by ~40-50% while preserving motor patterns at moderate RPE 5-6.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onStartDeloadWorkout,
                        colors = ButtonDefaults.buttonColors(containerColor = DeloadIndigo),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_deload_button")
                    ) {
                        Text("Start Deload Session")
                    }

                    if (!isAcknowledged) {
                        OutlinedButton(
                            onClick = onAcknowledge,
                            modifier = Modifier.testTag("ack_deload_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Acknowledge")
                        }
                    }
                }
            } else {
                Text(
                    text = "Every 6th week is a scheduled light deload week to dissipate accumulated systemic fatigue and solidify strength gains.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
