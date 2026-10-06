# A&P — Source Usage and Return Log

Human-owned, append-only history. Add a dated entry whenever material is consulted, compiled, extracted, verified, rejected or deferred. Corrections should reference the older entry rather than silently rewriting its evidence. Match the source fingerprint in [README](README.md).

## Activity

| Date / record ID | Purpose / destination | Section and material IDs | PDF / printed pages | Evidence and status | Return needed |
|---|---|---|---|---|---|
| 2026-10-06 / AP-INDEX-001 | Initial metadata index; `chapters.md`, `graphics.md`, `index.json` | Chapters 1–28; all 169 numbered sections; 720 figure and 104 table IDs | Full scan PDF 1–1347; numbered portion PDF 17–1347 / printed 1–1331 | Contents/bookmarks corroborated; 1,297 footers matched +16 offset; caption locators scanned; no artwork extracted | Rendered-page and item-level rights review before reuse; source hash check before new work |
| 2026-10-06 / AP-CURATE-001 | Initial movement-oriented planning shortlist; `curated-guide.md` | 25 selected stable figure IDs in the guide | Exact PDF/printed pages listed per figure in the guide | Text/caption-based usefulness planning only; no lesson or existing excerpt approved | Review each chosen figure's panels, complete caption, actual artwork page and credits when used |
| 2026-10-06 / AP-VERIFY-001 | Copilot automated validation; `tools/test_index_anatomy_physiology.py` and skill page-reader smoke check; no learner output | Caption sample from every chapter; 9.4 / `ap-figure-9.10`; edition fingerprint as recorded in README | Reader retrieved PDF 350 / printed 334; remaining chapter sample locations in generated inventory | 21 tests passed; all local links resolved; selected figure IDs/pages matched; source fingerprint matched; regeneration preserved human files byte-for-byte; raw page text inspected in memory only | Figure 9.10 rendered panels/credit/rights still pending before actual reuse; this is retrieval validation, not lesson/domain approval |
| 2026-10-06 / AP-GRAPHICS-001 | Copilot visual/source-location review; `graphic-review.md`; no learner output or final assets | 21 numbered entries in the visual catalogue: six bone, seven joint and eight muscle graphics | PDF 214, 217, 219–220, 225–226, 347, 350, 354–355, 360–361, 364–367, 388, 390–391, 395, 401, 403, 426, 428; printed = PDF − 16 | Source fingerprint matched README; inspected six 120-dpi contact sheets/24 pages; recorded panels, crop guidance, alt-text drafts and suggested lesson IDs; Figure 9.12 caption continues onto PDF 355; Figure 6.2 is a credited photo; Figure 6.12 contains a separately credited micrograph | Intended-use rights, actual crops, high-resolution/mobile readability and domain/editorial review pending; 11 original shortlist items still visually unreviewed. This advances AP-CURATE-001/AP-VERIFY-001 without rewriting their historical status |
| 2026-10-06 / AP-GRAPHICS-VERIFY-001 | Copilot automated validation; graphic preview tool, visual catalogue and skill workflow | All 21 reviewed records; dry run of `ap-figure-9.8` and `ap-figure-9.19` with neighboring page 366 | Dry run PDF 347, 366–367 / printed 331, 350–351 | 31 tests passed: IDs/pages, lesson IDs/links, caption continuation, credit holds, selection limits and contact-sheet layout; source hash matched; preview cache confirmed ignored; dry run did not overwrite the 24-page review batch | None for tool/reference validation; intended-use rights, final crops and lesson/domain approval remain open as recorded above |
| 2026-10-06 / AP-CURRICULUM-001 | Copilot reverse assignment-metadata index; `curriculum-usage.md` and main guide usage section | A&P Chapters 1, 4, 6, 9, 10 and 11 → 38 lessons across curriculum Chapters 1–4 | Exact A&P PDF/printed spans and lesson/reading links in the usage map; source edition taken from the saved index fingerprint | Scanned 73 authored lessons and their linked reading metadata; excluded other-book ranges and suggested graphics; validated lesson IDs, attribution, page bounds, printed offset and map freshness; 42 tests passed; no lessons/readings regenerated or new textbook-content review claimed | Refresh with `tools/index_curriculum_usage.py --write` after assignment changes; original excerpt/source/rights review tasks remain unchanged |

Existing optional reading assignments remain in the [reading registry](../../curriculum/foundations-of-movement/reading-options.md). They are **not** new source-review evidence. Backfill individual usage here only as their actual source pages are checked.

## Required Details for a New Entry

- Dated record ID, reviewer and task/learning objective.
- Source title/edition and SHA-256; textbook chapter/section IDs (not just curriculum chapter numbers).
- Inclusive PDF viewer and printed page ranges; exact start/end headings or paragraph boundaries, exclusions, skipped panels and continuation pages.
- Figure/table stable IDs, verified artwork pages, credit/rights status and caption/visual verification status where applicable.
- What was learned or checked, in original concise wording; uncertainty, correction or reason for rejection.
- Learning-material destination path and lesson/reading ID; output type (original explanation, glossary card, study prompt, verbatim licensed excerpt or extracted asset).
- Extraction/draft/review/publication status; required attribution, changes/crops and license exceptions.
- Explicit source-return question and trigger, or `none for this checked objective`; never imply approval of an entire chapter.

## Outstanding Source-Return Register

| ID | Trigger | Where to return | Resolution required | Status |
|---|---|---|---|---|
| AP-RETURN-001 | A curated figure is selected for material creation | Its `ap-figure-*` inventory entry, visual catalogue and caption/artwork pages | Check actual review coverage; resolve intended-use rights, crop, alt text and domain fit | Partially advanced by AP-GRAPHICS-001: 21 visual records; per-figure reuse approval still open |
| AP-RETURN-002 | An existing optional assignment is revised or regenerated | Its reading-registry assignment and cited A&P sections | Confirm topic-specific boundaries and remove unrelated neighboring material | Open; do not mark complete from a metadata scan |
| AP-RETURN-003 | Local PDF fingerprint changes | License notice, Contents, numbered footers, affected chapters and graphic IDs | Regenerate metadata; mark prior records stale and revalidate selected references | Conditional |
| AP-RETURN-004 | Figure 6.2 photo or Figure 6.12 micrograph is proposed for reuse | PDF 214 / printed 198; PDF 225 / printed 209; credited creators | Establish photo/micrograph-specific terms or use a verified alternative; do not assume blanket book rights | Open; credit identified, permission/exception decision unresolved |
| AP-RETURN-005 | Figure 9.12 is cropped or assigned alone | PDF 354–355 / printed 338–339 | Preserve the caption continuation and selected action-pair labels | Open for extraction; visual boundary already checked |
| AP-RETURN-006 | Figure 10.13 wording is used to define isometric action | PDF 401 / printed 385 and §10.4 | Domain-review the simplified caption; avoid a universal load-exceeds-force claim | Open; visual teaching candidate only |

Add specific unresolved questions as work reveals them. Resolve each with a dated usage entry containing the verified pages and evidence; leave the historical trigger visible.