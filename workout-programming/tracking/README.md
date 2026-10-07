# Workout Programming — Progress & Tracking

This folder describes **proposed data schemas, tracking logic, and progress models** for the training track, not evidence of implemented schemas or backend features. Follow [shared repository guidance](../../AGENTS.md) and authoritative [product decisions](../../docs/product-decisions.md); fixed deload cadence is product policy, not a universal physiological guarantee.

## Tracking Domains

### Weekly Training Goal
The training goal is **weekly** — users set a target number of workouts per week (minimum 2).
There is no daily training streak. Consistency is measured week-over-week.

```
user_training_goals
├── user_id
├── workouts_per_week_goal     -- user-set, min 2
├── primary_goal               -- strength | hypertrophy | endurance | general_fitness
├── current_program_id
├── current_week_number        -- week within the current program cycle
└── updated_at

weekly_training_progress
├── user_id
├── week_key                   -- boundary/timezone policy pending; ISO encoding is a proposal
├── workouts_logged            -- count of logged workouts this week
├── workouts_goal              -- snapshot of goal at week start
├── goal_met                   -- bool
├── is_deload_week             -- bool (auto-set every 6th week)
└── busyness_signal            -- 1–5, user self-reported at week start
```

**Streak (training):** Consecutive policy-defined weeks where `workouts_logged >= workouts_goal`. Week start, timezone attribution and grace/makeup boundaries need explicit decisions; ISO weeks are a proposal, not established policy.
This is a *weekly* streak, displayed separately from the learning daily streak.

See [learning tracking](../../theory-and-knowledge/tracking/README.md) for the independent daily system. Lesson and optional-reading activity do not count as logged workouts or satisfy the weekly training goal.

---

### Deload Cycle
```
program_cycles
├── user_id
├── program_id
├── cycle_start_date
├── weeks_completed
├── deload_week_number         -- default: every 6th week
└── next_deload_date           -- computed: cycle_start + (deload_week_number × 7 days)
```

**Deload rule:** Every 6th week is automatically designated as a light week.
- Volume reduced ~40–50% (fewer sets, not fewer exercises)
- Intensity maintained at moderate (RPE 5–6, not max effort)
- Movement patterns preserved — not a rest week
- User is informed 3 days in advance: *"Your light week starts Monday — this is planned recovery."*
- Cannot be silently skipped; user must explicitly defer with a reason logged.

---

### Workout Logs
```
workout_logs
├── id
├── user_id
├── logged_at                  -- timestamp (timezone-aware)
├── week_key                   -- derived from logged_at using approved week/timezone rules
├── workout_type               -- push | pull | legs | upper | lower | full_body | custom
├── is_deload                  -- bool
├── source                     -- program | custom | user_entry
├── duration_minutes
└── notes

workout_log_exercises
├── workout_log_id
├── exercise_id
├── set_number
├── reps
├── weight_kg
├── rpe                        -- optional: Rate of Perceived Exertion 1–10
└── completed                  -- bool
```

---

### Adaptive Split Engine — Audit Log
```
adaptive_split_computations
├── user_id
├── computed_at
├── input_workouts_last_week   -- JSON array of workout_log ids
├── input_goal                 -- user's primary goal at computation time
├── input_busyness             -- 1–5
├── input_available_days       -- optional
├── output_suggested_split     -- JSON (e.g., ["Push","Pull","Legs"])
└── rule_version               -- version of adaptation rules applied
```
All inputs and outputs are logged for debugging, physio review, and future ML training data.

---

## Analytics Events (PostHog)
| Event | Properties |
|---|---|
| `workout_logged` | workout_type, duration, is_deload, source |
| `weekly_goal_met` | workouts_logged, workouts_goal, week_key |
| `weekly_goal_missed` | workouts_logged, workouts_goal, week_key |
| `deload_week_started` | week_number, program_id |
| `adaptive_split_accepted` | suggested_split, rule_version |
| `adaptive_split_modified` | suggested_split, user_split, modification_type |
| `training_streak_extended` | streak_weeks |
| `training_streak_broken` | streak_weeks_lost |
