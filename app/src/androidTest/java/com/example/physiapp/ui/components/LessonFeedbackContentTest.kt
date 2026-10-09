package com.example.physiapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.physiapp.data.model.LessonComment
import com.example.physiapp.data.model.LessonFeedbackContract
import com.example.physiapp.data.model.LessonFeedbackState
import com.example.physiapp.data.model.LessonReviewStatus
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LessonFeedbackContentTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun normalUserSeesLiteralPrivateTextButNoAdminControls() {
        val literal = "**not markdown** [not a link](https://example.invalid) <script>plain</script>"
        compose.setContent {
            MaterialTheme {
                LessonFeedbackContent(
                    LessonFeedbackState(ready = true, comments = listOf(LessonComment("id", "uid", literal))),
                    " \n", false, null, {}, {}, {}, {}, {}
                )
            }
        }
        compose.onNodeWithTag("lesson_comment_id").assertTextEquals(literal)
        compose.onNodeWithTag("review_approved").assertDoesNotExist()
        compose.onNodeWithTag("submit_lesson_comment").assertIsNotEnabled()
        compose.onNodeWithText("Only administrators can change review status.").assertExists()
    }

    @Test
    fun adminControlsDispatchExactStatusAndPendingDisablesWrites() {
        var selected: LessonReviewStatus? = null
        val pending = mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                LessonFeedbackContent(
                    LessonFeedbackState(ready = true, isAdmin = true), "Comment", pending.value, null,
                    {}, {}, {}, { selected = it }, {}
                )
            }
        }
        compose.onNodeWithTag("review_needs_improvement").performClick()
        assertEquals(LessonReviewStatus.NEEDS_IMPROVEMENT, selected)
        compose.runOnIdle { pending.value = true }
        compose.onNodeWithTag("review_approved").assertIsNotEnabled()
        compose.onNodeWithTag("submit_lesson_comment").assertIsNotEnabled()
    }

    @Test
    fun failedReviewReleasesControlsAndNeverShowsPendingStatusAsConfirmed() {
        val state = mutableStateOf(LessonFeedbackState(scopeVersion = 1, ready = true, isAdmin = true,
            reviewStatus = LessonReviewStatus.APPROVED))
        val completion = CompletableDeferred<Result<Unit>>()
        var selected: LessonReviewStatus? = null
        compose.setContent {
            MaterialTheme {
                LessonFeedbackEditor(state.value, { Result.success(Unit) }, { Result.success(Unit) },
                    { selected = it; completion.await() }, {})
            }
        }
        compose.onNodeWithTag("review_rejected").performClick().assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals(LessonReviewStatus.REJECTED, selected)
            state.value = state.value.copy(reviewStatus = LessonFeedbackContract.confirmedReviewStatus(
                state.value.reviewStatus, LessonReviewStatus.REJECTED, hasPendingWrites = true))
        }
        compose.onNodeWithTag("lesson_review_status").assertTextEquals("Review status: Approved")
        compose.runOnIdle { completion.complete(Result.failure(IllegalStateException("Private provider detail"))) }
        compose.onNodeWithTag("review_rejected").assertIsEnabled()
        compose.onNodeWithTag("lesson_review_status").assertTextEquals("Review status: Approved")
        compose.onNodeWithText("Unable to save lesson feedback. Check your connection and access.").assertExists()
        compose.onNodeWithText("Review status updated.").assertDoesNotExist()
        compose.onNodeWithText("Private provider detail").assertDoesNotExist()
        compose.runOnIdle {
            state.value = state.value.copy(reviewStatus = LessonFeedbackContract.confirmedReviewStatus(
                state.value.reviewStatus, LessonReviewStatus.REJECTED, hasPendingWrites = false))
        }
        compose.onNodeWithTag("lesson_review_status").assertTextEquals("Review status: Rejected")
    }

    @Test
    fun failedScopeHidesCommentsEditorAndAdminControls() {
        val state = mutableStateOf(LessonFeedbackState(ready = true, isAdmin = true,
            comments = listOf(LessonComment("id", "uid", "Private comment"))))
        compose.setContent {
            MaterialTheme { LessonFeedbackContent(state.value, "Private draft", false, null, {}, {}, {}, {}, {}) }
        }
        compose.runOnIdle { state.value = LessonFeedbackState(scopeVersion = 2, error = "Lesson feedback unavailable.") }
        compose.onNodeWithText("Private comment").assertDoesNotExist()
        compose.onNodeWithTag("lesson_comment_input").assertDoesNotExist()
        compose.onNodeWithTag("review_approved").assertDoesNotExist()
        compose.onNodeWithText("Retry lesson feedback").assertExists()
    }

    @Test
    fun scopeChangeDiscardsDraftAndPendingResult() {
        val state = mutableStateOf(LessonFeedbackState(scopeVersion = 1, ready = true))
        val completion = CompletableDeferred<Result<Unit>>()
        compose.setContent {
            MaterialTheme {
                LessonFeedbackEditor(state.value, { completion.await() }, { Result.success(Unit) },
                    { Result.success(Unit) }, {})
            }
        }
        compose.onNodeWithTag("lesson_comment_input").performTextInput("Private draft")
        compose.onNodeWithTag("submit_lesson_comment").performClick().assertIsNotEnabled()
        compose.runOnIdle { state.value = LessonFeedbackState(scopeVersion = 2, ready = true) }
        compose.onNodeWithText("Private draft").assertDoesNotExist()
        compose.onNodeWithTag("submit_lesson_comment").assertIsNotEnabled()
        compose.runOnIdle { completion.complete(Result.success(Unit)) }
        compose.onNodeWithText("Comment submitted.").assertDoesNotExist()
    }

    @Test
    fun failurePreservesDraftUsesGenericErrorAndSuccessClearsIt() {
        var succeed = false
        compose.setContent {
            MaterialTheme {
                LessonFeedbackEditor(LessonFeedbackState(scopeVersion = 1, ready = true),
                    { if (succeed) Result.success(Unit) else Result.failure(IllegalStateException("Private provider detail")) },
                    { Result.success(Unit) }, { Result.success(Unit) }, {})
            }
        }
        compose.onNodeWithTag("lesson_comment_input").performTextInput("Private draft")
        compose.onNodeWithTag("submit_lesson_comment").performClick()
        compose.onNodeWithText("Unable to save lesson feedback. Check your connection and access.").assertExists()
        compose.onNodeWithText("Private provider detail").assertDoesNotExist()
        compose.onNodeWithText("Private draft").assertExists()
        compose.runOnIdle { succeed = true }
        compose.onNodeWithTag("submit_lesson_comment").performClick()
        compose.onNodeWithText("Comment submitted.").assertExists()
        compose.onNodeWithText("Private draft").assertDoesNotExist()
        compose.onNodeWithTag("submit_lesson_comment").assertIsNotEnabled()
    }

    @Test
    fun editorRejectsOverLimitInputAndWhitespace() {
        compose.setContent {
            MaterialTheme {
                LessonFeedbackEditor(LessonFeedbackState(ready = true), { Result.success(Unit) },
                    { Result.success(Unit) }, { Result.success(Unit) }, {})
            }
        }
        compose.onNodeWithTag("lesson_comment_input").performTextInput("a".repeat(2001))
        compose.onNodeWithText("Comments must be at most 2000 characters.").assertExists()
        compose.onNodeWithTag("submit_lesson_comment").assertIsNotEnabled()
        compose.onNodeWithTag("lesson_comment_input").performTextReplacement(" \n\t")
        compose.onNodeWithTag("submit_lesson_comment").assertIsNotEnabled()
    }
}