# PhysiApp Workflow and Execution Rules

## Core Operating Model
This project runs with a lightweight planning discipline. Read [shared repository policy](../AGENTS.md) first. The markdown planning files guide project rhythm and tasks; [product decisions](../docs/product-decisions.md) remain the product authority.

## Before Every Major Work Session
Use the project review checklist before starting large work.

Checklist tasks:
- confirm priority
- confirm owner
- note changed assumptions
- inspect risk and blockers
- confirm project-management docs reflect current reality
- apply PD-008: Kotlin/Compose is the implemented Android app and owner-confirmed current direction; extend the existing app through the VS Code orchestration / Google AI Studio handoff
- distinguish implemented Android source from proposed backend/services and the future web companion's undecided technology

## Project Workflow

### 1. Intake
New ideas, tasks, and opportunities are captured in backlog or decisions.

### 2. Triage
The PM agent decides:
- is this MVP / V1 / V2?
- does it fit the core product thesis?
- what is the smallest useful version?

### 3. Planning
Large tasks are broken into:
- epic
- user story or technical task
- owner
- acceptance criteria
- dependencies
- validation approach

### 4. Execution
Work proceeds in small, reviewable slices. All engineering work follows
`project-management/engineering-standards.md` (linters, formatters, tests, coverage) and
`project-management/ci-cd.md` (pipelines, branch protection, environment promotion).

For app work, follow the owner-confirmed [VS Code / Google AI Studio handoff](../docs/development-workflow.md):
1. In VS Code, Android Developer prepares a bounded Kotlin/Compose task with target files, current state/APIs, constraints, acceptance criteria and validation requirements.
2. The owner builds/implements in Google AI Studio; this is a user-mediated workflow, not an autonomous integration or evidence of an agent-run build.
3. Review returned source/build changes in VS Code against the task and product decisions; preserve unrelated work, IDs, namespaces and private configuration.
4. Run available Gradle/Android checks and record exact results. Missing SDK/toolchain or device access is a blocker, not a passing build; content-tool checks do not validate the app.
5. Coordinate focused commits and push only when requested and authorized; report completed Git operations separately from pending ones.

### 5. Review
After implementation, the PM and relevant specialists review:
- scope fit
- quality
- risk
- measurement
- CI status: required checks (format, lint, typecheck, tests, content validation) are green before merge
- accessibility: any UI change has Accessibility Specialist sign-off, not just an automated pass
- content changes: require the source/index handoff and actual review evidence below; tests or role labels do not substitute for domain, editorial or rights approval

### 6. Rebalance
If the task changed direction, backlog and decision log are updated before further work proceeds.

## Content Index Handoff and Checks
Follow the [curriculum indexing/inclusion process](../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md#content-indexing-textbook-parsing-and-inclusion), the canonical [textbook-learning-material skill](../.agents/skills/textbook-learning-material/SKILL.md) and [content operations](content-operations.md); this workflow coordinates handoffs rather than repeating that full process.

- The author/editor hands off changed lesson/reading/asset IDs and paths, exact source edition/fingerprint, section/figure IDs, PDF and printed pages, partial boundaries, registry/usage updates, pending approvals and unresolved source-return questions.
- Physio can review sources and return dated findings read-only. An authorized editor records the review; an execution-capable owner handles requested extraction/rendering and checks. Include reviewer, date, scope and evidence, not merely an assigned role name.
- After reading-assignment changes, the execution owner refreshes the reverse usage map with `tools/index_curriculum_usage.py --write`, verifies `--check`, and runs the applicable `test_index_*.py` suite using the configured Python environment. Report only checks actually run; preserve human-owned curated/usage/visual-review records.
- QA checks stable IDs, links, source/page mappings, rights/credits and honest inclusion states. Confirm optional content stays out of required checks, gates, streak conditions and texting exports; required questions must remain answerable from the standard lesson alone.
- Record automation results separately from domain, editorial and rights approvals. Unresolved evidence, permissions or approval gaps remain pending/blocking as applicable; a passing index check cannot publish content.

## Workflow Rules
- No large implementation without a decomposition plan
- No unowned task
- No feature without reason or KPI
- No scope expansion without decision logging
- No product drift without backlog update

## Collaboration Model
- PM owns roadmap and prioritization
- Domain specialists own product quality and safety
- Engineers own implementation and validation
- Repository Maintainer keeps the repo lean and clean
- QA owns release confidence and enforces CI quality gates as merge blockers
- Solutions Architect + Repository Maintainer own `engineering-standards.md`; DevOps Engineer owns `ci-cd.md` and day-2 platform operations (no separate infra-manager role exists at MVP scale)

This keeps the project organized without becoming a heavy process.
