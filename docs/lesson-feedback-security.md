# Individual lesson review and private feedback

## Confirmed scope and local implementation

- A module is an **individual curriculum lesson**, using the existing stable lesson ID.
- **Only administrators** may change review metadata: Approved, Rejected, Needs improvement. Wire values are `approved`, `rejected`, `needs_improvement`.
- Authenticated users may submit **private comments**, readable only by their author and administrators. There is no public discussion feed.
- Review status lives at `curriculum_lessons/{lessonId}/review/status`; comments at `curriculum_lessons/{lessonId}/comments/{autoId}`. See [schema](firestore-schema.md) and [blueprint](../firebase-blueprint.json).
- Missing review means Not reviewed. Status does not change lesson completion, gates, streaks, or training. It does not itself certify domain/editorial/rights approval.

## Authorization

Client rules use the Firebase Auth token's **Boolean custom claim `admin == true`**. Missing, false, string `"true"`, numeric values, profile `admin` fields, and UI state do not grant access. User documents are not an authority for roles. No user-facing claim-grant endpoint is introduced.

App admins are different from Google Cloud IAM administrators. A trusted server/Admin SDK identity bypasses client rules and must enforce its own permissions and validation. Do not grant broad IAM access to ordinary app reviewers just to enable in-app status controls.

### Provisioning or revoking an app admin

1. The project owner verifies the intended Firebase project and the person's Auth UID out of band. Do not identify administrators from editable profile names/emails.
2. Use a trusted maintenance environment with the Firebase Admin SDK and IAM permission to manage Auth users, preferably ADC/short-lived identity. Do not ship service-account private keys in the app or paste them into chats.
3. Fetch existing custom claims with `getUser(uid)`, merge the Boolean `admin: true` claim, and call `setCustomUserClaims(uid, mergedClaims)`. This API **replaces** custom claims; preserve unrelated approved claims. The local Firestore CLI does not currently provide this operation.
4. Refresh the user's ID token (sign out/in or a deliberate `getIdToken(true)` refresh) and verify the server recognizes the claim. Granting a Cloud Console role or setting a Firestore profile field does not perform this step.
5. To remove access, remove/set false the claim in the trusted environment and revoke refresh tokens if appropriate. Existing ID tokens may remain usable until expiry unless a separately implemented revocation check is present; do not promise immediate Firestore-rule revocation. Protect administrator accounts with MFA and review access periodically.

No live claims are granted or revoked by this source change.

## Comment safety

- **Plain data:** use fixed collection/document paths and typed Firestore SDK payloads. Comment strings are never used as paths, query syntax, credentials, role claims, commands, or code.
- **Enforced payload:** rules allow exactly `authorId`, `text`, and `createdAt`, for an existing parent lesson. Author must match the authenticated UID; nonblank string size is bounded at 2000; timestamp must equal request time using `serverTimestamp()`.
- **Immutable:** no comment updates, including by admins. Authors/admins may delete; a new comment gets a new auto ID. Ownership and creation metadata cannot be edited in place.
- **Private queries:** non-admin reads require `whereEqualTo("authorId", uid)`; unrestricted queries are denied. The app shows up to 50 comments, locally sorted within that subset, not necessarily the newest 50.
- **Safe rendering:** `OutlinedTextField` input and Compose `Text(comment.text)` output only. No Markdown, HTML/WebView, automatic links, template evaluation, or execution. `<script>`, SQL fragments, and Markdown are literal text, not a reason to execute or trust anything. A keyword blacklist is not the security boundary.
- **Separate UI/backend validation:** app rejects blank/oversized input; rules enforce validation even for modified clients. Kotlin UTF-16 length checks can be stricter for some Unicode than server string-size checks. Rules reject ASCII whitespace-only text; the app additionally rejects Kotlin-recognized Unicode whitespace.
- **Account scope:** repository/listeners and transient editor state are bound to account and lesson. Account/token changes and read errors clear private UI state and drafts. No private draft is placed in saved instance state. Errors do not echo comment text or raw provider details. Cancelling UI work cannot recall a write already dispatched to Firestore.
- **Online submission:** feedback mutations use read-first transactions, which fail offline rather than queue optimistic writes. The UI retains the last acknowledged review status during pending writes. Transaction retries reuse the same new comment ID. A network disruption after dispatch can still leave an uncertain outcome; do not blindly resubmit without checking the stored result.

Firestore is not SQL, and a typed string write does not run SQL or JavaScript. This is not a blanket guarantee against every security issue. If comments are later shown on the web, rendered as rich text, exported to spreadsheets, indexed by a service, or used in an AI prompt, apply context-specific output encoding/sanitization, formula-injection protection, and treat AI inputs as untrusted. Never give comment text authority to run tools or access databases.

## Rollout and remaining safeguards

1. Review source/rules and validate locally/staging; no deployment is automatic.
2. Establish trusted admin provisioning and explicitly publish the lesson index. New comments/reviews require existing remote lesson documents. Do not auto-publish as a normal user to make comments work.
3. Inspect legacy index documents for extra fields. The index now allows exactly its existing 12 fields; authorized migration must relocate any review data and remove unsupported fields without dropping reviewed/user data.
4. Deploy reviewed rules to the **explicit project and named database** in `firebase.json`, and release the updated client. Older clients' automatic index writes will be denied; do not weaken rules to accommodate them.
5. Verify normal-user and administrator flows against staging, including account switching, offline failures, and claim changes. Admin SDK access does not test client rules. Existing Firestore disk cache is not purged by this UI implementation; device/account privacy review must address shared devices, logout/cache policy, and OS storage.
6. Before production, plan abuse controls: App Check, appropriate quotas/billing alerts, and a server-enforced submission rate limit if needed. The 2000-character limit and pending-button disable are not robust rate limiting.
7. Define feedback retention, account deletion/anonymization, and moderation policy. Private comments can still contain sensitive personal information; UI should not solicit medical details. Deleting a lesson/profile/Auth account does not cascade comments. There is no automated erasure pipeline here.

Tests prove local permissions/validation/rendering properties, not deployed security, scientific/legal approval, or production readiness. Follow [development workflow](development-workflow.md) for AI Studio handoff/device validation.