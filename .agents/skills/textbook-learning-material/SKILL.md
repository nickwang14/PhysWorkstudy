---
name: textbook-learning-material
description: 'Use when indexing or comparing textbook PDFs with curriculum lessons, finding learning material or curriculum gaps, drafting source-grounded lessons/readings, reviewing useful graphics, or maintaining chapter/section/page and curriculum-use indices. Consult maintained indices, read targeted source pages, preserve rights and review boundaries, and update the relevant content/usage records. Not a general app implementation workflow.'
---

# Textbook and Curriculum Learning Material

Input: source/topic, target curriculum path or lesson ID, and desired output (gap analysis, lesson, study brief, glossary cards, optional reading or figure-study prompt). Commands run from the repository root with the configured Python environment. Source PDFs remain local-only.

Use focused curriculum comparison and indexed retrieval; never parse an entire book for a small lesson request.

## Required References

- [Canonical curriculum workflow](../../../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md): read **Content Indexing, Textbook Parsing, and Inclusion**.
- [Shared content-role rules](../../../.github/instructions/textbook-content.instructions.md) and [approval lifecycle](../../../project-management/content-operations.md).
- [Source registry](../../../theory-and-knowledge/knowledge/textbook-indices/README.md), [A&P guide](../../../theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e/README.md), [reviewed graphics](../../../theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e/graphic-review.md), and [curriculum usage map](../../../theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e/curriculum-usage.md).
- [Bounded page reader](../../../tools/read_pdf_pages.py), [graphics preview tool](../../../tools/review_textbook_graphics.py), [tool responsibilities](../../../tools/README.md), and [learning brief template](./assets/learning-brief-template.md).

## 1. Locate Existing Content and Scope the Request

Read the required process, relevant chapter/subchapter guides, lesson frontmatter, reading registry and source curated/usage records. Preserve stable lesson IDs, progression and unrelated content. Distinguish textbook chapter numbers from curriculum chapter numbers; authored lessons from outlines; core-source consultation from assigned optional readings; and proposed graphics from included assets.

For A&P, search saved metadata with `python tools/index_anatomy_physiology.py --query "synovial"` or a stable ID such as `ap-figure-9.10`. Use Key Terms, Contents/bookmarks, the alphabetical Index and References to navigate. An index/summary is a locator, not proof of a factual claim.

## 2. Check Edition, Navigation and Rights

Verify A&P freshness with `python tools/index_anatomy_physiology.py --check-source`. If a PDF is missing, metadata remains searchable but source reading/extraction is blocked; report its expected local path. If the fingerprint changed, regenerate metadata and mark prior locations/credit/review decisions stale until checked.

For a new/unindexed source, inspect title/edition/ISBN and the local license notice; locate Contents, bookmarks, glossary/Key Terms, index, references, appendices, objectives, summaries, question banks, answer keys and interactive links. Record present/absent/unverified aids. Verify PDF/printed numbering against actual footers and detect numbering changes; never assume A&P's +16 offset or that navigation lies entirely in the first 10–20 pages. OCR/empty-text pages require visual review. A&P's full-book indexer is edition-specific, not a generic adapter for other books.

Check the local edition's license and item-specific exceptions before reproduction. Supplied A&P is CC BY-NC-SA 4.0 with required notice **Access for free at openstax.org.** Preserve attribution, NC/SA restrictions and third-party credits; do not automatically clear a commercial build, copy chapters/question banks/glossaries, or assume an older edition's rights apply.

## 3. Read a Small Source Selection and Compare

Read exact sections plus necessary qualifications and boundary paragraphs. For a bounded selection use `python tools/read_pdf_pages.py --pdf-pages "346-350" --layout`; `--metadata-only` validates without printing text. It checks the indexed fingerprint, accepts inclusive one-based PDF viewer pages, and reads at most 12 distinct pages to stdout without exporting files/images. Another indexed source requires explicit `--pdf` and `--index` paths with the documented metadata schema.

Compare the source with existing lessons: what is covered, what is missing, what is a misconception, and what belongs in a later chapter/backlog? Add only high-value content that improves the specific learner objective. Do not treat a source's topic depth as permission to broaden a foundation gate; log deferred concepts in the [curriculum backlog](../../../project-management/curriculum-backlog.md).

Inspect rendered pages for multi-column order, equations, tables, panels/labels and captions. If visual inspection is unavailable, keep rendered review pending. Record exact edition, sections, inclusive PDF/printed spans, partial-page boundaries, exclusions and unresolved questions.

## 4. Review Graphics Separately

Start with the human-owned `graphic-review.md`, not just generated caption locators. Prefer labelled teaching diagrams over context/decorative photos. Preview a selected batch with `python tools/review_textbook_graphics.py --ids ap-figure-9.8 ap-figure-9.19 --extra-pages 366`; `--dry-run` validates without writing. It checks the fingerprint, caps batches at 32 pages and writes full-page PNGs/contact sheets/manifest only to ignored `docs/graphics-review/`. Cache files may be replaced by later batches; keep lasting evidence in the review catalogue/log.

Visually inspect each selected graphic, artwork page, complete caption/continuation, panels, orientation, graph axes/units, credit and crop boundaries. Caption and artwork pages can differ. Consult the source's visual-review catalogue for item-specific page/credit findings; keep unresolved rights panel-specific.

Maintain distinct states: **located → visually inspected → intended-use rights assessed → cropped/exported → domain/editorial approved**. Rendering alone is not inspection; inspection is not permission. No visible separate credit is not public-domain/commercial clearance. For actual requested extraction, approve rights first, preserve necessary panels/caption/attribution, record precise crop/conversion and meaningful alt text in `assets/README.md`, and validate final-size readability. Never commit whole-page review previews, trace artwork as “original,” or inject suggested graphics automatically.

## 5. Compile Material in the Correct Layer

Use the brief template when helpful. Write original teaching: observable objective, prerequisite terms, linked concepts, worked movement example, misconception correction, application and independently checked questions. Maintain a claim→source record. Follow the canonical daily-lesson depth; a short card is not the whole daily lesson.

Learner-facing optional readings link to `optional-readings/{lesson_id}-optional.md`, not ignored PDFs. Keep attributed verbatim extracts separate from original explanations; never label paraphrase as source text. Retain license/credits, extraction status, actual source-word/time estimate and `required_for_gate: false`, `include_in_texting_curriculum: false`. Optional material cannot supply required check answers or gate/streak prerequisites.

`python tools/extract_optional_readings.py --dry-run` validates assignments; repeated `--lesson-id` flags select focused lessons for validation or regeneration. An unfiltered write regenerates all authored assignments: do not overwrite reviewed extracts casually. Full-page machine extraction does not honor every partial-page/skip instruction; compare with the rendered source, trim unrelated content and preserve attribution before approval.

## 6. Maintain Records and Validate

Update lesson/guide links, reading/asset registries and source curated/visual-review records when material changes. Keep current source fingerprint, exact locations/IDs, destinations, review/rights evidence and unresolved return tasks. Record dates/reviewers where needed to establish actual approval or source review, not as a journal of routine tool runs. Git records edit history; never invent approval or erase required credits/evidence. Read-only/non-executing agents hand off edits/commands rather than widening permissions.

Generated `index.json`, `chapters.md` and `graphics.md` refresh with `python tools/index_anatomy_physiology.py --write` when needed; human records are preserved. Continued table labels retain their pages in `caption_occurrences`; unlabelled artwork needs a manual discovery record.

After reading-assignment changes, refresh the separate reverse map with `python tools/index_curriculum_usage.py --write` and verify with `--check`. It covers linked optional-reading metadata only, not every core-source consultation or proposed graphic.

Validate local links, stable IDs, page bounds/maps, fingerprint, credits, optional flags and honest review states. Run `python -m unittest discover -s tools -p "test_*.py"` after tooling changes. Keep source content draft until the actual source/rendered, domain, editorial and intended-use rights checks are complete.

## Output and Source-Return Rules

Report gap analysis (covered/missing/deferred), files changed or edits handed off, exact source locations, records updated, checks actually performed, pending approvals and next source-return trigger. Reopen the textbook for a new mechanism/number, disputed definition, graph/table interpretation, ambiguous boundary, incomplete caption, unknown rights, conflicting notes or changed edition. Follow references/additional evidence if the book cannot answer the question; log the gap rather than guessing.