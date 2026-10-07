# PhysiApp Backlog

Use [shared guidance](../AGENTS.md) and the [development workflow](../docs/development-workflow.md). Status review dated 2026-10-07: `[x]` means the stated planning/documentation task is complete; unchecked work may already have a partial source implementation, noted below. Source presence is not build, test, device, AI Studio, or release acceptance evidence. Update status only with acceptance evidence.

## Current Priority Order

### P0 — Must Have for MVP
- [x] Define and lock product guardrails and core curriculum model — policy is recorded in `docs/product-decisions.md`; the curriculum hierarchy is represented in `app/src/main/java/com/example/physiapp/data/model/CurriculumModels.kt`.
- [x] Finalize MVP feature list for learning and training loops — scoped in [MVP](mvp.md).
- [ ] Define the canonical domain model and API contracts for users, curriculum, exercises, programs, progress, workout logs, weekly goals, and deload cycles — **In Progress:** Android models and repositories exist; formal backend/API contracts remain undefined.
- [ ] Build the backend foundation for authentication, PostgreSQL persistence, API validation, and environment configuration — **Backlog:** the proposed PostgreSQL/API foundation is not implemented; the current Android app uses Firebase Auth/Firestore.
- [ ] Build offline-first mobile persistence and idempotent outbox synchronization — **In Progress:** local `SharedPreferences` persistence exists, but there is no durable sync outbox; some profile/workout writes go directly to Firestore.
- [ ] Build a versioned content pipeline that validates the Git-backed curriculum and publishes app-ready bundles — **Backlog:** Markdown parsing/rendering exists, but lessons are currently supplied by in-memory repository data; schema validation, versioned bundles, publishing and caching are not implemented.
- [ ] Connect authored YAML/Markdown lessons to Android runtime loading and verify formatted, accessible reading — **In Progress:** Markdown parsing, formatted rendering and heading semantics exist; loading authored repository files, metadata validation and Android validation remain pending. See [Content Delivery](infrastructure-plan.md#content-delivery).
- [ ] Create the curated MVP exercise library with stable IDs, movement tags, coaching cues, substitutions, and media references — **In Progress:** an inline exercise catalog includes IDs, patterns, cues and media URLs; explicit substitutions and verified source/license/curation records remain incomplete.
- [ ] Build progression map state model and daily node logic — **In Progress:** sequential lesson selection, completion state and map UI exist; the curriculum catalog is still hardcoded/limited, and native validation remains pending.
- [ ] Build workout logging and weekly goal tracking — **In Progress:** workout logging and goal counters exist in the app; week-boundary behavior and end-to-end validation remain open.
- [ ] Build curriculum and daily learning flow — **In Progress:** lessons, knowledge checks, completion and streak logic exist; authored-source loading is not connected, and no optional reading entries are currently attached to lessons.
- [ ] Implement deload cycle logic — **In Progress:** week-six state, UI and cycle controls exist; native behavior and edge-case validation remain pending.
- [ ] Extend and validate the existing Kotlin/Compose Android app shell and mobile-first UI — **In Progress:** the Android app and screens exist; Gradle/build, device and formal QA/accessibility evidence are not recorded here.
- [ ] Build companion web dashboard for review and progress visibility — **Backlog:** future web technology remains undecided and no web implementation is present.
- [ ] Define QA and accessibility validation gates — **In Progress:** standards/checklists are documented; formal review and automated enforcement are not established.
- [ ] Establish CI/CD, development/staging/production environments, secrets management, backups, crash reporting, and baseline product analytics — **Backlog:** these remain proposed; no active CI workflow is recorded.

### P1 — Important Next
- [ ] Adaptive split engine v1 — **Backlog:** no adaptive recommendation engine found; readiness check-in advice is not split adaptation.
- [ ] Analytics instrumentation and reporting — **In Progress:** event/KPI definitions exist in [event-kpi-spec.md](event-kpi-spec.md), but app event emission, analytics integration and reporting were not found.
- [ ] Repo maintenance and technical hygiene workflow — **In Progress:** shared guidance and maintenance standards exist; recurring automated native validation/enforcement is not configured.
- [ ] Validate lesson and optional-reading favorites — **In Progress:** local persistence, Saved UI, and the optional-reading reader path were added in commit `46d47a5`; unit and Compose tests were authored but not run because Gradle/the wrapper were unavailable. The current curriculum has no optional-reading entries yet. Acceptance: run Android checks, verify add/remove/reopen for both item types, link optional readings through approved content work, and confirm favorites do not affect progression or streaks.
- [ ] Build a provider-neutral media catalog with source, revision, license, creator, attribution, approval, and expiration metadata — **Backlog:** current exercise media URLs do not provide this provenance/approval model.
- [ ] Add a curated Wikimedia API ingestion pilot for lesson reference media — **Backlog:** no Wikimedia ingestion implementation found.
- [ ] Complete a MuscleWiki API commercial-license and product-fit spike — **Backlog:** no completed licensing/product-fit review recorded.

### P2 — Near-Term Enhancements
- [ ] More advanced personalization logic — **Backlog:** no personalization engine found; keep behind reliable usage signal and approved product scope.
- [ ] Expanded curriculum sequencing and modular learning tracks — **In Progress:** authored curriculum and indices are expanding, but Android still uses a smaller hardcoded lesson catalog and does not load the authored corpus.
- [ ] Scale-phase infra and observability improvements — **Backlog:** no scale-phase service or observability implementation found.
- [ ] Add the official MuscleWiki API through the backend provider gateway if the license, cost, and editorial review gates are approved — **Backlog:** provider gateway, license approval and integration are not implemented.

### Deferred — Not in MVP
- [ ] Social encouragement flows and friend streak nudges
- [ ] Milestone sharing and social distribution surfaces
- [ ] Additional sharing and engagement features
- [ ] User-generated content management and moderation workflows
- [ ] Advanced mechanics curriculum depth
- [ ] Pathology and rehab-specific curriculum depth

## Status Notes
- Keep backlog focused; remove or defer work that does not support the core product thesis.
- Rebalance priorities when new signal appears.
- Every item should have an owner before implementation begins.
- Watch for feature flushing in these areas:
  - social mechanics before the core loop is stable
  - personalization before reliable usage signal exists
  - web scope expansion beyond the companion dashboard
  - curriculum expansion before lesson completion and retention is validated
  - blended learning and training signals that confuse the product model
  - third-party content becoming the source of truth for curated lessons or workout plans
