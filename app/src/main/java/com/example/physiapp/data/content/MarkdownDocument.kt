package com.example.physiapp.data.content

/** Frontmatter stays separate from display text; YAML schema parsing belongs in ingestion. */
data class MarkdownDocument(
    val frontmatter: String?,
    val body: String
)

fun parseMarkdownDocument(source: String): MarkdownDocument {
    val normalized = source.removePrefix("\uFEFF").replace("\r\n", "\n").replace('\r', '\n')
    val lines = normalized.split('\n')
    if (lines.first().trimEnd() != "---") {
        return MarkdownDocument(frontmatter = null, body = normalized)
    }

    val closingIndex = (1 until lines.size).firstOrNull {
        lines[it].trimEnd() == "---" || lines[it].trimEnd() == "..."
    }
    require(closingIndex != null) { "Unterminated YAML frontmatter" }
    return MarkdownDocument(
        frontmatter = lines.subList(1, closingIndex).joinToString("\n"),
        body = lines.drop(closingIndex + 1).joinToString("\n")
    )
}