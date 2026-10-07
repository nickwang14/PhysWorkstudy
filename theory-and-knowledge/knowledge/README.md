# Theory & Knowledge — Knowledge Repository

This folder contains the **curriculum content, academic syllabuses, and educational resource library** for PhysiApp's learning track.

Follow [shared repository guidance](../../AGENTS.md) and the canonical [curriculum indexing and inclusion workflow](curriculum/foundations-of-movement/README.md#content-indexing-textbook-parsing-and-inclusion).

## What Goes Here

### `curriculum/`
Academic curriculum definitions — the learning path is continuous and chapter-based rather than segmented by academic calendar:
```
Program (e.g., "Foundations of Movement")
  └── Chapter (major concept cluster)
        └── Subchapter (focused concept group)
              └── Lesson / micro-lesson
                    └── Knowledge check and application prompt
```
This is designed to feel like a Duolingo or Candy Crush progression map: users move through concept gates, not academic semesters. Each gate unlocks after enough mastery and completion signals.

Each lesson file includes: title, learning objective, core concept, key terms, knowledge check questions, and "apply it" tip. Authored lessons can include a topic-matched optional textbook reading. See the internal [lesson reading index](curriculum/foundations-of-movement/reading-options.md).

Textbook PDFs in `textbooks/` are local-only source material and are ignored by Git. Optional reading content for the app is prepared as a separately attributed excerpt in `curriculum/foundations-of-movement/optional-readings/`; lessons link to those local files, not to the PDFs. Excerpts remain drafts until checked against the source pages and reviewed under [content operations](../../project-management/content-operations.md). Optional readings stay outside required checks, prerequisites, gates, daily streak requirements and texting exports.

### `textbook-indices/`
Start source-backed content work with the [textbook reference indices](textbook-indices/README.md). A&P is fully indexed by chapter, numbered subchapter, navigation aid, figure and table with PDF/printed pages. Its curated guide identifies useful teaching material, while its usage log tracks what was checked, created and needs another textbook visit. FES has a focused source map, not a full index; other sources are queued. Use the canonical [textbook-learning-material skill](../../.agents/skills/textbook-learning-material/SKILL.md) to locate, read and compile material, then update these records alongside the content.

### `frameworks/` (planned; folder not yet present)
Reference documents from certified educational or exercise frameworks used as source material for curriculum and content accuracy reviews.

### `exercise-library/` (planned; folder not yet present)
Definitions of individual exercises as educational content (not programming):
- Movement pattern classification (hinge, squat, push, pull, carry)
- Muscle group involvement
- Common errors and cues
- Learning prerequisite (what the user should understand before this exercise)

### `glossary/` (planned; folder not yet present)
Canonical terminology maintained by the Content Strategist:
- Term, user-facing definition (Grade 9–10 reading level), technical definition, first-use context

---

## Source Textbooks and Curriculum Direction
We are using a layered academic approach: start with human movement foundations, then move into anatomy and physiology, then biomechanics and kinetics, then program design and adaptation.

### Key source texts held locally in `textbooks/` (not included in clones)
- `Biomechanics-of-Human-Movement-1600891203._print.pdf`
  - Major themes: prerequisite skills, anatomy basics, linear and angular kinematics, kinetics, work, power, and energy
  - Best fit for: biomechanics concepts, movement analysis, force and motion reasoning
- `Body-Physics-Motion-to-Metabolism-1571156906.pdf`
  - Major themes: body measurement, error and composition, balance, strength and elasticity, body in motion, locomotion
  - Best fit for: applied human movement, body mechanics, energy and locomotion, movement quality
- `Foundations-of-Exercise-Science-1748368639.pdf`
  - Major themes: public health, history, research methods, professionalism, exercise physiology, motor behavior, biomechanics, psychology, aging, adapted physical activity
  - Best fit for: defining the broad exercise science lens and interdisciplinary framing
- `anatomy-and-physiology-2e_-_WEB.pdf`
  - Major themes: organization of the human body, support and movement, muscular system, joints, performance under exercise stress
  - Best fit for: anatomy and physiology foundation, movement system understanding, exercise-specific structure

### Curriculum planning implication
The textbook review suggests a strong sequence, but it should be delivered as a continuous progression path rather than a fixed year or term calendar:
1. Foundations of movement and terminology
2. Anatomy and physiology of movement
3. Biomechanics and kinematics or kinetics
4. Exercise physiology, load, fatigue, and recovery
5. Program design and weekly planning
6. Goal-based adaptation, progress, and deload planning
7. Applied coaching and intervention logic

This aligns with the current `Foundations of Movement` program and supports a continuous learning ladder: foundations first, then applied training science, then periodization and progression logic. The app should present these as chapter and subchapter gates that unlock progressively, not as semester blocks.

The curriculum plan contains seven foundation chapters and five advanced chapters. See the [program guide](curriculum/foundations-of-movement/README.md) and [reading registry](curriculum/foundations-of-movement/reading-options.md) for current authored drafts and assignments; outline-only modules are not completed lessons or readings.

Deferred curriculum additions that do not fit the current gate should be tracked in `project-management/curriculum-backlog.md`.

---

## Content Ownership
| Content Type | Owner | Reviewer |
|---|---|---|
| Curriculum structure & scope | Physio Consultant | Product Manager |
| Lesson writing & microcopy | Content Strategist | Physio Consultant |
| Terminology | Content Strategist | Physio Consultant |
| Framework references | Physio Consultant | — |

## Lesson File Format
All lesson files are Markdown with YAML frontmatter:
```yaml
---
id: "foundations-01-01"
program: "foundations-of-movement"
chapter: "foundations-of-movement-and-terminology"
subchapter: "movement-terminology"
lesson: 1
title: "What Is Movement?"
learning_objective: "Define movement and explain why movement terminology matters in exercise and training"
estimated_duration_minutes: 8
estimated_reading_minutes: 4
status: draft
knowledge_check_count: 3
---
```

`estimated_duration_minutes` estimates the whole standard daily update; `estimated_reading_minutes` estimates its core reading only. Each optional lesson assignment has a unique reading ID and an estimated duration in the internal reading registry. Its progression and delivery flags are tracked separately from learner-facing reading copy.