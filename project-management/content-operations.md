# Content Operations Workflow

This defines how curriculum and educational content moves from draft to publish.

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
Content should be revisable over time with version notes.
- update notes should be logged
- outdated lessons should be marked as stale
- major curriculum changes should trigger product review

## Content Operation Rules
- No lesson is considered final until reviewed by the domain expert
- No public-facing claim should imply medical treatment or diagnosis
- Learning content should be broken into micro-lessons that are easy to complete and easy to track
- Every lesson should answer: what the user learns, why it matters, and how to apply it next

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