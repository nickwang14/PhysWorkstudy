# Textbook Reference Indices

Start here when planning, checking or compiling learning material. Indices locate sources; they do **not** replace reading the relevant textbook pages.

## Source Registry

| Source / local filename in `docs/` | Index status | Start here |
|---|---|---|
| A&P — `anatomy-and-physiology-2e_-_WEB.pdf` | Indexed: 28 chapters, 169 numbered sections, 720 figures and 104 tables | [A&P guide](anatomy-and-physiology-2e/README.md) |
| Biomechanics — `Biomechanics-of-Human-Movement-1600891203._print.pdf` | Queued; existing lesson assignments are not a full textbook index | [Existing reading registry](../curriculum/foundations-of-movement/reading-options.md) |
| Body Physics — `Body-Physics-Motion-to-Metabolism-1571156906.pdf` | Queued | [Existing reading registry](../curriculum/foundations-of-movement/reading-options.md) |
| Exercise Science — `Foundations-of-Exercise-Science-1748368639.pdf` | Focused Chapter 5 pages checked; full index queued | [Focused source map](foundations-of-exercise-science/README.md) |

Only A&P has a full textbook index. FES has a focused source map for curriculum Chapter 5; full indexing remains queued. Each additional textbook needs its own verified edition, page map and navigation/graphics audit. Do not reuse the A&P adapter or assume its offset applies elsewhere.

## Index-First, Source-Verified Workflow

1. Search the chapter/section and graphics indices, then check curated notes and the usage log.
2. Check that the local PDF fingerprint matches the index. If it changed, mark previous references stale and revalidate them.
3. Open the relevant source pages, including boundary headings, figure panels and credits. Resolve technical terms using chapter Key Terms; use the book Index to find concepts spread across chapters.
4. Build a focused, original explanation, study brief, glossary card, figure-study prompt or lesson draft. Keep licensed verbatim extracts separate and explicitly attributed.
5. Record exact section, inclusive PDF and printed pages, partial-page boundaries, stable figure/table IDs, destination content IDs/paths, review/rights status, and outstanding source-return questions.
6. Update the curated guide and usage log in the same change as the learning material. Regenerate only machine-owned files; never erase human notes or usage history.

Follow the canonical [curriculum workflow](../curriculum/foundations-of-movement/README.md#content-indexing-textbook-parsing-and-inclusion) and [textbook-learning-material skill](../../../.agents/skills/textbook-learning-material/SKILL.md) for the detailed procedure and learning-brief template. Executable page reading lives in [tools/read_pdf_pages.py](../../../tools/read_pdf_pages.py), not in a skill bundle.

## Storage and Rights

- PDFs remain local-only in `docs/`, covered by `.gitignore`; indexes link to them with one-based `#page=` viewer anchors. A clone without the PDFs can still search the metadata.
- Commit navigation metadata, original usefulness notes, source citations and review records—not whole-book text dumps, source PDF pages or an unreviewed gallery of images.
- A figure locator is **not** permission to reproduce it. Verify its caption, credit, license exception, crop/panels and alt text before extraction or publication.
- Follow [content operations](../../../project-management/content-operations.md). NC/SA restrictions and third-party credits remain applicable; there is no automatic commercial-build clearance.

## Adding Another Source

Create a source folder with `README.md`, generated `index.json`, `chapters.md` and `graphics.md`, plus human-owned `curated-guide.md` and append-only `usage-log.md`. Check contents, bookmarks, glossary/Key Terms, alphabetical index, references, appendices, objectives, summaries, review questions and answer keys; record what is present, absent or unverified. Identify labelled figures/tables and flag unlabelled artwork as a coverage gap. Fingerprint the local edition and test page references before adding the registry entry.