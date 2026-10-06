# PhysiApp - Product Decisions

This document tracks key product decisions made outside of individual agent roles. It serves as the authoritative record of why we made a design choice, not just what it is.

---

## Decision Log

---

### PD-001: Dual-Track Streak Model
**Date:** 2026-08-06
**Status:** Decided

**Decision:**
PhysiApp has two independent streak or goal systems:

| Track | Cadence | Type | Social |
|---|---|---|---|
| **Learning** | Daily | Streak (consecutive days) | Friend poke enabled post-MVP |
| **Training** | Weekly | Goal (x workouts per week, min 2) | Shared milestone celebrations post-MVP |

**Rationale:**
- Learning is a daily habit (5-10 min/day), so daily streaks and optional friend nudges can fit later without pressuring recovery.
- Training is not a daily commitment, so weekly goals better accommodate flexible schedules and reduce guilt pressure.
- Mixing the two would punish rest days and undermine the consistency-over-intensity philosophy.

**Friend poke scope:** Enabled only on the learning streak. A friend can send an encouragement nudge when a user has not yet completed today's learning node and it is evening. This is not implemented for the training track and is deferred until post-MVP.

**Social mechanics for training:** Encouragement sends and shared weekly milestone celebrations are allowed in the future product, but they are also deferred until post-MVP.

---

### PD-002: Deload Week Cadence
**Date:** 2026-08-06
**Status:** Decided

**Decision:**
Every 6th week of a training program is a designated light (deload) week.

**Definition of light week:**
- Volume reduced about 40-50 percent (fewer sets, same exercise patterns)
- Intensity maintained at moderate effort (RPE 5-6)
- Movement patterns preserved; this is active recovery, not a rest week
- User is notified 3 days in advance with positive framing: "Your light week starts Monday - this is planned recovery that helps your body adapt."
- Cannot be silently skipped; user must explicitly defer (reason logged for program adaptation context)

**Rationale:**
- Deload weeks are standard practice in periodized programming (NSCA, ACSM frameworks)
- A 6-week cycle is a common and well-supported interval for recreational and intermediate trainees
- Proactive framing keeps the consistency-first philosophy intact; deload is part of showing up, not an absence

**Physio Consultant to define:** exact volume reduction rules per split type and goal tier.

---

### PD-003: Editorial Point of View - Curated Program is the Core Product
**Date:** 2026-08-06
**Status:** Revised 2026-08-06

**Decision:**
PhysiApp is a full-featured app with a strong editorial point of view: the curated, physio-validated program and academic curriculum are the core product, not an optional add-on.

Users can:
- Enter their own workouts (custom logging - counts toward weekly goals)
- Adjust program configurations (workouts per week, goal type)
- Browse and explore program and curriculum content freely

Users cannot (without explicit override):
- Skip deload weeks without acknowledgment
- Set workouts per week below 2

The app is designed to become shareable over time through progress screenshots, milestone cards, and similar surfaces. This is what "marketing surface" means: the product markets itself through the quality of the experience, not that it is a marketing page. These sharing features are not required for MVP.

**Rationale:**
The differentiator is editorial quality (physio-validated programs plus academic curriculum). Custom logging exists so users are never blocked, but the app always surfaces and guides users back to their structured program path.

**Implication for design:**
The progression map is always anchored to the curated program. Custom entries appear in logs and count toward goals, but do not drive map progression on their own.

---

### PD-004: Gamification Anchors on Consistency, Not Intensity
**Date:** 2026-08-06
**Status:** Decided

**Decision:**
No reward, badge, celebration, or social mechanic in PhysiApp may be triggered by an increase in intensity, load, or performance.

**What we reward:**
- Completing today's learning node
- Logging a workout (any workout)
- Hitting weekly training goal
- Completing a deload week (same celebration as a regular week)
- Reaching a streak milestone (learning) or weekly goal streak (training)
- Finishing a chapter, subchapter, or program milestone

**What we do not reward with primary mechanics:**
- New PRs or load increases
- Volume increases
- Beating another user's performance

**Rationale:**
Intensity-based rewards create incentive misalignment. They encourage overtraining, pushing through fatigue, and injury. PhysiApp's target user is building a long-term habit, not chasing short-term performance peaks.

---

### PD-005: Platform Roles - Android Primary, Web Companion
**Date:** 2026-08-06
**Status:** Decided

**Decision:**
PhysiApp has two platform surfaces with distinct roles:

| Platform | Role | Features |
|---|---|---|
| **Android** | Primary product | Full feature set over time: progression map, workout logging, learning modules, adaptive splits, streaks, notifications, and later social surfaces |
| **Flutter Web** | Companion dashboard | Progress review, curriculum browsing, account settings, and later shareable milestone pages - no workout logging |

Both are built from a single Flutter codebase. Flutter Web is not a separate product or a marketing landing page - it is a read-oriented companion that lets users check in from a desktop and, in later phases, share progress links.

**What Flutter Web includes over the product roadmap:**
- Progress dashboard (weekly training goal status, learning streak, program position)
- Curriculum browser (explore chapters and lessons without interactive completion)
- Shareable milestone or streak cards (public URL, no auth required, post-MVP)
- Account and program settings

**What Flutter Web excludes (Android-only):**
- Workout logging (requires gym-context, offline-first, mobile UX)
- Daily learning node completion (push notifications and daily habit loop are mobile-native)
- Progression map interaction (node completion triggers are mobile-only)

**Rationale:**
Workout logging and daily learning are active, in-the-moment behaviors that belong on the device in your pocket. The web surface serves review, sharing, and onboarding - not the daily habit loop. This keeps the Flutter Web scope contained and avoids building two full products.

**MVP note:**
Shareable and social web surfaces are deferred until after the core Android and curriculum loops are stable.

**Implication for architecture:**
The Solutions Architect must ensure Flutter Web only renders read-only views of state owned by the mobile client and backend. No web-only write paths for core progression data.

---

### PD-006: Continuous Learning Path - Chapter and Subchapter Gates
**Date:** 2026-09-10
**Status:** Decided

**Decision:**
The learning system is a continuous progression path, not a semester-based model. The map is organized by chapter and subchapter gates, each representing a coherent concept cluster.

**Structure:**
- Program (for example, Foundations of Movement)
- Chapter (major concept cluster)
- Subchapter (focused concept group)
- Lesson or micro-lesson
- Knowledge check plus application step

This should feel like Duolingo or Candy Crush: users unlock new gates as they complete the prerequisite concept work, rather than following fixed school-year timelines.

**Rationale:**
A continuous structure fits the product's habit-forming loop. It allows learning to feel progressive, modular, and flexible without forcing users into semester-style pacing. It also supports the app's daily completion model while preserving a clear academic backbone.

**Implication for design:**
The progression map should represent concept gates, not calendar periods. Curriculum content should support rolling unlock logic, prerequisite dependencies, and chapter-based mastery progression.

---

### PD-007: Fuller Daily Updates with Lesson-Specific Optional Reading
**Date:** 2026-10-05
**Status:** Decided (content drafts; delivery preference not yet implemented)

**Decision:**
Daily updates contain substantive teaching rather than only a short summary: approximately 3–5 minutes of reading, with existing knowledge checks and application bringing the standard update to approximately eight minutes. Each authored lesson may link to its own distinct, optional extended reading. Target 15–20 minutes; relevant assignments may run up to about 30 minutes, while shorter source selections should offer additional optional material rather than filler. A genuinely short or transitional lesson may omit an extension.

**Delivery:**
- A lesson may offer a **Read more** link to its own optional reading assignment.
- Optional readings can be skipped or resumed. Their contribution to progression and daily accounting is governed internally; those rules are not advertised in the educational reading copy.
- Optional reading and its textbook assignments must not be built into the texting curriculum. No texting or longer-update UI is introduced by this decision.

**Academic basis:**
Use selected, verified sections from the four local textbooks in `docs` with original synthesis and worked cases. Specify inclusive PDF and printed page ranges and estimate the actual assigned reading, not just the guide's length. Preserve source rights and professional review boundaries.

**Current coverage:**
All 73 authored lessons in Chapters 1–4 have expanded daily-reading drafts. Optional extended reading is associated with the individual lesson, with distinct source selections. See the internal [reading source index](../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/reading-options.md).

**Rationale:**
Increase depth and usefulness for everyday learning while preserving the 5–10-minute standard habit and allowing interested learners to go further without extra progression pressure.

---

*Add new decisions below this line in PD-NNN format.*