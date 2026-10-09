package com.example.physiapp.data.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MarkdownImagePathTest {
    private val lessonPath = "curriculum/chapter-1/subchapter-2/lesson-01.md"

    @Test
    fun resolvesRelativeGraphicsFromTheLessonDirectory() {
        assertEquals(
            "file:///android_asset/curriculum/assets/squat-versus-hinge.svg",
            resolveMarkdownImagePath("../../assets/squat-versus-hinge.svg", lessonPath)
        )
        assertEquals(
            "file:///android_asset/curriculum/chapter-1/subchapter-2/figure.jpg",
            resolveMarkdownImagePath("./figure.jpg", lessonPath)
        )
    }

    @Test
    fun handlesEncodedPathsAndLeavesWebImagesUnchanged() {
        assertEquals(
            "file:///android_asset/curriculum/assets/my%20figure.svg",
            resolveMarkdownImagePath("../../assets/my%20figure.svg", lessonPath)
        )
        assertEquals("https://example.org/figure.jpg", resolveMarkdownImagePath("https://example.org/figure.jpg", ""))
    }

    @Test
    fun rejectsMissingBasePathsAndEscapesOutsideTheCurriculum() {
        for (link in listOf("../../../private.jpg", "../../%2e%2e/private.jpg", "/private.jpg", "file:///private.jpg", "//example.org/image.jpg", "", "bad path.jpg")) {
            assertNull(link, resolveMarkdownImagePath(link, lessonPath))
        }
        assertNull(resolveMarkdownImagePath("../../assets/figure.svg", ""))
    }
}