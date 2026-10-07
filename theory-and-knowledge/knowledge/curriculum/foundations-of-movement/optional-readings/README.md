# Local Optional Reading Excerpts

This folder contains per-lesson text excerpts generated from locally held source textbooks. Each lesson links to its own `{lesson_id}-optional.md` file; the app must not link to the ignored textbook PDFs.

Follow the canonical [curriculum indexing and inclusion workflow](../README.md#content-indexing-textbook-parsing-and-inclusion) and [textbook-learning-material skill](../../../../../.agents/skills/textbook-learning-material/SKILL.md). Current assignments and coverage are recorded in the [reading registry](../reading-options.md), not inferred from outlines.

## Regenerate

1. Place the four assigned, licensed PDF editions in the repository's `docs/` folder. These binary PDFs are ignored by Git.
2. Install the content-extraction dependency with `python -m pip install -r tools/requirements-content.txt`.
3. Validate every lesson, source mapping, and inclusive page range without writing files: `python tools/extract_optional_readings.py --dry-run`.
4. To add or revise only selected readings, pass one `--lesson-id` per ID to both validation and generation (for example: `python tools/extract_optional_readings.py --lesson-id planning-05-01 --lesson-id planning-05-02 --dry-run`, then the same command without `--dry-run`).
5. Generate every assignment only when intentionally rebuilding the entire bundle: `python tools/extract_optional_readings.py`.
6. Review each generated extract against its cited PDF pages. The text layer may have spacing, order, table, or equation defects. Correct or remove problematic passages before use. Re-run the generator only when intentionally replacing the generated drafts.

## Scope and rights

- Excerpts contain text from the specifically assigned PDF pages, not embedded page images. Figure images are omitted and follow the separate [asset workflow](../assets/README.md) and [content operations](../../../../../project-management/content-operations.md).
- Each file records its lesson and optional-reading IDs, source title/creator, exact PDF and printed pages, extracted source word count, estimated reading time, license, attribution, and draft extraction status.
- A&P and *Body Physics* content is CC BY-NC-SA 4.0; *Foundations of Exercise Science* is CC BY-NC 4.0; *Biomechanics of Human Movement* is CC BY 4.0 except where otherwise noted. Preserve all source notices and ShareAlike requirements. Do not redistribute or use NC-licensed excerpts in commercial builds without separate permission.
- These files remain optional and separate from the core lesson, required checks, gate completion, streaks, and texting exports. They are draft source extracts, not reviewed teaching copy.
