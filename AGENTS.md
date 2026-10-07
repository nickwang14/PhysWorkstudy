# Shared Agent Guidance

## Owner-Confirmed Working Context
- **App implementation:** PhysiApp is already built in **Kotlin with Jetpack Compose**. This is the current app/Android direction, confirmed by the owner on 2026-10-06; use the implemented Android stack for app work.
- **Orchestration:** The owner coordinates conversations, specialist agents, repository review, content work and Git operations in **VS Code**.
- **App building:** The owner builds/implements the app with **Google AI Studio**. Prepare clear Kotlin/Compose implementation handoffs there, then reconcile returned source/build changes with this repository. Do not assume an automated VS Code↔AI Studio integration or claim an AI Studio/Android build was run without actual evidence.
- Read [development workflow](docs/development-workflow.md) for handoff/validation expectations. Keep source, decisions and approvals versioned here; execution happens in the appropriate workspace.

## Authority and Current State
- [Product decisions](docs/product-decisions.md) own product policy. [Project management](project-management/README.md) owns execution/workflow records; agent defaults never override either. Record unresolved decisions instead of inventing policy.
- `app/` and Gradle contain the Kotlin/Compose Android application. **PD-008 confirms the Android technology and working context**; future web/backend proposals are not implemented features or permission to change the Android framework. Web-companion scope remains separate from its undecided implementation technology.
- Learning and training are independent signals. Apply PD-001 through PD-008; keep optional readings outside required checks, gates, streak requirements and texting. Fixed deload cadence is product policy, not a universal physiological guarantee.
- Keep educational material non-diagnostic and honest about limits. Publication/domain/rights approval needs actual recorded evidence, not an agent title, index entry or passing test.

## Shared Skills and Profiles
- Author skills only in [.agents/skills](.agents/skills/README.md). The shared [textbook-learning-material skill](.agents/skills/textbook-learning-material/SKILL.md) replaces both old textbook skills; consult it for curriculum, source comparison, graphics, indexing and inclusion work.
- Canonical specialist profiles live in `.claude/agents/` with native Claude tool names. `.github/agents/` contains generated Copilot adapters, not separately edited roles. Read-only reviewers hand edits/execution to authorized roles.
- After cloning or changing a canonical profile/skill, run `python tools/setup_agent_platforms.py`; verify with `--check`. This creates local ignored skill-discovery aliases and refreshes generated Copilot profiles without copying skill content. Never request elevation; Windows junction fallback is supported.
- Codex and current Copilot clients discover `.agents/skills` natively; the Claude/Copilot legacy locations point to it. Other clients must support the Agent Skills standard or be pointed explicitly to this location. Web/Cowork uploads and unavailable client versions are not automatically configured by repository files.

## Content Workflow and Handoffs
- For education/content work, read **Content Indexing, Textbook Parsing, and Inclusion** in the [curriculum README](theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md), [content-role instructions](.github/instructions/textbook-content.instructions.md), and [content operations](project-management/content-operations.md).
- Start with maintained source/lesson indices, then inspect relevant source pages. Preserve stable IDs, exact PDF/printed pages, captions/panels/credits and source fingerprints. Distinguish consultation, optional assignments, proposed graphics and included assets.
- Preserve user edits, reviewed readings and human usage history. PDFs and full-page previews remain local-only. Verify item-specific licenses; NC/SA material is not automatically commercial-build cleared.
- Update applicable reading/asset/usage records with actual changes; refresh reverse assignment maps after assignment changes. Report changed paths, source IDs/pages, commands actually run, unresolved approvals and source-return questions.

## Validation
- Content tools share `tools/content_common.py`; retain task-specific parsers and CLI contracts. Do not regenerate whole books or all readings for a small focused request.
- Run `python -m unittest discover -s tools -p "test_*.py"`, `python tools/setup_agent_platforms.py --check`, and relevant metadata/index freshness checks. Tests validate tooling/records, not scientific or legal approval.
- Use platform-specific build/test commands only for implementations actually present. Include missing runtime/client dependencies and policy decisions as explicit blockers.