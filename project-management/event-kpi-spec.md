# Event and KPI Specification

This document defines the minimum analytics and measurement model for PhysiApp.

## Product Metrics by Area

### Learning track
- `lesson_started`
- `lesson_completed`
- `knowledge_check_completed`
- `learning_streak_extended`
- `learning_streak_broken`
- `learning_daily_goal_met`

Core KPIs:
- daily lesson completion rate
- weekly learning retention rate
- average time-to-complete lesson
- knowledge check pass rate
- daily streak continuation rate

### Training track
- `workout_logged`
- `weekly_goal_met`
- `weekly_goal_missed`
- `deload_week_started`
- `adaptive_split_suggested`
- `adaptive_split_accepted`
- `training_streak_extended`
- `training_streak_broken`

Core KPIs:
- workouts per week average
- percentage of weeks meeting goal
- deload compliance rate
- adaptation acceptance rate
- training consistency trend by user cohort

### Product engagement
- `progression_map_opened`
- `progression_map_completed`
- `web_dashboard_viewed`
- `milestone_shared`
- `encouragement_sent`

Core KPIs:
- weekly active users
- feature activation rate
- retention at 7/30 days
- days active per week
- completion rate of learning and training loops

## Event Rules
- Every event must be tied to a user and a clear purpose
- All tracking should be minimal and explainable
- Avoid reuse of vague events like `completed` without context
- Store enough metadata to segment behavior by goal, program, or lifestyle context

## KPI Quality Checks
Before a feature is considered ready, answer:
- What is the user behavior we want to improve?
- What metric proves it?
- What is the baseline we expect?
- What will we do if the metric does not move?

## Reporting Requirements
The product should be able to answer:
- Are users completing learning daily?
- Are users meeting weekly training goals?
- Are users dropping off after the onboarding loop?
- Are deload and adaptive split suggestions actually being accepted?
- Are users engaging in a durable, consistent pattern, or only short bursts?

## KPI Guardrails
- Do not optimize for vanity metrics alone
- Do not track actions without a decision response plan
- Do not use intensity metrics as the primary engagement signal
