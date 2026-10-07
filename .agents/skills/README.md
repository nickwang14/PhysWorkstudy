# Shared Skills — Single Source of Truth

Author skills only here. [textbook-learning-material](textbook-learning-material/SKILL.md) provides focused source review, curriculum comparison and material inclusion. Reusable executable code lives in [tools](../../tools/README.md).

## Platform Discovery

| Client | Discovery | Repository setup |
|---|---|---|
| Codex CLI/IDE | Native `.agents/skills/` scanning | None for skills; read root `AGENTS.md` |
| Current VS Code/GitHub Copilot CLI | Supports `.agents/skills/` | Native path; `.github/skills/<name>` aliases also cover clients using that path |
| Claude Code | Project `.claude/skills/<name>` | Run `python tools/setup_agent_platforms.py` after cloning; each native entry links to the canonical folder |
| Other Agent Skills clients | Client-specific locations/imports | Point the client at this directory or package/import the canonical skill; do not maintain divergent copies |

The setup command creates ignored discovery symlinks (Windows junction fallback, no elevation). It also generates `.github/agents/*.agent.md` adapters from native `.claude/agents/` profiles. `--check` validates aliases and adapter freshness without writes. Commit canonical skills/profiles and generated adapters; aliases are local filesystem setup, so run the command in each checkout, including cloud containers before launch when applicable.

Use only standard portable skill frontmatter (`name`, `description`, and optional standard metadata). Client-specific invocation/tool features must not redefine the workflow or grant new permissions. A native agent's allowed tools remain client-specific; profiles are not universally portable merely because skills are.

Use each client's skill/agent list to confirm loading, and restart/reload after adding a previously absent skills directory. Repository setup does not automatically upload skills to account libraries. Git records configuration history; this README describes current setup only.

Client documentation: [VS Code](https://code.visualstudio.com/docs/copilot/customization/agent-skills), [Copilot CLI](https://docs.github.com/en/copilot/how-tos/copilot-cli/customize-copilot/create-skills), [Claude Code](https://code.claude.com/docs/en/skills), [Codex](https://learn.chatgpt.com/docs/build-skills). Discovery behavior remains version-specific.