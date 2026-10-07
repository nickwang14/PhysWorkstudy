# Claude Entry Point

@AGENTS.md

The owner orchestrates with **VS Code** and builds/implements the **Kotlin/Jetpack Compose Android app** with **Google AI Studio**. Read [development workflow](docs/development-workflow.md) and PD-008 before implementation handoffs.

Use `.agents/skills/` as the single authored skill location. Run `python tools/setup_agent_platforms.py` after cloning to expose the same skill folders through `.claude/skills/` (symlinks or Windows junctions). Canonical agent definitions are in `.claude/agents/`; do not edit generated Copilot adapters.