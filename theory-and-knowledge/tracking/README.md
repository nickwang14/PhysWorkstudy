# Theory & Knowledge — Progress & Tracking

This folder defines the **data schemas, tracking logic, and progress models** for the learning track.

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

**Grace period rule:** 1 missed day per week does not break the learning streak (configurable). This is distinct from the training weekly goal — these are parallel, independent systems.

**Friend poke:** Enabled on the learning streak. A friend can send an encouragement nudge when a user's learning streak is at risk (has not yet completed a lesson today and it's evening). See `docs/product-decisions.md` for rationale.

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
├── course_id
├── topic_id
├── module_id
├── current_lesson_id
├── module_completion_pct
├── course_completion_pct
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

**Daily completion threshold:** A learning node is `completed` when: lesson is finished AND knowledge check passed (or attempted) AND time_in_app ≥ configured minimum (default: 3 minutes).

---

## Analytics Events (PostHog)
| Event | Properties |
|---|---|
| `lesson_started` | lesson_id, course_id, source |
| `lesson_completed` | lesson_id, time_spent, score |
| `knowledge_check_attempted` | lesson_id, score, attempt_number |
| `learning_streak_extended` | streak_days |
| `learning_streak_broken` | streak_days_lost, day_of_week |
| `friend_poke_sent` | sender_id (anonymized) |
| `friend_poke_received` | — |
