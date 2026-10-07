# Workout Programming — Knowledge Repository

This folder contains the **workout program templates, training frameworks, exercise definitions, and programming rules** for PhysiApp's training track.

Follow [shared repository guidance](../../AGENTS.md) and authoritative [product decisions](../../docs/product-decisions.md). These are content/design specifications, not proof of implemented training features or completed domain review. Training goals remain separate from [learning progress](../../theory-and-knowledge/tracking/README.md). For source-backed educational copy, use the canonical [curriculum workflow](../../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md#content-indexing-textbook-parsing-and-inclusion) and [textbook-learning-material skill](../../.agents/skills/textbook-learning-material/SKILL.md).

## What Goes Here

### `programs/`
Curated workout program definitions for Physio Consultant authoring and domain review; approval must be recorded, not inferred from folder placement:
```
Program Template
├── name
├── target_goal           -- strength | hypertrophy | endurance | general_fitness
├── duration_weeks        -- e.g., 12 weeks
├── workouts_per_week     -- default (2–5), user adjusts
├── deload_cycle_weeks    -- default: 6 (light week every 6th week)
├── weekly_splits[]       -- array of split options by days/week
└── progression_rules     -- how volume/intensity changes week to week
```

### `splits/` (planned; folder not yet present)
Training split templates for each goal and frequency combination:
- 2x/week: Full Body A / Full Body B
- 3x/week: Full Body A / B / C or Push / Pull / Legs
- 4x/week: Upper / Lower / Upper / Lower or Push / Pull / Legs / Full Body
- 5x/week: PPL + Upper + Lower
- Deload variants for each split (reduced volume, maintained movement patterns)

### `exercises/` (planned; folder not yet present)
Exercise definitions from a programming perspective:
- Exercise name, category (compound/isolation), primary muscles, secondary muscles
- Default rep ranges by goal (strength: 3–5, hypertrophy: 6–12, endurance: 15–20)
- Progression options: load, volume, density, range of motion
- Substitution list (equipment-free and travel alternatives)

### `adaptation-rules/` (planned; folder not yet present)
The rules the adaptive split engine uses to modify next week's plan.
Defined by the Physio Consultant; implemented as configurable rule sets by the Backend Engineer.

The fixed deload cadence below is product policy (PD-002), not a universal physiological guarantee.

---

## Programming Philosophy
- **Gamify consistency, not intensity.** Weekly goal = x workouts/week (min 2x).
- **Deload every 6 weeks.** A light week (reduced volume ~40–50%, maintain movement patterns) is built into every program cycle. This is non-negotiable and cannot be skipped by the user without an explicit override.
- **Adaptive, not prescriptive.** The app suggests next week's split based on actual logged workouts, goal, and user's self-reported busyness — never guilt-trips missed days.

## Content Ownership
| Content Type | Owner | Reviewer |
|---|---|---|
| Program templates | Physio Consultant | Product Manager |
| Training splits | Physio Consultant | — |
| Exercise definitions | Physio Consultant | Content Strategist (copy) |
| Adaptation rules | Physio Consultant | Solutions Architect (impl.) |
