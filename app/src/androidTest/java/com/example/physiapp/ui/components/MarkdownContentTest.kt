package com.example.physiapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.font.FontWeight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.physiapp.data.repository.CurriculumRepository
import com.example.physiapp.ui.screens.LessonDetailScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarkdownContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun headingsAreRenderedWithoutMarkersAndExposeHeadingSemantics() {
        compose.setContent {
            MaterialTheme {
                MarkdownContent("# Chapter 1\n\n## Movement\n\n### Practice\n\n#### Apply\n\n##### Review\n\n###### Summary")
            }
        }
        val heading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)
        compose.onNodeWithText("Chapter 1").assertTextEquals("Chapter 1").assert(heading)
        compose.onNodeWithText("Movement").assert(heading)
        compose.onNodeWithText("Practice").assert(heading)
        compose.onAllNodes(heading).assertCountEquals(6)
        compose.onNodeWithText("# Chapter 1").assertDoesNotExist()
    }

    @Test
    fun lessonScreenUsesTheFormattedReader() {
        val lesson = CurriculumRepository.allLessons.first().copy(
            fullMarkdownText = "# Chapter 1\n\nSome **formatted** text."
        )
        compose.setContent {
            MaterialTheme {
                LessonDetailScreen(
                    lesson = lesson,
                    isAlreadyCompleted = false,
                    isFavorite = false,
                    onBack = {},
                    onCompleteLesson = {},
                    onToggleFavorite = {},
                    onOpenOptionalReading = {}
                )
            }
        }
        compose.onNodeWithText("Chapter 1")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .assert(hasAnyAncestor(hasTestTag("lesson_markdown_body")))
        compose.onNodeWithText("Some formatted text.").assertExists()
        compose.onNodeWithText("# Chapter 1", substring = true).assertDoesNotExist()
    }

    @Test
    fun listsQuotesAndInlineCodeDoNotExposeMarkup() {
        compose.setContent {
            MaterialTheme {
                MarkdownContent("- Squat\n- Hinge\n\n1. Practice\n2. Rest\n\n> Keep learning\n\nUse `tempo`.")
            }
        }
        compose.onNodeWithText("Squat").assertExists()
        compose.onNodeWithText("Hinge").assertExists()
        compose.onNodeWithText("Practice").assertExists()
        compose.onNodeWithText("Rest").assertExists()
        compose.onNodeWithText("Keep learning").assertExists()
        compose.onNodeWithText("Use tempo.").assertExists()
        compose.onNodeWithText("`tempo`", substring = true).assertDoesNotExist()
    }

    @Test
    fun frontmatterIsHiddenAndBoldIsStyledRatherThanLiteral() {
        compose.setContent {
            MaterialTheme {
                MarkdownContent("---\nid: internal-lesson-id\n---\n# Title\n\nRead **carefully**.")
            }
        }
        compose.onNodeWithText("internal-lesson-id", substring = true).assertDoesNotExist()
        val text = compose.onNodeWithText("Read carefully.").fetchSemanticsNode()
            .config[SemanticsProperties.Text].single()
        assertTrue(text.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        compose.onNodeWithText("**carefully**", substring = true).assertDoesNotExist()
    }

    @Test
    fun setextHeadingsAlsoExposeHeadingSemantics() {
        compose.setContent {
            MaterialTheme { MarkdownContent("Chapter 1\n=========\n\nMovement\n--------") }
        }
        compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .assertCountEquals(2)
    }

    @Test
    fun malformedFrontmatterShowsErrorWithoutMetadata() {
        compose.setContent {
            MaterialTheme { MarkdownContent("---\nid: internal-lesson-id") }
        }
        compose.onNodeWithText("This lesson could not be displayed.", substring = true).assertExists()
        compose.onNodeWithText("internal-lesson-id", substring = true).assertDoesNotExist()
    }
}