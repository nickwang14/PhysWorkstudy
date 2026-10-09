import java.io.FileOutputStream
import java.util.Properties
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

val includeExerciseDbDevelopmentKey = providers.gradleProperty("includeExerciseDbDevelopmentKey").orNull == "true"

// Reuse the maintained graphics and credits without duplicating source assets.
abstract class SyncLessonGraphics : Sync() {
    @get:OutputDirectory
    abstract val assetOutputDirectory: DirectoryProperty
}

val syncLessonGraphics = tasks.register<SyncLessonGraphics>("syncLessonGraphics") {
    from(rootProject.file("theory-and-knowledge/knowledge/curriculum/foundations-of-movement/assets")) {
        include("*.svg", "*.jpg", "README.md")
        into("curriculum/assets")
    }
    assetOutputDirectory.set(layout.buildDirectory.dir("generated/lessonGraphics"))
    into(assetOutputDirectory)
}

android {
    namespace = "com.example.physiapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aistudio.physiapp.kzmpqw"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val envFile = rootProject.file(".env")
        val envProperties = Properties()
        if (envFile.exists()) {
            envFile.inputStream().use { envProperties.load(it) }
        }
        val exerciseDbApiKey = if (includeExerciseDbDevelopmentKey) {
            envProperties.getProperty("EXERCISE_DB_API_KEY") ?: System.getenv("EXERCISE_DB_API_KEY") ?: ""
        } else ""
        val exerciseDbHost = envProperties.getProperty("EXERCISE_DB_API_HOST")
            ?: System.getenv("EXERCISE_DB_API_HOST")
            ?: "exercisedb.p.rapidapi.com"

        buildConfigField("String", "EXERCISE_DB_API_KEY", "\"$exerciseDbApiKey\"")
        buildConfigField("String", "EXERCISE_DB_API_HOST", "\"$exerciseDbHost\"")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            // Private provider credentials must never be included in release builds.
            buildConfigField("String", "EXERCISE_DB_API_KEY", "\"\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(syncLessonGraphics, SyncLessonGraphics::assetOutputDirectory)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.okhttp)
    implementation(libs.markdown.m3)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.withType<Test> {
    failFast = true
}

val packageInstallerZip = tasks.register("packageInstallerZip") {
    group = "distribution"
    description = "Packages the debug APK into PhysiApp_installer.zip at the repository root."
    dependsOn("packageDebug")

    val apkFile = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")
    inputs.file(apkFile)
    inputs.property("includeExerciseDbDevelopmentKey", includeExerciseDbDevelopmentKey)
    outputs.file(rootProject.file("PhysiApp_installer.zip"))
    outputs.file(rootProject.file("PhysiApp.apk"))

    doFirst {
        check(!includeExerciseDbDevelopmentKey) {
            "Shared installers must omit private keys. Build without includeExerciseDbDevelopmentKey."
        }
    }

    doLast {
        val srcApk = apkFile.get().asFile
        if (srcApk.exists()) {
            val rootApk = rootProject.file("PhysiApp.apk")
            srcApk.copyTo(rootApk, overwrite = true)

            val zipFile = rootProject.file("PhysiApp_installer.zip")
            if (zipFile.exists()) {
                zipFile.delete()
            }
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                val entry = ZipEntry("PhysiApp.apk")
                zipOut.putNextEntry(entry)
                srcApk.inputStream().use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }
    }
}

tasks.matching { it.name in listOf("assembleDebug", "packageDebug") }.configureEach {
    finalizedBy(packageInstallerZip)
}
