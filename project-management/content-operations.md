# Content Operations Workflow

This defines how curriculum and educational content moves from draft to publish.

Follow [shared repository guidance](../AGENTS.md) and the canonical [curriculum indexing and inclusion workflow](../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md#content-indexing-textbook-parsing-and-inclusion); this document owns the approval lifecycle.

## Content Lifecycle

### 1. Draft
A content item is created in the curriculum knowledge repo as a draft markdown file.

Required fields:
- curriculum ID
- title
- learning objective
- domain tag
- linked prerequisite topic or gate
- estimated duration
- concept summary

Deferred or future additions that should not be written yet belong in `project-management/curriculum-backlog.md`.

### 2. Domain Review
The Physio Consultant reviews the content for:
- accuracy and safety
- alignment with evidence-informed frameworks
- appropriateness for the target audience
- appropriate medical disclaimer boundary

### 3. Editorial Review
The Content Strategist reviews the same draft for:
- clarity
- terminology consistency
- tone and reading level
- knowledge check quality
- microcopy and user explanation quality

### 4. QA / Validation
The QA and PM reviewers confirm:
- the lesson is placed correctly in the curriculum tree
- the flow is understandable by the user
- the concept is not overcomplicated or over-promised
- the lesson can be tracked and measured in the product flow

### 5. Publish
Once approved, the content is marked `published` and included in the product content pipeline.

### 6. Version and Maintenance
Content should be revisable over time; Git records routine edits and past workflows.
- update current usage, review evidence and open questions when material status changes; retain dates/reviewers that establish actual source review or approval
- outdated lessons should be marked as stale
- major curriculum changes should trigger product review

## Content Operation Rules
- No lesson is considered final until reviewed by the domain expert
- No public-facing claim should imply medical treatment or diagnosis
- Learning content should be broken into micro-lessons that are easy to complete and easy to track
- Every lesson should answer: what the user learns, why it matters, and how to apply it next

## Reading Depth and Delivery
- Standard daily lessons target roughly 500–750 words of teaching (about 3–5 minutes reading), plus the existing check and application for an approximately eight-minute update. Include explanation, a worked example, and misconceptions; a 30–120-second summary is a preview, not the whole lesson.
- Associate an optional extended reading with each authored lesson to allow varied, focused topics. Target 15–20 minutes; when relevant source material makes an assignment a little longer, keep it up to about 30 minutes rather than cutting useful content. If the source selection is short, add a distinct optional excerpt instead of padding. Record the assigned-source word count and estimated time.
- Read the relevant local textbook sections in `docs`, not just their contents pages. Cite the exact source edition, section, inclusive PDF viewer pages, printed pages, and partial-page boundaries. Count the assigned text and guide at an explicit pace (approximately 200 wpm); explain diagram-study variability.
- Keep textbook PDFs local and out of Git. Store approved local reading extracts under `theory-and-knowledge/knowledge/curriculum/foundations-of-movement/optional-readings/`, one `{lesson_id}-optional.md` per assignment. Lessons and the app content bundle link to these local excerpts, never to the ignored PDFs. Each extract records lesson/reading IDs, source author/title/edition, exact section and page spans, license and required attribution, extraction status, and the optional-content tracking flags.
- Generate text extracts reproducibly from the specified inclusive PDF pages using `tools/extract_optional_readings.py`; use repeated `--lesson-id` flags to limit validation or regeneration to selected readings, since the default run rewrites every assigned excerpt. Extraction is a draft: compare it with the rendered source pages, correct reading-order/spacing artifacts, honor stated partial-page boundaries and skip instructions, omit images and third-party figure content, and obtain editorial/domain review before publication. Do not silently paraphrase an extract as if it were verbatim source text.
- Before extracting any text or image, verify the source license and any figure/page-specific exception. Include title, creator, source location, license, and required attribution with every reuse. The app's intended private/non-redistributed use does not remove attribution or license conditions.
- For textbook figures, save the original extracted asset in the curriculum `assets/` folder only after confirming its caption and figure-level credit. Record the source PDF, author/title, figure number/caption, PDF and printed page, license/exception, and any crop/conversion in `assets/README.md` and beside the lesson figure. Add meaningful alt text; keep teaching context in the lesson. Use original schematics when no suitable reusable source figure exists, and label them as original rather than textbook-derived.
- Keep optional content out of knowledge checks, prerequisites, gate completion, daily streak requirements, and texting exports. Use `content_type: optional_extended_reading`, `required_for_gate: false`, and `include_in_texting_curriculum: false`; an exporter must exclude optional guides even if a preference requests longer updates.
- Place the optional reading link on its corresponding lesson. The learner-facing reading copy describes its topic and estimated time; keep eligibility, completion, streak, progression, analytics, and delivery rules in internal product/tracking documentation.
- A short/transitional gate may omit its own extension with a documented reason. Outline-only modules get this content when their lessons are authored; do not label an outline a finished reading assignment.
- Use original explanations and cases. Do not copy textbook chapters or diagrams into lessons; use only assigned, license-cleared extracts in the separate optional-reading bundle. Never package source PDFs; check redistribution rights before including any extract or image in a product build.
- Reading additions remain drafts until domain and editorial review. Confirm every required check is answerable from the standard lesson alone.

Current coverage and source assignments: [Foundations reading options](../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/reading-options.md).

## Textbook Index and Source-Return Maintenance

- Start textbook-backed work with the [source registry and indices](../theory-and-knowledge/knowledge/textbook-indices/README.md), then read the relevant source pages. A&P has full chapter/subchapter, graphics and navigation indices; FES has a focused source map, with full indexing still queued. The other sources are queued.
- Use the canonical [textbook-learning-material skill](../.agents/skills/textbook-learning-material/SKILL.md) for index-first retrieval and compilation. Check the PDF edition fingerprint, both page-number systems, exact section boundaries, figure panels and item-specific rights. An index/summary alone is not evidence for a new learning claim.
- When material use or review status changes, update the source's human-owned `curated-guide.md` and current usage/review record in `usage-log.md`: source version, exact PDF/printed pages, section and figure/table IDs, partial-page boundaries, destination lesson/reading IDs, actual review evidence and unresolved return questions. Keep the filename for compatibility; use Git for routine edit, tool-run and refresh history, not a new entry for every operation.
- Reopen the textbook when a new claim, definition dispute, figure/table interpretation, ambiguous boundary or rights question arises. A source-version change makes existing references stale until revalidated. Follow the book's references or another source if the textbook does not answer the question.
- Regenerate machine-owned indices without overwriting curated discoveries, current usage or required review/rights evidence. Caption detection is not rendered review, image extraction permission or publication approval. Update existing assignment evidence when the relevant source pages are actually revisited; do not backfill past operations or imply retrospective review.

## Lesson Planning Structure

### 1. Initiate the lesson plan
Each lesson starts by defining a clear purpose in the active chapter flow:
- chapter and subchapter placement
- learner objective
- prerequisite concept(s)
- why this lesson matters to training or decision-making
- evidence source(s) used to shape the lesson

A lesson should not begin as a large essay. It should begin as a small, targeted learning unit.

### 2. Extend the details
Once the topic is placed, expand it into the instructional shape of the chapter:
- brief concept summary
- key terms and definitions
- common misunderstandings or misconceptions
- practical examples
- a short knowledge check or application prompt
- a link to the next concept or chapter gate

The goal is to create enough detail for a learner to understand and act, without overloading the content with advanced theory that belongs elsewhere.

### 3. Compare against the textbook and source material
Before finalizing the lesson, compare its content against the relevant textbook or reference source:
- confirm the concept sequence is accurate
- check whether the lesson matches the chapter's level of depth
- identify missing foundations that should be added before moving on
- flag advanced material that belongs in a later chapter or backlog

This phase should be narrow and disciplined: read the relevant table of contents, then only the relevant chapter pages needed to validate or expand the material.

### 4. Move advanced material to backlog
If a topic is valid but is too advanced for the current chapter, move it to `project-management/curriculum-backlog.md` instead of forcing it into the active lesson path.

Examples of backlog-worthy material:
- formal mechanics calculations beyond the app's current learning stage
- advanced pathology or rehab-specific classification
- specialized clinical assessment frameworks
- deeper quantitative biomechanical formulas not needed for MVP learning

This prevents over-scoping the active curriculum while preserving the topic for future refinement.

### 5. Add advanced chapter tracks
The curriculum can continue beyond the base foundation chapters with a second set of advanced chapters. These should:
- reuse the same subject areas in a deeper form
- be clearly identified as advanced chapter content
- remain separate from the MVP-safe foundation flow
- preserve the same lesson planning workflow for drafting, textbook comparison, and QA

Advanced chapters are not a review overlay. They are a real continuation of the curriculum once the foundation path is complete.

## Content Approval Checklist
- [ ] Learning objective clear and measurable
- [ ] Fits curriculum sequence
- [ ] Uses plain-language explanations
- [ ] Includes knowledge check or practical application
- [ ] Safety boundary reviewed
- [ ] Source material checked against textbook or chapter reference
- [ ] Advanced material either deferred or clearly marked for later chapters
- [ ] Properly assigned to program, chapter, subchapter, and lesson