package com.example.physiapp.data.repository

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExerciseCatalogRepositoryTest {
    @Test
    fun providerHostsAndInsecureRemoteEndpointsAreRejected() {
        assertFalse(GatewayExerciseCatalogRepository("https://exercisedb.p.rapidapi.com/exercises").isRemoteConfigured)
        assertFalse(GatewayExerciseCatalogRepository("https://api.rapidapi.com/exercises").isRemoteConfigured)
        assertFalse(GatewayExerciseCatalogRepository("https://exercisedb.p.rapidapi.co/exercises").isRemoteConfigured)
        assertFalse(GatewayExerciseCatalogRepository("http://catalog.example.test/v1/exercises").isRemoteConfigured)
        assertTrue(GatewayExerciseCatalogRepository("https://api.physiapp.test/v1/exercises").isRemoteConfigured)
    }

    @Test
    fun mediaPolicyAllowsOnlyTheConfiguredGatewayOrigin() {
        assertEquals(
            "https://api.physiapp.test/media/bench.gif",
            ExerciseCatalogMediaPolicy.gatewayOwnedUrl(
                "https://api.physiapp.test/media/bench.gif",
                GATEWAY_ENDPOINT
            )
        )
        assertTrue(
            ExerciseCatalogMediaPolicy.gatewayOwnedUrl(
                "https://media.example.test/bench.gif",
                GATEWAY_ENDPOINT
            ).isEmpty()
        )
        assertTrue(
            ExerciseCatalogMediaPolicy.gatewayOwnedUrl(
                "https://api.rapidapi.com/bench.gif",
                "https://api.rapidapi.com/v1/exercises"
            ).isEmpty()
        )
    }

    @Test
    fun searchEncodesFiltersAndParsesNormalizedItems() = runTest {
        var capturedRequest: okhttp3.Request? = null
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                capturedRequest = chain.request()
                response(chain.request(), 200, EXERCISE_RESPONSE)
            })
            .build()
        val repository = GatewayExerciseCatalogRepository(
            gatewayEndpoint = GATEWAY_ENDPOINT,
            client = client
        )

        val items = repository.search(
            query = "bench press & squat",
            bodyPart = "upper legs",
            limit = 100
        ).getOrThrow()

        assertEquals("bench press & squat", capturedRequest!!.url.queryParameter("query"))
        assertEquals("upper legs", capturedRequest!!.url.queryParameter("bodyPart"))
        assertEquals("50", capturedRequest!!.url.queryParameter("limit"))
        assertNull(capturedRequest!!.header("x-rapidapi-key"))
        assertEquals("bench press", items.single().name)
        assertEquals(listOf("triceps"), items.single().secondaryMuscles)
        assertTrue("Third-party media URLs must not reach Coil directly.", items.single().gifUrl.isEmpty())
    }

    @Test
    fun transientServerFailureIsRetriedWithBoundedBackoff() = runTest {
        var requestCount = 0
        val retryDelays = mutableListOf<Long>()
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                requestCount++
                val status = if (requestCount == 1) 503 else 200
                response(chain.request(), status, EXERCISE_RESPONSE)
            })
            .build()
        val repository = GatewayExerciseCatalogRepository(
            gatewayEndpoint = GATEWAY_ENDPOINT,
            client = client,
            waitBeforeRetry = { retryDelays.add(it) }
        )

        assertTrue(repository.search().isSuccess)
        assertEquals(2, requestCount)
        assertEquals(listOf(250L), retryDelays)
    }

    @Test
    fun rateLimitRetryHonorsAndCapsRetryAfter() = runTest {
        var requestCount = 0
        val retryDelays = mutableListOf<Long>()
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                requestCount++
                if (requestCount == 1) {
                    response(chain.request(), 429, "{}").newBuilder()
                        .header("Retry-After", "7")
                        .build()
                } else {
                    response(chain.request(), 200, EXERCISE_RESPONSE)
                }
            })
            .build()
        val repository = GatewayExerciseCatalogRepository(
            gatewayEndpoint = GATEWAY_ENDPOINT,
            client = client,
            waitBeforeRetry = { retryDelays.add(it) }
        )

        assertTrue(repository.search().isSuccess)
        assertEquals(2, requestCount)
        assertEquals(listOf(5_000L), retryDelays)
    }

    @Test
    fun localFallbackCanBeFilteredWithoutGateway() {
        val items = ExerciseCatalogFallbacks.filter(query = "press", bodyPart = "chest")

        assertEquals(listOf("barbell bench press"), items.map { it.name })
        assertTrue(ExerciseCatalogFallbacks.filter(query = "not a starter", bodyPart = null).isEmpty())
    }

    private fun response(request: okhttp3.Request, status: Int, body: String): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(status)
            .message(if (status == 200) "OK" else "Service Unavailable")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()

    private companion object {
        const val GATEWAY_ENDPOINT = "https://api.physiapp.test/v1/exercises"
        const val EXERCISE_RESPONSE = """[
            {
                "id": "bench-press",
                "name": "bench press",
                "bodyPart": "chest",
                "equipment": "barbell",
                "gifUrl": "https://media.example.test/bench.gif",
                "target": "pectorals",
                "secondaryMuscles": ["triceps"],
                "instructions": ["Press with control."]
            }
        ]"""
    }
}
