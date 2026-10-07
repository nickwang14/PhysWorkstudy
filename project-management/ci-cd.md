# PhysiApp CI/CD Process

This document defines the pipelines, branch strategy, and environment promotion rules for
PhysiApp. It implements the gates defined in `project-management/engineering-standards.md`
and the stack decisions in `project-management/infrastructure-plan.md`. Owned by **DevOps
Engineer**; architecture-level changes to this process require **Solutions Architect** sign-off.

The repo contains the Kotlin/Jetpack Compose Android app and Gradle configuration. Apply
[PD-008](../docs/product-decisions.md#pd-008-kotlin-app-and-vs-code--google-ai-studio-workflow)
and the owner-confirmed [development workflow](../docs/development-workflow.md): VS Code
orchestration and owner-mediated Google AI Studio app building, not an autonomous integration.
The workflows and branch protections below are target specifications, not verified live
configuration. Backend/cloud providers remain proposed; future web technology is undecided.

## 1. Proposed branch strategy
- `main` — always deployable; protected; direct pushes disabled
- `develop` — optional integration branch once the team is large enough to need one; not required at MVP team size, `main` can serve as the integration branch with short-lived feature branches
- `feature/<short-name>` — one epic/story or technical concern per branch, per engineering-standards.md PR scoping rule
- Merges to `main` happen via PR only, with required status checks (below) green and at least one review

## 2. Target status checks (configure branch protection on `main`)
- `android-ci` (for the existing Kotlin/Compose client, once wired and verified)
- `backend-ci` (once backend exists)
- `content-ci` (once content pipeline exists)
- Secret scan
- No direct merges without these passing — this mirrors engineering-standards.md section 5

## 3. Proposed Android CI workflow (`.github/workflows/android-ci.yml`, not created by this documentation update)
Trigger: PR to `main`, push to `main`
1. Checkout the existing Android project; verify a compatible Gradle/JDK for AGP 9.1.1 and install the required Android SDK (compile/target 36; Java compatibility 21 in the app configuration).
2. No Gradle wrapper is checked in. Provision a compatible pinned Gradle installation and inspect `gradle :app:tasks --all`; do not assume `gradlew` or `gradlew.bat` exists.
3. Run confirmed Android Lint and build tasks, such as `gradle :app:lintDebug` and `gradle :app:assembleDebug`; verify debug-signing prerequisites first. Missing SDK/toolchain/signing files block the check rather than count as a pass.
4. Configure Kotlin unit-test and Compose UI-test sources/dependencies; run `gradle :app:testDebugUnitTest` and emulator/device-backed `gradle :app:connectedDebugAndroidTest` only after verifying their availability. No-source tasks are not test coverage evidence.
5. Select and configure Kotlin formatting and native coverage tasks before adding format/coverage status checks. Upload actual reports and enforce engineering-standard targets only once reporting is wired.
6. Record exact commands, failures, blockers and remaining device/AI Studio checks. AI Studio build evidence must come from actual owner-provided output; CI does not infer or automate that workspace.

## 4. Backend CI workflow (`.github/workflows/backend-ci.yml`, to be created with the backend project)
Trigger: PR to `main`, push to `main`
1. Checkout, set up Node.js LTS
2. `npm ci`
3. `npm run format:check`
4. `npm run lint`
5. `tsc --noEmit`
6. Spin up ephemeral Postgres (GitHub Actions service container) and run migrations
7. `npm test` (unit + integration against the ephemeral database)
8. Validate OpenAPI spec is in sync with registered routes

## 5. Proposed content pipeline CI workflow (`.github/workflows/content-ci.yml`)
Trigger: PR touching `theory-and-knowledge/knowledge/**`, `workout-programming/knowledge/**` or relevant content tooling/records
1. Validate frontmatter schema and chapter/subchapter/lesson linkage
2. Check for orphaned lessons, missing knowledge-check answer keys, broken media references
3. Compile content into a versioned bundle as a dry run; fail on compile errors
4. Require Physio Consultant + Content Strategist review labels before merge (process check, tracked via `project-management/review-checklist.md`, not something CI can fully enforce)

## 6. Proposed deployment / environment promotion
- **Future web (technology undecided):** Cloudflare Pages is a proposed candidate; choose a compatible web stack/build before implementing preview and production promotion
- **API/worker (proposed Fastify + pg-boss):** Render is proposed — `main` would deploy to staging first, with production promotion behind manual approval (host protection or a GitHub Actions `environment` gate)
- **Android:** proposed Gradle signed-AAB release workflow → proposed Firebase App Distribution internal beta → manual Google Play promotion; signing, accounts and workflow implementation require verification
- **Database migrations:** run as a distinct CI/CD step before the API deploy step completes; migrations must be backward-compatible with the currently-running API version (expand/contract pattern) since there is no maintenance-window downtime budget assumed at MVP

## 7. Proposed rollback (when the corresponding services are implemented)
- Render: redeploy the previous successful build via one-click rollback
- Cloudflare Pages: previous deployment remains addressable; promote it back as production
- Android: halt the Play Store staged rollout percentage; do not attempt in-place hotfix without going through CI again
- Database: no destructive migrations without a documented down-migration or backup checkpoint immediately prior (ties to DevOps Engineer's backup/restore-drill ownership in `devops-engineer.agent.md`)

## 8. Proposed observability tie-in
- Sentry is proposed for Android and backend errors; upload applicable Android mapping/symbol files and service debug artifacts once instrumentation/release workflows exist
- PostHog is proposed; document new event names in the analytics plan before shipping, without assuming instrumentation or validation is already implemented

## 9. What this document does not cover
- Local toolchain setup (use the existing Android project and development workflow; future services/web need their own setup guidance when implemented)
- Cost/vendor/provider account management, incident response, and backup/restore drills — owned by DevOps Engineer as platform operations (see the "Platform Operations Ownership" section of `.claude/agents/devops-engineer.agent.md`); no separate Infrastructure Manager role exists at MVP scale, per the infra-role gap analysis
