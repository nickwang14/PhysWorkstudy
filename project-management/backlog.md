# PhysiApp Backlog

Use [shared guidance](../AGENTS.md) and the [development workflow](../docs/development-workflow.md). Status review dated 2026-10-08: `[x]` means the stated planning/documentation task is complete; unchecked work may already have a partial source implementation, noted below. Source presence is not build, test, device, AI Studio, or release acceptance evidence. Update status only with acceptance evidence.

## Current Priority Order

### P0 — Must Have for MVP
- [x] Define and lock product guardrails and core curriculum model — policy is recorded in `docs/product-decisions.md`; the curriculum hierarchy is represented in `app/src/main/java/com/example/physiapp/data/model/CurriculumModels.kt`.
- [x] Finalize MVP feature list for learning and training loops — scoped in [MVP](mvp.md).
- [ ] Define the canonical domain model and API contracts for users, curriculum, exercises, programs, progress, workout logs, weekly goals, and deload cycles — **In Progress:** Android models and repositories exist; formal backend/API contracts remain undefined.
- [ ] Build the backend foundation for authentication, PostgreSQL persistence, API validation, and environment configuration — **Backlog:** the proposed PostgreSQL/API foundation is not implemented; the current Android app uses Firebase Auth/Firestore.
- [ ] Build offline-first mobile persistence and idempotent outbox synchronization — **In Progress:** local `SharedPreferences` persistence exists, but there is no durable sync outbox; some profile/workout writes go directly to Firestore.
- [ ] Build a versioned content pipeline that validates the Git-backed curriculum and publishes app-ready bundles — **In Progress:** bundled lesson Markdown, an asset manifest and Firestore indexing source exist; authored-only eligibility, revision/schema validation and controlled publishing remain pending. Epic 6 CM-01/CM-02 cover index/migration integrity; publishing requires admin approval of the revision, encompassing required reviews and rights clearance.
- [ ] Connect authored YAML/Markdown lessons to Android runtime loading and verify formatted, accessible reading — **In Progress:** asset-backed loading, formatted rendering and heading semantics exist in source; authored-record parity, metadata validation and Android validation remain pending. See [Content Delivery](infrastructure-plan.md#content-delivery) and [Epic 6](epics.md#epic-6-content-maintenance-and-migration).
- [ ] Create the curated MVP exercise library with stable IDs, movement tags, coaching cues, substitutions, and media references — **In Progress:** an inline exercise catalog includes IDs, patterns, cues and media URLs; explicit substitutions and verified source/license/curation records remain incomplete.
- [ ] Build progression map state model and daily node logic — **In Progress:** sequential lesson selection, completion state and map UI exist against the asset-backed catalog; authored-only eligibility and native validation remain pending.
- [ ] Build workout logging and weekly goal tracking — **In Progress:** workout logging and goal counters exist in the app; week-boundary behavior and end-to-end validation remain open.
- [ ] Build curriculum and daily learning flow — **In Progress:** asset-backed lessons, knowledge checks, local completion/streak logic and cloud lesson-progress source exist; authored-source parity and end-to-end Android/Firebase validation remain pending, and no optional reading entries are currently attached to lessons.
- [ ] Implement deload cycle logic — **In Progress:** week-six state, UI and cycle controls exist; native behavior and edge-case validation remain pending.
- [ ] Extend and validate the existing Kotlin/Compose Android app shell and mobile-first UI — **In Progress:** the Android app and screens exist; Gradle/build, device and formal QA/accessibility evidence are not recorded here.
- [ ] Build companion web dashboard for review and progress visibility — **Backlog:** future web technology remains undecided and no web implementation is present.
- [ ] Define QA and accessibility validation gates — **In Progress:** standards/checklists are documented; formal review and automated enforcement are not established.
- [ ] Establish CI/CD, development/staging/production environments, secrets management, backups, crash reporting, and baseline product analytics — **Backlog:** these remain proposed; no active CI workflow is recorded.

### P1 — Important Next
- [ ] Deliver lesson feedback and admin publication approval under [Epic 6: Content Maintenance and Migration](epics.md#epic-6-content-maintenance-and-migration) — **Backlog:** revision-linked feedback and authorized **Approve / Reject / Needs improvement** decisions; approval encompasses required reviews/rights clearance and permits publication. CM-03/CM-04 below require admin-access and feedback-privacy decisions before implementation.
- [ ] Adaptive split engine v1 — **Backlog:** no adaptive recommendation engine found; readiness check-in advice is not split adaptation.
- [ ] Analytics instrumentation and reporting — **In Progress:** event/KPI definitions exist in [event-kpi-spec.md](event-kpi-spec.md), but app event emission, analytics integration and reporting were not found.
- [ ] Repo maintenance and technical hygiene workflow — **In Progress:** shared guidance and maintenance standards exist; recurring automated native validation/enforcement is not configured.
- [ ] Validate lesson and optional-reading favorites — **In Progress:** local persistence, Saved UI, and the optional-reading reader path were added in commit `46d47a5`; unit and Compose tests were authored but not run because Gradle/the wrapper were unavailable. The current curriculum has no optional-reading entries yet. Acceptance: run Android checks, verify add/remove/reopen for both item types, link optional readings through approved content work, and confirm favorites do not affect progression or streaks.
- [ ] Build a provider-neutral media catalog with source, revision, license, creator, attribution, approval, and expiration metadata — **Backlog:** current exercise media URLs do not provide this provenance/approval model.
- [ ] Add a curated Wikimedia API ingestion pilot for lesson reference media — **Backlog:** no Wikimedia ingestion implementation found.

### P2 — Near-Term Enhancements
- [ ] More advanced personalization logic — **Backlog:** no personalization engine found; keep behind reliable usage signal and approved product scope.
- [ ] Expanded curriculum sequencing and modular learning tracks — **In Progress:** authored curriculum and indices are expanding, and Android source loads an 83-entry asset catalog; entries do not prove authored/reviewed status, and full authored-corpus coverage and native validation remain pending.
- [ ] Scale-phase infra and observability improvements — **Backlog:** no scale-phase service or observability implementation found.

### Deferred — Not in MVP
- [ ] Social encouragement flows and friend streak nudges
- [ ] Milestone sharing and social distribution surfaces
- [ ] Additional sharing and engagement features
- [ ] User-generated content management and moderation workflows
- [ ] Advanced mechanics curriculum depth
- [ ] Pathology and rehab-specific curriculum depth

## Epic 6 — Content Maintenance and Migration

Planning only; detailed requirements and acceptance criteria live in the [epic specification](epics.md#epic-6-content-maintenance-and-migration). Points are provisional relative estimates for owner review, not delivery commitments. No story is Done without acceptance evidence.

| ID / priority | Story | Acceptance slice | Suggested owner / points | Status / dependency |
|---|---|---|---|---|
| CM-01 / P0 | As a maintainer, I want an authored-only inventory and metadata contract so that real lessons are traceable and scaffolds are excluded. | All authored lessons, including drafts; stable ID, placement/objective, lifecycle/revision, authoritative source and relative asset pointer; explicit eligibility and discrepancy report. | Content Strategist + Backend Engineer / 3 | Backlog; existing MVP content-pipeline scope |
| CM-02 / P0 | As a maintainer, I want lesson metadata reconciled into Firebase so that each authored lesson resolves to its local asset without losing maintenance records. | Full eligible coverage; no scaffolds/duplicates; asset and metadata/answer-key parity; repeat runs preserve feedback and review evidence; report missing/changed/retired assets. | Backend Engineer + Android Developer + QA Engineer / 5 | In Progress for basic index source only; CM-01 and authorized index-write policy required for acceptance |
| CM-03 / P1 | As an authorized admin, I want to approve, reject or request improvements so that my decision controls publication of the reviewed revision. | Unreviewed default; admin-only decisions with revision/reviewer/time, actionable rejection/improvement notes and supporting review/rights evidence; Approve authorizes publication without a separate clearance gate; other decisions block new publication; changed revisions need renewed approval. | Backend Engineer + Content Strategist + QA Engineer / 5 | Backlog; CM-01/CM-02, admin-role/access decision and content-review alignment |
| CM-04 / P1 | As a learner, I want to send lesson feedback so that maintainers can improve confusing content. | Lesson/revision, feedback signal/time, privacy-approved optional comment; success/failure state; authorized maintainer access; no exposure of others' identity or completion/streak/goal side effects. | Android Developer + Backend Engineer + QA Engineer / 5 | Backlog; CM-01/CM-02, feedback format and Legal Compliance privacy/retention approval |

**Smallest shippable increment:** CM-01 + CM-02 with inventory/parity and permission evidence. Then CM-03 and CM-04 as V1 slices. Existing authenticated-client writes to the shared index are not an accepted admin-authorization model; validate access, preservation and revision identity in every slice.

**Scope boundary:** this update changes plans only, not app code, Firebase rules/schema, assets or curriculum. Legacy lessons/progress may remain replaced in the current merge, as owner-approved; no legacy-data migration or future silent reset is introduced. Open handling of already-published content and feedback-policy decisions is recorded in the epic.

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
