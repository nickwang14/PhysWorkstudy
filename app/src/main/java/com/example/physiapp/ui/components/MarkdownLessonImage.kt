package com.example.physiapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import coil.Coil
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.example.physiapp.data.content.resolveMarkdownImagePath
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode

/** Standalone lesson figures, including accessible alt text and an explicit load-error fallback. */
@Composable
internal fun MarkdownLessonImage(content: String, node: ASTNode, documentAssetPath: String) {
    val link = node.findImageChild(MarkdownElementTypes.LINK_DESTINATION)
        ?.getUnescapedTextInNode(content).orEmpty()
    val alt = node.findImageChild(MarkdownElementTypes.LINK_TEXT)
        ?.getUnescapedTextInNode(content)?.removeSurrounding("[", "]").orEmpty()
    val model = remember(link, documentAssetPath) { resolveMarkdownImagePath(link, documentAssetPath) }
    val context = LocalContext.current
    // Keep the app's caches/network configuration, and support SVG even in standalone readers/tests.
    val imageLoader = remember(context) {
        Coil.imageLoader(context).newBuilder().components { add(SvgDecoder.Factory()) }.build()
    }
    var status by remember(model) { mutableStateOf(if (model == null) "unavailable" else "loading") }

    if (model != null) {
        AsyncImage(
            model = model,
            imageLoader = imageLoader,
            contentDescription = alt.ifBlank { "Lesson illustration" },
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
                .testTag("markdown_image_$link")
                .semantics { stateDescription = status },
            onSuccess = { status = "loaded" },
            onError = { status = "unavailable" }
        )
    }
    if (status == "unavailable") {
        Text(
            text = "Illustration unavailable: ${alt.ifBlank { link }}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun ASTNode.findImageChild(elementType: IElementType): ASTNode? {
    if (type == elementType) return this
    for (child in children) {
        child.findImageChild(elementType)?.let { return it }
    }
    return null
}