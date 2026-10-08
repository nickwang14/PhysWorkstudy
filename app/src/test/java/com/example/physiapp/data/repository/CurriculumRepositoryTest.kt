package com.example.physiapp.data.repository

import com.example.physiapp.data.model.TopicCategory
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class CurriculumRepositoryTest {

    @Test
    fun testCurriculumManifest_containsAll83LessonsAnd5Chapters() {
        // Read curriculum_index.json from assets directory directly in JVM unit test
        val assetFile = File("src/main/assets/curriculum/curriculum_index.json")
        assertTrue("Manifest file must exist in assets", assetFile.exists())

        val content = assetFile.readText(Charsets.UTF_8)
        val json = JSONObject(content)

        val totalLessons = json.getInt("totalLessons")
        assertEquals(83, totalLessons)

        val chapters = json.getJSONArray("chapters")
        assertEquals(5, chapters.length())

        val lessons = json.getJSONArray("lessons")
        assertEquals(83, lessons.length())

        // Check first lesson structure
        val firstLesson = lessons.getJSONObject(0)
        assertEquals("terminology-01", firstLesson.getString("id"))
        assertEquals("What Is Movement?", firstLesson.getString("title"))
        assertEquals("MOVEMENT_PATTERNS", firstLesson.getString("category"))
        assertTrue(firstLesson.getString("assetPath").startsWith("curriculum/"))
        assertTrue(firstLesson.getJSONArray("questions").length() >= 1)
        assertTrue(firstLesson.getJSONArray("keyTerms").length() >= 1)

        // Verify that the markdown asset file referenced actually exists on disk
        val mdFile = File("src/main/assets", firstLesson.getString("assetPath"))
        assertTrue("Referenced markdown file must exist: ${mdFile.path}", mdFile.exists())
        val mdText = mdFile.readText(Charsets.UTF_8)
        assertTrue(mdText.contains("# What Is Movement?"))
    }

    @Test
    fun testTopicCategory_allCategoriesAreValid() {
        val categories = TopicCategory.entries
        assertEquals(5, categories.size)
        assertTrue(categories.any { it.name == "MOVEMENT_PATTERNS" })
        assertTrue(categories.any { it.name == "ANATOMY_PHYSIOLOGY" })
        assertTrue(categories.any { it.name == "BIOMECHANICS" })
        assertTrue(categories.any { it.name == "LOAD_AND_RECOVERY" })
        assertTrue(categories.any { it.name == "PROGRAMMING_COACHING" })
    }
}
