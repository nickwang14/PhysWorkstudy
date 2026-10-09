package com.example.physiapp.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
abstract class FirestoreEmulatorTestBase {

    protected lateinit var firestore: FirebaseFirestore
    protected lateinit var auth: FirebaseAuth
    protected val databaseId: String = "ai-studio-android-physiapp-429fc4fa-f1c5-40b6-ae8f-cc5e0f3a237a"

    @Before
    open fun setUpFirebase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowPackageManager = org.robolectric.Shadows.shadowOf(context.packageManager)
        val serviceInfo = android.content.pm.ServiceInfo().apply {
            name = "com.google.firebase.components.ComponentDiscoveryService"
            packageName = context.packageName
            metaData = android.os.Bundle().apply {
                putString(
                    "com.google.firebase.components:com.google.firebase.firestore.FirestoreRegistrar",
                    "com.google.firebase.components.ComponentRegistrar"
                )
                putString(
                    "com.google.firebase.components:com.google.firebase.auth.FirebaseAuthRegistrar",
                    "com.google.firebase.components.ComponentRegistrar"
                )
            }
        }
        shadowPackageManager.addOrUpdateService(serviceInfo)

        val app = FirebaseApp.getApps(context).firstOrNull { it.name == TEST_APP_NAME }
            ?: FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("com.aistudio.physiapp.kzmpqw")
                    .setProjectId(PROJECT_ID)
                    .setApiKey("fake-api-key-for-emulator")
                    .build(),
                TEST_APP_NAME
            )

        firestore = FirebaseFirestore.getInstance(app, databaseId)
        try {
            firestore.useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()
        } catch (_: IllegalStateException) {}

        auth = FirebaseAuth.getInstance(app)
        try {
            auth.useEmulator(EMULATOR_HOST, AUTH_PORT)
        } catch (_: IllegalStateException) {}
    }

    @After
    open fun tearDownFirebase() {
        if (::auth.isInitialized) {
            auth.signOut()
        }
    }

    protected suspend fun signInTestUser(email: String): String = withContext(Dispatchers.IO) {
        withTimeout(AUTH_TIMEOUT_MS) {
            val result = try {
                auth.createUserWithEmailAndPassword(email, DEFAULT_PASSWORD).await()
            } catch (unused: FirebaseAuthUserCollisionException) {
                auth.signInWithEmailAndPassword(email, DEFAULT_PASSWORD).await()
            }
            checkNotNull(result.user?.uid) { "User auth failed" }
        }
    }

    companion object {
        const val EMULATOR_HOST = "127.0.0.1"
        private const val TEST_APP_NAME = "physiapp-rule-tests"
        val FIRESTORE_PORT = emulatorPort("FIRESTORE_EMULATOR_HOST", 8085)
        val AUTH_PORT = emulatorPort("FIREBASE_AUTH_EMULATOR_HOST", 9099)
        val PROJECT_ID = (System.getenv("GCP_PROJECT") ?: "demo-no-project").also {
            require(it.startsWith("demo-")) { "Repository rules tests require an isolated demo project" }
        }
        const val DEFAULT_PASSWORD = "password123"
        const val AUTH_TIMEOUT_MS = 5000L

        private fun emulatorPort(variable: String, fallback: Int): Int {
            val address = System.getenv(variable) ?: return fallback
            require(address.substringBeforeLast(':') in setOf("127.0.0.1", "localhost")) {
                "Repository rules tests require loopback emulators"
            }
            return address.substringAfterLast(':').toInt().also { require(it in 1..65535) }
        }
    }
}
