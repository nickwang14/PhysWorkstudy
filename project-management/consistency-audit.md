# Agent, Content and Tooling Consistency Audit

**Date:** 2026-10-06. Scope: role profiles, project-management guidance, major learning/workout READMEs, skills and content tools. This is not clinical, legal, publication or production approval.

## Confirmed Context and Authority
- [PD-008](../docs/product-decisions.md) confirms the Kotlin/Jetpack Compose Android app, VS Code orchestration and Google AI Studio app-building workflow.
- Root [AGENTS.md](../AGENTS.md) makes that context available to all roles; [development workflow](../docs/development-workflow.md) defines handoff/returned-source review and evidence boundaries.
- Product policy stays in `docs/product-decisions.md`; project-management decisions/workflow and role defaults do not create competing policy.

## Consistency Changes
- Canonical roles use native Claude tools; generated Copilot adapters preserve capability restrictions. Android Developer owns the current app role; domain/legal reviewers remain read-only.
- One authored skill/template in [.agents/skills](../.agents/skills/README.md), with local ignored discovery aliases rather than competing skill copies.
- Shared [content helpers and CLI responsibilities](../tools/README.md): paths/frontmatter/fingerprints/page validation are deduplicated; targeted extraction flags and task-specific parsers are preserved.
- Major READMEs distinguish implemented code, proposed folders/services/schemas, authored drafts/outlines and actual review states. Reading assignments, suggested graphics and included assets remain separate.
- Learning tracking uses program/chapter/subchapter IDs; grace/time/qualification and workout week-boundary proposals remain unconfirmed until decided.
- Essential MVP scope and dual-track/consistency-first boundaries remain intact; no application source or source-reading publication state changed in this cleanup.

## Remaining Decisions
- Audience/pricing assumptions; authentication, privacy, timezone/week/streak qualification and offline conflict rules.
- Future companion technology/settings writes and proposed backend/provider/hosting choices.
- Item-specific redistribution rights and actual domain/editorial approval; indexed/rendered content is not cleared for publication.
- Production credential packaging: current Gradle BuildConfig fields are client-visible. An ignored `.env` protects Git storage, not a compiled client secret; DevOps/Architect security review remains required.

## Validation and Maintenance
- The expanded tooling suite passed **55 tests**; use `python -m unittest discover -s tools -p "test_*.py"`.
- `python tools/setup_agent_platforms.py --check` verifies canonical skill aliases, adapter freshness and native capability translation. Run setup after cloning or canonical role changes.
- Copilot CLI listed the single shared project skill. Claude Code/Codex were not installed here, so runtime loading there is not claimed; see the [compatibility matrix](../.agents/skills/README.md).
- Source/map/reading checks and local links were verified; no source excerpts or app builds were regenerated. Android/AI Studio/device validation needs actual evidence in the appropriate workspace.
- Authoring counts change; use the live [reading registry](../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/reading-options.md) rather than frozen snapshots.
- Local PDFs, full-page previews, aliases and `.env` stay uncommitted. Unrelated editor settings and unregistered images are excluded from the requested consistency/agent commits.
- The owner requested focused commits and push after validation; actual commit hashes/results belong to the Git history and final handoff, not assumed success in this report.