# Foundations of Movement

## Program Overview
This program introduces the movement foundations that support safe, effective, and consistent training.

## Learning Goals
- Understand the basic principles of movement, stability, and force production
- Learn the key movement patterns used in training and daily life
- Build a vocabulary for exercise selection, progression, and recovery
- Connect learning to realistic training decisions across a flexible weekly schedule

## Curriculum Structure
The plan contains seven foundation chapters (1–7) and five advanced chapters (8–12). Authored drafts and outline-only modules coexist; chapter placement does not imply review or publication. Consult the [chapter overview](chapter-overview.md), chapter/subchapter guides and [reading registry](reading-options.md) for current status.

- Chapter 1: Foundations of Movement and Terminology
- Chapter 2: Anatomy and Physiology of Movement
- Chapter 3: Biomechanics and Motion Analysis
- Chapter 4: Load, Fatigue, and Recovery
- Chapter 5: Weekly Training Planning and Structure
- Chapter 6: Goal-Based Progression and Deloads
- Chapter 7: Applied Exercise Decision-Making and Coaching Logic
- Chapter 8: Advanced Biomechanics and Motion Analysis
- Chapter 9: Advanced Load, Fatigue, and Recovery
- Chapter 10: Advanced Weekly Training Planning and Structure
- Chapter 11: Advanced Goal-Based Progression and Deloads
- Chapter 12: Advanced Applied Exercise Decision-Making and Coaching Logic

## Program Principles
- Consistency beats intensity
- Recovery is a training tool, not a failure state
- Good movement is built through repeated, purposeful exposure
- Each workout should be explainable in relation to the user's goal and schedule
- Progression is organized by chapter and subchapter gates, not school terms
- Advanced chapters reuse the same educational areas with deeper case-based and technical material

## Daily and Optional Reading
Daily lessons should teach a concept, not just summarize it. Standard daily lessons target approximately 3–5 minutes of core reading, with an estimated eight-minute daily update including the existing checks and application prompt. See the [lesson reading index](reading-options.md) and lesson records for current coverage and draft status.

Authored lessons may link their own optional reading assignment, targeting **15–20 minutes**, or up to about **30 minutes** where relevant material warrants it; short/transitional lessons may omit an extension with a documented reason. The precise sections, page links, and reading focus appear directly in the lesson. These content drafts remain subject to source, rights, editorial, and domain review; optional readings stay outside required checks, prerequisites, gates, daily streak requirements and texting exports.

See the internal [lesson reading index](reading-options.md) for the assignment registry and textbook editions.

## Content Indexing, Textbook Parsing, and Inclusion

Use an **index-first, source-verified workflow** when creating, revising, or compiling lessons. Keep the relationship searchable in both directions: **curriculum → lesson → learning material → source**, and **textbook chapter → curriculum chapter/subchapter → lesson**. An index locates evidence; it does not replace reading the source or approve material for publication.

### Index Locations and Ownership

| Record | What it indexes | Maintenance |
|---|---|---|
| [Chapter overview](chapter-overview.md), chapter guides, and subchapter `README.md` lesson lists | Program sequence, topic scope, prerequisites and ordered lesson locations | Human maintained |
| Each `lesson-*.md` file's frontmatter and teaching sections | Stable lesson ID, title, objective, placement, terms, reading/duration estimates, checks and draft/published status | Human maintained; the lesson is the content record |
| [Reading registry](reading-options.md) and [optional readings](optional-readings/README.md) | Lesson → reading ID, assignment, source edition, attributed text, source words and estimated time | Registry/review decisions human maintained; extraction produces drafts |
| [Textbook source registry](../../textbook-indices/README.md) | Available books, index coverage, source locations and queued sources | Human maintained |
| [A&P chapter/section index](../../textbook-indices/anatomy-and-physiology-2e/chapters.md) and [graphics inventory](../../textbook-indices/anatomy-and-physiology-2e/graphics.md) | Source headings, navigation aids, figure/table IDs and PDF/printed pages | Generated from the verified local edition |
| [A&P curriculum usage map](../../textbook-indices/anatomy-and-physiology-2e/curriculum-usage.md) | Textbook chapter → actual lesson-linked optional reading → curriculum destination and pages | Generated from linked reading metadata |
| [Curated source guide](../../textbook-indices/anatomy-and-physiology-2e/curated-guide.md), [visual review catalogue](../../textbook-indices/anatomy-and-physiology-2e/graphic-review.md), and [current source usage/review](../../textbook-indices/anatomy-and-physiology-2e/usage-log.md) | Useful materials, proposed lesson matches, actual consultation/reuse, review evidence and source-return questions | Human maintained; never overwritten by index generation |
| [Curriculum asset registry](assets/README.md) | Included graphic files, source/credit, figure IDs, pages, license, transformations and lesson destinations | Human maintained after item-specific checks |

Lesson indexing currently uses the existing guides and lesson records; the A&P textbook and reverse assignment maps have automated generators. A&P is the first fully indexed textbook. Do not treat the other books as fully indexed or proposed graphics as already embedded content.

### 1. Index the Lesson Content

For each authored lesson:
1. Preserve its unique `id`, `program`, `chapter`, `subchapter`, lesson order and file path. Update the subchapter's ordered lesson links whenever a lesson is created, renamed or moved; update the chapter/program overview when scope or sequence changes.
2. Keep the title, one observable learning objective, prerequisite concepts, key terms, teaching scope, knowledge-check count, reading/duration estimates and review status discoverable in the lesson or its guide. Link to the authoritative lesson instead of duplicating its entire prose across indices.
3. Record which source sections support factual explanations, which passages form optional readings, and which figures are merely suggested versus actually included. Use exact source/reading/figure IDs and destination lesson IDs; a topic match alone is not evidence of use.
4. Keep outline-only modules identified as outlines. Do not count a proposed lesson, reading or image as authored, reviewed or published material.

### 2. Parse and Index the Textbook Before Selecting Material

Start with the canonical [textbook-learning-material skill](../../../../.agents/skills/textbook-learning-material/SKILL.md) and the source registry, following [shared repository guidance](../../../../AGENTS.md). For each new source or edition:
- Record author/title/edition, local filename, ISBN where available, source fingerprint, license notice and required attribution. PDFs remain local-only in `docs/`.
- Check Contents, PDF bookmarks, glossary/Key Terms, alphabetical index, references, appendices, objectives, summaries, review questions, answer keys and interactive links. Record exact locations and whether each aid is present, absent or unverified.
- Verify PDF viewer versus printed page numbering from actual footers; detect numbering changes rather than assuming a universal offset or trusting PDF page labels.
- Index chapters, numbered subchapters/sections, labelled figures and tables with stable IDs and both page-number systems. Record continuation pages, shared-page boundaries and unlabelled-artwork/OCR coverage gaps.
- Preserve human review records when refreshing generated files. A changed source fingerprint makes previous page, caption and rights decisions stale until checked against the new edition.

For the current A&P source, use [the indexer](../../../../tools/index_anatomy_physiology.py). Other books need their own verified parsing/page-map adapter; the A&P adapter is not a generic whole-library parser.

### 3. Read, Review, and Compile a Focused Selection

Search the source index and existing lesson records first, then read the relevant textbook pages and their qualifications. Use the shared [bounded page reader](../../../../tools/read_pdf_pages.py) for text and [local graphics previews](../../../../tools/review_textbook_graphics.py) for visual inspection. Check exact first/last paragraph boundaries, captions, panels, labels, graph axes/units, tables and item-specific credits.

Write original teaching explanations, worked examples, misconception corrections and learning checks. Keep a claim-to-source record with exact sections, inclusive PDF/printed pages and partial-page exclusions. A contents entry, summary or diagram alone does not establish a new factual or quantitative claim.

For graphics, distinguish **caption located → visually inspected → intended-use rights assessed → cropped/exported → domain/editorial approved**. Rendering is not visual review; a visual review is not permission to reuse. Record artwork and caption pages separately, including cross-page captions, panel choices, crop/conversion details and meaningful alt text. Do not trace or relabel textbook artwork as an original schematic.

### 4. Include Material in the Correct Content Layer

| Material | Where it belongs | Inclusion rules |
|---|---|---|
| Standard teaching, original examples and checks | Corresponding `lesson-*.md` | Keep required checks answerable from the standard lesson alone; publish only after the existing review lifecycle |
| Licensed optional textbook text | `optional-readings/{lesson_id}-optional.md`, linked from that lesson's Read More section | Retain source/section/pages, attribution, source words/time and extraction status; use `content_type: optional_extended_reading`, `required_for_gate: false`, `include_in_texting_curriculum: false` |
| Selected, rights-cleared graphics | Curriculum `assets/`, documented in `assets/README.md` and beside the lesson figure | Preserve needed panels/caption/credits; record transformations and alt text; verify final-size readability and intended-use rights |
| Source PDFs and rendered full-page/contact-sheet previews | Local `docs/` and ignored `docs/graphics-review/` | Internal reference/review only; never package as app assets or replace learner-facing local reading links with ignored PDF links |

Verify the local edition's license and figure/page-specific exceptions before reproduction. Retain attribution and applicable NonCommercial/ShareAlike conditions; do not automatically clear an extract or graphic for a commercial build. Proposed or machine-extracted material remains draft until the actual source, rights, domain and editorial checks are recorded. Follow [content operations](../../../../project-management/content-operations.md) for the approval lifecycle.

### 5. Refresh the Indices Alongside Content Changes

- Update lesson metadata/links, the reading registry and relevant asset records in the same change as the content. When material use or review status changes, update current consultation/inclusion evidence in the source's `usage-log.md` and relevant curated/visual-review notes. Retain exact source locations, credits and dates/reviewers establishing actual review or approval; Git records routine edits and tool runs, not a new usage entry for every operation.
- Keep **consulted for a core explanation**, **assigned optional reading**, **suggested graphic**, and **included asset** distinct. The A&P reverse map records linked optional-reading assignments only; it does not prove that every core claim is sourced or every suggested graphic has been used.
- After reading assignment changes, refresh `curriculum-usage.md` with `python tools/index_curriculum_usage.py --write`. Refresh the textbook's generated index only when the source/index needs it. Neither operation should erase human notes, current usage or required review/rights evidence; neither needs a routine run-log row.
- For existing optional excerpts, validate first with `python tools/extract_optional_readings.py --dry-run`; use repeated `--lesson-id` flags to limit validation or regeneration to selected lessons. An unfiltered write regenerates all assigned readings; do not casually overwrite reviewed material. Machine extraction takes full assigned pages, so check and trim partial-page boundaries and skipped/unrelated content manually before approval.

### 6. Validate and Know When to Return to the Source

Run commands from the repository root using the configured Python environment:

| Check or refresh | Command |
|---|---|
| Verify the local A&P edition | `python tools/index_anatomy_physiology.py --check-source` |
| Rebuild A&P chapter/section/graphic metadata | `python tools/index_anatomy_physiology.py --write` |
| Validate reading assignments without regenerating excerpts | `python tools/extract_optional_readings.py --dry-run` |
| Refresh the A&P chapter-to-curriculum map | `python tools/index_curriculum_usage.py --write` |
| Detect a stale curriculum usage map | `python tools/index_curriculum_usage.py --check` |
| Run content-tool regression checks, including shared-helper and platform tests | `python -m unittest discover -s tools -p "test_*.py"` |

Also check lesson/reading IDs, local links, exact page spans, source/asset credits, optional-content flags and whether recorded review states reflect work actually completed. The automated usage map can be refreshed without PDFs; source verification, source parsing, extraction and rendered-page inspection require the relevant local source files.

**Return to the textbook** for a new mechanism or numerical claim, terminology dispute, ambiguous boundary, figure/table interpretation, incomplete caption, unknown item rights, conflicting notes or source-version change. Use Key Terms and the alphabetical index to navigate, then follow References or another evidence source when the book cannot answer the question. Update the unresolved question, exact place to revisit and actual resolution evidence in `usage-log.md`; do not fill an evidence gap by guessing or backfill past assignments as reviewed.