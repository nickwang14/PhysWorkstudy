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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiapp.BuildConfig
import com.example.physiapp.data.model.ExerciseDbItem
import com.example.physiapp.data.repository.ExerciseCatalogFallbacks
import com.example.physiapp.data.repository.GatewayExerciseCatalogRepository
import com.example.physiapp.ui.components.ExerciseDbItemDialog
import com.example.physiapp.ui.theme.TealPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseDbBrowserDialog(
    onDismiss: () -> Unit,
    onSelectExercise: (ExerciseDbItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBodyPart by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var exerciseItems by remember { mutableStateOf(ExerciseCatalogFallbacks.starterItems) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedItemForDetail by remember { mutableStateOf<ExerciseDbItem?>(null) }
    var refreshToken by remember { mutableStateOf(0) }
    val repository = remember {
        GatewayExerciseCatalogRepository(BuildConfig.EXERCISE_CATALOG_GATEWAY_URL)
    }

    val bodyParts = listOf("chest", "back", "upper legs", "waist", "shoulders", "upper arms", "cardio")

    LaunchedEffect(searchQuery, selectedBodyPart, refreshToken, repository) {
        if (!repository.isRemoteConfigured) {
            exerciseItems = ExerciseCatalogFallbacks.filter(searchQuery, selectedBodyPart)
            errorMessage = null
            isLoading = false
            return
        }

        // Avoid spending provider quota on each keystroke; the gateway also enforces its own quota.
        delay(350)
        isLoading = true
        errorMessage = null
        val result = repository.search(
            query = searchQuery,
            bodyPart = selectedBodyPart
        )
        isLoading = false
        result.fold(
            onSuccess = { items -> exerciseItems = items },
            onFailure = {
                errorMessage = "The online catalog is unavailable. Showing local exercise examples."
                exerciseItems = ExerciseCatalogFallbacks.filter(searchQuery, selectedBodyPart)
            }
        )
    }

    fun refreshSearch() {
        refreshToken += 1
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ExerciseDB Library",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // API Status Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (repository.isRemoteConfigured) TealPrimary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (repository.isRemoteConfigured) "● PhysiApp exercise catalog configured"
                            else "● Local exercise examples (online catalog unavailable)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (repository.isRemoteConfigured) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or target muscle...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { refreshSearch() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercisedb_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                // Body Part Filter Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedBodyPart == null,
                        onClick = { selectedBodyPart = null },
                        label = { Text("All Parts") }
                    )
                    bodyParts.forEach { part ->
                        FilterChip(
                            selected = selectedBodyPart == part,
                            onClick = {
                                selectedBodyPart = if (selectedBodyPart == part) null else part
                            },
                            label = { Text(part.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = "Notice: $errorMessage",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Exercise items list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exerciseItems) { item ->
                        val pattern = item.inferredPattern()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedItemForDetail = item }
                                .testTag("exercisedb_item_${item.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(pattern.color)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.name.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${pattern.label} • ${item.target} • ${item.equipment}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )

    // Detail Modal for selected item
    selectedItemForDetail?.let { item ->
        ExerciseDbItemDialog(
            item = item,
            onDismiss = { selectedItemForDetail = null },
            onAddToWorkout = { selectedItem ->
                selectedItemForDetail = null
                onSelectExercise(selectedItem)
                onDismiss()
            }
        )
    }
}
