# Local Google Cloud CLI and Authentication

This is a **local workstation setup**, not deployed cloud infrastructure. It enables CLI and trusted maintenance-tool access to the existing project, subject to the signed-in account's IAM permissions. It does not create projects/services, enable billing/APIs, deploy rules, or assign app administrators. Follow the [development workflow](development-workflow.md).

## Local toolchain layout

- Windows x64 CLI location: `%LOCALAPPDATA%\PhysiApp\toolchain\gcloud\google-cloud-sdk\bin\gcloud.cmd`.
- Google provides a self-contained archive with bundled Python; use that runtime rather than depending on a repository virtual environment.
- Installation archive: `google-cloud-sdk-588.0.0-windows-x86_64-bundled-python.zip`. Published SHA-256: `9e00e3c9b042f732e29eb7ca6095fdb44bda03938013274a148b5c5f95a6ec92`.
- Download from Google's official versioned archive, verify its published checksum, and extract outside Git. Windows `Expand-Archive` avoids `tar` failures on archive link entries without requesting elevation. If a future version is selected, verify that version's own checksum; do not reuse this one.
- Add the `bin` directory to **user PATH**, preserving existing entries. Existing VS Code processes/terminals can retain old environment values: restart VS Code or invoke the full executable path if `gcloud` is not found.
- Verify with **`gcloud --version`**. Updates are deliberate: **`gcloud components update`** changes the local SDK; check compatibility and version afterward. Do not require elevated installation.

## Dedicated project configuration

Use a named CLI configuration **`physiapp`** for the live project **`gen-lang-client-0444088676`**. Create it with **`gcloud config configurations create physiapp`**, or activate an existing configuration with **`gcloud config configurations activate physiapp`**. Set its project with **`gcloud config set project gen-lang-client-0444088676 --configuration=physiapp`**.

Prefer explicit **`--configuration=physiapp --project=gen-lang-client-0444088676`** on consequential commands, even if the configuration is active. Selecting a project is a local preference, not a grant of permissions or a cloud-resource creation operation. Do not configure an arbitrary compute zone, create service accounts, or enable billable services just to log in.

The Firestore named database is **`ai-studio-android-physiapp-429fc4fa-f1c5-40b6-ae8f-cc5e0f3a237a`**, not the project ID and not `(default)`. Google Auth custom claims are project-level Auth metadata; they are not stored in this Firestore database.

## Sign in locally, without sharing secrets

1. Run **`gcloud auth login --update-adc --configuration=physiapp`** and complete account selection/MFA/consent directly in Google's browser flow. Choose the account authorized for the intended project. Never send passwords, authorization codes, tokens, or credential JSON to a chat/agent.
2. The `--update-adc` option also writes ADC for the Firebase Admin SDK/Google client libraries. Without it, **CLI login and ADC login are separate**. Alternative: `gcloud auth login --configuration=physiapp`, then `gcloud auth application-default login`.
3. Run **`gcloud auth application-default set-quota-project gen-lang-client-0444088676`** after successful login. This requires `serviceusage.services.use` on that project; it identifies the quota consumer and does not enable billing or grant IAM roles.
4. Check configured accounts with **`gcloud auth list --configuration=physiapp`**. Do not use commands that print access/identity tokens for diagnostics shared with an agent.
5. Use a read-only metadata check: **`gcloud projects describe gen-lang-client-0444088676 --configuration=physiapp --format="value(projectId,lifecycleState)"`**. A successful login does not prove project access; an IAM denial needs an administrator's review, not weaker Firestore rules.

If authentication falls back to asking for a verification code, enter it directly in the terminal yourself. An interactive browser/login step is expected and cannot safely be automated by asking an agent for your secrets.

## Credential storage and account separation

- Default Windows configuration/CLI credentials live under **`%APPDATA%\gcloud`**, including credential databases and logs. ADC normally lives at **`%APPDATA%\gcloud\application_default_credentials.json`**. These are sensitive local files, not app configuration, and should never be copied to Git or app-building uploads.
- `CLOUDSDK_CONFIG` can relocate CLI configuration. `GOOGLE_APPLICATION_CREDENTIALS`, if set, takes precedence over local ADC for SDKs. Check which mechanism is active without printing contents; do not silently use a stale service-account key.
- Named CLI configurations separate CLI project/account settings, **not ADC identities**: running ADC login again changes the credential used by local SDKs. The backend tool explicitly targets its project/database, but still needs the intended IAM identity.
- Prefer browser user ADC or approved short-lived service-account impersonation to downloaded private keys. Local user ADC includes a refresh token and deserves the same care as a password. Use Windows account/filesystem protections and organization-approved device security.
- Firebase CLI authentication is separate; only configure it when a Firebase CLI cloud operation is needed. Emulator operations do not need a real account.
- Sign-out/recovery: **`gcloud auth revoke --configuration=physiapp`** manages CLI credentials; **`gcloud auth application-default revoke`** manages ADC separately. Do not revoke unrelated accounts or delete a shared credential directory casually.

## Existing repository tools and safety boundaries

- **`npm.cmd run backend -- --help`** explains the local Firestore manager. It defaults to `demo-physiapp` on loopback emulators and the explicitly configured named database.
- Live read example: **`npm.cmd run backend -- query curriculum_lessons --mode live --project gen-lang-client-0444088676 --database ai-studio-android-physiapp-429fc4fa-f1c5-40b6-ae8f-cc5e0f3a237a --fields title,chapterNumber --limit 10`**. This reads curriculum metadata, not personal profiles.
- An active `FIRESTORE_EMULATOR_HOST` blocks that tool's live mode. `FIREBASE_AUTH_EMULATOR_HOST` redirects Auth SDK operations. Use separate terminals for live and emulator workflows and verify targets before applying any mutation; starting the Emulator UI never connects it to production.
- Firestore writes remain previews unless explicitly applied with the full project/database/path confirmation. Live before-images remain in ignored `.local/firebase-backups/`; protect and retain them appropriately. See [Firebase local development](firebase-local-development.md).
- Client rules do **not** govern Admin SDK access; IAM does. Start read-only with appropriate project visibility/Firestore viewer permissions. Auth custom-claim maintenance needs a trusted identity authorized for Firebase Authentication administration (for example, project-scoped Firebase Authentication Admin or a reviewed narrower role), not just Firestore permissions. Do not grant project-wide Owner/Editor merely to make a command work.
- The local Firestore tool does not yet manage Auth custom claims. App-admin grants are separate trusted maintenance operations; installing/logging into the CLI does not perform them. Preserve other claims and verify each grant by reading the Auth account back. See [app admin provisioning](lesson-feedback-security.md#provisioning-or-revoking-an-app-admin).

## Troubleshooting

- **Command not found:** open a terminal with refreshed user PATH or use the full `.cmd` path above.
- **ADC missing:** complete `--update-adc` or the separate ADC login, not just a Firebase Console/Firebase CLI sign-in.
- **403 / permission denied:** verify account, explicit project, needed IAM permissions and organization policy. Do not auto-grant broader roles or disable security checks.
- **Certificate/proxy error:** use the organization's approved CA/proxy configuration. Do not disable TLS certificate validation. Configure CLI and Node independently if both tools need a corporate CA.
- **Unexpected emulator/live target:** inspect only relevant environment-variable presence and explicit arguments; do not dump the full environment (it may contain secrets).

## Official references

- [Official installation and checksum table](https://docs.cloud.google.com/sdk/docs/downloads-versioned-archives)
- [CLI authentication options](https://docs.cloud.google.com/sdk/gcloud/reference/auth/login)
- [Application Default Credentials](https://docs.cloud.google.com/docs/authentication/provide-credentials-adc)
- [Named CLI configurations](https://docs.cloud.google.com/sdk/docs/configurations)