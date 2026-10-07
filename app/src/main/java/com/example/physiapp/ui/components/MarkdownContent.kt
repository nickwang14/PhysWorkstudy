package com.example.physiapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.physiapp.data.content.parseMarkdownDocument
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownText
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownTypography
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes

/** Shared reader for lesson bodies, and future imported/optional Markdown readings. */
@Composable
fun MarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val document = remember(markdown) { runCatching { parseMarkdownDocument(markdown) } }
    val body = document.getOrNull()?.body
    if (body == null) {
        Text(
            text = "This lesson could not be displayed. Please try again after updating the content.",
            modifier = modifier,
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    val typography = MaterialTheme.typography
    val bodyStyle = typography.bodyLarge.copy(lineHeight = 24.sp)
    Markdown(
        content = body,
        modifier = modifier.fillMaxWidth(),
        colors = markdownColor(
            text = MaterialTheme.colorScheme.onSurface,
            linkText = MaterialTheme.colorScheme.primary
        ),
        typography = markdownTypography(
            h1 = typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            h2 = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            h3 = typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            h4 = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            h5 = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            h6 = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            text = bodyStyle,
            paragraph = bodyStyle,
            ordered = bodyStyle,
            bullet = bodyStyle,
            list = bodyStyle
        ),
        components = markdownComponents(
            heading1 = accessibleHeading({ it.h1 }),
            heading2 = accessibleHeading({ it.h2 }),
            heading3 = accessibleHeading({ it.h3 }),
            heading4 = accessibleHeading({ it.h4 }),
            heading5 = accessibleHeading({ it.h5 }),
            heading6 = accessibleHeading({ it.h6 }),
            setextHeading1 = accessibleHeading({ it.h1 }, MarkdownTokenTypes.SETEXT_CONTENT),
            setextHeading2 = accessibleHeading({ it.h2 }, MarkdownTokenTypes.SETEXT_CONTENT)
        )
    )
}

private fun accessibleHeading(
    style: (MarkdownTypography) -> TextStyle,
    contentChildType: IElementType = MarkdownTokenTypes.ATX_CONTENT
): MarkdownComponent = {
    MarkdownText(
        content = it.content,
        node = it.node,
        style = style(it.typography),
        modifier = Modifier.semantics { heading() },
        contentChildType = contentChildType
    )
}