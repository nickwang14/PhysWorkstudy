---
description: 'Use the lesson-indexing, textbook-parsing and inclusion workflow for education, curriculum and content authoring/review; maintain source references, indices, usage records and unresolved source-return questions.'
applyTo: 'theory-and-knowledge/knowledge/**/*.md,workout-programming/knowledge/**/*.md,project-management/content-operations.md,project-management/curriculum*.md,tools/*reading*.py,tools/index_*.py'
---

# Source-Grounded Learning Content

## Required Process Reference for Education and Content Work

Before planning, authoring, compiling or reviewing curriculum content, read **Content Indexing, Textbook Parsing, and Inclusion** in the [curriculum README](../../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md). This is the canonical process for lesson records, source parsing, graphics review, inclusion rules, index refreshes and source-return triggers. Use the single shared [textbook-learning-material skill](../../.agents/skills/textbook-learning-material/SKILL.md) for focused source comparisons, retrieval, review and inclusion; follow [content operations](../../project-management/content-operations.md) for approval and [AGENTS.md](../../AGENTS.md) for policy precedence and platform setup.

Apply the relevant responsibilities without treating a role label or automated check as evidence of completed expert review:
- **Education/curriculum/content author or Content Strategist:** preserve lesson IDs, placement and ordered guide links; connect claims, optional readings and proposed/included graphics to exact sources; update reading/asset registries and actual usage records alongside content changes.
- **Physio Consultant/domain-review role:** read the cited source material, verify claims and qualifications, check learner scope and safety, and record exact evidence and unresolved questions. Do not mark source, domain or rights review complete merely because material is indexed or machine-extracted.
- **Curriculum QA/review role:** verify lesson/reading IDs and links, page mappings, inclusion flags, credits and honest review states; check the reverse usage map is current and required checks remain answerable from standard lessons alone.

After reading-assignment changes, refresh the A&P reverse map with `python tools/index_curriculum_usage.py --write`; use `--check` to verify freshness. The map reflects **linked optional readings**, not all core-lesson source consultation or suggested graphics. Keep those other uses in the appropriate human-maintained records. Report changed content/index paths, validation actually performed, pending approvals and explicit source-return questions in the handoff.

## Source and Inclusion Rules

- Start source-backed content work at `theory-and-knowledge/knowledge/textbook-indices/README.md`; consult the relevant chapter/section index, graphics inventory, curated guide and usage log before searching the whole PDF.
- Use the canonical `.agents/skills/textbook-learning-material/SKILL.md` for textbook comparison, indexing, retrieval, extraction and learning-material compilation. A&P has a full index; Exercise Science has a focused partial index, while full indices for the other sources remain queued.
- Indices locate evidence, not replace it. Check the source fingerprint and read exact local textbook pages before adding factual claims, resolving terms, extracting figures/passages or interpreting quantitative material. Use chapter Key Terms, the book Index and References when needed.
- Record both one-based PDF viewer and printed pages, section/graphic IDs, partial-page boundaries and destination lesson/reading IDs. Navigation spans can overlap; never treat them as automatically approved extraction ranges.
- Update current source-use, curated and visual-review records when material or approval changes; keep exact evidence, credits and unresolved return questions. Git stores routine edit/tool-run history; do not create an activity entry for every operation or imply unperformed review.
- `index.json`, `chapters.md` and `graphics.md` are generated. Regeneration must preserve current human review/rights evidence; a changed PDF makes prior references stale until revalidated.
- Keep PDFs in `theory-and-knowledge/knowledge/textbooks/` local-only. Respect edition/item-specific rights, required attribution, NC/SA terms and existing content operations. Draft extraction and indexed graphics are not publication or commercial-reuse clearance. Preserve optional-content flags and avoid changes to unrelated lessons.