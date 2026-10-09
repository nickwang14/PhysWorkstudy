package com.example.physiapp.data.content

import java.net.URI

/** Resolve a Markdown destination against its bundled document, not the device filesystem. */
fun resolveMarkdownImagePath(link: String, documentAssetPath: String): String? {
    val destination = runCatching { URI(link) }.getOrNull() ?: return null
    if (destination.scheme in listOf("https", "http") && !destination.host.isNullOrBlank()) {
        return link
    }
    if (destination.isAbsolute || destination.rawAuthority != null || link.startsWith("/")) return null
    if (!documentAssetPath.startsWith("curriculum/") || destination.path.isNullOrBlank()) return null

    val segments = documentAssetPath.substringBeforeLast('/').split('/').toMutableList()
    for (segment in destination.path.split('/')) {
        when (segment) {
            "", "." -> Unit
            ".." -> {
                if (segments.size <= 1) return null
                segments.removeAt(segments.lastIndex)
            }
            else -> {
                if ('\\' in segment) return null
                segments.add(segment)
            }
        }
    }
    if (segments.firstOrNull() != "curriculum") return null
    val encodedPath = URI(null, null, "/${segments.joinToString("/")}", null).rawPath
    return "file:///android_asset$encodedPath"
}