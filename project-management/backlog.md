# PhysiApp Backlog

Use [shared guidance](../AGENTS.md) and the [development workflow](../docs/development-workflow.md). Unchecked items are pending scope/validation, not implemented features; update status only with acceptance evidence.

## Current Priority Order

### P0 — Must Have for MVP
- [ ] Define and lock product guardrails and core curriculum model
- [ ] Finalize MVP feature list for learning and training loops
- [ ] Define the canonical domain model and API contracts for users, curriculum, exercises, programs, progress, workout logs, weekly goals, and deload cycles
- [ ] Build the backend foundation for authentication, PostgreSQL persistence, API validation, and environment configuration
- [ ] Build offline-first mobile persistence and idempotent outbox synchronization
- [ ] Build a versioned content pipeline that validates the Git-backed curriculum and publishes app-ready bundles
- [ ] Create the curated MVP exercise library with stable IDs, movement tags, coaching cues, substitutions, and media references
- [ ] Build progression map state model and daily node logic
- [ ] Build workout logging and weekly goal tracking
- [ ] Build curriculum and daily learning flow
- [ ] Implement deload cycle logic
- [ ] Extend and validate the existing Kotlin/Compose Android app shell and mobile-first UI
- [ ] Build companion web dashboard for review and progress visibility
- [ ] Define QA and accessibility validation gates
- [ ] Establish CI/CD, development/staging/production environments, secrets management, backups, crash reporting, and baseline product analytics

### P1 — Important Next
- [ ] Adaptive split engine v1
- [ ] Analytics instrumentation and reporting
- [ ] Repo maintenance and technical hygiene workflow
- [ ] Build a provider-neutral media catalog with source, revision, license, creator, attribution, approval, and expiration metadata
- [ ] Add a curated Wikimedia API ingestion pilot for lesson reference media
- [ ] Complete a MuscleWiki API commercial-license and product-fit spike

### P2 — Near-Term Enhancements
- [ ] More advanced personalization logic
- [ ] Expanded curriculum sequencing and modular learning tracks
- [ ] Scale-phase infra and observability improvements
- [ ] Add the official MuscleWiki API through the backend provider gateway if the license, cost, and editorial review gates are approved

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
