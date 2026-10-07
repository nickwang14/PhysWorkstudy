# Development and Orchestration Context

**Owner-confirmed 2026-10-06; authority: [PD-008](product-decisions.md).**

## Current App and Workspaces

- **App:** Kotlin/Jetpack Compose Android application in `app/`, built around the repository's Gradle configuration. Extend the existing Kotlin/Compose app as the current Android direction.
- **VS Code:** conversation/agent orchestration, requirements, curriculum/source indexing, planning, repository edits/review, validation that is actually available locally, and Git coordination.
- **Google AI Studio:** the owner's app-building/implementation workspace. Prepare implementation context for AI Studio and review returned Kotlin/source/build changes here. This document describes the user's workflow, not a newly installed connector, API automation or remote build service.
- **Future services/web:** backend, provider gateway and read-oriented web-companion plans require separate scoped decisions and implementation evidence. Web technology remains undecided; these plans do not establish implemented services or a shared client codebase.

## Agent Handoff

1. Read root [AGENTS.md](../AGENTS.md), product decisions, the active task and relevant source files. State what is implemented, proposed, pending or unverified.
2. In VS Code, prepare a bounded task for AI Studio: objective, target Kotlin/Compose files/screens, current APIs/state/navigation, source context, constraints, acceptance criteria and relevant checks. Preserve existing IDs, namespaces, unrelated work and safety/content-review boundaries.
3. The owner performs the app-building work in AI Studio. Do not claim that an agent has run that workspace, compiled an APK or validated a device flow unless actual output/evidence is available.
4. Reconcile exported/returned changes with this repository: inspect the diff, compare against product/task decisions, check dependencies/resources/permissions and verify that approved behavior was preserved. Avoid blanket replacement of current project files or private configuration.
5. Run appropriate Kotlin/Gradle/Android checks when the required SDK/toolchain is available; record exact commands/results and remaining AI Studio/device validation. Python content-tool tests do not certify the Android app.
6. Obtain the required domain/editorial/security reviews, then organize focused commits and push only when requested. Git history is the durable record of accepted source and guidance.

## Role Boundaries

- Android Developer owns Kotlin/Compose implementation/review and AI Studio handoff details.
- Solutions Architect and DevOps align interfaces/build/security with the actual Kotlin/Compose Android project and its Gradle configuration. Production provider keys must not be shipped in a client build.
- Content/Physio roles use the canonical curriculum/source workflow. A read-only or non-executing role hands off edits/build commands rather than widening permissions.
- QA distinguishes content/index checks, Android tests, AI Studio build evidence and device/UX validation; report unrun or unavailable checks honestly.

Top-level [CLAUDE.md](../CLAUDE.md) and [Copilot instructions](../.github/copilot-instructions.md) route to the shared context; canonical `.claude/agents` and generated `.github/agents` must stay aligned through `python tools/setup_agent_platforms.py`.