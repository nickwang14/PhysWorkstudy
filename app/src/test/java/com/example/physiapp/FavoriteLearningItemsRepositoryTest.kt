package com.example.physiapp

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.example.physiapp.data.repository.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FavoriteLearningItemsRepositoryTest {

    private lateinit var context: Context

    @Before
    fun clearPreferences() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("physiapp_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun lessonAndReadingFavoritesPersistIndependently() {
        val repository = UserPreferencesRepository(context)

        repository.toggleFavoriteLesson("lesson-1")
        repository.toggleFavoriteOptionalReading("reading-1")

        val restored = UserPreferencesRepository(context).favoritesFlow.value
        assertEquals(setOf("lesson-1"), restored.lessonIds)
        assertEquals(setOf("reading-1"), restored.optionalReadingIds)
    }

    @Test
    fun togglingAnExistingFavoriteRemovesOnlyThatItem() {
        val repository = UserPreferencesRepository(context)
        repository.toggleFavoriteLesson("lesson-1")
        repository.toggleFavoriteLesson("lesson-2")
        repository.toggleFavoriteOptionalReading("reading-1")

        repository.toggleFavoriteLesson("lesson-1")

        val favorites = repository.favoritesFlow.value
        assertEquals(setOf("lesson-2"), favorites.lessonIds)
        assertEquals(setOf("reading-1"), favorites.optionalReadingIds)
        assertEquals(favorites, UserPreferencesRepository(context).favoritesFlow.value)
    }

    @Test
    fun remoteCompletionsMergeWithoutChangingFavoritesStreakOrTraining() {
        val repository = UserPreferencesRepository(context)
        repository.completeLesson("lesson-local")
        repository.toggleFavoriteLesson("lesson-local")
        repository.toggleFavoriteOptionalReading("reading-1")
        val before = repository.progressFlow.value
        val favorites = repository.favoritesFlow.value
        val history = repository.workoutHistoryFlow.value

        repository.syncRemoteCompletedLessons(setOf("lesson-local", "lesson-remote"))

        val expected = before.copy(
            completedLessonIds = setOf("lesson-local", "lesson-remote"),
            totalLessonsCompleted = 2
        )
        assertEquals(expected, repository.progressFlow.value)
        assertEquals(favorites, repository.favoritesFlow.value)
        assertEquals(history, repository.workoutHistoryFlow.value)
        val restored = UserPreferencesRepository(context)
        assertEquals(expected, restored.progressFlow.value)
        assertEquals(favorites, restored.favoritesFlow.value)

        repository.syncRemoteCompletedLessons(setOf("lesson-remote"))
        repository.syncRemoteCompletedLessons(emptySet())
        assertEquals(expected, repository.progressFlow.value)
    }

    @Test
    fun savingReferencesDoesNotCompleteLearningOrChangeTraining() {
        val repository = UserPreferencesRepository(context)
        val before = repository.progressFlow.value

        repository.toggleFavoriteLesson("lesson-1")
        repository.toggleFavoriteOptionalReading("reading-1")

        assertEquals(before, repository.progressFlow.value)
        assertEquals(before, UserPreferencesRepository(context).progressFlow.value)
    }
}
