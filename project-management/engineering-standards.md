# PhysiApp Engineering Standards

This is the canonical reference for linters, formatters, test requirements, and coverage
thresholds across every layer of the stack. Role files (Solutions Architect, DevOps Engineer,
Backend Engineer, Flutter Developer, QA Engineer, Repository Maintainer) reference this
document instead of duplicating tool choices — if a role file and this document disagree,
this document wins and the role file should be corrected.

Owned jointly by **Solutions Architect** (tool/standard selection, architecture fit) and
**Repository Maintainer** (day-to-day enforcement, drift detection, dependency freshness).
**QA Engineer** enforces these as merge-blocking CI gates. **DevOps Engineer** wires them into
GitHub Actions per `project-management/ci-cd.md`.

## 1. Flutter / Dart (client)
- **Formatter:** `dart format` — CI fails on any unformatted file (`dart format --output=none --set-exit-if-changed .`)
- **Linter:** `flutter analyze` using `flutter_lints` (strict ruleset) — zero errors required to merge; warnings tracked but do not block until backlog of pre-existing warnings is cleared
- **Static typing:** Dart's sound null safety is mandatory; no `dynamic` escape hatches without a documented reason in a code comment
- **Tests:** `flutter test` — widget tests for all interactive screens, unit tests for state notifiers/providers (Riverpod) and Drift DAOs/queries
- **Coverage threshold:** 70% line coverage minimum on `lib/` at MVP; progression/state-machine and sync-outbox code require 90%+ coverage given correctness risk
- **Structure conventions:** feature-first folder structure (`lib/features/<feature>/{data,domain,presentation}`); shared/reusable code in `lib/core/`; no cross-feature imports of `presentation` layers

## 2. Backend (Node.js / TypeScript)
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
A pull request cannot merge unless all of the following pass, per the affected layer:
- Format check
- Lint (zero errors)
- Typecheck
- Unit tests
- Integration tests (backend routes; Flutter widget tests for changed screens)
- Content validation (if content bundles changed)
- Accessibility review acknowledgement (if UI changed)

See `project-management/ci-cd.md` for how these are implemented as GitHub Actions workflows,
branch protection rules, and environment promotion steps.
