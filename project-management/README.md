# PhysiApp Project Management

This folder is the repository's lightweight, Notion-style project operating system.

## Purpose
Use these markdown files to manage:
- Product roadmap
- Epics and user stories
- Task ownership
- Priorities and trade-offs
- Major decisions
- Project health across phases
- Deferred curriculum additions and later content work

## Files
- `roadmap.md` — milestones and phases
- `backlog.md` — current work queue, priorities, and ordering
- `epics.md` — epic breakdowns, objectives, and owners
- `decisions.md` — technical/workflow decisions and references to authoritative product decisions
- `mvp.md` — MVP scope, priorities, and exit criteria
- `definition-of-done.md` — feature readiness and release criteria
- `event-kpi-spec.md` — required analytics and KPI definitions
- `content-operations.md` — curriculum publication workflow
- `workflow.md` — execution rhythm and operating model
- `review-checklist.md` — pre-work and milestone review checklist
- `risk-register.md` — triggers and response rules
- `curriculum-backlog.md` — deferred curriculum topics and later content additions
- `infrastructure-plan.md` — recommended stack, system boundaries, integrations, and delivery sequence
- `consistency-audit.md` — agent/content/tooling alignment, validation evidence and remaining decisions

## Operating Rules
1. Treat this folder as the living task/delivery guidance, subject to the authority hierarchy below.
2. Update it before and after major work, not only after completion.
3. Every epic or task needs an owner and acceptance criteria.
4. Every major scope change needs a decision entry.
5. Keep the backlog prioritized and concise.
6. Before large tasks begin, record the minimal decomposition plan.
7. Park non-MVP ideas explicitly instead of letting them drift into active scope.

## Authority and Shared Customizations
- Read [root AGENTS.md](../AGENTS.md) for shared repository policy before role-specific work.
- [Product decisions](../docs/product-decisions.md) are the product authority. This folder translates those decisions into tasks and delivery guidance; do not copy product policy into a competing decision log.
- [Decisions](decisions.md) and [workflow](workflow.md) guide technical/workflow choices and execution. Raise conflicts explicitly rather than silently overriding product authority.
- Canonical role profiles stay in `.claude/agents/*.agent.md`. `.github/agents` profiles are generated Copilot platform adapters, not independently maintained roles; preserve role restrictions when translating tools.
- Shared skills live in `.agents/skills/`; use the canonical [textbook-learning-material skill](../.agents/skills/textbook-learning-material/SKILL.md). The former `.claude/skills/textbook-parsing` and `.github/skills/textbook-learning-material` copies are retired by the consolidation, not alternative authorities.

## Current Implementation and Future Proposals
The implemented app is native Android Kotlin/Jetpack Compose: `app/build.gradle.kts` enables Compose, and `app/src/main/java/com/example/physiapp/MainActivity.kt` launches the Compose UI. Current preference persistence uses `UserPreferencesRepository.kt` and Android SharedPreferences; this does not establish the proposed backend or offline synchronization as implemented.

[PD-008](../docs/product-decisions.md#pd-008-kotlin-app-and-vs-code--google-ai-studio-workflow) records the owner's 2026-10-06 confirmation: Kotlin/Compose is the current app direction, and the Android technology choice is resolved. PD-005 retains Android-primary/web-companion boundaries. [Infrastructure planning](infrastructure-plan.md) separates the actual Android stack from proposed backend/cloud services and a future read-oriented web companion whose technology remains undecided.

The owner orchestrates conversations, agents, review and Git in VS Code and builds/implements the app with Google AI Studio. Follow the [development and handoff workflow](../docs/development-workflow.md): prepare bounded Kotlin/Compose tasks, review returned changes here, and record only checks actually run. This user-provided working context does not imply autonomous AI Studio integration or build evidence.

Audience segmentation and business-model hypotheses must be labeled as assumptions where undecided. Launch authentication methods, week start, streak/grace boundaries, offline edit conflict rules and MVP web-settings write scope remain open in the infrastructure plan; do not promote role defaults or prototype behavior into policy.

## Status Model
Use these labels in the backlog and roadmap:
- `Backlog`
- `Ready`
- `In Progress`
- `Blocked`
- `Done`
- `Deferred`

## Working Rhythm
- Rebalance the backlog before starting major work
- Shorten scope when a task expands beyond the original goal
- Check project state before every significant execution session
- Keep planning docs lean and explicit; avoid endless commentary

## Minimum Project Discipline
- No unowned work
- No undocumented major decision
- No oversized tasks without decomposition
- No roadmap drift without revision notes