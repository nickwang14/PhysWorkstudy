package com.example.physiapp

import com.example.physiapp.base.FirestoreEmulatorTestBase
import com.example.physiapp.data.model.UserProfile
import com.example.physiapp.data.model.WorkoutLogDoc
import com.example.physiapp.data.repository.UserProfileRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun testAuthenticatedUser_canSaveAndReadProfile() = runBlocking {
        val aliceUid = signInTestUser("alice_rule_test@physiapp.com")
        val repository = UserProfileRepository(firestore, auth)

        val profile = UserProfile(
            userId = aliceUid,
            email = "alice_rule_test@physiapp.com",
            displayName = "Alice Athlete",
            trainingGoal = "Strength & Hypertrophy",
            fitnessLevel = "Intermediate",
            weeklyTargetWorkouts = 4L,
            favoriteSplit = "Upper / Lower (4-Day Hypertrophy)",
            weightKg = 65.0,
            heightCm = 170.0
        )

        val result = repository.saveUserProfile(profile)
        assertTrue("Save user profile should succeed", result.isSuccess)

        val readProfile = repository.observeUserProfile(aliceUid).first()
        assertNotNull("Observed profile should not be null", readProfile)
        assertEquals(aliceUid, readProfile?.userId)
        assertEquals("Alice Athlete", readProfile?.displayName)
    }

    @Test
    fun testUnauthenticatedUser_cannotSaveProfile() = runBlocking {
        auth.signOut()
        val repository = UserProfileRepository(firestore, auth)

        val unauthProfile = UserProfile(
            userId = "unauthenticated_user_123",
            email = "ghost@physiapp.com",
            displayName = "Ghost",
            trainingGoal = "Movement Foundations",
            fitnessLevel = "Beginner",
            weeklyTargetWorkouts = 3L,
            favoriteSplit = "Full Body (3-Day)"
        )

        try {
            val result = repository.saveUserProfile(unauthProfile)
            assertTrue("Save without auth should return failure", result.isFailure)
        } catch (e: IllegalStateException) {
            // requireUserId() threw as expected
            assertTrue(e.message?.contains("authenticated") == true)
        }
    }

    @Test
    fun testAuthenticatedUser_canLogWorkout() = runBlocking {
        val aliceUid = signInTestUser("alice_workout_log@physiapp.com")
        val repository = UserProfileRepository(firestore, auth)

        val log = WorkoutLogDoc(
            logId = "log_test_1",
            userId = aliceUid,
            workoutName = "Upper Body Hypertrophy",
            splitDay = "Upper Body",
            durationMinutes = 45L,
            completedExercisesCount = 5L,
            totalExercisesCount = 5L,
            notes = "Felt great on bench press"
        )

        val result = repository.logWorkout(log)
        assertTrue("Logging workout should succeed", result.isSuccess)

        val logs = repository.observeWorkoutLogs(aliceUid).first()
        assertTrue("Logs list should not be empty", logs.isNotEmpty())
        assertEquals("log_test_1", logs[0].logId)
        assertEquals("Upper Body Hypertrophy", logs[0].workoutName)
    }
}
