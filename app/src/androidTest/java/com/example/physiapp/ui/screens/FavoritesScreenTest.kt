package com.example.physiapp.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.physiapp.data.model.CurriculumOptionalReading
import com.example.physiapp.data.model.FavoriteLearningItems
import com.example.physiapp.data.repository.CurriculumRepository
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun savedLessonsAndOptionalReadingsCanBeReopenedOrRemoved() {
        val baseLesson = CurriculumRepository.allLessons.first()
        val reading = CurriculumOptionalReading(
            id = "reading-reference-1",
            title = "Further reading",
            summary = "A short optional reference.",
            durationMinutes = 12,
            fullMarkdownText = "# Further reading\n\nReference text."
        )
        val lesson = baseLesson.copy(optionalReading = reading)
        val favorites = FavoriteLearningItems(
            lessonIds = setOf(lesson.id),
            optionalReadingIds = setOf(reading.id)
        )
        var openedLessonId: String? = null
        var openedReadingId: String? = null
        var removedLessonId: String? = null
        var removedReadingId: String? = null

        compose.setContent {
            MaterialTheme {
                FavoritesScreen(
                    allLessons = listOf(lesson),
                    favorites = favorites,
                    onOpenLesson = { openedLessonId = it },
                    onOpenOptionalReading = { openedReadingId = it },
                    onRemoveLesson = { removedLessonId = it },
                    onRemoveOptionalReading = { removedReadingId = it }
                )
            }
        }

        compose.onNodeWithText(lesson.title).assertExists().performClick()
        compose.onNodeWithText(reading.title).assertExists().performClick()
        compose.onNodeWithTag("remove_favorite_lesson_${lesson.id}").performClick()
        compose.onNodeWithTag("remove_favorite_reading_${reading.id}").performClick()

        assertEquals(lesson.id, openedLessonId)
        assertEquals(reading.id, openedReadingId)
        assertEquals(lesson.id, removedLessonId)
        assertEquals(reading.id, removedReadingId)
    }
}
