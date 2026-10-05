package com.example.physiapp.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.physiapp.data.model.MovementPattern
import com.example.physiapp.ui.theme.TealPrimary
import kotlin.math.PI
import kotlin.math.sin

/**
 * Robust animated motion player for exercises.
 * Attempts to play the animated GIF from ExerciseDB/remote URL with Coil.
 * If the URL is unavailable, offline, or returns 404, it immediately renders an
 * interactive animated biomechanical demonstrator that actively plays on any emulator!
 */
@Composable
fun ExerciseMotionPlayer(
    exerciseName: String,
    pattern: MovementPattern,
    gifUrl: String = "",
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var useFallbackMotion by remember { mutableStateOf(gifUrl.isBlank()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .testTag("motion_player_${exerciseName.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!useFallbackMotion && gifUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(gifUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "$exerciseName Form Demo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TealPrimary)
                        }
                    },
                    error = {
                        // Automatically switch to animated biomechanical demonstrator if remote GIF fails
                        useFallbackMotion = true
                    }
                )
            }

            // Animated Biomechanical Motion Demonstrator
            if (useFallbackMotion || gifUrl.isBlank()) {
                BiomechanicalKineticVisualizer(
                    exerciseName = exerciseName,
                    pattern = pattern,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top Status Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) TealPrimary else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "DEMO PLAYING" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // Play / Pause Control
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { isPlaying = !isPlaying }
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause animation" else "Play animation",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Smooth 60fps kinetic stick-figure demonstrator that visualizes the rep tempo and joint angles.
 */
@Composable
fun BiomechanicalKineticVisualizer(
    exerciseName: String,
    pattern: MovementPattern,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RepTempoLoop")

    // Continuous rep cycle progress from 0.0 (top) to 1.0 (bottom of rep) and back to 0.0
    val repProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RepProgress"
    )

    val currentProgress = if (isPlaying) repProgress else 0.5f
    val repPhaseText = when {
        currentProgress < 0.2f -> "Peak Contraction (Lockout)"
        currentProgress > 0.8f -> "Deep Position (Joint Mobility)"
        else -> "Controlled Tempo Transition"
    }

    Box(modifier = modifier.background(Color(0xFF1E222A)), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width * 0.5f
            val groundY = height * 0.82f

            // Floor Reference Line
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(width * 0.15f, groundY),
                end = Offset(width * 0.85f, groundY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            val primaryColor = pattern.color
            val accentColor = Color.White
            val strokeWidth = 8f

            when (pattern) {
                MovementPattern.SQUAT -> {
                    // Squat kinetics: Hip & knee flexion
                    val descent = currentProgress * 42f
                    val hipY = groundY - 70f + descent
                    val kneeX = centerX - 32f - (currentProgress * 12f)
                    val kneeY = groundY - 35f + (descent * 0.5f)
                    val footX = centerX - 24f
                    val headY = hipY - 55f

                    // Head
                    drawCircle(accentColor, radius = 10f, center = Offset(centerX, headY))

                    // Spine / Torso
                    drawLine(accentColor, start = Offset(centerX, headY + 10f), end = Offset(centerX - 8f, hipY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Thigh (Hip to Knee)
                    drawLine(primaryColor, start = Offset(centerX - 8f, hipY), end = Offset(kneeX, kneeY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Shin (Knee to Ankle)
                    drawLine(primaryColor, start = Offset(kneeX, kneeY), end = Offset(footX, groundY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Weight Load (Goblet / Front load at chest)
                    drawCircle(Color(0xFFFFB300), radius = 8f, center = Offset(centerX + 14f, headY + 28f))
                }

                MovementPattern.HINGE -> {
                    // Hinge kinetics: Hip pushed backward, soft knees
                    val hingeBack = currentProgress * 40f
                    val torsoDip = currentProgress * 34f
                    val hipX = centerX - 20f - hingeBack
                    val hipY = groundY - 65f + (currentProgress * 10f)
                    val headX = centerX + 18f
                    val headY = hipY - 55f + torsoDip
                    val kneeX = centerX - 12f
                    val kneeY = groundY - 34f
                    val footX = centerX - 8f

                    // Head
                    drawCircle(accentColor, radius = 10f, center = Offset(headX, headY))

                    // Spine / Torso
                    drawLine(accentColor, start = Offset(headX - 6f, headY + 8f), end = Offset(hipX, hipY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Hamstrings / Thigh (Hip to Knee)
                    drawLine(primaryColor, start = Offset(hipX, hipY), end = Offset(kneeX, kneeY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Shin (Knee to Foot)
                    drawLine(primaryColor, start = Offset(kneeX, kneeY), end = Offset(footX, groundY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Barbell / Dumbbell moving along shins
                    val loadY = groundY - 45f + (currentProgress * 25f)
                    drawCircle(Color(0xFFFFB300), radius = 8f, center = Offset(kneeX + 16f, loadY))
                    drawLine(Color(0xFFFFB300), start = Offset(headX, headY + 16f), end = Offset(kneeX + 16f, loadY), strokeWidth = 3f)
                }

                MovementPattern.PUSH -> {
                    // Push kinetics: Press from chest to lockout
                    val pressUp = (1f - currentProgress) * 44f
                    val headY = groundY - 80f
                    val shoulderY = headY + 20f
                    val hipY = groundY - 45f
                    val handY = shoulderY - 8f - pressUp

                    // Head & Body
                    drawCircle(accentColor, radius = 10f, center = Offset(centerX, headY))
                    drawLine(accentColor, start = Offset(centerX, headY + 10f), end = Offset(centerX, hipY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Legs
                    drawLine(primaryColor, start = Offset(centerX, hipY), end = Offset(centerX - 16f, groundY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(primaryColor, start = Offset(centerX, hipY), end = Offset(centerX + 16f, groundY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Arms pressing load
                    val elbowX = centerX + 18f - (pressUp * 0.15f)
                    val elbowY = shoulderY + 12f - (pressUp * 0.4f)
                    drawLine(accentColor, start = Offset(centerX, shoulderY), end = Offset(elbowX, elbowY), strokeWidth = 6f, cap = StrokeCap.Round)
                    drawLine(accentColor, start = Offset(elbowX, elbowY), end = Offset(centerX + 16f, handY), strokeWidth = 6f, cap = StrokeCap.Round)

                    // Weight Barbell / Dumbbell
                    drawCircle(Color(0xFFFFB300), radius = 9f, center = Offset(centerX + 16f, handY))
                }

                MovementPattern.PULL -> {
                    // Pull kinetics: Elbow retraction towards hip
                    val pullBack = (1f - currentProgress) * 36f
                    val headY = groundY - 76f
                    val hipY = groundY - 45f
                    val handX = centerX + 34f - pullBack
                    val handY = groundY - 55f + (pullBack * 0.3f)

                    // Head & Torso
                    drawCircle(accentColor, radius = 10f, center = Offset(centerX, headY))
                    drawLine(accentColor, start = Offset(centerX, headY + 10f), end = Offset(centerX - 6f, hipY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Legs
                    drawLine(primaryColor, start = Offset(centerX - 6f, hipY), end = Offset(centerX - 18f, groundY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(primaryColor, start = Offset(centerX - 6f, hipY), end = Offset(centerX + 14f, groundY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Pulling Arm
                    val elbowX = centerX - 6f - (pullBack * 0.4f)
                    val elbowY = groundY - 60f + (pullBack * 0.1f)
                    drawLine(accentColor, start = Offset(centerX, headY + 18f), end = Offset(elbowX, elbowY), strokeWidth = 6f, cap = StrokeCap.Round)
                    drawLine(accentColor, start = Offset(elbowX, elbowY), end = Offset(handX, handY), strokeWidth = 6f, cap = StrokeCap.Round)

                    // Pulled Weight
                    drawCircle(Color(0xFFFFB300), radius = 8f, center = Offset(handX, handY))
                }

                MovementPattern.CARRY -> {
                    // Loaded locomotion kinetics: Tall spine, marching stride
                    val stepAngle = sin(currentProgress * PI.toFloat() * 2f) * 18f
                    val headY = groundY - 82f
                    val hipY = groundY - 45f

                    // Head & Rigid Torso
                    drawCircle(accentColor, radius = 10f, center = Offset(centerX, headY))
                    drawLine(accentColor, start = Offset(centerX, headY + 10f), end = Offset(centerX, hipY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Walking Legs
                    drawLine(primaryColor, start = Offset(centerX, hipY), end = Offset(centerX - stepAngle, groundY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(primaryColor, start = Offset(centerX, hipY), end = Offset(centerX + stepAngle, groundY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Arms holding weights at side
                    drawLine(accentColor, start = Offset(centerX, headY + 18f), end = Offset(centerX - 14f, hipY + 10f), strokeWidth = 5f, cap = StrokeCap.Round)
                    drawLine(accentColor, start = Offset(centerX, headY + 18f), end = Offset(centerX + 14f, hipY + 10f), strokeWidth = 5f, cap = StrokeCap.Round)

                    // Kettlebells / Dumbbells
                    drawCircle(Color(0xFFFFB300), radius = 7f, center = Offset(centerX - 14f, hipY + 15f))
                    drawCircle(Color(0xFFFFB300), radius = 7f, center = Offset(centerX + 14f, hipY + 15f))
                }

                MovementPattern.ROTATION -> {
                    // Anti-rotation press: Arms extending away from sternum
                    val extension = currentProgress * 42f
                    val headY = groundY - 80f
                    val hipY = groundY - 45f
                    val handX = centerX + 10f + extension
                    val handY = headY + 28f

                    // Head & Braced Spine
                    drawCircle(accentColor, radius = 10f, center = Offset(centerX, headY))
                    drawLine(accentColor, start = Offset(centerX, headY + 10f), end = Offset(centerX, hipY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Wide Stance
                    drawLine(primaryColor, start = Offset(centerX, hipY), end = Offset(centerX - 24f, groundY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(primaryColor, start = Offset(centerX, hipY), end = Offset(centerX + 24f, groundY), strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    // Cable Handle
                    drawLine(accentColor, start = Offset(centerX, headY + 22f), end = Offset(handX, handY), strokeWidth = 5f, cap = StrokeCap.Round)
                    drawCircle(Color(0xFFFFB300), radius = 7f, center = Offset(handX, handY))

                    // Resistance Line (anti-rotation anchor tension)
                    drawLine(
                        color = Color.Cyan.copy(alpha = 0.5f),
                        start = Offset(0f, handY),
                        end = Offset(handX, handY),
                        strokeWidth = 2f,
                        pathEffect = null
                    )
                }
            }
        }

        // Bottom Phase Badge Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${pattern.label} Biomechanics: $repPhaseText",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = pattern.color
            )
        }
    }
}
