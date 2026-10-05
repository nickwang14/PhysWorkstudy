# PhysiApp Workflow and Execution Rules

## Core Operating Model
This project runs with a lightweight planning discipline. The markdown planning files are the source of truth for project rhythm.

## Before Every Major Work Session
Use the project review checklist before starting large work.

Checklist tasks:
- confirm priority
- confirm owner
- note changed assumptions
- inspect risk and blockers
- confirm project-management docs reflect current reality

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

### 5. Review
After implementation, the PM and relevant specialists review:
- scope fit
- quality
- risk
- measurement
- CI status: required checks (format, lint, typecheck, tests, content validation) are green before merge
- accessibility: any UI change has Accessibility Specialist sign-off, not just an automated pass

### 6. Rebalance
If the task changed direction, backlog and decision log are updated before further work proceeds.

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
