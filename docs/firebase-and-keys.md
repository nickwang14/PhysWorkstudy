# Firebase, API Keys, and Android Build Configuration

This guide follows the owner's [VS Code / Google AI Studio workflow](development-workflow.md). Console setup is performed by the owner; these instructions do not establish a deployed backend, automatic cloud integration, or a validated Android build.

## 1. Know which key you are being asked for

| Value | Where it comes from | Where it belongs | Secret? |
| --- | --- | --- | --- |
| Firebase Android configuration, including the Firebase client API key | Firebase console, Android app settings, `google-services.json` | Ignored `.env` → generated ignored `app/google-services.json`, supplied to the Android build | Not an admin secret; shipped client configuration |
| Google OAuth client ID | Generated through Firebase Google sign-in setup; also listed in Cloud credentials | Generated `default_web_client_id` Android resource | Public identifier, not a client secret |
| Gemini API key | Google AI Studio API Keys, for a selected Cloud project | Trusted server/tool environment; production Secret Manager | Yes; never ship it in the APK |
| ExerciseDB/RapidAPI key | RapidAPI account and subscribed API application | Development-only root `.env`; production server secret | Yes; current client integration exposes it in the APK |
| Service-account private key JSON | Cloud IAM / Firebase service accounts, only if explicitly needed | Trusted server only; prefer keyless identity instead | Highly sensitive; never an Android configuration file |
| Android upload/release signing key and passwords | Android signing tools / Play App Signing setup | Encrypted backup and protected build credentials | Yes; separate from all API keys |

**`google-services.json` is not a service-account JSON file.** Do not choose "Generate new private key" under Firebase service accounts when configuring Android.

Firebase client keys identify the project, not the user's permissions. Protect data with Authentication and Security Rules, and supported endpoints with App Check. Keep the Firebase key restricted to required Firebase APIs; never add Gemini/Generative Language API access to a publicly distributed Firebase key. This repository keeps client config out of Git as an environment-selection convention, not as a substitute for security.

You do not normally need to provide a Firebase key separately: the Google services Gradle plugin reads the JSON. If another chat asks for `fire-base-api-key`, ask what consumes it. That is not a variable read by this app. For a legitimate Firebase REST tool, the Android key is under the matching client's `api_key[].current_key` in the JSON; view it locally, not in chat.

## 2. Register or recover the Firebase Android configuration

1. Open the [Firebase console](https://console.firebase.google.com/) with the Google account that owns the app's existing project. Prefer the existing AI Studio-linked project if that is where your data lives; do not create an unrelated project just to obtain a key. Firebase projects are backed by Google Cloud projects.
2. Select the project, then **gear icon → Project settings → General → Your apps**. Check the project ID, not just its display name. If you cannot see the project, request access from its owner.
3. Select the Android app, or **Add app → Android** if it is not registered. Use the exact `applicationId` from `app/build.gradle.kts`: **`com.aistudio.physiapp.kzmpqw`**. The Kotlin namespace `com.example.physiapp` is NOT the app registration ID. The registered package name cannot be edited afterward.
4. Add the signing certificate's **SHA-1 and SHA-256** fingerprints. These are safe identifiers, not private keys. Google sign-in requires SHA-1; App Check Play Integrity uses SHA-256. See the signing section below.
5. Open **Authentication → Sign-in method/providers → Google**, enable it, choose the support email, and save. This app already uses Google sign-in through Credential Manager.
6. Return to **Project settings → General → Your apps → Android** and download the updated **`google-services.json`** after enabling Google sign-in. It should include the web OAuth client entry required to generate `default_web_client_id`.
7. Place the file at **`app/google-services.json`**, beside `app/build.gradle.kts`. Keep that exact filename, not `google-services (1).json`. Do not place it at the repository root or paste its contents into a chat.
8. Import the downloaded file into ignored `.env` with **`npm.cmd run firebase:config:import`**, then use **`npm.cmd run firebase:config`** and **`npm.cmd run firebase:config:check`**. See the template workflow below. Supply the generated client config in the corresponding app module of the actual Android build workspace, then sync/build there. The Google services plugin reads that generated JSON; it does not read `FIREBASE_*` variables directly.

### Local environment → client config template workflow

Track only [`.env.example`](../.env.example), [`app/google-services.template.json`](../app/google-services.template.json), and [`tools/google_services_config.mjs`](../tools/google_services_config.mjs). The template contains placeholders for **every scalar value** in the current downloaded config: project number/ID/bucket, mobile SDK app ID, package names, Android/web/App Invite OAuth IDs/types, signing certificate hash, API key, and configuration version. OAuth client types become JSON numbers; the configuration version remains a string.

| Action | Repository-root PowerShell command |
|---|---|
| Import existing authentic `app/google-services.json` into `.env` | `npm.cmd run firebase:config:import` |
| Import a newly downloaded copy | `npm.cmd run firebase:config:import -- --source "app/google-services copy.json"` |
| Generate ignored `app/google-services.json` from `.env` | `npm.cmd run firebase:config` |
| Check generated config against `.env`/template | `npm.cmd run firebase:config:check` |
| Tool help | `node tools/google_services_config.mjs generate --help` |

The importer creates `.env` if absent, adds missing Firebase variables, and replaces placeholder values. It preserves unrelated entries/comments, including quoted multiline values, and never prints configuration values. If an existing Firebase value differs, import fails without writing; review the source project's registration, then explicitly use **`--overwrite-env`** when replacing those values is intended. Do not overwrite an existing `.env` from `.env.example`.

Generation requires every placeholder's actual value in the selected local `.env` file; **process environment variables do not substitute for missing values**. Quoted single-line dotenv values are accepted, but imported Firebase values use plain single-line tokens. Gradle's unrelated ExerciseDB variables still follow Java Properties conventions. Missing/blank/placeholder/duplicate variables fail before writing. If output exists and differs, generation requires **`--force`** after you review the local values. If it already matches, nothing is rewritten. Writes use a temporary sibling file and rename, not partial output.

The script validates the project against `.firebaserc`, the Android package against `applicationId` in `app/build.gradle.kts`, the mobile app's project number, and OAuth types/signing SHA-1 shape. It does not authenticate with Firebase or prove a key/certificate is registered. It uses Node 22+ standard-library APIs and Git to ensure write destinations are untracked and ignored. No Google Cloud CLI or new package dependency is needed.

For a deliberately selected local environment, use **`--env-file .env.development`** consistently for import, generate and check. Only ignored `.env` / `.env.<environment>` destinations are accepted; tracked examples and arbitrary files are rejected. The output remains `app/google-services.json`, and the selected project/package must still match the repository. No automatic Gradle generation hook is installed: regenerate/check before building or supplying JSON to AI Studio.

The template mirrors the **current single Android app registration**, including one Android OAuth entry and one web OAuth entry. Import rejects unknown fields, missing fields or additional array entries rather than dropping them. If a new download has additional certificates/apps/platform clients, update the placeholder template and matching `.env.example` first, assigning a distinct variable to every new value. Numeric OAuth placeholders use names ending in `_CLIENT_TYPE`. Optional `--template FILE` selects an alternative reviewed template; it does not fetch or invent registration values.

Live JSON, downloaded `google-services*.json` copies, temporary files and actual `.env` files stay ignored. Only the exact placeholder template is exempted. Do not force-add live config, print it, or include `.env` in source archives. This protects Git hygiene, **not secrecy inside the built APK**: Firebase client API keys and OAuth IDs are intentionally shipped identifiers, not Admin SDK credentials. Protect backend data with Auth, rules/IAM and appropriate API restrictions.

If you only need to find the key online, select the same project in [Google Cloud → APIs & Services → Credentials](https://console.cloud.google.com/apis/credentials). Firebase typically creates the Android key automatically. Review API restrictions and test changes before tightening them; removing a required Firebase API can break sign-in. Do not reuse this key for billable non-Firebase APIs.

### Firestore must match too

The app gets its database ID from `app/src/main/res/values/firebase_applet_config.xml`. The current value is **`ai-studio-android-physiapp-429fc4fa-f1c5-40b6-ae8f-cc5e0f3a237a`**; `firebase.json` targets the same named database for rules. **That database ID is not the Firebase project ID.** The latter is in Project settings and `google-services.json`.

In the selected project's Firestore console, verify that this named database exists. If intentionally moving to another project/database, reconcile both files and data migration needs; do not silently substitute `(default)`. `google-services.json` alone does not select this named database or deploy rules. For a new database, start with locked-down/production rules, not open test-mode access; review and deploy rules to the explicitly selected project and database through an authenticated deployment workflow.

Existing `firestore.rules` restrict user documents to their owner, but allow **any authenticated user to create/update curriculum lessons**. That shared-content write policy needs a scoped security fix/review before production. The presence of a local rules file does not prove it has been deployed.

## 3. Signing fingerprints and app-building credentials

- This project's debug signing configuration points to **repository-root `debug.keystore`**, alias `androiddebugkey`, password `android`. These are standard debug credentials, never production signing credentials. Do not assume the certificate in `%USERPROFILE%\.android\debug.keystore` is the one this build uses.
- In your configured Android build environment, run the Gradle **`:app:signingReport`** task or inspect the actual keystore with Java `keytool`. Register the fingerprints for the certificate that signs the installed APK, including the AI Studio build certificate if different. If the keystore is missing, have the build environment create/configure a development keystore; do not regenerate an existing one casually.
- This checkout does not include a Gradle wrapper. Use your established Gradle installation/build workspace, not an assumed `gradlew.bat`. The installed Android toolchain and private config still need verification before local builds.
- Keep release/upload keystores outside source control, back them up encrypted, and store passwords in a password manager or protected build credential store. A debug key is not a release strategy; the current release build has no release signing configuration.
- When distributing through Google Play, use **Play App Signing**. In Play Console's **App integrity / App signing** area, obtain the SHA-1/SHA-256 of the **app signing certificate** used for installed Play builds. It differs from the upload certificate; register the correct certificates in Firebase and retain the upload-key recovery plan.

## 4. A maintainable storage model

### Local development in VS Code

- Keep **`.env.example`** tracked, with placeholders and comments only. Your ignored root **`.env`** is the local development copy; edit it directly in your editor and do not paste values into chat. If it already exists, preserve it rather than replacing it from the template.
- The current Android Gradle script reads only `EXERCISE_DB_API_KEY` and `EXERCISE_DB_API_HOST`, from `.env` first, then the process environment. An existing `.env` property wins even if blank or a placeholder. It uses Java Properties parsing, not a general dotenv loader: avoid surrounding quotes and shell expansion.
- `.env.local`, `.env.staging`, and other `.env.*` files are ignored for safety but **not automatically loaded** by Gradle. Firebase generation can read one explicitly with `--env-file`; `GEMINI_API_KEY` is not consumed by this app.
- Keep actual Firebase config values in `.env` and generate local `app/google-services.json` using the tracked placeholder template. Import authentic Firebase registration values rather than inventing keys. Treat files under an optional ignored root `secrets/` folder as local-only, with restricted filesystem access—not as an encrypted vault.
- Git ignore rules prevent new files from being added accidentally; they do not protect against forced adds, prior commits, agent access, or copied archives. Check staged filenames before committing. Previously tracked sensitive files require deliberate removal from the index and credential rotation when appropriate.

### Online key generation

- **Firebase:** register the app in Firebase; its client key is automatically provisioned. Manage restrictions in the same project's Cloud Credentials page.
- **Gemini (only if a separate tool/service needs it):** open [Google AI Studio → API Keys](https://aistudio.google.com/apikey), select/import the intended Cloud project, and create a dedicated key. Do not generate one just to build Kotlin or enable Firebase sign-in. This Android app currently has no Gemini API integration.
- **ExerciseDB:** use your RapidAPI application's key after subscribing to the intended API/plan. Use a separate limited development key, not a shared production key.

### Online secret storage for a future backend

1. Select the intended project in [Google Cloud → Secret Manager](https://console.cloud.google.com/security/secret-manager). Enable the Secret Manager API and any required billing after reviewing costs.
2. Choose **Create secret**, use a descriptive name such as `physiapp-dev-gemini-api-key` or `physiapp-prod-exercisedb-api-key`, and enter the value directly in the console as the initial version. Secret Manager stores keys; it does not generate provider keys.
3. Give the trusted backend's runtime service account **Secret Manager Secret Accessor** on just the necessary secret, not broad project administration. Prefer the platform's attached identity and, for CI, workload identity federation rather than downloading long-lived service-account private keys.
4. Explicitly configure the chosen server runtime to read/inject that secret. For example, a future Cloud Run service can bind a secret to an environment variable. Creating a secret alone does not wire it to AI Studio, Gradle, or an APK.
5. The Android app calls the authenticated server; the server calls the provider with its secret. Validate Firebase ID tokens, apply authorization and rate limits, and consider App Check. **Never let Android download provider secrets from Secret Manager, Firestore, Remote Config, or your server.**

This is a proposed production pattern, not an implemented service in this repository. The current ExerciseDB path uses `BuildConfig`, so even an online secret injected during compilation would be extractable from the final APK. Keep it development-only until an approved backend replaces that path.

### Google AI Studio and future build automation

- Signing in to Google AI Studio for implementation is different from giving the built app a Gemini key. Only create app-runtime keys when the feature actually requires them.
- Use a workspace's documented protected secrets/settings facility for supported **server-side** dependencies, not its chat prompt. The local Gradle script does not prove that an AI Studio Secrets panel injects these values; check the actual build environment and exported configuration.
- Supply Firebase's client config through the private build workspace/file mechanism. Do not include `.env`, admin JSON, or release keystores in broad source uploads or implementation handoffs.
- For future GitHub Actions, store necessary build credentials under **repository Settings → Secrets and variables → Actions**, preferably protected development/production environments. Use the environment's matching Firebase config; never expose production credentials to untrusted pull-request code. No such build-secret wiring is established by this guide.
- Longer term, separate development and production Firebase projects, keys, billing, and secret access. The Google services plugin supports `app/src/debug/google-services.json` and `app/src/release/google-services.json`, but this app's named database resource and deployment target must also match each environment. Do not assume adding variant JSON files alone completes environment separation.

## 5. Ongoing security and troubleshooting

- Record only non-secret inventory: purpose, owner, environment/project ID, console/secret name, consumers, and rotation procedure. Keep actual values in the vault/secret store. Enable account MFA and least-privilege collaborator access.
- Enable billing alerts and appropriate quotas at the provider/project level. Alerts are notifications, **not spending caps**.
- Before production, implement App Check in the app, register certificates/provider settings, monitor valid traffic, and then enable enforcement for the services used. It is not currently in the app dependencies. Debug/emulator builds need a development provider; debug tokens are secrets. Consider the root installer APK's sideload distribution when selecting Play Integrity settings.
- `google-services.json missing`: verify its name and `app/` placement in the build workspace.
- `No matching client found for package name`: download config for `com.aistudio.physiapp.kzmpqw`, not the Kotlin namespace.
- Missing `default_web_client_id` / Google sign-in failure: enable Google provider, register the actual signing SHA-1, re-download config, and verify the web OAuth client exists. Do not substitute an Android client ID for the web/server client ID.
- Firestore permission failure: check the signed-in user, selected project, named database, and deployed rules; do not temporarily make the database public to bypass it.
- Exposure response: private provider/admin keys require prompt revocation/rotation, usage/billing review, and redeployment. For a safe rotation, provision a replacement, update/test consumers, then revoke the old key; revoke immediately if active abuse warrants it. A Firebase-only client key being visible is expected, but incorrect restrictions or exposed data still require remediation. Deleting a file from Git does not undo exposure.

## Official references

- [Firebase Android setup](https://firebase.google.com/docs/android/setup)
- [Firebase API keys: public identifiers, restrictions, and finding keys](https://firebase.google.com/docs/projects/api-keys)
- [Firebase Google sign-in setup](https://firebase.google.com/docs/auth/android/google-signin)
- [Android signing fingerprints](https://developers.google.com/android/guides/client-auth)
- [Gemini key creation and safe use](https://ai.google.dev/gemini-api/docs/api-key)
- [Secret Manager: creating secrets](https://docs.cloud.google.com/secret-manager/docs/creating-and-accessing-secrets)
- [App Check with Play Integrity](https://firebase.google.com/docs/app-check/android/play-integrity-provider)