package com.example.physiapp.data.repository

import com.example.physiapp.data.model.LessonFeedbackContract
import com.example.physiapp.data.model.LessonReviewStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonFeedbackContractTest {
    @Test
    fun commentValidationRejectsWhitespaceAndOverLimitWithoutEchoingInput() {
        listOf("", " \t\n", "\u2003").forEach { assertFalse(LessonFeedbackContract.isValidComment(it)) }
        assertTrue(LessonFeedbackContract.isValidComment("a".repeat(2000)))
        val privateText = "private".repeat(300)
        assertFalse(LessonFeedbackContract.isValidComment(privateText))
        val error = runCatching { LessonFeedbackContract.commentPayload("uid", privateText, Any()) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertFalse(error!!.message.orEmpty().contains(privateText))
        assertFalse(LessonFeedbackContract.isValidComment(" " + "a".repeat(2000)))
    }

    @Test
    fun idsRejectBlankAndSlashesBeforePathConstruction() {
        listOf("", "  ", "/", "lesson/comments", "chapter/lesson").forEach {
            assertFalse(LessonFeedbackContract.isValidId(it))
        }
        assertTrue(LessonFeedbackContract.isValidId("terminology-01"))
    }

    @Test
    fun onlyTheBooleanAdminClaimEnablesAdminAccess() {
        assertTrue(LessonFeedbackContract.isAdminClaim(true))
        listOf(null, false, "true", 1, "admin").forEach { assertFalse(LessonFeedbackContract.isAdminClaim(it)) }
    }

    @Test
    fun reviewStatusesHaveOnlyExactWireValues() {
        assertEquals(setOf("approved", "rejected", "needs_improvement"), LessonReviewStatus.entries.map { it.wireValue }.toSet())
        LessonReviewStatus.entries.forEach { assertEquals(it, LessonReviewStatus.fromWire(it.wireValue)) }
        listOf(null, "", "APPROVED", " approved", "pending").forEach { assertNull(LessonReviewStatus.fromWire(it)) }
    }

    @Test
    fun pendingReviewNeverOverwritesLastConfirmedStatus() {
        val statuses = listOf<LessonReviewStatus?>(null) + LessonReviewStatus.entries
        statuses.forEach { previous ->
            statuses.forEach { incoming ->
                assertEquals(previous, LessonFeedbackContract.confirmedReviewStatus(previous, incoming, true))
            }
        }
    }

    @Test
    fun acknowledgedReviewReplacesStatusIncludingDocumentRemoval() {
        val statuses = listOf<LessonReviewStatus?>(null) + LessonReviewStatus.entries
        statuses.forEach { previous ->
            statuses.forEach { incoming ->
                assertEquals(incoming, LessonFeedbackContract.confirmedReviewStatus(previous, incoming, false))
            }
        }
    }

    @Test
    fun reviewChangesOnlyOnAcknowledgementAndFailureRetainsConfirmedStatus() {
        val confirmed = LessonReviewStatus.APPROVED
        val pending = LessonFeedbackContract.confirmedReviewStatus(confirmed, LessonReviewStatus.REJECTED, true)
        assertEquals(confirmed, pending)
        assertEquals(confirmed, LessonFeedbackContract.confirmedReviewStatus(pending, confirmed, false))
        assertEquals(LessonReviewStatus.REJECTED,
            LessonFeedbackContract.confirmedReviewStatus(pending, LessonReviewStatus.REJECTED, false))
    }

    @Test
    fun payloadsContainOnlyContractFieldsAndUseTheSuppliedTimestampSentinel() {
        val timestamp = Any()
        val comment = LessonFeedbackContract.commentPayload("uid", "  **plain** https://example.invalid  ", timestamp)
        assertEquals(setOf("authorId", "text", "createdAt"), comment.keys)
        assertEquals("uid", comment["authorId"])
        assertEquals("**plain** https://example.invalid", comment["text"])
        assertTrue(comment["createdAt"] === timestamp)
        LessonReviewStatus.entries.forEach { status ->
            val review = LessonFeedbackContract.reviewPayload("uid", status, timestamp)
            assertEquals(setOf("status", "updatedBy", "updatedAt"), review.keys)
            assertEquals(status.wireValue, review["status"])
            assertEquals("uid", review["updatedBy"])
            assertTrue(review["updatedAt"] === timestamp)
        }
    }
}