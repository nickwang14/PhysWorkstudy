# PhysiApp Engineering Standards

This is the canonical reference for linters, formatters, test requirements, and coverage
thresholds across every layer of the stack. Role files (Solutions Architect, DevOps Engineer,
Backend Engineer, Android Developer, QA Engineer, Repository Maintainer) reference this
document instead of duplicating tool choices — if a role file and this document disagree,
this document wins and the role file should be corrected.

Owned jointly by **Solutions Architect** (tool/standard selection, architecture fit) and
**Repository Maintainer** (day-to-day enforcement, drift detection, dependency freshness).
**QA Engineer** enforces these as merge-blocking CI gates. **DevOps Engineer** wires them into
GitHub Actions per `project-management/ci-cd.md`.

Apply [PD-008](../docs/product-decisions.md#pd-008-kotlin-app-and-vs-code--google-ai-studio-workflow) and the [development workflow](../docs/development-workflow.md): the owner orchestrates in VS Code and builds/implements in Google AI Studio. Handoff/review is user-mediated, not autonomous integration. Backend/web and CI plans do not establish implemented services or live checks.

## 1. Android / Kotlin / Jetpack Compose (current client)
- **Source:** preserve the actual `app/src/main/java/com/example/physiapp/` packages, resources and Gradle configuration; no framework migration is pending. Keep UI, state and data responsibilities testable without imposing a replacement directory tree.
- **Formatting:** follow existing Kotlin style; a dedicated formatter/task is not configured in the reviewed Gradle files. Select and wire a Kotlin formatter before naming a required format command; do not assume ktlint, Spotless or detekt is installed.
- **Static checks:** Kotlin compilation and Android Lint are the applicable native checks. With a verified compatible Gradle installation, JDK and Android SDK, inspect `gradle :app:tasks --all`, then run confirmed tasks such as `gradle :app:lintDebug` and `gradle :app:assembleDebug`. No Gradle wrapper is currently present; do not prescribe `gradlew`/`gradlew.bat` as available.
- **Toolchain blockers:** the checked-in configuration uses compile/target SDK 36, Java compatibility 21 and AGP 9.1.1; verify the compatible Gradle/JDK/SDK before execution. Debug signing references a root `debug.keystore` that also needs checking. Missing SDK, Gradle, signing prerequisites or dependencies mean blocked/unrun, not a successful build.
- **Tests:** add Kotlin unit tests for domain/state/persistence logic and Compose UI/instrumentation tests for changed interactive screens. Confirm test sources, dependencies and task availability before `gradle :app:testDebugUnitTest` or `gradle :app:connectedDebugAndroidTest`; instrumentation requires an emulator/device. The current dependency list does not establish these test suites or a coverage reporter as configured; a no-source task is not test evidence.
- **Coverage target:** retain the MVP target of 70% line coverage for Android application code and 90%+ for progression/state-machine and sync-outbox code; configure native coverage reporting before enforcing or claiming these thresholds.
- **Evidence:** report exact commands/results, blocked or unrun checks and remaining device/AI Studio validation separately. Content-tool tests and the owner's use of AI Studio do not certify an Android build.
- **Future web:** preserve read-oriented companion boundaries; its technology and build/test tooling remain undecided. Current Android work uses Kotlin/Gradle/Android checks.

## 2. Backend (proposed Node.js / TypeScript service)
These are target standards for the proposed backend, not commands available in a backend project already present here.
- **Formatter:** Prettier, run via `npm run format:check` in CI
- **Linter:** ESLint with `@typescript-eslint` strict config — zero errors required to merge
- **Static typing:** TypeScript `strict: true`; `tsc --noEmit` must pass in CI; no `any` without an inline justification comment
- **Tests:** Vitest (or Jest, pick one and standardize — Vitest preferred for speed) — unit tests for services/business logic, integration tests for API routes against a real Postgres test database
- **Coverage threshold:** 75% line coverage minimum; progression state machines (learning + training, kept separate) and the provider-gateway module (Wikimedia/MuscleWiki) require 90%+ coverage
- **API contracts:** every route must be described in the OpenAPI 3.1 spec before or alongside implementation; CI validates the spec against actual route registrations where feasible
- **Structure conventions:** modular monolith organized by domain module (e.g., `modules/learning`, `modules/training`, `modules/provider-gateway`, `modules/auth`); no domain module reaches directly into another module's database tables — cross-module access goes through an exported service interface

## 3. Content pipeline (Git-backed Markdown curriculum)
- **Validation:** a CI step lints Markdown content bundles against the content schema (required frontmatter fields, valid chapter/subchapter/lesson linkage, no orphaned lessons, valid media references)
- **Compilation:** content must compile into a versioned bundle without errors before merge; broken links or missing knowledge-check answer keys block merge
- **Review requirement:** curriculum content changes require Physio Consultant sign-off (accuracy) in addition to passing automated validation; educational copy changes also require Content Strategist review per `project-management/review-checklist.md`

## 4. Cross-cutting / all layers
- **Accessibility:** any new or changed UI surface must satisfy the Accessibility Specialist's checklist (semantic labeling, contrast, touch targets, screen reader support) before merge — treated as a review gate, not a suggestion
- **Secrets:** no secrets, API keys, or credentials committed to source; CI includes a secret-scanning step
- **Commit/PR hygiene:** PRs should be scoped to one epic/story or one clear technical concern; commit messages follow the convention already established in repo history
- **Dependency freshness:** Repository Maintainer reviews dependency updates on a regular cadence (not ad hoc per-PR) and flags security advisories immediately regardless of cadence

## 5. Required PR status checks (merge-blocking)
A pull request requires the applicable configured checks and review gates below. Missing tooling/checks must be recorded and resolved or explicitly triaged, not marked passing or replaced with retired client commands:
- Format check
- Lint (zero errors)
- Typecheck
- Unit tests
- Integration tests (backend routes when implemented; Android/Compose UI tests for changed screens when configured)
- Content validation (if content bundles changed)
- Accessibility review acknowledgement (if UI changed)

See `project-management/ci-cd.md` for proposed GitHub Actions workflows, branch protection
rules and environment promotion steps; this document does not certify that they are live.
