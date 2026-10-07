package com.example.physiapp.data.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MarkdownDocumentTest {
    @Test
    fun markdownWithoutMetadataIsPreserved() {
        val source = "# Chapter 1\n\n**Movement**\n\n---\n\nMore reading"
        val document = parseMarkdownDocument(source)
        assertNull(document.frontmatter)
        assertEquals(source, document.body)
    }

    @Test
    fun metadataIsSeparatedWithoutChangingBodyMarkup() {
        val document = parseMarkdownDocument("---\nid: lesson-1\ntitle: Movement\n---\n# Chapter 1\n\n- **Read**")
        assertEquals("id: lesson-1\ntitle: Movement", document.frontmatter)
        assertEquals("# Chapter 1\n\n- **Read**", document.body)
    }

    @Test
    fun bomAndWindowsLineEndingsAreSupported() {
        val document = parseMarkdownDocument("\uFEFF---\r\nid: lesson-1\r\n...\r\n# Chapter 1")
        assertEquals("id: lesson-1", document.frontmatter)
        assertEquals("# Chapter 1", document.body)
    }

    @Test
    fun emptyInputAndEmptyMetadataAreSupported() {
        assertEquals("", parseMarkdownDocument("").body)
        assertEquals("", parseMarkdownDocument("---\n---\n# Title").frontmatter)
    }

    @Test(expected = IllegalArgumentException::class)
    fun incompleteFrontmatterIsRejectedRatherThanLeaked() {
        parseMarkdownDocument("---\nid: private-metadata\n# Chapter 1")
    }

    @Test
    fun fencedYamlInsideTheBodyIsNotFrontmatter() {
        val source = "# Example\n\n```yaml\n---\nid: example\n---\n```"
        assertEquals(source, parseMarkdownDocument(source).body)
    }
}