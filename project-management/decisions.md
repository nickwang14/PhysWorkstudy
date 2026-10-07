# PhysiApp Decisions

## Decision Log

Product policy is maintained only in [docs/product-decisions.md](../docs/product-decisions.md), including PD-001–PD-008. The former summaries here are replaced by that reference to avoid policy drift. This log records technical/workflow decisions and open implementation issues, not new product PDs.

### WF-001 - Shared policy, canonical roles and skill consolidation
- **Date:** 2026-10-06
- **Status:** Validated workflow configuration; shared skill discovery, profile adapters and capability boundaries are regression-tested. See [consistency audit](consistency-audit.md) for runtime verification limits and remaining decisions.
- **Decision:** [Root AGENTS.md](../AGENTS.md) carries shared repository policy. Canonical roles remain in `.claude/agents/*.agent.md`; `.github/agents` profiles are generated Copilot adapters, with equivalent capabilities and unchanged role restrictions.
- **Shared skill:** `.agents/skills/textbook-learning-material/SKILL.md` merges focused textbook parsing with indexed retrieval, source review and inclusion. Retire `.claude/skills/textbook-parsing` and `.github/skills/textbook-learning-material` rather than maintaining competing copies.
- **Role configuration:** Use Claude-native tool lists in canonical profiles. Preload `textbook-learning-material` for Content Strategist, Physio Consultant, Plan and Design, and QA Engineer. Preserve read-only Physio/Legal and non-execution content-role boundaries; hand extraction/validation to execution-capable owners.
- **Rationale:** Remove duplicate references and platform-specific tool drift without changing product policy. Domain, editorial and rights approval require recorded evidence; role labels and passing tests are not approval.

### IMPL-001 - Android implementation direction resolved by PD-008
- **Date:** 2026-10-06
- **Status:** Resolved by [PD-008](../docs/product-decisions.md#pd-008-kotlin-app-and-vs-code--google-ai-studio-workflow), following owner confirmation on 2026-10-06; Kotlin/Compose is chosen as the current Android direction.
- **Evidence:** `app/build.gradle.kts` uses Android/Kotlin Compose plugins and dependencies; `MainActivity.kt` launches Compose UI; `UserPreferencesRepository.kt` persists preferences with Android SharedPreferences. The owner confirms Kotlin/Compose as the implemented app and current development direction.
- **Resolution:** Extend the existing Kotlin/Compose Android app under PD-005's Android-primary/web-companion policy. The Android technology choice is resolved; future web-companion technology remains undecided. Backend/cloud/provider services in `infrastructure-plan.md` remain proposals, not implementation evidence.
- **Workflow:** The owner orchestrates in VS Code and builds/implements in Google AI Studio. Android Developer prepares bounded Kotlin/Compose handoffs and reviews returned changes under the [development workflow](../docs/development-workflow.md); no autonomous connector, remote execution or successful build is inferred. Validate with the actual Gradle/Android project when its toolchain is available.
- **Still open:** Launch authentication methods, week start, exact streak/grace behavior, offline workout edit conflict rules and MVP web-settings writes remain listed for decision in the infrastructure plan. Audience/business-model hypotheses remain assumptions unless decided by product authority. Do not settle these through agent defaults.

## Process
- Add technical/workflow decisions here; record product decisions in `docs/product-decisions.md` and link them instead of copying their policy.
- Date and label each decision clearly.
- Keep decision rationale concise and explicit.