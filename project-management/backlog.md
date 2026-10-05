# PhysiApp Backlog

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
- [ ] Implement Android app shell and mobile-first UI
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

## External API Acceptance Criteria

### Wikimedia API ingestion pilot
- Owner: Backend Engineer + Content Strategist; Legal Compliance reviews the license policy.
- Depends on: versioned content pipeline and provider-neutral media catalog.
- Query Wikimedia Commons through official APIs from a backend ingestion job, not directly from the app.
- Limit the pilot to reviewed Public Domain, CC0, and CC BY assets; require legal approval before accepting ShareAlike content.
- Persist source URL, revision, creator, license, attribution text, retrieval date, checksum, and editorial approval.
- Copy approved reusable media to controlled storage and serve it through the app media layer; a Wikimedia outage must not break a published lesson.
- Validate the pilot with a small set of MVP lessons before broad ingestion.

### MuscleWiki API feasibility and integration
- Owner: Product Manager + Solutions Architect; Legal Compliance approves terms before implementation.
- Depends on: canonical exercise library and provider-neutral media catalog.
- Confirm the official paid plan supports the required exercises, routines, workouts, body maps, languages, request volume, and commercial use.
- Record the recurring cost, quota behavior, attribution language, caching limits, termination risk, and fallback behavior.
- Use only the official API; do not scrape MuscleWiki or use an unofficial scraped API.
- Keep the API key server-side. Use the backend proxy and short-lived media tokens required for mobile playback.
- Do not permanently store or re-host MuscleWiki videos. Respect provider-specific metadata and image cache limits.
- Require internal physio/editorial approval before provider content is mapped into a curated program.
- The core workout, learning, logging, and progression flows must continue to work when MuscleWiki is unavailable.