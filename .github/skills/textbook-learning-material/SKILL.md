---
name: textbook-learning-material
description: 'Use when finding, indexing or extracting textbook PDF learning material; drafting lessons, study briefs, glossary cards or figure-study prompts; checking A&P anatomy and physiology references; or maintaining chapter, subchapter, graphics, glossary and source-page indices. Consult maintained indices first, read targeted local source pages, verify rights and boundaries, and update usage and source-return records.'
argument-hint: 'Textbook/topic + target lesson or output (for example: A&P synovial joints, study brief)'
---

# Textbook Learning Material

Turn indexed source material into focused, usable learning resources with traceable evidence. This is a repository skill; commands below run from the repository root. Source PDFs are local-only. Never mistake an index or summary for source evidence.

## Resources

- [Canonical lesson-indexing, textbook-parsing and inclusion process](../../../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md) — read the **Content Indexing, Textbook Parsing, and Inclusion** section.
- [Shared education/content-role instructions](../../instructions/textbook-content.instructions.md)
- [Source registry](../../../theory-and-knowledge/knowledge/textbook-indices/README.md)
- [A&P reference guide](../../../theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e/README.md)
- [A&P visually reviewed graphics](../../../theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e/graphic-review.md)
- [Local graphic-preview renderer](../../../tools/review_textbook_graphics.py)
- [Content/rights workflow](../../../project-management/content-operations.md)
- [Bounded source-page reader](./scripts/read_pdf_pages.py)
- [Learning-brief template](./assets/learning-brief-template.md)

## Procedure

### 1. Select the objective and inspect existing indices

For education, curriculum, content-authoring or review tasks, first read the canonical process linked above. Follow its lesson-index ownership, material-inclusion rules and role-appropriate review/handoff requirements; do not treat these as optional textbook-only conventions.

Identify source, learner level, curriculum chapter/subchapter, objective and intended output: original brief/lesson, glossary cards, diagram-study prompts, a comparison table, or an explicitly licensed excerpt. Infer these from the task where possible; ask only about material ambiguity. Start with the source registry, relevant `chapters.md`, `graphics.md`, `curated-guide.md` and `usage-log.md`. Distinguish **textbook** chapter IDs from **curriculum** chapter IDs.

For A&P, search without loading the whole book: `python tools/index_anatomy_physiology.py --query "synovial"` or `--query "ap-figure-9.10"`. Use chapter Key Terms and the alphabetical Index to resolve terms or cross-chapter topics. Search curated notes/logs separately; generated JSON does not contain human review decisions.

### 2. Check edition, navigation and rights

For A&P, run `python tools/index_anatomy_physiology.py --check-source`. If the PDF is missing, report the exact expected local path; metadata remains usable but source claims/extraction are blocked. If the fingerprint differs, regenerate and mark prior curated/usage references stale until checked. No credentials or environment variables are required.

When indexing a new book: inspect title/edition/ISBN, copyright/license notice, PDF bookmarks and Contents; locate glossary/Key Terms, index, references, appendices, objectives, summaries, question banks, answer keys and interactive links. Record present/absent/unverified aids and exact PDF/printed locations. Verify printed-page mapping from footers at multiple points and detect numbering changes; do not blindly apply A&P's +16 offset or PDF page labels. OCR/scanned pages require an explicit workflow and visual checks; never claim full extraction coverage from empty text. Use a source-specific adapter and preserve human-owned files.

Before reproduction, check this **local edition's** license and item-specific credits. A&P's supplied edition is CC BY-NC-SA 4.0; retain required attribution and ShareAlike, inspect third-party exceptions, and do not clear a commercial build automatically. Indexing a figure does not approve its reuse. Do not bulk-copy chapters, question banks, glossary definitions or images into the app.

### 3. Read a narrow selection of source pages

Select exact sections and candidate figure IDs from the index. Its navigation spans overlap on shared heading pages; inspect actual text boundaries before selecting an excerpt.

For a small A&P selection use `python .github/skills/textbook-learning-material/scripts/read_pdf_pages.py --pdf-pages "350"` (synovial-joint figure), or `--pdf-pages "346-350" --layout` (nearby explanation). The bundled reader:

- Verifies the supplied PDF against its index fingerprint before reading.
- Accepts one-based **PDF viewer** pages; emits PDF and mapped printed references with source/rights metadata.
- Reads at most 12 distinct pages per invocation, writes no files, exports no images, and marks text as machine-extracted/review-required.
- Supports another indexed source with explicit `--pdf` and `--index` paths using the same source metadata schema. Without a verified index, build one first.

Read surrounding qualifications where needed, not just a caption/summary. Inspect the rendered page in a PDF viewer for figures, multi-column text, tables, equations, panel labels, credits and reading order. If visual inspection is unavailable, explicitly leave `rendered_review: pending`; do not claim visual verification or extract/publish the figure.

#### Graphics-focused requests

Start with `graphic-review.md` when available; it overlays human visual/credit decisions on the generated inventory and is not overwritten by indexing. Select a bounded topic batch, prioritize labelled diagrams over decorative/context photos, and check the actual learner objective before proposing an asset.

Install `tools/requirements-content.txt` in the selected Python environment. Render with `python tools/review_textbook_graphics.py --ids ap-figure-9.8 ap-figure-9.19 --extra-pages 366`; add `--dry-run` to validate without images. The renderer verifies the source fingerprint and limits a batch to 32 pages. It creates full-page PNGs, labelled contact sheets and a manifest **only in ignored `docs/graphics-review/`**, not approved curriculum assets. Individual page previews retain their rendering resolution; contact sheets are browsing aids, not mobile-readability evidence. The cache manifest/sheets can be overwritten by another batch, so record lasting evidence in the catalogue/log.

Inspect every selected figure visually, zooming to an individual page or rerendering at higher resolution if needed. Read the complete caption and neighboring pages; captions may continue without a repeated FIGURE label (A&P 9.12 needs PDF 354–355). Check labelled panel coverage, view orientation, arrows, graph axes/units and third-party credits. Record actual artwork pages separately from caption pages, teaching priority, crop/panel guidance, alt-text draft, lesson matches and limitations. Rendering alone must not set `visual_review: complete`.

Maintain distinct statuses: **caption located → visually inspected → credit/rights assessed for intended use → cropped/exported → domain/editorial approved**. No separately visible credit is not public-domain/commercial clearance. Keep photograph and micrograph rights holds panel-specific; A&P 6.2 credits Benjamin J. DeLong and 6.12(b) credits the Regents of University of Michigan Medical School. Do not bulk-extract these or erase credits when cropping. For actual asset extraction, confirm rights, record precise crop coordinates/conversion, preserve caption/attribution and validate the final-size result. Update `graphic-review.md`, `curated-guide.md` and `usage-log.md` with the real work performed, leaving unreviewed items explicitly pending.

### 4. Compile usable material

Use the bundled learning-brief template at a task-appropriate location (normally beside the relevant curriculum lesson, as a draft). Do not create a parallel learner path unintentionally. Write **original explanations**: a precise objective, prerequisite terms, a few key concepts, a worked movement example, a misconception, an application prompt and independently checked questions. Add an internal claim→source map with exact sections/pages. A glossary card needs a learner-friendly definition plus source location and context, not a copied glossary entry.

For a figure-study prompt, specify stable ID, exact artwork/caption pages, which panels/labels to examine, teaching question and expected observation. Keep a link to the local source in internal material rather than copying the artwork. If an actual asset is requested and rights are verified, extract only the chosen figure (not an entire page), preserve all needed panels/credits, record crop/conversion and alt text in the curriculum asset registry, and leave publication subject to review.

For existing optional reading assignments, follow `tools/extract_optional_readings.py`: `--dry-run` validates assignments; the default regenerates **all** assigned extracts, so do not run it for a single-topic request or overwrite reviewed extracts casually. Its full-page machine extraction does **not** honor every partial-page/skip instruction automatically. Inspect first/last headings, trim neighboring content and preserve attribution/flags before approval. Never present paraphrased text as a verbatim extract. Learner-facing files link to prepared local readings, not ignored PDFs; source PDF links belong in internal reference material.

### 5. Update the maintained indices in the same task

Add human discoveries to `curated-guide.md`: useful subsection/box/graphic, stable ID, tags/teaching purpose, exact PDF and printed pages, evidence status, rights status and source-return reason. Record actual consultation/output in `usage-log.md`: date, source fingerprint, reviewer, section/figure IDs, inclusive pages, partial boundaries/exclusions, destination content ID/path, review status, uncertainty and next action. Add or resolve outstanding return questions with dated evidence; do not silently erase history.

The generated A&P files (`index.json`, `chapters.md`, `graphics.md`) are machine-owned. Refresh with `python tools/index_anatomy_physiology.py --write` only when necessary; it preserves curated notes/logs. Continued table labels are listed under `caption_occurrences`; inspect all pages and locate the actual table start above the label. Unlabelled graphics need manual discovery entries, not an invented auto-detected ID.

After changing a lesson-linked reading assignment, refresh the separate A&P `curriculum-usage.md` reverse map with `python tools/index_curriculum_usage.py --write`, then check freshness with `--check`. It indexes actual optional-reading metadata only: record core-lesson consultations, suggested graphics and included assets separately rather than presenting them as assigned use.

### 6. Validate and report

Verify page bounds, printed/PDF mapping, fingerprint, stable IDs, local links, partial-page boundaries and the claim→source map. Run `python -m unittest discover -s tools -p "test_index_*.py" -v` after index/tool changes. Keep material draft until source/rendered, domain, editorial and rights checks are complete. Optional readings stay `required_for_gate: false` and `include_in_texting_curriculum: false`.

Report the created/updated files, source sections/pages used, what is checked versus pending, and the next source-return trigger. If no usable source is found, log the gap and identify which textbook navigation aid, reference or additional source should be checked next instead of guessing.

## Mandatory Source-Return Triggers

Reopen the textbook before a new mechanism or quantitative claim, terminology resolution, graph/table interpretation, figure extraction, correction or scope expansion; when a boundary is ambiguous; when notes conflict; when page/item rights are unknown; or when the edition fingerprint changes. An existing usage record supports only the checked objective—not blanket approval of a chapter. Follow references or additional evidence when the textbook cannot answer the question safely.