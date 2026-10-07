# Theory & Knowledge — Progress & Tracking

This folder describes **proposed data schemas, tracking logic, and progress models** for the learning track, not evidence of implemented schemas or delivery features. Follow [shared repository guidance](../../AGENTS.md) and authoritative [product decisions](../../docs/product-decisions.md).

## Tracking Domains

### Learning Streak (Daily)
The learning streak is **daily** — the primary daily engagement loop for theory and knowledge.

```
learning_streaks
├── user_id
├── current_streak_days        -- consecutive days with ≥1 lesson completed
├── longest_streak_days
├── last_activity_date         -- ISO date (timezone-aware)
├── grace_period_used_today    -- bool: has user used today's grace period
└── streak_freeze_count        -- optional: earned streak freezes

learning_streak_history
├── user_id
├── week_start_date
├── days_active_this_week      -- count of days with lesson completed
└── streak_intact              -- bool
```

**Grace-period proposal (not decided):** The fields above accommodate a possible grace/freeze design; “one missed day per week” is a candidate, not approved policy or implemented behavior. Confirm learning-streak grace, timezone and week-boundary rules through the product/project decision records before implementation. Learning streaks remain independent of the training weekly goal.

**Friend poke:** Planned for the learning streak only, post-MVP under PD-001. A friend could send an encouragement nudge when a user's learning streak is at risk (has not yet completed a lesson today and it's evening); this is not a current implementation claim.

---

### Lesson Progress
```
user_lesson_progress
├── user_id
├── lesson_id                  -- FK to curriculum
├── status                     -- not_started | in_progress | completed
├── completed_at               -- timestamp
├── time_spent_seconds
├── knowledge_check_score      -- 0.0–1.0
└── attempts                   -- number of attempts on knowledge checks

user_curriculum_progress
├── user_id
├── program_id
├── chapter_id
├── subchapter_id
├── current_lesson_id
├── subchapter_completion_pct
├── chapter_completion_pct
├── program_completion_pct
└── last_accessed_at
```

### Daily Learning Node (Progression Map)
```
daily_learning_nodes
├── user_id
├── date                       -- ISO date
├── lesson_id                  -- assigned lesson for today
├── node_state                 -- locked | available | in_progress | completed | grace_period
├── completed_at
└── time_in_app_seconds        -- counts toward daily completion threshold
```

**Completion-rule proposal (unconfirmed):** Required lesson completion, knowledge-check qualification and any minimum-time rule need explicit acceptance criteria. The earlier “passed (or attempted)” wording and three-minute default are not approved equivalents or implementation guarantees. PD-007's reading estimate is not itself a completion threshold; optional-reader time cannot satisfy a required lesson/gate condition.

### Optional Lesson Reading and Progress Attribution (Design Specification)
- Each lesson may link its own optional extended reading, identified by a unique `reading_id` and `lesson_id`. The internal registry derives `reading_id` as `{lesson_id}-optional` and records `content_type: optional_extended_reading`, `required_for_gate: false`, `include_in_texting_curriculum: false`, and estimated duration.
- Proposed optional-reading progress is stored separately in `user_optional_reading_progress` (`user_id`, `reading_id`, `last_position`, `last_accessed_at`). It does not write lesson status, curriculum completion percentages, or daily learning-node state.
- Keep optional-reader time separate from required lesson time and any configured daily threshold. Reading may be resumed; it does not create an additional required daily assignment.
- Only the existing lesson-completion logic contributes to its current streak/gate effects. Optional-reading activity does not emit `lesson_completed` or `learning_streak_extended` and is excluded from lesson-time analytics.
- Any future texting export filters out `content_type: optional_extended_reading`, regardless of the user's reading preference.

These are internal tracking boundaries, not learner-facing copy, a new UI rule, or an implemented schema migration. See the internal [reading source index](../knowledge/curriculum/foundations-of-movement/reading-options.md).

---

## Analytics Events (PostHog)
| Event | Properties |
|---|---|
| `lesson_started` | lesson_id, program_id, chapter_id, subchapter_id, source |
| `lesson_completed` | lesson_id, time_spent, score |
| `knowledge_check_attempted` | lesson_id, score, attempt_number |
| `learning_streak_extended` | streak_days |
| `learning_streak_broken` | streak_days_lost, day_of_week |
| `friend_poke_sent` | sender_id (anonymized) |
| `friend_poke_received` | — |
