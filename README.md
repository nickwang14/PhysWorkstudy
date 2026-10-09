# PhysiApp - Evidence-Based Movement Science & Training Planner

PhysiApp is already built as a **Kotlin/Jetpack Compose Android application**, supporting movement-science learning and weekly training under the **Consistency Over Intensity** philosophy. The owner **orchestrates in VS Code and builds/implements the app with Google AI Studio**; see the [development workflow](docs/development-workflow.md) and owner-confirmed [PD-008](docs/product-decisions.md). This workflow does not imply automatic integration or a successful Android build without actual evidence.

## Repository Status and Guidance

- Current application source/build: `app/` and Gradle. Use Kotlin/Compose and the native Android stack; backend/cloud services remain proposed, and future web-companion technology is undecided.
- Product policy: [product decisions](docs/product-decisions.md). Execution/scope: [project management](project-management/README.md). Agent role defaults are subordinate to those records.
- Academic content: [knowledge repository](theory-and-knowledge/knowledge/README.md) and [curriculum workflow](theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md). Authored material remains draft until actual reviews are recorded; use the live reading registry for coverage.
- Agent setup: [AGENTS.md](AGENTS.md) and [shared skills](.agents/skills/README.md). Run `python tools/setup_agent_platforms.py` after cloning. Skills are authored once in `.agents/skills/`; Copilot profiles are generated from canonical Claude profiles.
- Content utilities: [tools/README.md](tools/README.md). Source PDFs and review previews are local-only, not distributed app assets.
- Firebase maintenance: [local setup and management](docs/firebase-local-development.md), [schema at a glance](docs/firestore-schema.md). Emulator-first tooling supports explicit named-database reads and guarded document writes; live access requires local Google ADC and IAM.
- Google Cloud CLI: [local toolchain and authentication](docs/google-cloud-local-setup.md). User-local binaries, a dedicated project configuration, and credentials outside the repository; no cloud deployment is implied.
- Android installer: [local build, signing, and validation](docs/android-build-and-installer.md). The development key stays local; the root installer ZIP is tracked, and private provider keys are omitted from shared builds.
- Firebase and credentials: [setup and key-storage guide](docs/firebase-and-keys.md). Register the actual Android application ID, keep local configuration out of Git, and never ship private provider/admin keys in an APK.

The feature outline below describes the app's product intentions, not a production-readiness, scientific-validation or completeness checklist; actual acceptance/release status belongs in project-management records.

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
- The content repository plans **seven foundation chapters and five advanced chapters**, under Program → Chapter → Subchapter → Lesson gates. Foundation topics:
  1. Foundations of Movement & Terminology
  2. Anatomy & Physiology of Movement
  3. Biomechanics & Motion Analysis (Torque, Moment Arms, Levers)
  4. Load, Fatigue & Periodization
  5. Weekly Training Planning & Split Architecture
  6. Goal-Based Progression and Deloads
  7. Applied Exercise Decision-Making and Coaching Logic
- See the [curriculum overview](theory-and-knowledge/knowledge/curriculum/foundations-of-movement/chapter-overview.md) for the advanced continuation and live guides for draft/outline coverage. In-app content is not automatically synchronized with every authored Markdown lesson.
- Sequential unlocking as prerequisite knowledge checks are mastered.

### 3. Planned Deload Periodization (PD-002)
- Every 6th week of training is automatically designated as an active recovery (deload) week.
- Volume reduced by ~40-50% while preserving motor patterns at moderate RPE (5-6).
- Use positive, non-medical planned-recovery framing. The six-week cadence is product policy, not a universal physiological guarantee; retain explicit defer/acknowledgment behavior from PD-002 and obtain domain review for explanatory claims.

### 4. Six Fundamental Movement Patterns
- Exercise-library organization follows six core movement patterns:
  - **Squat**: Goblet Squat, Bulgarian Split Squat
  - **Hinge**: Romanian Deadlift, Kettlebell Swing
  - **Push**: Dumbbell Bench Press, Standing Overhead Press
  - **Pull**: Single-Arm Dumbbell Row, Cable Face Pull
  - **Carry**: Farmer's Walk, Suitcase Carry
  - **Rotate / Anti-Rotate**: Pallof Press, Bird Dog
- Educational cues, regressions and progressions require source/domain review and must not imply individualized diagnosis or treatment.
- Pattern-balance visualization is an educational aid, not a guarantee of symmetrical joint loading or a clinical assessment.

### 5. In-Session Workout Runner & Rest Timer
- Real-time workout session logging for sets, reps, weight, and RPE.
- Interactive countdown rest timer sheet with haptic vibration alerts and quick preset adjustments.
- Support for curated programs as well as custom user workout entries.

### 6. Autoregulated Readiness Check-In
- Daily 1-minute survey for Sleep, Soreness, and Energy.
- Non-medical educational guidance based on self-reported readiness; not diagnosis, treatment, or automated clinical advice.

### 7. Consistency-Based Gamification (PD-004)
- Badges and milestone celebrations are strictly anchored to consistency, showing up, and deload compliance, never load ego or injury-inducing intensity.

## Current Android Technical Details
- **UI Framework**: Jetpack Compose with Material 3 (M3)
- **Architecture**: MVVM with Kotlin StateFlow & Coroutines
- **Target SDK**: Android 36, Min SDK 26
- **Gradle**: 9.3.1 with Android Gradle Plugin (AGP) 9.1.1
- **Icons & Assets**: Custom adaptive vector launcher icon and sports biomechanics hero banner
