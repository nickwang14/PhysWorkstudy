# Biomechanics of Human Movement — Working Reference Guide

## Where to Look

| File | Purpose | Maintenance |
|---|---|---|
| [chapters.md](chapters.md) | All 10 bookmarked chapters, sections, navigation pages and PDF/printed page spans | Generated |
| [graphics.md](graphics.md) | Figure/table caption-line candidates, stable page-based IDs and candidate sections | Generated; not visually or rights cleared |
| [index.json](index.json) | Searchable metadata, source fingerprint, page-map evidence, sections and caption candidates | Generated |
| [curated-guide.md](curated-guide.md) | Human-owned teaching routes, use notes and discovery priorities | Human maintained |
| [usage-log.md](usage-log.md) | Current source consultation, review evidence and open source-return questions | Human maintained |

This index is a locator, not evidence for a new factual claim or approval to reproduce material. Existing curriculum assignments are in the [reading registry](../../curriculum/foundations-of-movement/reading-options.md); they do not amount to a full source review.

## Edition and Page Map

- Local PDF: `theory-and-knowledge/knowledge/textbooks/Biomechanics-of-Human-Movement-1600891203._print.pdf` (local-only; do not commit).
- Title page: *Biomechanics of Human Movement*, Karine Hamm, Douglas College (PDF 3); license notice is on PDF 4–5.
- PDF metadata: 747 pages; SHA-256 `4bb936aa15a0ba2a02f4d794608b3a6c296f09828786ded1bdf7cfdfa0eda77d`.
- Printed page + 12 = PDF viewer page, verified against 729 recognized footer labels. Six chapter-opening/divider pages have no recognized printed footer: PDF 56, 160, 238, 290, 556 and 732. PDF viewer pages are not printed page numbers.
- Contents: PDF 7–10; bookmark outline includes 10 chapters and section headings. The generated index retains bookmark titles and destinations without copying textbook prose.
- License statement: PDF 4–5 says CC BY 4.0 except where otherwise noted and contains redistribution-attribution instructions.

### Open provenance and attribution question

The title page identifies *Biomechanics of Human Movement*, but the license page's bibliographic citation names *College Physics* and gives a CNX URL whose path also names *Introduction to Science and…*. Do not silently substitute the citation or treat this indexing pass as clearance for new redistribution. Verify the correct source record, title-specific attribution and any item-level exceptions before creating or redistributing new excerpts/assets. This discrepancy does not itself revise existing asset records; check their evidence individually.

## Coverage and Limits

- All 747 PDF pages were scanned for extractable text, footer labels and lines beginning `Figure N.` or `Table N.`. Footer evidence is consistent with the +12 mapping; pages with no recognized footer are listed above.
- Chapter and subsection locations come from PDF bookmarks. Spans include the next heading's page where headings share pages; they are navigation ranges, not approved extraction ranges.
- Figure/table numbering repeats throughout the book. Caption candidates use page-based stable IDs to prevent collisions. A captured caption lead is a short locator, not a verified figure title or complete caption.
- Unlabelled artwork is outside the text scan. Caption detection does not verify artwork location, panels, full captions, credits or reuse rights. Rendered visual review has not been recorded in this index pass.
- Standalone glossary, alphabetical index, references, appendices, objectives, summaries, review questions and answer keys have not been fully audited. Do not infer presence or absence from the bookmark outline alone.

## Commands from the Repository Root

- Search saved metadata: `python tools/index_biomechanics.py --query "synovial"`.
- Verify the local edition fingerprint and page count: `python tools/index_biomechanics.py --check-source`.
- Validate a fresh scan without writing: `python tools/index_biomechanics.py`.
- Refresh generated navigation and caption locators only: `python tools/index_biomechanics.py --write`.
- Run content-tool regression tests: `python -m unittest discover -s tools -p "test_*.py"`.

PDFs and visual previews remain local-only. Follow the [textbook learning-material workflow](../../../../.agents/skills/textbook-learning-material/SKILL.md), [content operations](../../../../project-management/content-operations.md) and item-specific license/credit evidence before selecting or including material.
