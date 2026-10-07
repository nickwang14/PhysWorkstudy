package com.example.physiapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.physiapp.data.model.CurriculumLesson
import com.example.physiapp.data.model.FavoriteLearningItems

@Composable
fun FavoritesScreen(
    allLessons: List<CurriculumLesson>,
    favorites: FavoriteLearningItems,
    onOpenLesson: (String) -> Unit,
    onOpenOptionalReading: (String) -> Unit,
    onRemoveLesson: (String) -> Unit,
    onRemoveOptionalReading: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteLessons = allLessons.filter { it.id in favorites.lessonIds }
    val favoriteReadings = allLessons.mapNotNull { lesson ->
        lesson.optionalReading
            ?.takeIf { it.id in favorites.optionalReadingIds }
            ?.let { lesson to it }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 18.dp, bottom = 4.dp)) {
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Your saved lessons and optional readings, ready to revisit.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (favoriteLessons.isEmpty() && favoriteReadings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("favorites_empty_state"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text("Nothing saved yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Open a lesson and tap the bookmark to keep it here. Optional readings can be saved from their reading page.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (allLessons.none { it.optionalReading != null }) {
                            Text(
                                "Optional readings are not available in this curriculum yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (favoriteLessons.isNotEmpty()) {
            item { SectionHeading("Lessons", Icons.Default.School) }
            items(favoriteLessons, key = { "lesson_${it.id}" }) { lesson ->
                FavoriteItemCard(
                    title = lesson.title,
                    subtitle = "${lesson.category.displayName} • ${lesson.durationMinutes} min",
                    testTag = "favorite_lesson_${lesson.id}",
                    onClick = { onOpenLesson(lesson.id) },
                    onRemove = { onRemoveLesson(lesson.id) }
                )
            }
        }

        if (favoriteReadings.isNotEmpty()) {
            item { SectionHeading("Optional readings", Icons.Default.MenuBook) }
            items(favoriteReadings, key = { "reading_${it.second.id}" }) { (lesson, reading) ->
                FavoriteItemCard(
                    title = reading.title,
                    subtitle = "For ${lesson.title} • ${reading.durationMinutes} min",
                    testTag = "favorite_reading_${reading.id}",
                    onClick = { onOpenOptionalReading(reading.id) },
                    onRemove = { onRemoveOptionalReading(reading.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SectionHeading(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FavoriteItemCard(
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRemove, modifier = Modifier.testTag("remove_$testTag")) {
                Icon(
                    imageVector = Icons.Default.BookmarkRemove,
                    contentDescription = "Remove $title from favorites",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
