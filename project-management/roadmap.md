# PhysiApp Roadmap

## Product Vision
Build a consistency-first workout and human kinetics learning app that rewards showing up, teaches safely, and adapts to flexible schedules.

## Phases

### Phase 0 — Product Framing and Planning
- Finalize product thesis and guardrails
- Set curriculum structure and training model
- Set decision log and project management operating rules
- Define MVP scope and platform split

### Phase 1 — MVP Foundation
- Learn and training progression map
- Workout logging and weekly goal tracking
- Learning daily streak and knowledge checks
- Authored-only lesson metadata index, local asset pointers and migration/parity checks — [Epic 6](epics.md#epic-6-content-maintenance-and-migration), CM-01/CM-02; extends existing content-delivery work and excludes scaffolds
- Deload scheduling and adaptation logic
- Android-first app shell and core flows
- Basic web companion dashboard

### Phase 2 — V1 Expansion
- Stronger personalization and content modules
- Progress analytics and retention dashboards
- Social encouragement flow and milestone sharing
- Improved custom workout configuration and quality loops
- Lesson-specific user feedback and authorized admin publication approval — [Epic 6](epics.md#epic-6-content-maintenance-and-migration), CM-03/CM-04; Approve / Reject / Needs improvement, with approval encompassing required reviews and rights clearance
- Accessibility and QA hardening

### Phase 3 — Scale and Optimization
- Additional personalization logic
- Better analytics and recommendations
- More advanced program creation and curriculum sequencing
- Optimization for retention and long-term engagement

## Current Phase
`Phase 0 / Phase 1` — product planning and foundation delivery/validation continue alongside the implemented Kotlin/Compose Android app. This phase label does not imply that the app is absent or that all MVP features are complete.

**Owner-confirmed context (2026-10-06):** [PD-008](../docs/product-decisions.md#pd-008-kotlin-app-and-vs-code--google-ai-studio-workflow) confirms Kotlin/Compose as the current Android direction. Extend the current app using the [VS Code orchestration / Google AI Studio handoff](../docs/development-workflow.md). Future web technology is undecided and backend/cloud/provider plans remain proposed; phase bullets are delivery targets, not implementation or build evidence.

**Content maintenance plan (2026-10-08):** Epic 6 adds authored-only indexing/migration integrity to foundation work, then V1 feedback and revision-specific admin approval. Admin approval is the final authorization to publish and encompasses required reviews and rights clearance, not a separate clearance gate. Asset-backed curriculum and Firestore indexing/progress exist in source; authored-only coverage, admin approval and feedback are not accepted implementations. This is a plan update only, not a deployment or approval of existing content.

## Guidelines
- Keep scope lean and focused on the core differentiators
- Protect the learning + training separation
- Avoid intensity-based rewards or pressure-heavy mechanics
- Build Android-first; treat web as a companion dashboard
