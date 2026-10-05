# PhysiApp - Evidence-Based Movement Science & Training Planner

PhysiApp is a native Android application built with Kotlin and Jetpack Compose. It translates academic physiotherapy, biomechanics, and exercise physiology into a daily learning habit and a weekly training planner governed by the **Consistency Over Intensity** philosophy.

## Core Features & Architecture

### 1. Dual-Track Consistency System (PD-001)
- **Daily Learning Track**:
  - Bite-sized daily micro-lessons (5-10 minutes) covering human movement science.
  - Interactive knowledge checks with instant physiological explanations and rationales.
  - "Apply It Today" real-world movement tests and postural cues.
  - Daily habit streak tracking.
- **Weekly Training Track**:
  - Flexible workout goal target (minimum 2 workouts per week; default 3-4).
  - Rest days are framed as vital recovery and tissue remodeling wins, avoiding daily workout guilt.
  - Weekly goal streak tracking.

### 2. Continuous Concept Progression Path (PD-006)
- Gamified visual progression journey with concept gates (similar to Duolingo/Candy Crush).
- Lessons organized across 5 core academic modules:
  1. Foundations of Movement & Terminology
  2. Anatomy & Physiology of Movement
  3. Biomechanics & Motion Analysis (Torque, Moment Arms, Levers)
  4. Load, Fatigue & Periodization
  5. Weekly Training Planning & Split Architecture
- Sequential unlocking as prerequisite knowledge checks are mastered.

### 3. Planned Deload Periodization (PD-002)
- Every 6th week of training is automatically designated as an active recovery (deload) week.
- Volume reduced by ~40-50% while preserving motor patterns at moderate RPE (5-6).
- Positive framing: active recovery facilitates connective tissue remodeling, central nervous system rejuvenation, and supercompensation.

### 4. Six Fundamental Movement Patterns
- Complete exercise library organized around the 6 core functional pillars:
  - **Squat**: Goblet Squat, Bulgarian Split Squat
  - **Hinge**: Romanian Deadlift, Kettlebell Swing
  - **Push**: Dumbbell Bench Press, Standing Overhead Press
  - **Pull**: Single-Arm Dumbbell Row, Cable Face Pull
  - **Carry**: Farmer's Walk, Suitcase Carry
  - **Rotate / Anti-Rotate**: Pallof Press, Bird Dog
- Detailed clinical cues, common compensations to avoid, regressions, and progressions for every exercise.
- Kinetic Pattern Balance visualizer to ensure symmetrical joint loading.

### 5. In-Session Workout Runner & Rest Timer
- Real-time workout session logging for sets, reps, weight, and RPE.
- Interactive countdown rest timer sheet with haptic vibration alerts and quick preset adjustments.
- Support for curated programs as well as custom user workout entries.

### 6. Autoregulated Readiness Check-In
- Daily 1-minute survey for Sleep, Soreness, and Energy.
- Instant clinical coaching advice based on fatigue metrics.

### 7. Consistency-Based Gamification (PD-004)
- Badges and milestone celebrations are strictly anchored to consistency, showing up, and deload compliance, never load ego or injury-inducing intensity.

## Technical Details
- **UI Framework**: Jetpack Compose with Material 3 (M3)
- **Architecture**: MVVM with Kotlin StateFlow & Coroutines
- **Target SDK**: Android 36, Min SDK 26
- **Gradle**: 9.3.1 with Android Gradle Plugin (AGP) 9.1.1
- **Icons & Assets**: Custom adaptive vector launcher icon and sports biomechanics hero banner
