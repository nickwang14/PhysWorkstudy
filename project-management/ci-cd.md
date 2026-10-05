# PhysiApp CI/CD Process

This document defines the pipelines, branch strategy, and environment promotion rules for
PhysiApp. It implements the gates defined in `project-management/engineering-standards.md`
and the stack decisions in `project-management/infrastructure-plan.md`. Owned by **DevOps
Engineer**; architecture-level changes to this process require **Solutions Architect** sign-off.

The repo currently contains no application code yet — this is the target pipeline design to
build against as soon as the Flutter and backend projects are scaffolded. Treat each workflow
below as a spec to implement, not a live pipeline.

## 1. Branch strategy
- `main` — always deployable; protected; direct pushes disabled
- `develop` — optional integration branch once the team is large enough to need one; not required at MVP team size, `main` can serve as the integration branch with short-lived feature branches
- `feature/<short-name>` — one epic/story or technical concern per branch, per engineering-standards.md PR scoping rule
- Merges to `main` happen via PR only, with required status checks (below) green and at least one review

## 2. Required status checks (branch protection on `main`)
- `flutter-ci` (once client exists)
- `backend-ci` (once backend exists)
- `content-ci` (once content pipeline exists)
- Secret scan
- No direct merges without these passing — this mirrors engineering-standards.md section 5

## 3. Flutter CI workflow (`.github/workflows/flutter-ci.yml`, to be created with the Flutter project)
Trigger: PR to `main`, push to `main`
1. Checkout, set up Flutter SDK (pinned version)
2. `flutter pub get`
3. `dart format --output=none --set-exit-if-changed .`
4. `flutter analyze`
5. `flutter test --coverage`
6. Upload coverage artifact; fail if below threshold in engineering-standards.md
7. On `main` only: `flutter build apk --debug` as a build-health smoke check (full release build handled by the release workflow, not every PR)

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

## 5. Content pipeline CI workflow (`.github/workflows/content-ci.yml`)
Trigger: PR touching `content/**` (curriculum Markdown)
1. Validate frontmatter schema and chapter/subchapter/lesson linkage
2. Check for orphaned lessons, missing knowledge-check answer keys, broken media references
3. Compile content into a versioned bundle as a dry run; fail on compile errors
4. Require Physio Consultant + Content Strategist review labels before merge (process check, tracked via `project-management/review-checklist.md`, not something CI can fully enforce)

## 6. Deployment / environment promotion
- **Web (Flutter Web build):** Cloudflare Pages — auto-deploys a preview per PR; merge to `main` promotes to production
- **API/worker (Fastify + pg-boss):** Render — `main` auto-deploys to a staging environment first; production promotion is a manual approval step in the same workflow (Render environment protection or a manual GitHub Actions `environment` gate)
- **Android:** GitHub Actions builds signed AAB on release tag → Firebase App Distribution for internal beta → manual promotion to Google Play production track
- **Database migrations:** run as a distinct CI/CD step before the API deploy step completes; migrations must be backward-compatible with the currently-running API version (expand/contract pattern) since there is no maintenance-window downtime budget assumed at MVP

## 7. Rollback
- Render: redeploy the previous successful build via one-click rollback
- Cloudflare Pages: previous deployment remains addressable; promote it back as production
- Android: halt the Play Store staged rollout percentage; do not attempt in-place hotfix without going through CI again
- Database: no destructive migrations without a documented down-migration or backup checkpoint immediately prior (ties to DevOps Engineer's backup/restore-drill ownership in `devops-engineer.agent.md`)

## 8. Observability tie-in
- Sentry captures errors from both Flutter client and backend; CI release step should upload source maps/symbols so stack traces are readable
- PostHog analytics events are validated informally (no automated schema check at MVP) but any new event name should be documented in the analytics plan before shipping

## 9. What this document does not cover
- Local dev environment setup (belongs in each project's own README once scaffolded)
- Cost/vendor/provider account management, incident response, and backup/restore drills — owned by DevOps Engineer as platform operations (see the "Platform Operations Ownership" section of `.claude/agents/devops-engineer.agent.md`); no separate Infrastructure Manager role exists at MVP scale, per the infra-role gap analysis
