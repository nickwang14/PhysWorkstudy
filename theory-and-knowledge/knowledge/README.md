# Theory & Knowledge — Knowledge Repository

This folder contains the **curriculum content, academic syllabuses, and educational resource library** for PhysiApp's learning track.

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

Each lesson file includes: title, learning objective, core concept, key terms, knowledge check questions, and "apply it" tip.

### `frameworks/`
Reference documents from certified educational or exercise frameworks used as source material for curriculum and content accuracy reviews.

### `exercise-library/`
Definitions of individual exercises as educational content (not programming):
- Movement pattern classification (hinge, squat, push, pull, carry)
- Muscle group involvement
- Common errors and cues
- Learning prerequisite (what the user should understand before this exercise)

### `glossary/`
Canonical terminology maintained by the Content Strategist:
- Term, user-facing definition (Grade 9–10 reading level), technical definition, first-use context

---

## Source Textbooks and Curriculum Direction
We are using a layered academic approach: start with human movement foundations, then move into anatomy and physiology, then biomechanics and kinetics, then program design and adaptation.

### Key source texts in the repo
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
estimated_duration_minutes: 5
knowledge_check_count: 3
---
```