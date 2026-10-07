# Anatomy and Physiology 2e — Working Reference Guide

## Where to Look

| File | Purpose | Maintenance |
|---|---|---|
| [chapters.md](chapters.md) | All 28 chapters, 169 numbered sections/subchapters, page ranges and chapter study aids | Generated |
| [curriculum-usage.md](curriculum-usage.md) | Textbook chapter → curriculum chapter/subchapter/lesson, with linked readings and exact page spans | Generated from current lesson-linked reading metadata |
| [graphics.md](graphics.md) | 720 figure and 104 table locators, stable IDs, caption-page links and candidate sections | Generated; not visually/rights cleared |
| [graphic-review.md](graphic-review.md) | 21 visually inspected bone/joint/muscle graphics; priorities, panels, crop guidance, credits, alt text and lesson matches | Human maintained; reuse/domain approval separate |
| [index.json](index.json) | Searchable metadata, edition fingerprint, mapping evidence, continuation pages and coverage limits | Generated |
| [curated-guide.md](curated-guide.md) | Useful teaching routes, selected graphics, source-return triggers and discovery backlog | Human maintained |
| [usage-log.md](usage-log.md) | Current material usage, actual review/rights evidence and open source-return questions | Human maintained; update when material use or review status changes; Git records routine history |

## Where This Textbook Is Used in Our Curriculum

Use the [textbook-to-curriculum usage map](curriculum-usage.md) to see where every A&P chapter is used. Its overview lists all 28 textbook chapters and the curriculum chapters that assign them; detailed tables link to individual lessons and optional reading files, identify the curriculum subchapter, and record the A&P PDF/printed pages used.

The map records **actual linked optional-reading assignments**, not inferred topic matches. A lesson can use more than one textbook chapter. Chapters without assignments are explicitly listed, but this does not mean they have never been consulted during drafting. [Suggested graphics](graphic-review.md) remain separate from assigned readings and embedded assets.

Refresh after changing assignments with `python tools/index_curriculum_usage.py --write`; check freshness without writing with `python tools/index_curriculum_usage.py --check`. This reads the saved textbook index and Markdown readings only; it does not regenerate excerpts or require the local PDFs. Update current source consultation or graphic-reuse evidence in `usage-log.md` when material use or review status changes; preserve actual review dates/reviewers and approvals, not routine refresh logs.

## Edition and Page Map

- Local file: `theory-and-knowledge/knowledge/textbooks/anatomy-and-physiology-2e_-_WEB.pdf`, 1,347 PDF viewer pages.
- Title: *Anatomy and Physiology 2e*, J. Gordon Betts and fellow contributing authors / OpenStax, Rice University. Original publication 2022; supplied PDF copyright 2026, creation date 2026-04-20. Digital ISBN 978-1-951693-42-8.
- SHA-256: `aa2e577b2083c343f4d57b38f00dd935dd2d98befdb38c0f368d72d636d0ff46`.
- **Printed page + 16 = PDF viewer page**, verified against 1,297 recognized printed footers. PDF page labels themselves are viewer numbers. Front matter before PDF 17 is not mapped to printed numbers.
- Source/rights notice: [PDF 4](../../textbooks/anatomy-and-physiology-2e_-_WEB.pdf#page=4); author list: [PDF 3](../../textbooks/anatomy-and-physiology-2e_-_WEB.pdf#page=3).
- Source: [OpenStax book details](https://openstax.org/details/books/anatomy-and-physiology-2e). This local edition is **[CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)**, with separately credited exceptions. Required notice: **Access for free at openstax.org.** Do not infer this edition's rights from older OpenStax editions or assume the live web edition has identical pagination.

## Navigation Audit

| Reference | Exact location | How to use |
|---|---|---|
| Contents | [PDF 7–16](../../textbooks/anatomy-and-physiology-2e_-_WEB.pdf#page=7); unnumbered | All indexed section/study-aid starts corroborate the printed TOC |
| PDF bookmarks | Viewer outline pane | Chapters, numbered sections, chapter study aids; References has section-specific child bookmarks |
| Preface | [PDF 17–22](../../textbooks/anatomy-and-physiology-2e_-_WEB.pdf#page=17), printed 1–6 | Book organization, learning features, scope and contributor information |
| Chapter glossaries | Each chapter's **Key Terms**, linked in `chapters.md` | Check exact terminology before making learner-friendly definitions; no standalone glossary entry found in contents/bookmarks |
| Objectives / summaries | Chapter opening (Chapter Objectives), Introduction and Chapter Review | Scope/prerequisites first; summaries are a preview, not sufficient evidence for a new claim |
| Practice and interactive references | Interactive Link Questions where present, Review Questions and Critical Thinking Questions in each chapter | Find learning checks and linked activities; do not copy question banks or treat optional material as gate content |
| References | [PDF 1321–1324](../../textbooks/anatomy-and-physiology-2e_-_WEB.pdf#page=1321), printed 1305–1308 | Follow section-specific evidence to original sources when more depth is needed |
| Alphabetical Index | [PDF 1325–1347](../../textbooks/anatomy-and-physiology-2e_-_WEB.pdf#page=1325), printed 1309–1331 | Find cross-chapter terms; its page references are **printed**, so add 16 when opening the PDF |
| Appendices / standalone answer key | No entry found in contents/bookmarks | Do not assume these exist or infer correct answers from a question alone |

## Commands from the Repository Root

- Search metadata without opening the PDF: `python tools/index_anatomy_physiology.py --query "synovial"`.
- Check edition freshness: `python tools/index_anatomy_physiology.py --check-source`.
- Validate a fresh scan without writing: `python tools/index_anatomy_physiology.py`.
- Refresh only generated files: `python tools/index_anatomy_physiology.py --write`.
- Run regression checks, including shared-helper and platform tests: `python -m unittest discover -s tools -p "test_*.py"`.
- Read a bounded page selection: `python tools/read_pdf_pages.py --pdf-pages "346-350" --layout`; see the canonical [skill](../../../../.agents/skills/textbook-learning-material/SKILL.md) for limits and the [curriculum workflow](../../curriculum/foundations-of-movement/README.md#content-indexing-textbook-parsing-and-inclusion) for inclusion rules.
- Preview selected graphics: `python tools/review_textbook_graphics.py --ids ap-figure-9.8 ap-figure-9.19 --extra-pages 366`. Use `--dry-run` for selection/source validation only. Page previews/contact sheets go to ignored `docs/graphics-review/`, never the app asset folder.

Dependencies: `tools/requirements-content.txt` (`pypdf`, `pypdfium2` and Pillow). No API keys or external service are needed.

## What Is and Is Not Verified

All pages were scanned for text, contents/bookmarks were compared, recognized printed footers were checked, and caption labels were located. Section spans deliberately include the next heading page to preserve shared-page boundaries; **they are not approved full-page extraction assignments**. The graphic inventory stores short first-line caption locators only; blank leads and unlabelled artwork need source inspection. Continued tables retain every label occurrence under one stable ID.

The generated inventory and initial shortlist remain locator/planning records. A separate [visual review catalogue](graphic-review.md) now records 21 inspected bone/joint/muscle graphics, including photo/micrograph credit holds and a cross-page caption. The remaining 11 graphics from the original shortlist have not received visual review. Neither visual inspection nor local previews establish publication/commercial-reuse clearance. Existing optional reading assignments have not been retrospectively re-reviewed or relabelled by this work.