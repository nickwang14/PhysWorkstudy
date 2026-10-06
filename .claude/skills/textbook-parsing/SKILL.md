---
name: textbook-parsing
description: "Use when reviewing PDF textbooks, source books, tables of contents, chapter excerpts, or curriculum gaps. Consults maintained source/curriculum indices, checks navigation and exact source pages, compares findings against existing lessons, and records inclusion/review decisions without parsing entire books by default."
argument-hint: "topic or chapter to review, plus target curriculum path"
user-invocable: true
---

# Textbook Parsing

Use this skill to review source textbooks efficiently before updating PhysiApp curriculum content.

## Required Process References
- Read the **Content Indexing, Textbook Parsing, and Inclusion** section of the [curriculum README](../../../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md) before lesson/source work.
- Follow the [shared education/content instructions](../../../.github/instructions/textbook-content.instructions.md) for role responsibilities, records and approval boundaries.
- Use the [textbook-learning-material skill](../../../.github/skills/textbook-learning-material/SKILL.md) for bounded text retrieval, graphics previews, exact page/credit checks, inclusion rules and validation tools. This skill is the focused comparison/gap-analysis workflow, not a competing content policy.
- Start with the [source registry](../../../theory-and-knowledge/knowledge/textbook-indices/README.md); consult the source's chapter/section index, graphics inventory, curated/visual-review notes and usage log. For A&P, also check its [curriculum usage map](../../../theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e/curriculum-usage.md).

## When to Use
- Reviewing a textbook-backed chapter or subchapter
- Checking whether lesson content is missing key concepts from a source book
- Extracting only the relevant pages for a curriculum topic
- Comparing textbook chapter coverage against existing knowledge files

## Goals
- Avoid parsing entire textbooks by default
- Start from maintained indices; verify table-of-contents/navigation locations when the source is new, stale or insufficiently indexed
- Narrow extraction to the smallest useful page range
- Consolidate findings into curriculum updates or explicit gap notes

## Workflow
1. Read the required process references and identify the source in the maintained registry. The current PDFs are local-only under `docs/`; verify the edition/fingerprint rather than assuming a filename or universal page offset.
2. Consult existing chapter/section, graphics and usage records first. For a new or stale source, locate and inspect front matter, Contents/bookmarks, glossary/Key Terms, alphabetical index and other navigational aids; do not assume they all occur in the first 10–20 pages.
3. Map the request to specific source sections and existing curriculum chapter/subchapter/lesson IDs. Keep actual assignments separate from informal consultations and suggested graphics.
4. Read/extract only the narrow relevant PDF viewer pages and record printed pages and exact partial boundaries. Verify rendered layout, complete captions/continuations and item-specific credits where needed; preserve attribution and rights restrictions before reproducing material.
5. Review the existing curriculum files before editing anything.
6. Compare the curriculum against the extracted textbook concepts.
7. Add only high-value missing concepts, definitions, or learning structure.
8. Follow the current lesson format/depth and inclusion layers: original core teaching, separate attributed optional readings, and only individually cleared graphic assets. Keep checks answerable without optional content; source PDFs/full-page previews never become app assets.
9. Update lesson/reading/asset records and the source's human-owned curated/visual-review notes and usage log alongside actual content changes. Refresh the A&P reverse map after assignment changes, or hand off exact proposed updates/commands if the agent lacks edit/execution permission.
10. Validate links, IDs, source pages, optional flags and review states. Report checks actually performed, pending approvals and specific textbook-return questions; do not count indexing/extraction as source, domain or rights approval.

## Guardrails
- Do not parse entire books unless the user explicitly asks for a full-book pass.
- Prefer chapter titles, section headings, and nearby pages before broad extraction.
- Keep additions academically grounded but concise enough for mobile micro-lessons.
- Preserve the current chapter progression and avoid reorganizing unrelated curriculum files.
- Treat broad field-framing material as support context unless the chapter specifically needs it.
- Do not overwrite human review history during index refreshes or bulk-regenerate reviewed excerpts for a small topic request. Keep NonCommercial/ShareAlike and figure-specific exceptions explicit; source-rights checks remain separate from scientific/editorial review.

## Review Heuristics
- For foundations content, look especially for:
  - anatomical position and directional terms
  - planes, axes, and joint angles
  - joint classes and synovial movement
  - range of motion and segment relationships
  - center of gravity, base of support, balance, and stability
  - muscle tension, load type, and force control
- If the textbook adds detail without improving learner understanding, do not force it into Chapter 1.
- Prefer introducing advanced quantitative material in later chapters unless it is essential for terminology.

## Expected Outputs
- A short gap analysis: what is covered, what is missing, what should wait for later chapters
- Targeted curriculum edits or proposed additions
- Clear citation back to which textbook or section motivated the change
- Exact edition, section/figure IDs, inclusive PDF/printed pages, destination lesson/reading IDs, records updated (or handed off), actual review status and unresolved source-return tasks

## Repo Targets
- Curriculum source: `theory-and-knowledge\knowledge\curriculum\`
- Source textbooks: local-only `docs/*.pdf`; resolve the exact source filename through the registry
- Source indices and review records: `theory-and-knowledge/knowledge/textbook-indices/`
- Product decisions and scope checks: `docs\` and `project-management\`
