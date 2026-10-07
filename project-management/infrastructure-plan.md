# PhysiApp Infrastructure and Implementation Plan

## Product Goal

PhysiApp is a consistency-first fitness and human kinetics learning app. Its core product is a curated, physio-reviewed workout program paired with a structured learning path.

The product has two related but independent loops:
- daily learning: short lessons, knowledge checks, chapter gates, and a learning streak
- weekly training: flexible workout goals, workout logging, program guidance, and planned deload weeks

Android is the primary action surface, implemented in Kotlin/Jetpack Compose. A future read-oriented web companion supports progress review, curriculum browsing, and limited account or program settings; its technology is undecided. Gamification rewards participation and consistency, never intensity.

**Current context:** [PD-008](../docs/product-decisions.md#pd-008-kotlin-app-and-vs-code--google-ai-studio-workflow) confirms Kotlin/Compose as the implemented Android app and current direction. The owner orchestrates in VS Code and builds/implements in Google AI Studio through the [development handoff workflow](../docs/development-workflow.md); no autonomous integration or successful build is implied. Backend, synchronization, cloud hosting and provider-gateway plans below remain proposed until implemented and verified.

## Proposed Service Architecture Principles

1. The backend owns canonical progression, weekly goal, deload, and program state.
2. Android writes locally first and synchronizes through an idempotent outbox.
3. Web has no workout, lesson-completion, or progression-map mutation paths.
4. The curated curriculum and exercise library remain the source of truth.
5. Third-party APIs provide reviewed supplemental material and never become required for a core user flow.
6. Published content and approved media are versioned so clients can cache them safely.
7. Start with a modular monolith and managed services; do not introduce microservices for MVP.

## Current Android Stack and Proposed Services

| Layer | Implementation / proposal | Status and purpose |
|---|---|---|
| Android client | Kotlin / Jetpack Compose / Android Gradle project | Implemented in `app/`; current app direction |
| App state | AndroidX lifecycle / ViewModel Compose dependencies | Present in Gradle; preserve and review existing state ownership |
| Navigation | AndroidX Navigation Compose | Present Android dependency; future web routing undecided |
| Local data | Android SharedPreferences for current preferences | Implemented preference persistence; durable workout cache/outbox design remains to be selected and implemented |
| HTTP client | OkHttp | Present Android dependency; typed API contract integration remains proposed |
| Web companion | Technology undecided | Future read-oriented surface; no assumed shared client framework |
| Backend | Node.js LTS, TypeScript, Fastify | Proposed modular REST API, validation, progression logic and provider gateway |
| API contract | OpenAPI 3.1 | Proposed contract for Android, future web, backend tests and documentation |
| Database | Managed PostgreSQL on Supabase | Proposed relational source of truth, migrations, backups and read projections |
| Authentication | Supabase Auth | Proposed identity service with backend token verification |
| Object storage | Supabase Storage | Proposed versioned first-party and approved reusable media |
| Background jobs | pg-boss | Proposed content builds, media ingestion, attribution checks and retryable jobs |
| Content source | Git-backed Markdown plus validated manifests | Existing authoring/records; runtime bundle publication remains proposed |
| Web hosting | Cloudflare Pages | Proposed candidate, subject to the future web technology decision |
| API and worker hosting | Render | Proposed managed deployment for the modular API and worker |
| Product analytics | PostHog | Proposed MVP events, funnels and retention |
| Error monitoring | Sentry | Proposed Android and backend crash/error reporting |
| CI/CD | GitHub Actions | Proposed Android checks, tests, builds and service deployments; not evidence of live workflows |
| Secrets | GitHub environments plus host secret stores | Proposed deployment configuration; production provider secrets must not ship in clients |

Reconsider a visual CMS only when non-technical publishing volume makes the Git workflow a demonstrated bottleneck. Add Redis, a separate queue service, and distributed tracing only after measured load requires them.

## Proposed System Boundaries

```text
Kotlin/Compose Android -- planned local store/outbox --\
                                                       >-- Proposed Fastify API -- PostgreSQL
Future web (stack TBD) -- read-oriented requests ------/                        -- Object storage
                                                                                -- Background worker
```

The proposed Android/web integration routes credentialed supplemental-provider requests through a backend provider gateway to protect credentials, normalize data, and isolate provider failures. This is not current implementation evidence: the Android app's ExerciseDB BuildConfig credential packaging still requires review; an ignored `.env` does not protect a secret compiled into a client.

## Proposed Backend Modules

- identity and user settings
- curriculum catalog and published content bundles
- lesson, knowledge-check, and gate progression
- exercise library and curated workout programs
- workout logging and weekly goal evaluation
- deload scheduling and acknowledgement
- adaptive split decisions with user-facing explanations
- mobile bootstrap and synchronization
- web dashboard read projections
- media catalog, provenance, attribution, and approval
- provider gateway and ingestion jobs
- analytics event validation

## Core Data Domains

Define stable IDs and schemas for:
- users, devices, timezones, and preferences
- programs, chapters, subchapters, lessons, prerequisites, and content versions
- lesson attempts, knowledge checks, gate progress, and learning streaks
- exercises, movement patterns, equipment, substitutions, routines, and program templates
- program enrollment, weekly goals, sessions, workout logs, and exercise entries
- cycle weeks, deload decisions, and adaptive split recommendations
- sync mutations, server cursors, versions, and rejection reasons
- media assets, source provenance, attribution, approval, and retention rules

Use client-generated UUIDs for offline mutations and workout logs. Store the user's IANA timezone and the event's local date so later timezone changes do not rewrite historical streak or weekly-goal boundaries.

## Content Delivery

Keep lesson authoring in the existing repository for MVP:

1. Validate front matter, stable IDs, hierarchy, prerequisites, references, and publish state in CI.
2. Compile approved Markdown into immutable, versioned JSON content bundles.
3. Publish bundles and media to object storage.
4. Have Android cache bundles in the selected native persistence layer and request updates by version or ETag; the cache/outbox implementation is not yet established by current preference storage.
5. Project the same published content through read-only web endpoints.

This separates editorial source files from the runtime format while preserving review history.

## External API Strategy

## Delivery Sequence

These are planned delivery slices, not a claim that services, web or CI already exist. Extend the current Android app rather than scaffold a replacement framework; use the owner-mediated AI Studio handoff and repository review for app changes.

### Stage 0 - Decisions and contracts
- lock MVP scope and platform write boundaries
- approve domain vocabulary and stable identifiers
- define timezone, week-boundary, streak, deload, and conflict rules
- approve privacy, analytics, and third-party content policies
- write OpenAPI contracts and database migration conventions

### Stage 1 - Platform foundation
- extend and validate the existing Kotlin/Compose Android shell and design system
- scope the read-oriented web companion separately and decide its technology before implementation
- provision development, staging, and production environments
- implement authentication and backend authorization
- create PostgreSQL schema and migration pipeline
- select and implement native Android persistence, mobile bootstrap and outbox synchronization
- establish CI/CD, backups, Sentry, PostHog, and secrets management

### Stage 2 - Vertical learning slice
- compile and publish a small curriculum bundle
- deliver one chapter path with lessons and a knowledge check
- persist completion, gate state, and learning streak
- render progress in Android and the read-only web dashboard

### Stage 3 - Vertical training slice
- publish the canonical MVP exercise library and starter program
- configure a weekly goal and log workouts offline
- calculate weekly progress and the six-week deload cycle
- render training progress in Android and web

### Stage 4 - Combined journey and hardening
- connect both loops to the progression map without merging their success signals
- test timezone, offline retry, duplicate mutation, missed-day, rest-day, and deload edge cases
- complete accessibility, privacy, disclaimer, backup-restore, and release checks
- validate MVP KPIs with an internal or closed test cohort

## Decisions Required Before Implementation

- future web-companion implementation technology
- native Android durable workout/content storage and sync-outbox design
- final MVP curriculum slice and starter workout-program slice
- supported authentication methods at launch
- account deletion, export, and retention policy
- Monday-only versus user-configurable week start
- exact streak boundary and grace behavior
- conflict rules for edits to offline workout logs
- whether web settings are writable or strictly read-only for MVP
- approved Wikimedia license allowlist and attribution presentation
- hosting regions and data residency requirements

## Explicitly Deferred

- microservices and Kubernetes
- Redis and a separate queue cluster
- advanced machine-learning personalization
- real-time subscriptions for all state
- user-uploaded media and moderation
- a full visual CMS
- public milestone pages and social features
