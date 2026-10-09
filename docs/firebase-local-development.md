# Firebase local development and management

This is local maintenance tooling, not a deployed backend service. See the [schema at a glance](firestore-schema.md) and [machine-readable blueprint](../firebase-blueprint.json).

## Three different configurations

| Purpose | Configuration | Grants admin access? |
|---|---|---|
| Initialize the Android Firebase app | Authentic `app/google-services.json`, downloaded for package `com.aistudio.physiapp.kzmpqw` | No |
| Select the Firestore database | `firestore_database_id` in `firebase_applet_config.xml` and the matching entry in `firebase.json` | No |
| Administer data from this computer | Your Google Application Default Credentials (ADC), with suitable IAM permissions | Yes; bypasses Firebase client rules |

Project: **`gen-lang-client-0444088676`** (owner-provided). Named database: **`ai-studio-android-physiapp-429fc4fa-f1c5-40b6-ae8f-cc5e0f3a237a`**. Do not use the database ID as the project ID, and do not default to `(default)`.

## 1. Android app configuration

In Firebase Console → Project settings → General → Your apps, select/register the **Android** app with package `com.aistudio.physiapp.kzmpqw` (not the Kotlin namespace). Download `google-services.json` and place it at `app/google-services.json`. Do not manufacture JSON from the XML or put a service-account key here. This file is ignored by Git; no Firebase private key should ever ship in the app.

Import its actual values into ignored root `.env` with **`npm.cmd run firebase:config:import`**. Generate the live file from [`app/google-services.template.json`](../app/google-services.template.json) with **`npm.cmd run firebase:config`**, then verify with **`npm.cmd run firebase:config:check`**. Track only the placeholder template and `.env.example`; generated JSON, downloaded copies and real `.env` values stay local. See the [complete template workflow](firebase-and-keys.md#local-environment--client-config-template-workflow) for import sources, overwrite safeguards and environment selection. These commands require no Cloud CLI and grant no backend admin access.

For Google sign-in, enable the Google Auth provider and register the SHA fingerprints for the actual signing key. Download refreshed configuration if needed. The Gradle Google Services plugin is already present. Configuration presence does not prove a successful Android build or device sign-in.

## 2. Safe local backend browser

From the repository root, install the dependencies with **`npm.cmd ci`** (Node 22+, JDK 21+). If Java is not on PATH, set `$env:JAVA_HOME = [Environment]::GetEnvironmentVariable('JAVA_HOME','User')`, then `$env:Path = "$env:JAVA_HOME\bin;$env:Path"` in your PowerShell session. Confirm with **`java -version`**.

Start with **`npm.cmd run emulators`**. Open the local Emulator UI at **`http://127.0.0.1:4000`**. The default Firestore view is not the app's database: open **`http://127.0.0.1:4000/firestore/ai-studio-android-physiapp-429fc4fa-f1c5-40b6-ae8f-cc5e0f3a237a/data`** for the **named database**. The UI supports multiple databases by editing the database name in the URL; the CLI also targets it explicitly.

This starts Firestore on `127.0.0.1:8085` and Auth on `127.0.0.1:9099`, under **`demo-physiapp`**. No production login or Android JSON is needed. It is an isolated, initially empty database—not a copy or proxy of production. Only synthetic data belongs here. For persistence, stop/restart using `npx.cmd firebase emulators:start --only firestore,auth --project demo-physiapp --import .local/emulator-data --export-on-exit .local/emulator-data` after an initial export exists. `.local/` is ignored.

The Android application is **not automatically redirected** by starting these emulators. App emulator routing requires a separate debug-only implementation/handoff (Android Emulator reaches host at `10.0.2.2`); the existing app may still use live Firebase. Do not sign in expecting sandbox writes without that routing.

## 3. Query, create, update and delete locally

Run from a second terminal while emulators are running. Use **`npm.cmd run backend -- --help`** for syntax. The CLI only supports top-level `users` and `curriculum_lessons`; it deliberately has no bulk/recursive deletion or account-erasure command.

| Task | PowerShell command |
|---|---|
| List user document IDs, maximum 20 | `npm.cmd run backend -- query users` |
| Select profile fields | `npm.cmd run backend -- query users --fields displayName,fitnessLevel --limit 10` |
| List lesson metadata | `npm.cmd run backend -- query curriculum_lessons --fields title,chapterNumber,lessonIndex` |
| Read one complete profile | `npm.cmd run backend -- get users/local-review-user` |
| Preview synthetic profile creation | `npm.cmd run backend -- create users/local-review-user --data-file tools/firebase-examples/user.json` |
| Preview a profile update | `npm.cmd run backend -- update users/local-review-user --data-file tools/firebase-examples/user-update.json` |
| Preview optional metric removal | `npm.cmd run backend -- update users/local-review-user --data-file tools/firebase-examples/user-update.json --remove weightKg` |
| Preview a single document delete | `npm.cmd run backend -- delete users/local-review-user` |

Apply an operation by adding **`--apply --confirm "PROJECT/DATABASE/DOCUMENT_PATH"`**. The exact confirmation string is printed if missing. Example for the synthetic profile: `--apply --confirm "demo-physiapp/ai-studio-android-physiapp-429fc4fa-f1c5-40b6-ae8f-cc5e0f3a237a/users/local-review-user"`. This is required for emulator writes too.

Equality filtering uses `--where-eq` with a JSON `[field,value]` argument; e.g. in PowerShell, `--where-eq '[\"chapterNumber\",1]'`. Windows native argument quoting can vary; use **`node --% tools/firestore_admin.mjs query curriculum_lessons --where-eq "[\"chapterNumber\",1]" --fields title,chapterNumber`** if the shell strips JSON quotes. Queries are ordered by document ID and capped at 100; they are not a complete inventory or chronological curriculum ordering. Some live queries require a Firestore index; follow the SDK's index guidance rather than deploying guessed indexes.

JSON data files use ISO date-time strings for timestamp fields, or **`"now"`** for a server timestamp. The CLI converts them to native Firestore values and validates the resulting document against current blueprint constraints and ID equality. Use `--remove` for optional fields; null is not a substitute. Existing extra fields are preserved, but unknown changed fields are rejected. Do not rename lesson IDs casually. There is no curriculum-content publishing/approval workflow in this tool.

## 4. Connect this computer to live data

Console login alone does not authenticate a local Admin SDK. See [Google Cloud local setup](google-cloud-local-setup.md) for the user-local CLI, project configuration, and browser authentication. **`gcloud auth login --update-adc --configuration=physiapp`** configures both CLI credentials and Application Default Credentials (ADC); alternatively use separate `gcloud auth login` and **`gcloud auth application-default login`** flows. Complete the browser flow yourself, then run **`gcloud auth application-default set-quota-project gen-lang-client-0444088676`**. Firebase CLI login (`npx.cmd firebase login`) is separate and is only needed for Firebase CLI cloud operations, such as downloading Android config; it does not replace ADC.

Never paste passwords, access tokens, ADC JSON or service-account keys into chat. ADC is stored in your user profile outside the repository. If `GOOGLE_APPLICATION_CREDENTIALS` is already set, it overrides user ADC; inspect its presence without printing contents, and unset it in the dedicated terminal if using ADC. Prefer user credentials or short-lived service-account impersonation to downloaded private keys. Ask the project IAM administrator for least privilege: start with `roles/datastore.viewer`; `roles/datastore.user` permits writes and deletes. Quota-project setup may also require `serviceusage.services.use`. IAM errors are not a reason to loosen client rules.

First live read: **`npm.cmd run backend -- query curriculum_lessons --mode live --project gen-lang-client-0444088676 --database ai-studio-android-physiapp-429fc4fa-f1c5-40b6-ae8f-cc5e0f3a237a --fields title,chapterNumber --limit 10`**.

Live mode requires both project and database explicitly and refuses an active `FIRESTORE_EMULATOR_HOST`. Live mutations remain previews until you add `--apply` plus the full **live** confirmation target. Before an applied live write, a before-image is saved in ignored `.local/firebase-backups/`; updates/deletes use the read version as a precondition and fail rather than overwrite a concurrent edit. Before-images preserve timestamp seconds/nanoseconds but are not a complete database export or automatic restoration system. Review SDK types before restoring. Protect these personal-data files, restrict Windows folder access, establish a retention period and use a staging project/approved backup procedure before important changes. Admin SDK bypasses client rules: schema validation is not a substitute for security/privacy review.

## 5. Validation and operational limits

- **`npm.cmd test`**: offline guardrail/schema tests.
- **`npm.cmd run test:backend:emulator`**: actual CLI operations against the named emulator database.
- **`npm.cmd run test:rules`**: client-rule tests explicitly selecting the named database. These fail if the configured named database has open rules.
- **`npm.cmd run test:rules:legacy`**: existing default-database tests (not named-database coverage).
- Never run a deployment command as part of local setup. `.firebaserc` selects the real project for cloud operations; emulator scripts explicitly override it with `demo-physiapp`.
- Deleting a profile does **not** delete its subcollections or Firebase Auth account. This is not a GDPR/account-deletion implementation.
- App startup no longer republishes bundled lessons. Client curriculum publication and review writes now require a trusted `admin: true` Auth claim; private comments require owner/admin access. See [security and rollout](lesson-feedback-security.md). The local admin CLI only supports top-level users/lesson index operations, not review/comments or Auth claims, and bypasses client rules through IAM.
- Firestore profile email does not change Auth email. App-local progress/preferences may not reconcile remote edits or deletions. See [schema gaps](firestore-schema.md#known-gaps--recorded-not-fixed-here).
- Tool tests do not validate Android/AI Studio/device behavior or deployed permissions. Live connection remains unverified until authenticated reads succeed.