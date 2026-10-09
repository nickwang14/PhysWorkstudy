package com.example.physiapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.physiapp.data.model.LessonComment
import com.example.physiapp.data.model.LessonFeedbackContract
import com.example.physiapp.data.model.LessonFeedbackState
import com.example.physiapp.data.model.LessonReviewStatus
import com.example.physiapp.data.repository.LessonFeedbackRepository
import kotlinx.coroutines.launch

@Composable
fun LessonFeedbackSection(repository: LessonFeedbackRepository) {
    var retry by remember(repository) { mutableStateOf(0) }
    // Scope recreation disposes collectors and in-flight UI coroutines, never saving private drafts.
    key(repository, retry) {
        val flow = remember(repository, retry) { repository.observe() }
        val state by flow.collectAsState(initial = LessonFeedbackState())
        LessonFeedbackEditor(
            state, repository::addComment, repository::deleteComment, repository::setReviewStatus,
            onRetry = { retry++ }
        )
    }
}

/** Editor state is transient and belongs to this access epoch, never to saved instance state. */
@Composable
internal fun LessonFeedbackEditor(
    state: LessonFeedbackState,
    onAddComment: suspend (String) -> Result<Unit>,
    onDeleteComment: suspend (String) -> Result<Unit>,
    onUpdateStatus: suspend (LessonReviewStatus) -> Result<Unit>,
    onRetry: () -> Unit
) {
    key(state.scopeVersion) {
        var draft by remember { mutableStateOf("") }
        var inputError by remember { mutableStateOf<String?>(null) }
        var pending by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        fun perform(success: String, clearDraft: Boolean = false, action: suspend () -> Result<Unit>) {
            if (pending || !state.ready) return
            pending = true
            message = null
            inputError = null
            scope.launch {
                val result = action()
                pending = false
                if (result.isSuccess) {
                    if (clearDraft) draft = ""
                    message = success
                } else {
                    message = "Unable to save lesson feedback. Check your connection and access."
                }
            }
        }

        LessonFeedbackContent(
            state = state,
            draft = draft,
            pending = pending,
            message = inputError ?: message,
            onDraftChange = { text ->
                message = null
                if (text.length <= LessonFeedbackContract.MAX_COMMENT_LENGTH) {
                    draft = text
                    inputError = null
                } else {
                    inputError = "Comments must be at most 2000 characters."
                }
            },
            onSubmit = {
                val text = draft
                if (LessonFeedbackContract.isValidComment(text)) {
                    perform("Comment submitted.", clearDraft = true) { onAddComment(text) }
                }
            },
            onDelete = { comment -> perform("Comment deleted.") { onDeleteComment(comment.id) } },
            onSetStatus = { status -> perform("Review status updated.") { onUpdateStatus(status) } },
            onRetry = onRetry
        )
    }
}

/** Plain text only: comment strings are never passed to Markdown, links, or WebView. */
@Composable
fun LessonFeedbackContent(
    state: LessonFeedbackState,
    draft: String,
    pending: Boolean,
    message: String?,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDelete: (LessonComment) -> Unit,
    onSetStatus: (LessonReviewStatus) -> Unit,
    onRetry: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().testTag("lesson_feedback")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Lesson review & private comments", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() })
            Text("This module is this individual curriculum lesson. Your comments are visible only to you and administrators.")
            Text("Comments and review status do not affect lesson completion or learning progress.")

            if (state.error != null) {
                Text(state.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                TextButton(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) { Text("Retry lesson feedback") }
            } else if (!state.ready) {
                Text("Loading lesson feedback…")
            } else {
                Text("Review status: ${state.reviewStatus?.label ?: "Not reviewed"}",
                    modifier = Modifier.testTag("lesson_review_status"))
                if (state.isAdmin) {
                    Text("Administrator review controls")
                    LessonReviewStatus.entries.forEach { status ->
                        TextButton(
                            onClick = { onSetStatus(status) },
                            enabled = !pending,
                            modifier = Modifier.heightIn(min = 48.dp).testTag("review_${status.wireValue}")
                        ) { Text("Set review status: ${status.label}") }
                    }
                } else {
                    Text("Only administrators can change review status.")
                }

                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    enabled = !pending,
                    label = { Text("Private lesson comment") },
                    supportingText = { Text("${draft.length}/2000 characters. Nonblank text required.") },
                    modifier = Modifier.fillMaxWidth().testTag("lesson_comment_input"),
                    minLines = 3
                )
                Button(
                    onClick = onSubmit,
                    enabled = !pending && LessonFeedbackContract.isValidComment(draft),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("submit_lesson_comment")
                ) { Text(if (pending) "Saving lesson feedback…" else "Submit private comment") }

                Text("Showing up to 50 ${if (state.isAdmin) "lesson" else "your"} comments, sorted within this limited list; not necessarily the newest 50.")
                if (state.comments.isEmpty()) Text("No comments in this list.")
                state.comments.forEachIndexed { index, comment ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(comment.text, modifier = Modifier.testTag("lesson_comment_${comment.id}"))
                        TextButton(
                            onClick = { onDelete(comment) },
                            enabled = !pending,
                            modifier = Modifier.heightIn(min = 48.dp).testTag("delete_comment_${comment.id}")
                        ) { Text("Delete comment ${index + 1}") }
                    }
                }
            }
            message?.let {
                Text(it, modifier = Modifier.testTag("lesson_feedback_message")
                    .semantics { liveRegion = LiveRegionMode.Polite })
            }
        }
    }
}