package com.example.physiapp.data.repository

import android.content.Context
import android.util.Log
import com.example.physiapp.data.model.CurriculumChapter
import com.example.physiapp.data.model.CurriculumLesson
import com.example.physiapp.data.model.CurriculumLessonDoc
import com.example.physiapp.data.model.CurriculumSubchapter
import com.example.physiapp.data.model.KnowledgeCheckOption
import com.example.physiapp.data.model.KnowledgeCheckQuestion
import com.example.physiapp.data.model.TopicCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "CurriculumRepository"

object CurriculumRepository {

    @Volatile
    private var initialized = false
    private var appContext: Context? = null

    private var cachedChapters: List<CurriculumChapter> = emptyList()
    private var cachedLessons: List<CurriculumLesson> = emptyList()
    private val lessonCache = ConcurrentHashMap<String, CurriculumLesson>()
    private val markdownCache = ConcurrentHashMap<String, String>()

    val chapters: List<CurriculumChapter>
        get() {
            ensureInitialized()
            return cachedChapters
        }

    val allLessons: List<CurriculumLesson>
        get() {
            ensureInitialized()
            return cachedLessons
        }

    fun init(context: Context) {
        if (initialized && appContext != null) return
        synchronized(this) {
            if (initialized && appContext != null) return
            appContext = context.applicationContext
            loadManifestFromAssets(context.applicationContext)
            initialized = true
        }
    }

    private fun ensureInitialized() {
        if (!initialized && appContext != null) {
            init(appContext!!)
        }
    }

    fun getLessonById(id: String): CurriculumLesson? {
        ensureInitialized()
        val lesson = lessonCache[id] ?: cachedLessons.find { it.id == id } ?: return null
        return if (lesson.fullMarkdownText.isBlank() && lesson.assetPath.isNotBlank() && appContext != null) {
            val md = loadMarkdownText(appContext!!, lesson.assetPath)
            val updated = lesson.copy(fullMarkdownText = md)
            lessonCache[id] = updated
            updated
        } else {
            lesson
        }
    }

    fun getNextLesson(currentId: String): CurriculumLesson? {
        val list = allLessons
        val index = list.indexOfFirst { it.id == currentId }
        return if (index in 0 until list.size - 1) list[index + 1] else null
    }

    fun loadMarkdownText(context: Context, assetPath: String): String {
        if (assetPath.isBlank()) return ""
        markdownCache[assetPath]?.let { return it }
        return try {
            context.assets.open(assetPath).use { stream ->
                InputStreamReader(stream, Charsets.UTF_8).use { reader ->
                    val text = reader.readText().removePrefix("\uFEFF")
                    markdownCache[assetPath] = text
                    text
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed reading markdown asset at $assetPath", e)
            ""
        }
    }

    private fun loadManifestFromAssets(context: Context) {
        try {
            val jsonString = context.assets.open("curriculum/curriculum_index.json").use { stream ->
                InputStreamReader(stream, Charsets.UTF_8).use { it.readText() }
            }
            val root = JSONObject(jsonString)
            val chaptersJson = root.optJSONArray("chapters") ?: JSONArray()
            val parsedChapters = mutableListOf<CurriculumChapter>()
            val parsedLessons = mutableListOf<CurriculumLesson>()

            for (i in 0 until chaptersJson.length()) {
                val chObj = chaptersJson.getJSONObject(i)
                val chId = chObj.getString("id")
                val chNum = chObj.optInt("number", i + 1)
                val chTitle = chObj.getString("title")
                val chDesc = chObj.optString("description", "")
                val chCat = parseTopicCategory(chObj.optString("category", "MOVEMENT_PATTERNS"))

                val subchaptersJson = chObj.optJSONArray("subchapters") ?: JSONArray()
                val parsedSubchapters = mutableListOf<CurriculumSubchapter>()

                for (j in 0 until subchaptersJson.length()) {
                    val subObj = subchaptersJson.getJSONObject(j)
                    val subId = subObj.getString("id")
                    val subTitle = subObj.getString("title")
                    val subDesc = subObj.optString("description", "")

                    val lessonsJson = subObj.optJSONArray("lessons") ?: JSONArray()
                    val subLessons = mutableListOf<CurriculumLesson>()

                    for (k in 0 until lessonsJson.length()) {
                        val lObj = lessonsJson.getJSONObject(k)
                        val lesson = parseLessonFromJson(lObj, chCat, context)
                        subLessons.add(lesson)
                        parsedLessons.add(lesson)
                        lessonCache[lesson.id] = lesson
                    }

                    parsedSubchapters.add(
                        CurriculumSubchapter(
                            id = subId,
                            chapterId = chId,
                            title = subTitle,
                            description = subDesc,
                            lessons = subLessons
                        )
                    )
                }

                parsedChapters.add(
                    CurriculumChapter(
                        id = chId,
                        number = chNum,
                        title = chTitle,
                        description = chDesc,
                        category = chCat,
                        subchapters = parsedSubchapters
                    )
                )
            }

            cachedChapters = parsedChapters
            cachedLessons = parsedLessons
            Log.d(TAG, "Successfully loaded ${parsedChapters.size} chapters and ${parsedLessons.size} lessons from curriculum manifest")
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading curriculum manifest from assets", e)
        }
    }

    private fun parseLessonFromJson(
        lObj: JSONObject,
        defaultCategory: TopicCategory,
        context: Context
    ): CurriculumLesson {
        val lid = lObj.getString("id")
        val lTitle = lObj.getString("title")
        val lSubtitle = lObj.optString("subtitle", "")
        val lDur = lObj.optInt("durationMinutes", 6)
        val lSummary = lObj.optString("summary", lSubtitle)
        val lPath = lObj.optString("assetPath", "")
        val isGate = lObj.optBoolean("isGateMilestone", false)
        val cat = parseTopicCategory(lObj.optString("category", defaultCategory.name))

        val ktList = mutableListOf<String>()
        val ktArray = lObj.optJSONArray("keyTerms")
        if (ktArray != null) {
            for (i in 0 until ktArray.length()) {
                ktList.add(ktArray.getString(i))
            }
        }

        val qList = mutableListOf<KnowledgeCheckQuestion>()
        val qArray = lObj.optJSONArray("questions")
        if (qArray != null) {
            for (i in 0 until qArray.length()) {
                val qObj = qArray.getJSONObject(i)
                val qId = qObj.getString("id")
                val qText = qObj.getString("questionText")
                val optsArray = qObj.optJSONArray("options")
                val optsList = mutableListOf<KnowledgeCheckOption>()
                if (optsArray != null) {
                    for (j in 0 until optsArray.length()) {
                        val optObj = optsArray.getJSONObject(j)
                        optsList.add(
                            KnowledgeCheckOption(
                                id = optObj.getString("id"),
                                text = optObj.getString("text"),
                                isCorrect = optObj.optBoolean("isCorrect", false),
                                feedback = optObj.optString("feedback", "")
                            )
                        )
                    }
                }
                qList.add(
                    KnowledgeCheckQuestion(
                        id = qId,
                        questionText = qText,
                        options = optsList
                    )
                )
            }
        }

        // Preload markdown text if assetPath is present
        val markdownText = if (lPath.isNotBlank()) {
            loadMarkdownText(context, lPath)
        } else {
            ""
        }

        return CurriculumLesson(
            id = lid,
            chapterId = lObj.optString("chapterId", ""),
            subchapterId = lObj.optString("subchapterId", ""),
            lessonIndex = lObj.optInt("lessonIndex", 1),
            title = lTitle,
            subtitle = lSubtitle,
            durationMinutes = lDur,
            category = cat,
            summary = lSummary,
            fullMarkdownText = markdownText,
            keyTerms = ktList,
            practicalApplication = lObj.optString("practicalApplication", "Apply these movement principles during your upcoming workout sessions."),
            questions = qList,
            isGateMilestone = isGate,
            assetPath = lPath
        )
    }

    private fun parseTopicCategory(name: String): TopicCategory {
        return TopicCategory.entries.find { it.name.equals(name, ignoreCase = true) }
            ?: TopicCategory.MOVEMENT_PATTERNS
    }

    /**
     * Indexes all lessons into Cloud Firestore at /curriculum_lessons/{lessonId}.
     * References Firestore for lesson location and metadata across clients.
     */
    suspend fun syncLessonsToFirestore(db: FirebaseFirestore): Result<Int> = withContext(Dispatchers.IO) {
        try {
            ensureInitialized()
            val collection = db.collection("curriculum_lessons")
            var count = 0
            for (lesson in cachedLessons) {
                val doc = CurriculumLessonDoc(
                    lessonId = lesson.id,
                    title = lesson.title,
                    chapterId = lesson.chapterId,
                    chapterNumber = cachedChapters.find { it.id == lesson.chapterId }?.number?.toLong() ?: 1L,
                    chapterTitle = cachedChapters.find { it.id == lesson.chapterId }?.title ?: "",
                    subchapterId = lesson.subchapterId,
                    subchapterTitle = cachedChapters.flatMap { it.subchapters }.find { it.id == lesson.subchapterId }?.title ?: "",
                    lessonIndex = lesson.lessonIndex.toLong(),
                    durationMinutes = lesson.durationMinutes.toLong(),
                    category = lesson.category.name,
                    summary = lesson.summary,
                    assetPath = lesson.assetPath,
                    keyTerms = lesson.keyTerms,
                    isGateMilestone = lesson.isGateMilestone
                )
                collection.document(lesson.id).set(doc.toMap(), SetOptions.merge()).await()
                count++
            }
            Log.d(TAG, "Indexed $count lessons into Cloud Firestore /curriculum_lessons")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Failed indexing lessons to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Refreshes lesson metadata referencing Firestore for lesson location.
     */
    suspend fun refreshIndexFromFirestore(db: FirebaseFirestore): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val snapshot = db.collection("curriculum_lessons").get().await()
            var count = 0
            for (doc in snapshot.documents) {
                val lid = doc.getString("lessonId") ?: doc.id
                val assetPath = doc.getString("assetPath") ?: ""
                val existing = lessonCache[lid]
                if (existing != null && assetPath.isNotBlank() && existing.assetPath != assetPath) {
                    val updated = existing.copy(assetPath = assetPath)
                    lessonCache[lid] = updated
                    count++
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Failed refreshing lesson index from Firestore", e)
            Result.failure(e)
        }
    }
}
