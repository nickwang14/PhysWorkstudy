package com.example.physiapp.data.model

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

data class CurriculumOptionalReading(
    val id: String,
    val title: String,
    val summary: String,
    val durationMinutes: Int,
    val fullMarkdownText: String
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
    val optionalReading: CurriculumOptionalReading? = null
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
