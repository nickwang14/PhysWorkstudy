# Local Android Build and Installer

Use the installed JDK 21, Android SDK 36, and Gradle 9.3.1. This repository does not contain a Gradle wrapper. The owner can run available local validation/builds in VS Code; this is not evidence of execution in Google AI Studio or a production release.

## Signing and Firebase registration

The owner authorized a new **development-only** root `debug.keystore` because the original key was unavailable. It remains ignored, with the existing Gradle debug alias/settings. Back up the private keystore securely outside Git; do not share its contents in chat or app-source uploads. Never use this standard-password debug key for production signing.

The new public certificate fingerprints are:

- **SHA-1:** `C0:A9:75:88:94:4B:74:0A:F3:DA:6F:BC:19:E6:30:0D:E6:F2:87:E2`
- **SHA-256:** `81:D0:E6:8F:98:C1:2A:54:65:43:86:E7:E0:E9:2F:D8:5E:F1:40:59:CE:9E:E6:16:18:1F:1B:E9:A8:84:0D:45`

Verify the actual key with Gradle `:app:signingReport` when signing/build environments change. These fingerprints identify a certificate; they cannot recreate the private key.

**Required owner action:** in Firebase project `gen-lang-client-0444088676`, select Android app `com.aistudio.physiapp.kzmpqw` under Project settings → General, register these fingerprints, and download refreshed configuration if Google sign-in/OAuth configuration changes. Follow [the config import/template workflow](firebase-and-keys.md#local-environment--client-config-template-workflow); do not invent or silently substitute certificate/client configuration. Registration and Google sign-in with this certificate have not been verified.

The new certificate differs from the old APK certificate. Android cannot normally update an old-key installation with this APK. If uninstall/reinstall is needed, obtain user approval and preserve/export needed local data first; do not automatically delete app data to make installation succeed.

## Build outputs and private-key exclusion

- Validate Firebase client configuration with **`npm.cmd run firebase:config:check`**.
- Build using the available Gradle executable: **`:app:lintDebug :app:assembleDebug -PincludeExerciseDbDevelopmentKey=false`**.
- `assembleDebug` finalizes `packageInstallerZip`, producing ignored **`PhysiApp.apk`** and tracked root **`PhysiApp_installer.zip`**, whose only entry is `PhysiApp.apk`. The ordinary Gradle APK is `app/build/outputs/apk/debug/app-debug.apk`.
- Shared builds omit the private ExerciseDB key; release builds always omit it. The existing starter/offline exercise fallback remains available. The installer task rejects development-key opt-in, including incremental-build input changes. Do not force-add private local configuration or other build caches.
- A strictly local debug build can opt in with `-PincludeExerciseDbDevelopmentKey=true` only when also excluding `-x :app:packageInstallerZip`. Its embedded provider credential is extractable; never distribute/commit that APK. Production provider calls require an approved backend.
- Verify the APK with the SDK's `apksigner verify --print-certs`, and confirm the ZIP entry's SHA-256 equals the APK SHA-256 before committing the installer. APK signature checks establish artifact integrity/signing identity, not feature acceptance or security approval.

## Validation and isolation

- **`npm.cmd test`** checks local backend/config tooling; **`npm.cmd run test:rules`** checks default and named-database client permissions on demo emulators. Use alternate test ports when the owner's local Emulator UI is already running; never stop that instance or redirect tests to live data implicitly.
- **`:app:testDebugUnitTest`** includes Firestore/Auth integration tests. Run it under a Firebase `emulators:exec --only firestore,auth` invocation with an explicit `demo-` project, setting `GCP_PROJECT` to that same project. The fixture reads emulator-port variables provided by the CLI, enforces loopback/demo targets, and uses a dedicated Firebase app rather than the production default app.
- Robolectric may download Android runtime artifacts separately from Gradle dependencies. Where its default Maven endpoint has certificate trouble, the verified HTTPS alternate is `https://repo.maven.apache.org/maven2`, selected with JVM property `robolectric.dependency.repo.url`. Set that property for the test JVM (e.g. a temporary `JAVA_TOOL_OPTIONS`) and restore the prior setting afterward. Do not disable TLS validation.
- **`:app:compileDebugAndroidTestKotlin`** compiles instrumentation-test source. **`:app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.physiapp.ui.components.LessonFeedbackContentTest`** runs the seven feedback UI tests on an available device/emulator. These use synthetic state, not live user feedback.
- Lint is required; warnings remain visible and do not imply production readiness. Domain/editorial/rights approval is separate from tooling/UI tests.

## Cloud rollout is separate

The owner's app-admin Auth claim was independently provisioned and read-back verified. Building/installing this APK neither grants claims nor refreshes an existing user's token: sign out/in or explicitly refresh it.

The local admin-review/comment rules still require explicit reviewed deployment to the named Firestore database, plus authorized lesson-index publication/migration as needed. No rules deployment, fingerprint registration, or live feedback-flow validation is performed by the build. See [feedback security and rollout](lesson-feedback-security.md).