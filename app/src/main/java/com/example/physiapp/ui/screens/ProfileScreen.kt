package com.example.physiapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.physiapp.data.auth.AuthManager
import com.example.physiapp.data.model.UserProfile
import com.example.physiapp.data.model.WorkoutLogDoc
import com.example.physiapp.data.repository.UserProfileRepository
import com.example.physiapp.ui.theme.AmberTertiary
import com.example.physiapp.ui.theme.TealPrimary
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    user: FirebaseUser,
    authManager: AuthManager,
    profileRepository: UserProfileRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val profileFlow = remember(user.uid) { profileRepository.observeUserProfile(user.uid) }
    val currentProfile by profileFlow.collectAsState(initial = null)

    val workoutLogsFlow = remember(user.uid) { profileRepository.observeWorkoutLogs(user.uid) }
    val workoutLogs by workoutLogsFlow.collectAsState(initial = emptyList())

    // Editable form state
    var displayName by remember { mutableStateOf(user.displayName ?: "Athlete") }
    var trainingGoal by remember { mutableStateOf("Strength & Hypertrophy") }
    var fitnessLevel by remember { mutableStateOf("Intermediate") }
    var weeklyTargetWorkouts by remember { mutableIntStateOf(4) }
    var favoriteSplit by remember { mutableStateOf("Upper / Lower (4-Day Hypertrophy)") }
    var weightKgText by remember { mutableStateOf("") }
    var heightCmText by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showLogWorkoutDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<WorkoutLogDoc?>(null) }

    // Sync remote profile to local state on initial load or changes
    LaunchedEffect(currentProfile) {
        val p = currentProfile
        if (p != null) {
            displayName = p.displayName.ifBlank { user.displayName ?: "Athlete" }
            trainingGoal = p.trainingGoal
            fitnessLevel = p.fitnessLevel
            weeklyTargetWorkouts = p.weeklyTargetWorkouts.toInt().coerceIn(1, 7)
            favoriteSplit = p.favoriteSplit
            weightKgText = p.weightKg?.toString() ?: ""
            heightCmText = p.heightCm?.toString() ?: ""
        } else {
            // First time profile creation defaults
            displayName = user.displayName ?: "Athlete"
        }
    }

    val goalOptions = listOf(
        "Strength & Hypertrophy",
        "Movement Foundations",
        "Mobility & Posture",
        "Athletic Conditioning"
    )

    val levelOptions = listOf("Beginner", "Intermediate", "Advanced")

    val splitOptions = listOf(
        "Upper / Lower (4-Day Hypertrophy)",
        "Full Body (3-Day)",
        "Push / Pull / Legs (6-Day)",
        "Postural & Kinetic Mobility"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. User Header & Google Account Badge ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Photo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, TealPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user.photoUrl?.toString().isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = user.photoUrl.toString(),
                            contentDescription = "User Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = TealPrimary
                                )
                            },
                            error = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user.email ?: "google-account@auth",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Google SSO • Cloud Synced",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TealPrimary
                        )
                    }
                }

                IconButton(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier.testTag("sign_out_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // --- 2. Database Progress Overview & Workout Statistics ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cloud Progress & Logs",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = { showLogWorkoutDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("quick_log_workout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Workout", fontSize = 13.sp)
                    }
                }

                // Summary Metric Cards
                val totalMinutes = workoutLogs.sumOf { it.durationMinutes }
                val totalCompletedExercises = workoutLogs.sumOf { it.completedExercisesCount }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatPillCard(
                        modifier = Modifier.weight(1f),
                        title = "Sessions",
                        value = "${workoutLogs.size}",
                        accentColor = TealPrimary
                    )
                    StatPillCard(
                        modifier = Modifier.weight(1f),
                        title = "Minutes",
                        value = "$totalMinutes",
                        accentColor = AmberTertiary
                    )
                    StatPillCard(
                        modifier = Modifier.weight(1f),
                        title = "Exercises",
                        value = "$totalCompletedExercises",
                        accentColor = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = "Recent Cloud Workout History (${workoutLogs.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (workoutLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No workouts logged in database yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Finish an active workout session or tap 'Log Workout' above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        workoutLogs.take(5).forEach { log ->
                            WorkoutLogHistoryItem(
                                log = log,
                                onDelete = { logToDelete = log }
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Profile & Training Preferences Form ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Training Profile & Goals",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_display_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Training Goal Selector
                Text(
                    text = "Primary Training Goal",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    goalOptions.forEach { goal ->
                        FilterChip(
                            selected = trainingGoal == goal,
                            onClick = { trainingGoal = goal },
                            label = { Text(goal, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = TealPrimary
                            )
                        )
                    }
                }

                // Experience Level
                Text(
                    text = "Experience Level",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    levelOptions.forEach { level ->
                        FilterChip(
                            selected = fitnessLevel == level,
                            onClick = { fitnessLevel = level },
                            label = { Text(level) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = TealPrimary
                            )
                        )
                    }
                }

                // Weekly Workout Target Counter
                Text(
                    text = "Weekly Workout Target",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$weeklyTargetWorkouts sessions / week",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TealPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (weeklyTargetWorkouts > 1) weeklyTargetWorkouts-- },
                            enabled = weeklyTargetWorkouts > 1
                        ) {
                            Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "$weeklyTargetWorkouts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { if (weeklyTargetWorkouts < 7) weeklyTargetWorkouts++ },
                            enabled = weeklyTargetWorkouts < 7
                        ) {
                            Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Favorite Split
                Text(
                    text = "Preferred Workout Split",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    splitOptions.forEach { split ->
                        FilterChip(
                            selected = favoriteSplit == split,
                            onClick = { favoriteSplit = split },
                            label = { Text(split, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = TealPrimary
                            )
                        )
                    }
                }

                // Body Metrics (Weight / Height)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weightKgText,
                        onValueChange = { weightKgText = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = heightCmText,
                        onValueChange = { heightCmText = it },
                        label = { Text("Height (cm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save Profile Button
                Button(
                    onClick = {
                        if (isSaving) return@Button
                        isSaving = true
                        val weight = weightKgText.toDoubleOrNull()
                        val height = heightCmText.toDoubleOrNull()
                        val updatedProfile = UserProfile(
                            userId = user.uid,
                            email = user.email ?: "",
                            displayName = displayName.ifBlank { "Athlete" },
                            photoUrl = user.photoUrl?.toString(),
                            trainingGoal = trainingGoal,
                            fitnessLevel = fitnessLevel,
                            weeklyTargetWorkouts = weeklyTargetWorkouts.toLong(),
                            favoriteSplit = favoriteSplit,
                            weightKg = weight,
                            heightCm = height
                        )
                        coroutineScope.launch {
                            val result = profileRepository.saveUserProfile(updatedProfile)
                            isSaving = false
                            result.onSuccess {
                                Toast.makeText(context, "Profile updated & synced to Cloud!", Toast.LENGTH_SHORT).show()
                            }.onFailure { error ->
                                Toast.makeText(context, "Failed to save: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_profile_button"),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = Color.Black)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing to Cloud...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- Sign Out Confirmation Dialog ---
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out of PhysiApp?") },
            text = { Text("Your local session will end. Your workout logs and training profile remain safely preserved in Firebase Cloud.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        authManager.signOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Delete Workout Log Confirmation Dialog ---
    if (logToDelete != null) {
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("Delete Workout Log?") },
            text = { Text("Are you sure you want to remove '${logToDelete?.workoutName}' from your cloud database?") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = logToDelete?.logId ?: ""
                        logToDelete = null
                        coroutineScope.launch {
                            profileRepository.deleteWorkoutLog(id)
                            Toast.makeText(context, "Workout log removed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Quick Log Workout Dialog ---
    if (showLogWorkoutDialog) {
        QuickLogWorkoutDialog(
            onDismiss = { showLogWorkoutDialog = false },
            onLogWorkout = { workoutName, splitDay, durationMins, completedCount, notes ->
                showLogWorkoutDialog = false
                val newLog = WorkoutLogDoc(
                    logId = "log_${System.currentTimeMillis()}",
                    userId = user.uid,
                    workoutName = workoutName,
                    splitDay = splitDay,
                    durationMinutes = durationMins.toLong(),
                    completedExercisesCount = completedCount.toLong(),
                    totalExercisesCount = completedCount.toLong(),
                    notes = notes
                )
                coroutineScope.launch {
                    val res = profileRepository.logWorkout(newLog)
                    res.onSuccess {
                        Toast.makeText(context, "Workout logged to database!", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Failed to log: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun StatPillCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = accentColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WorkoutLogHistoryItem(
    log: WorkoutLogDoc,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val dateText = remember(log.completedAt) {
        val date = log.completedAt?.toDate() ?: Date()
        dateFormatter.format(date)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TealPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.workoutName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${log.splitDay} • ${log.durationMinutes} mins • ${log.completedExercisesCount} exercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = TealPrimary
                )
                if (!log.notes.isNullOrBlank()) {
                    Text(
                        text = "“${log.notes}”",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete log",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickLogWorkoutDialog(
    onDismiss: () -> Unit,
    onLogWorkout: (name: String, split: String, durationMins: Int, count: Int, notes: String) -> Unit
) {
    var workoutName by remember { mutableStateOf("Upper Body Hypertrophy") }
    var splitDay by remember { mutableStateOf("Upper Body") }
    var durationMins by remember { mutableIntStateOf(45) }
    var exerciseCount by remember { mutableIntStateOf(6) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = TealPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Workout Session")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { workoutName = it },
                    label = { Text("Workout Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = splitDay,
                    onValueChange = { splitDay = it },
                    label = { Text("Split / Focus") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = "$durationMins",
                        onValueChange = { durationMins = it.toIntOrNull() ?: 0 },
                        label = { Text("Duration (mins)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = "$exerciseCount",
                        onValueChange = { exerciseCount = it.toIntOrNull() ?: 0 },
                        label = { Text("Exercises") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Session Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (workoutName.isNotBlank() && splitDay.isNotBlank()) {
                        onLogWorkout(workoutName, splitDay, durationMins, exerciseCount, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary, contentColor = Color.Black)
            ) {
                Text("Log to Database")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
