# PhysiApp Project Review Checklist

Use this before every major work session or milestone review.

## 1. Priority Check
- [ ] What is the single highest-priority objective right now?
- [ ] What has changed since the last review?
- [ ] What should be deferred or narrowed to keep scope lean?
- [ ] Are any active tasks blocked by missing decisions or ownership?

## 2. Ownership and Scope
- [ ] Every active task has a clear owner
- [ ] Every task has an explicit objective
- [ ] The current scope matches the intended MVP / V1 / V2 slice
- [ ] No work is drifting into unrelated product territory

## 3. Epic and Story Health
- [ ] Epics remain grounded in product value
- [ ] Stories are split into small, testable chunks
- [ ] Acceptance criteria exist for the work being started
- [ ] Dependencies are visible and not implicit

## 4. Decision Hygiene
- [ ] Approved product policy is in `docs/product-decisions.md`; active specs/backlog contain relevant acceptance criteria and open choices, while Git records routine history
- [ ] Product decisions align with core principles: consistency over intensity, flexible schedules, academic grounding
- [ ] Platform boundary is respected: Android primary, web companion only

## 5. Repository and Technical Health
- [ ] The repo is not showing unreviewed drift or confusion
- [ ] PD-008's current Kotlin/Compose Android direction is applied and distinguished from proposed services and undecided future web technology
- [ ] Owner-mediated VS Code / Google AI Studio changes follow the [development handoff workflow](../docs/development-workflow.md), with actual build/test evidence and toolchain blockers recorded rather than inferred
- [ ] Large tasks have a logic plan before implementation begins
- [ ] Helper utilities or shared abstractions are considered before duplicate logic grows
- [ ] Refactors are narrow and justified
- [ ] Required CI checks pass per `project-management/engineering-standards.md` (format, lint, typecheck, tests, coverage) before merge
- [ ] Pipeline and environment changes follow `project-management/ci-cd.md` (no direct production changes without staging validation)

## 6. Risk Review
- [ ] Safety and medical boundary risks are reviewed where relevant
- [ ] Accessibility is not being deferred without explicit decision
- [ ] QA and validation steps are planned before implementation
- [ ] Any regulatory or compliance concerns are surfaced early

## 7. Feature Risk Watchlist
- [ ] No social feature is added before the core learning/training loop is stable
- [ ] Personalization is not overbuilt before there is reliable usage signal
- [ ] Web scope stays within companion/dashboard boundaries
- [ ] Curriculum breadth does not outpace validation of completion and retention
- [ ] Learning and training goals remain separate signals and are not blended accidentally
- [ ] New features map to a measurable KPI or explicit product reason
- [ ] The backlog is pruned regularly so low-signal ideas do not accumulate

## 8. Next Action Selection
- [ ] The next 1–3 actions are the smallest meaningful set
- [ ] Work is ordered to reduce risk and unblock dependencies
- [ ] No effort is being spent on low-value polishing before core tasks are clear

## 9. Final Signoff
- [ ] The plan is written down in the project-management files
- [ ] The next actions are shared with the relevant agent(s)
- [ ] The project is calm, focused, and ready for execution

## Default Rule
If a task is large, unclear, or expanding, stop and ask: "What is the smallest next step that reduces risk and creates forward motion?"
