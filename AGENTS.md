# Shared Agent Guidance

## Working Context
- PhysiApp is a **Kotlin/Jetpack Compose Android app** in `app/`, built with Gradle.
- The owner orchestrates conversations, agents, review and Git in **VS Code**, and builds/implements the app with **Google AI Studio**. Follow the [development workflow](docs/development-workflow.md); do not imply automated integration or claim unperformed builds/tests.

## Authority and Current State
- [Product decisions](docs/product-decisions.md) own product policy. [Project management](project-management/README.md) owns execution/workflow records; agent defaults never override either. Record unresolved decisions instead of inventing policy.
- Future web/backend proposals are not implemented features. Web-companion technology and other open choices require explicit approval.
- Learning and training are independent signals. Apply PD-001 through PD-008; keep optional readings outside required checks, gates, streak requirements and texting. Fixed deload cadence is product policy, not a universal physiological guarantee.
- Keep educational material non-diagnostic and honest about limits. Publication/domain/rights approval needs actual recorded evidence, not an agent title, index entry or passing test.

## Shared Skills and Profiles
- Author skills only in [.agents/skills](.agents/skills/README.md). Use [textbook-learning-material](.agents/skills/textbook-learning-material/SKILL.md) for curriculum/source comparison, graphics, indexing and inclusion.
- Canonical specialist profiles live in `.claude/agents/` with native Claude tool names. `.github/agents/` contains generated Copilot adapters, not separately edited roles. Read-only reviewers hand edits/execution to authorized roles.
- After cloning or changing a canonical profile/skill, run `python tools/setup_agent_platforms.py`; verify with `--check`. This creates local ignored skill-discovery aliases and refreshes generated Copilot profiles without copying skill content. Never request elevation; Windows junction fallback is supported.
- Client discovery/import requirements are documented in the shared skills README. Confirm runtime loading in the client actually used; local setup does not configure account uploads automatically.

## Content Workflow and Handoffs
- For education/content work, read **Content Indexing, Textbook Parsing, and Inclusion** in the [curriculum README](theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md), [content-role instructions](.github/instructions/textbook-content.instructions.md), and [content operations](project-management/content-operations.md).
- Start with maintained source/lesson indices, then inspect relevant source pages. Preserve stable IDs, exact PDF/printed pages, captions/panels/credits and source fingerprints. Distinguish consultation, optional assignments, proposed graphics and included assets.
- Preserve user edits, reviewed readings, source credits and approval evidence. PDFs, previews, build outputs, caches and private/local configuration stay local and ignored; source, tests, approved assets and current process documents belong in Git.
- Update current reading/asset/source-use records and reverse maps when material changes. Keep unresolved approvals and source-return questions visible; do not journal every parse, test or index refresh.

## Repository Maintenance
- Git history records routine changes and past workflows. Keep documentation focused on current policy, active work and necessary source/approval evidence; do not recreate deleted audit logs or duplicate decision histories.
- Keep `PhysiApp_installer.zip` tracked at the repository root as the owner's workflow installer. Other build outputs/caches remain local-only; do not relocate, ignore or untrack the installer during cleanup.
- Remove ignored tracked files from the index only, preserving local copies. Do not rewrite history, commit local artifacts or delete unrelated user work during cleanup.

## Validation
- Content tools share `tools/content_common.py`; retain task-specific parsers and CLI contracts. Do not regenerate whole books or all readings for a small focused request.
- Run `python -m unittest discover -s tools -p "test_*.py"`, `python tools/setup_agent_platforms.py --check`, and relevant metadata/index freshness checks. Tests validate tooling/records, not scientific or legal approval.
- Use platform-specific build/test commands only for implementations actually present. Include missing runtime/client dependencies and policy decisions as explicit blockers.