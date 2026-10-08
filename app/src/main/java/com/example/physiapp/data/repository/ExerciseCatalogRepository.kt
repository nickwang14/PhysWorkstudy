package com.example.physiapp.data.repository

import com.example.physiapp.data.model.ExerciseDbItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

/** App-owned boundary for optional exercise-catalog search. */
interface ExerciseCatalogRepository {
    val isRemoteConfigured: Boolean

    suspend fun search(
        query: String = "",
        bodyPart: String? = null,
        target: String? = null,
        equipment: String? = null,
        limit: Int = 30
    ): Result<List<ExerciseDbItem>>
}

/**
 * Calls the PhysiApp-owned gateway only. Provider credentials and provider-specific requests belong
 * on that server; this client intentionally has no ExerciseDB or RapidAPI credentials.
 *
 * [gatewayEndpoint] is the full HTTPS search endpoint supplied by build configuration. It is
 * optional until the provider gateway and its contract are deployed.
 */
class GatewayExerciseCatalogRepository(
    gatewayEndpoint: String,
    private val client: OkHttpClient = defaultClient(),
    private val waitBeforeRetry: suspend (Long) -> Unit = { delay(it) }
) : ExerciseCatalogRepository {
    private val endpoint: HttpUrl? = gatewayEndpoint.toHttpUrlOrNull()

    override val isRemoteConfigured: Boolean = endpoint?.let {
        val secureTransport = it.isHttps || it.host == "10.0.2.2" || it.host == "localhost"
        val isProviderHost = it.host == "exercisedb.p.rapidapi.com" ||
            it.host.endsWith(".rapidapi.com") || it.host.endsWith(".rapidapi.co") ||
            it.host == "rapidapi.com" || it.host == "rapidapi.co" ||
            it.host == "exercisedb.dev" || it.host.endsWith(".exercisedb.dev")
        secureTransport && !isProviderHost
    } == true

    override suspend fun search(
        query: String,
        bodyPart: String?,
        target: String?,
        equipment: String?,
        limit: Int
    ): Result<List<ExerciseDbItem>> = withContext(Dispatchers.IO) {
        if (!isRemoteConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Exercise catalog gateway is not configured.")
            )
        }

        val requestUrl = endpoint!!.newBuilder()
            .addQueryParameter("query", query.trim().takeIf(String::isNotEmpty))
            .addQueryParameter("bodyPart", bodyPart?.trim()?.takeIf(String::isNotEmpty))
            .addQueryParameter("target", target?.trim()?.takeIf(String::isNotEmpty))
            .addQueryParameter("equipment", equipment?.trim()?.takeIf(String::isNotEmpty))
            .addQueryParameter("limit", limit.coerceIn(1, MAX_RESULTS).toString())
            .build()
        val request = Request.Builder().url(requestUrl).get().build()

        try {
            val response = executeWithRetry(request)
            response.use {
                if (!it.isSuccessful) {
                    Result.failure(IOException("Exercise catalog gateway returned HTTP ${it.code}."))
                } else {
                    val body = it.body?.string()
                    if (body == null) {
                        Result.failure(IOException("Exercise catalog gateway returned an empty response."))
                    } else {
                        Result.success(parseItems(body))
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private suspend fun executeWithRetry(request: Request): Response {
        var lastIoException: IOException? = null

        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                val response = client.newCall(request).execute()
                val canRetry = attempt < MAX_ATTEMPTS - 1 &&
                    (response.code in 500..599 || response.code == 429 && response.header("Retry-After") != null)
                if (!canRetry) return response

                val retryAfterMillis = response.header("Retry-After")
                    ?.toLongOrNull()
                    ?.times(1_000L)
                    ?.coerceIn(0L, MAX_RETRY_DELAY_MILLIS)
                response.close()
                waitBeforeRetry(retryAfterMillis ?: (INITIAL_RETRY_DELAY_MILLIS shl attempt))
            } catch (error: IOException) {
                lastIoException = error
                if (attempt == MAX_ATTEMPTS - 1) throw error
                waitBeforeRetry(INITIAL_RETRY_DELAY_MILLIS shl attempt)
            }
        }

        throw lastIoException ?: IOException("Exercise catalog request failed after retries.")
    }

    private fun parseItems(json: String): List<ExerciseDbItem> {
        val array = JSONArray(json)
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            val secondaryMusclesJson = item.optJSONArray("secondaryMuscles")
            val instructionsJson = item.optJSONArray("instructions")
            ExerciseDbItem(
                id = item.optString("id"),
                name = item.optString("name"),
                bodyPart = item.optString("bodyPart"),
                equipment = item.optString("equipment"),
                gifUrl = ExerciseCatalogMediaPolicy.gatewayOwnedUrl(
                    item.optString("gifUrl"),
                    endpoint.toString()
                ),
                target = item.optString("target"),
                secondaryMuscles = secondaryMusclesJson?.let { values ->
                    List(values.length()) { valueIndex -> values.optString(valueIndex) }
                }.orEmpty(),
                instructions = instructionsJson?.let { values ->
                    List(values.length()) { valueIndex -> values.optString(valueIndex) }
                }.orEmpty()
            )
        }
    }

    companion object {
        private const val MAX_ATTEMPTS = 3
        private const val MAX_RESULTS = 50
        private const val INITIAL_RETRY_DELAY_MILLIS = 250L
        private const val MAX_RETRY_DELAY_MILLIS = 5_000L

        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .build()
    }
}

object ExerciseCatalogMediaPolicy {
    fun gatewayOwnedUrl(mediaUrl: String, gatewayEndpoint: String): String {
        val media = mediaUrl.toHttpUrlOrNull() ?: return ""
        val gateway = gatewayEndpoint.toHttpUrlOrNull() ?: return ""
        val isLocalDevelopmentHost = gateway.host == "localhost" || gateway.host == "10.0.2.2"
        val usesSecureTransport = gateway.isHttps || isLocalDevelopmentHost
        val isSameOrigin = media.scheme == gateway.scheme &&
            media.host == gateway.host &&
            media.port == gateway.port
        val isProviderHost = isKnownProviderHost(media.host) || isKnownProviderHost(gateway.host)

        return if (usesSecureTransport && isSameOrigin && isProviderHost.not() &&
            media.username.isEmpty() && media.password.isEmpty()
        ) {
            media.toString()
        } else {
            ""
        }
    }

    private fun isKnownProviderHost(host: String): Boolean =
        host == "rapidapi.com" || host == "rapidapi.co" ||
            host.endsWith(".rapidapi.com") || host.endsWith(".rapidapi.co") ||
            host == "exercisedb.dev" || host.endsWith(".exercisedb.dev")
}

/** Local starter content keeps optional catalog browsing usable without a network or gateway. */
object ExerciseCatalogFallbacks {
    val starterItems: List<ExerciseDbItem> = listOf(
        ExerciseDbItem(
            id = "0025",
            name = "barbell squat",
            bodyPart = "upper legs",
            equipment = "barbell",
            gifUrl = "",
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
            gifUrl = "",
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
            gifUrl = "",
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
            gifUrl = "",
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
            gifUrl = "",
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
            gifUrl = "",
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

    fun filter(query: String, bodyPart: String?): List<ExerciseDbItem> = starterItems.filter { item ->
        val matchesQuery = query.isBlank() || item.name.contains(query, ignoreCase = true) ||
            item.target.contains(query, ignoreCase = true)
        val matchesBodyPart = bodyPart == null || item.bodyPart.equals(bodyPart, ignoreCase = true)
        matchesQuery && matchesBodyPart
    }
}
