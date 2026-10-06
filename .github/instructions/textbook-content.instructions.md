---
description: 'Use maintained textbook indices and exact source pages when authoring, compiling, extracting or reviewing learning material; update source usage and unresolved textbook-return records.'
applyTo: 'theory-and-knowledge/knowledge/**/*.md,workout-programming/knowledge/**/*.md,project-management/content-operations.md,tools/*reading*.py,tools/index_*.py'
---

# Source-Grounded Learning Content

- Start source-backed content work at `theory-and-knowledge/knowledge/textbook-indices/README.md`; consult the relevant chapter/section index, graphics inventory, curated guide and usage log before searching the whole PDF.
- Use the `textbook-learning-material` skill for textbook indexing, retrieval, extraction and learning-material compilation. A&P has a full index; the other three sources are queued, not implicitly indexed.
- Indices locate evidence, not replace it. Check the source fingerprint and read exact local textbook pages before adding factual claims, resolving terms, extracting figures/passages or interpreting quantitative material. Use chapter Key Terms, the book Index and References when needed.
- Record both one-based PDF viewer and printed pages, section/graphic IDs, partial-page boundaries and destination lesson/reading IDs. Navigation spans can overlap; never treat them as automatically approved extraction ranges.
- Update the source's `curated-guide.md` and append a dated `usage-log.md` record in the same task; track unanswered questions and explicit source-return triggers. Do not imply source/rendered/rights review happened when it did not.
- `index.json`, `chapters.md` and `graphics.md` are generated. Regeneration must preserve curated notes and usage history; a changed PDF makes prior references stale until revalidated.
- Keep PDFs local-only. Respect edition/item-specific rights, required attribution, NC/SA terms and existing content operations. Draft extraction and indexed graphics are not publication or commercial-reuse clearance. Preserve optional-content flags and avoid changes to unrelated lessons.