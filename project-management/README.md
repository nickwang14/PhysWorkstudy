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
- `mvp.md` — MVP scope, priorities, and exit criteria
- `definition-of-done.md` — feature readiness and release criteria
- `event-kpi-spec.md` — required analytics and KPI definitions
- `content-operations.md` — curriculum publication workflow
- `workflow.md` — execution rhythm and operating model
- `review-checklist.md` — pre-work and milestone review checklist
- `risk-register.md` — triggers and response rules
- `curriculum-backlog.md` — deferred curriculum topics and later content additions
- `infrastructure-plan.md` — recommended stack, system boundaries, integrations, and delivery sequence

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
- [Workflow](workflow.md) guide technical/workflow choices and execution. Raise conflicts explicitly rather than silently overriding product authority.
- Canonical role profiles stay in `.claude/agents/*.agent.md`. `.github/agents` profiles are generated Copilot platform adapters, not independently maintained roles; preserve role restrictions when translating tools.
- Shared skills live in `.agents/skills/`; use [textbook-learning-material](../.agents/skills/textbook-learning-material/SKILL.md) for content work. Run `python tools/setup_agent_platforms.py` after canonical agent/skill changes.

## Active Context and Records
Use root `AGENTS.md` and the [development workflow](../docs/development-workflow.md) for the Kotlin/Compose app and VS Code/AI Studio handoffs. [Infrastructure planning](infrastructure-plan.md) identifies proposed services and open choices; do not infer implementation or approval from a plan.

Keep current priorities, owners and acceptance evidence in the backlog/specs. Product policy belongs in `docs/product-decisions.md`; routine changes and past workflows belong in Git history, not parallel audit/decision logs. Preserve necessary review/rights evidence and unresolved decisions without recording every work session.

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