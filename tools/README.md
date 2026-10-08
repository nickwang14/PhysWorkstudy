# Content and Agent Tools

Firestore maintenance is a separate Node workflow: see [Firebase local development](../docs/firebase-local-development.md). `firestore_admin.mjs` manages exact `users` / `curriculum_lessons` documents with emulator defaults, dry-run writes and explicit live-target confirmation. Run `npm.cmd test` for its offline tests; it is not an app backend service or curriculum-content publishing tool.

`google_services_config.mjs` imports authentic Android config into ignored `.env`, generates ignored `app/google-services.json` from the tracked placeholder template, and checks freshness without printing values. Use `npm.cmd run firebase:config:import`, `npm.cmd run firebase:config`, and `npm.cmd run firebase:config:check`; see [config template workflow](../docs/firebase-and-keys.md#local-environment--client-config-template-workflow). Generation is explicit, not a Gradle hook. Tests use synthetic credentials only.

Run from the repository root using the configured Python environment. Install `requirements-content.txt` for PDF reading/rendering. Curriculum-map and agent-setup operations use the standard library. `content_common.py` holds shared validation; each CLI owns its task-specific parsing/output.

| Entry point | Single responsibility | Safe inspection / selected work |
|---|---|---|
| `index_anatomy_physiology.py` | A&P-specific full-book metadata adapter | `--query`, `--check-source`; default validates a scan; `--write` refreshes generated metadata |
| `index_biomechanics.py` | Biomechanics-specific chapter, section and caption-line locator index | `--query`, `--check-source`; default validates a scan; `--write` refreshes generated metadata |
| `index_curriculum_usage.py` | Lesson-linked A&P readings → curriculum reverse map | `--check`; `--write` refreshes only the map, no PDFs needed |
| `read_pdf_pages.py` | At most 12 selected PDF pages → stdout | `--pdf-pages "350" --metadata-only`; no files/images exported |
| `review_textbook_graphics.py` | Selected source pages → ignored local visual previews | `--ids ap-figure-9.8 --dry-run`; 32-page batch cap, not final assets or visual/rights approval |
| `extract_optional_readings.py` | Attributed draft excerpts from lesson assignments | `--lesson-id planning-05-01 --dry-run`; repeat IDs to scope a write; unfiltered writes affect every authored assignment |
| `setup_agent_platforms.py` | Skill discovery aliases and Copilot profile adapters | `--check`; default configures aliases/refreshes adapters, refuses nonempty real skill directories |
| `content_common.py` | Shared paths, scalar frontmatter, fingerprints and page validation | Library, not another CLI; task-specific parsers remain in their tools |

Natural-language assignment ranges, excerpt headings and CLI page selections have different semantics; keep their parsers separate. The 12/32 page caps are intentional. Source attribution belongs in the extraction source registry, not duplicated into skills. Git records tool history; routine validation does not need an additional activity log.

Test with `python -m unittest discover -s tools -p "test_*.py"`. PDF integration checks may skip when local sources are absent; configure skill aliases before platform discovery checks.

**No tool result alone clears publication.** Never commit full-page previews or source PDFs, silently erase reviewed text, or treat parsing/rendering as scientific/editorial/rights approval. See [shared policy](../AGENTS.md) and the [content workflow](../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md).