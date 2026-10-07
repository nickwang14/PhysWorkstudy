# Textbook Reference Indices

Start here when planning, checking or compiling learning material. Indices locate sources; they do **not** replace reading the relevant textbook pages.

## Source Registry

| Source / local filename in `theory-and-knowledge/knowledge/textbooks/` | Index status | Start here |
|---|---|---|
| A&P — `anatomy-and-physiology-2e_-_WEB.pdf` | Indexed: 28 chapters, 169 numbered sections, 720 figures and 104 tables | [A&P guide](anatomy-and-physiology-2e/README.md) |
| Biomechanics — `Biomechanics-of-Human-Movement-1600891203._print.pdf` | Indexed: 10 chapters, 71 numbered sections, 220 caption-line candidates; navigation-aid and rights audit remain open | [Biomechanics guide](biomechanics-of-human-movement/README.md) |
| Body Physics — `Body-Physics-Motion-to-Metabolism-1571156906.pdf` | Queued | [Existing reading registry](../curriculum/foundations-of-movement/reading-options.md) |
| Exercise Science — `Foundations-of-Exercise-Science-1748368639.pdf` | Focused Chapter 5 pages checked; full index queued | [Focused source map](foundations-of-exercise-science/README.md) |

Only A&P currently has a full navigation-and-graphics index. Biomechanics now has a source-specific chapter/section and caption-line locator index; its full navigation-aid, visual, citation and rights reviews remain open. FES has a focused source map for curriculum Chapter 5; full indexing remains queued. Each additional textbook needs its own verified edition, page map and navigation/graphics audit. Do not reuse the A&P adapter or assume its offset applies elsewhere.

## Index-First, Source-Verified Workflow

1. Search the chapter/section and graphics indices, then check curated notes and current usage/review evidence in `usage-log.md`.
2. Check that the local PDF fingerprint matches the index. If it changed, mark previous references stale and revalidate them.
3. Open the relevant source pages, including boundary headings, figure panels and credits. Resolve technical terms using chapter Key Terms; use the book Index to find concepts spread across chapters.
4. Build a focused, original explanation, study brief, glossary card, figure-study prompt or lesson draft. Keep licensed verbatim extracts separate and explicitly attributed.
5. Record exact section, inclusive PDF and printed pages, partial-page boundaries, stable figure/table IDs, destination content IDs/paths, review/rights status, and outstanding source-return questions.
6. When material use or review status changes, update the curated guide and current usage/review evidence in `usage-log.md` alongside the learning material. Preserve actual review dates/reviewers, approvals and credits. Regenerate only machine-owned files; never erase human notes or required evidence. Git records routine history; do not create a usage entry for every operation or backfill past assignments as reviewed.

Follow the canonical [curriculum workflow](../curriculum/foundations-of-movement/README.md#content-indexing-textbook-parsing-and-inclusion) and [textbook-learning-material skill](../../../.agents/skills/textbook-learning-material/SKILL.md) for the detailed procedure and learning-brief template. Executable page reading lives in [tools/read_pdf_pages.py](../../../tools/read_pdf_pages.py), not in a skill bundle.

## Storage and Rights

- PDFs remain local-only in `theory-and-knowledge/knowledge/textbooks/`, covered by `.gitignore`; indexes link to them with one-based `#page=` viewer anchors. A clone without the PDFs can still search the metadata.
- Commit navigation metadata, original usefulness notes, source citations and review records—not whole-book text dumps, source PDF pages or an unreviewed gallery of images.
- A figure locator is **not** permission to reproduce it. Verify its caption, credit, license exception, crop/panels and alt text before extraction or publication.
- Follow [content operations](../../../project-management/content-operations.md). NC/SA restrictions and third-party credits remain applicable; there is no automatic commercial-build clearance.

## Adding Another Source

Create a source folder with `README.md`, generated `index.json`, `chapters.md` and `graphics.md`, plus human-owned `curated-guide.md` and `usage-log.md` for current usage, review/rights evidence and open questions. Check contents, bookmarks, glossary/Key Terms, alphabetical index, references, appendices, objectives, summaries, review questions and answer keys; record present, absent or unverified aids. Identify labelled figures/tables and unlabelled-artwork coverage gaps. Fingerprint the edition and test page references before adding the registry entry.