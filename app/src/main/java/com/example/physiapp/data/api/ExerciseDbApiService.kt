package com.example.physiapp.data.api

import android.util.Log
import com.example.physiapp.BuildConfig
import com.example.physiapp.data.model.ExerciseDbItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ExerciseDbApiService {

    private const val TAG = "ExerciseDbApi"
    private const val BASE_URL = "https://exercisedb.p.rapidapi.com"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.EXERCISE_DB_API_KEY
        return key.isNotBlank() && !key.contains("YOUR_")
    }

    /**
     * Fetch exercises from ExerciseDB API with optional query parameters.
     */
    suspend fun fetchExercises(
        query: String = "",
        target: String? = null,
        bodyPart: String? = null,
        equipment: String? = null,
        limit: Int = 30
    ): Result<List<ExerciseDbItem>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.EXERCISE_DB_API_KEY
        val apiHost = BuildConfig.EXERCISE_DB_API_HOST

        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("EXERCISE_DB_API_KEY is not configured in AI Studio Secrets panel.")
            )
        }

        try {
            val urlBuilder = StringBuilder(BASE_URL)
            when {
                query.isNotBlank() -> {
                    urlBuilder.append("/exercises/name/${query.trim().lowercase()}?limit=$limit")
                }
                !target.isNullOrBlank() -> {
                    urlBuilder.append("/exercises/target/${target.trim().lowercase()}?limit=$limit")
                }
                !bodyPart.isNullOrBlank() -> {
                    urlBuilder.append("/exercises/bodyPart/${bodyPart.trim().lowercase()}?limit=$limit")
                }
                !equipment.isNullOrBlank() -> {
                    urlBuilder.append("/exercises/equipment/${equipment.trim().lowercase()}?limit=$limit")
                }
                else -> {
                    urlBuilder.append("/exercises?limit=$limit&offset=0")
                }
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .addHeader("x-rapidapi-key", apiKey.trim())
                .addHeader("x-rapidapi-host", apiHost.trim())
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                val errorMsg = "ExerciseDB error ${response.code}: ${response.message}"
                Log.e(TAG, errorMsg)
                return@withContext Result.failure(Exception(errorMsg))
            }

            val items = parseExerciseJson(responseBody)
            Result.success(items)
        } catch (e: Exception) {
            Log.e(TAG, "Network call failed", e)
            Result.failure(e)
        }
    }

    private fun parseExerciseJson(jsonString: String): List<ExerciseDbItem> {
        val list = mutableListOf<ExerciseDbItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", "")
                val name = obj.optString("name", "")
                val bodyPart = obj.optString("bodyPart", "")
                val equipment = obj.optString("equipment", "")
                val gifUrl = obj.optString("gifUrl", "")
                val target = obj.optString("target", "")

                val secondaryMuscles = mutableListOf<String>()
                val secArray = obj.optJSONArray("secondaryMuscles")
                if (secArray != null) {
                    for (j in 0 until secArray.length()) {
                        secondaryMuscles.add(secArray.optString(j))
                    }
                }

                val instructions = mutableListOf<String>()
                val instArray = obj.optJSONArray("instructions")
                if (instArray != null) {
                    for (j in 0 until instArray.length()) {
                        instructions.add(instArray.optString(j))
                    }
                }

                list.add(
                    ExerciseDbItem(
                        id = id,
                        name = name,
                        bodyPart = bodyPart,
                        equipment = equipment,
                        gifUrl = gifUrl,
                        target = target,
                        secondaryMuscles = secondaryMuscles,
                        instructions = instructions
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error", e)
        }
        return list
    }

    /**
     * Curated starter items matching ExerciseDB schema for immediate preview / offline fallback.
     */
    val starterExerciseDbItems: List<ExerciseDbItem> = listOf(
        ExerciseDbItem(
            id = "0025",
            name = "barbell squat",
            bodyPart = "upper legs",
            equipment = "barbell",
            gifUrl = "https://static.exercisedb.dev/media/yn8yg1r.gif",
            target = "quads",
            secondaryMuscles = listOf("glutes", "hamstrings", "calves", "core"),
            instructions = listOf(
                "Position barbell across upper back (trapezius), hands gripping the bar securely.",
                "Set feet shoulder-width apart with toes angled slightly outward.",
                "Descend by bending knees and pushing hips backward as if sitting into a chair.",
                "Lower until thighs are parallel to ground, maintaining braced neutral spine.",
                "Drive through whole foot to stand back up, exhaling at the top."
            )
        ),
        ExerciseDbItem(
            id = "0032",
            name = "barbell deadlift",
            bodyPart = "upper legs",
            equipment = "barbell",
            gifUrl = "https://static.exercisedb.dev/media/wQ2c4XD.gif",
            target = "glutes",
            secondaryMuscles = listOf("hamstrings", "lower back", "lats", "forearms"),
            instructions = listOf(
                "Stand with feet hip-width apart, barbell over midfoot.",
                "Hinge at hips and grip bar just outside knees.",
                "Pull chest proud, take slack out of bar, and brace core.",
                "Drive floor away through feet, extending hips and knees simultaneously.",
                "Lock out with hips tall, avoiding hyperextension of lumbar spine."
            )
        ),
        ExerciseDbItem(
            id = "0047",
            name = "barbell bench press",
            bodyPart = "chest",
            equipment = "barbell",
            gifUrl = "https://assets.exercisedb.dev/media/qU7GQpl.gif",
            target = "pectorals",
            secondaryMuscles = listOf("triceps", "anterior deltoids"),
            instructions = listOf(
                "Lie flat on bench with eyes under barbell, feet planted firmly.",
                "Grip bar with medium width, pull shoulder blades retracted and depressed.",
                "Unrack bar and lower under control towards mid-chest (sternum).",
                "Touch chest lightly without bouncing.",
                "Drive bar up in slight J-curve path back to full extension over shoulders."
            )
        ),
        ExerciseDbItem(
            id = "0027",
            name = "barbell bent over row",
            bodyPart = "back",
            equipment = "barbell",
            gifUrl = "https://static.exercisedb.dev/media/BJ0Hz5L.gif",
            target = "upper back",
            secondaryMuscles = listOf("biceps", "lats", "posterior deltoids"),
            instructions = listOf(
                "Stand hip-width apart, hold bar with overhand grip.",
                "Hinge forward until torso is roughly 45 degrees, spine rigid.",
                "Pull elbows backward towards hip crease, squeezing shoulder blades together.",
                "Lower bar under control without rounding back."
            )
        ),
        ExerciseDbItem(
            id = "0335",
            name = "dumbbell farmer's carry",
            bodyPart = "cardio",
            equipment = "dumbbell",
            gifUrl = "https://static.exercisedb.dev/media/qPEzJjA.gif",
            target = "forearms",
            secondaryMuscles = listOf("traps", "abs", "glutes"),
            instructions = listOf(
                "Deadlift two heavy dumbbells off the ground at your sides.",
                "Keep shoulders down and back, chest tall, ribs tucked.",
                "Walk forward with steady, deliberate steps without swaying.",
                "Maintain posture and grip integrity throughout the specified distance or time."
            )
        ),
        ExerciseDbItem(
            id = "0652",
            name = "cable pallof press",
            bodyPart = "waist",
            equipment = "cable",
            gifUrl = "https://static.exercisedb.dev/media/9pa4H5m.gif",
            target = "abs",
            secondaryMuscles = listOf("obliques", "glutes", "shoulders"),
            instructions = listOf(
                "Set cable at chest height. Stand perpendicular to cable column.",
                "Grip handle with both hands at center of chest.",
                "Brace core and press handle straight out without letting torso rotate.",
                "Hold extended position for 2 seconds resisting the rotational pull.",
                "Return handle back to chest with control."
            )
        )
    )
}
