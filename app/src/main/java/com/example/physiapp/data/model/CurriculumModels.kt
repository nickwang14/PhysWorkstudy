package com.example.physiapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

enum class TopicCategory(val displayName: String) {
    MOVEMENT_PATTERNS("Movement Patterns"),
    ANATOMY_PHYSIOLOGY("Anatomy & Physiology"),
    BIOMECHANICS("Biomechanics"),
    LOAD_AND_RECOVERY("Load & Deloads"),
    PROGRAMMING_COACHING("Programming & Coaching")
}

data class KnowledgeCheckOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
    val feedback: String
)

data class KnowledgeCheckQuestion(
    val id: String,
    val questionText: String,
    val options: List<KnowledgeCheckOption>
)

data class CurriculumLesson(
    val id: String,
    val chapterId: String,
    val subchapterId: String,
    val lessonIndex: Int,
    val title: String,
    val subtitle: String,
    val durationMinutes: Int,
    val category: TopicCategory,
    val summary: String,
    val fullMarkdownText: String,
    val keyTerms: List<String>,
    val practicalApplication: String,
    val questions: List<KnowledgeCheckQuestion>,
    val isGateMilestone: Boolean = false,
    val assetPath: String = ""
)

data class CurriculumSubchapter(
    val id: String,
    val chapterId: String,
    val title: String,
    val description: String,
    val lessons: List<CurriculumLesson>
)

data class CurriculumChapter(
    val id: String,
    val number: Int,
    val title: String,
    val description: String,
    val category: TopicCategory,
    val subchapters: List<CurriculumSubchapter>
)

/**
 * Firestore entity stored at /curriculum_lessons/{lessonId}.
 * Provides index metadata and relative markdown asset location.
 */
data class CurriculumLessonDoc(
    val lessonId: String = "",
    val title: String = "",
    val chapterId: String = "",
    val chapterNumber: Long = 1L,
    val chapterTitle: String = "",
    val subchapterId: String = "",
    val subchapterTitle: String = "",
    val lessonIndex: Long = 1L,
    val durationMinutes: Long = 5L,
    val category: String = "MOVEMENT_PATTERNS",
    val summary: String = "",
    val assetPath: String = "",
    val keyTerms: List<String> = emptyList(),
    val isGateMilestone: Boolean = false
) {
    @Suppress("UNCHECKED_CAST")
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "lessonId" to lessonId,
            "title" to title,
            "chapterId" to chapterId,
            "chapterNumber" to chapterNumber,
            "chapterTitle" to chapterTitle,
            "subchapterId" to subchapterId,
            "subchapterTitle" to subchapterTitle,
            "lessonIndex" to lessonIndex,
            "durationMinutes" to durationMinutes,
            "category" to category,
            "summary" to summary,
            "assetPath" to assetPath
        )
        return map.filterValues { it != null } as Map<String, Any>
    }
}

/**
 * Firestore entity stored at /users/{userId}/lesson_progress/{lessonId}.
 */
data class LessonProgressDoc(
    val lessonId: String = "",
    val userId: String = "",
    val completedAt: Timestamp? = null
) {
    @Suppress("UNCHECKED_CAST")
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "lessonId" to lessonId,
            "userId" to userId,
            "completedAt" to (completedAt ?: FieldValue.serverTimestamp())
        )
        return map.filterValues { it != null } as Map<String, Any>
    }
}
