# PhysiApp MVP Scope

## Execution Context
The Android app is already built in Kotlin/Jetpack Compose. The owner orchestrates conversations, agents, review and Git in VS Code and builds/implements app changes with Google AI Studio. Follow [PD-008](../docs/product-decisions.md) and the [development workflow](../docs/development-workflow.md). Scope below is a delivery/validation checklist, not evidence that every proposed feature or service is implemented.

## Product Goal
Ship the smallest version that proves users can build consistent workout habits, learn movement/training concepts through a structured curriculum, and see meaningful weekly progress without punishment for flexible schedules.

## MVP Principles
- Consistency over intensity; flexible scheduling is a feature.
- Learning and training remain independent loops and completion signals.
- Android is primary; a future web companion is read-oriented with technology undecided.
- Curated programs/curriculum are the editorial core; custom logging is supportive.
- Follow authoritative product decisions, not role defaults or unverified app assumptions.

## In Scope for MVP

### 1. Learning Track
- Daily learning nodes and program → chapter → subchapter → lesson progression.
- Standard daily teaching, knowledge checks and application prompts.
- Daily learning streak and basic progress state, subject to approved qualification/grace rules.
- Lesson-specific optional readings remain separate from required checks, gates, streak requirements and texting.

### 2. Training Track
- Weekly workout goal configuration, minimum two workouts per week.
- Manual workout logging on Android and weekly progress summary.
- PD-002 six-week deload cycle with explicit acknowledgment/defer behavior.
- Basic adaptive split suggestions from approved, explainable rules.

### 3. Progression and Feedback
- Chapter/subchapter concept-gate states for learning, with a distinct weekly training-goal view.
- Achievement/milestone celebrations tied to consistency, not intensity or combined completion signals.
- Rest-day framing as planned recovery, without guilt mechanics.

### 4. Kotlin/Compose Android App
- Onboarding, home/dashboard, workout logging, learning, settings and goal configuration.
- Preserve actual state/navigation/persistence behavior; propose missing durability/backend work explicitly rather than assuming it exists.
- Review AI Studio changes against repository source, acceptance criteria and Android build/device evidence.

### 5. Future Companion Dashboard
- Progress summary and curriculum browsing/read-only review.
- No workout logging, lesson completion or core progression writes.
- Account/program settings write scope and implementation technology need explicit decisions.

### 6. Quality Gates
- QA evidence for core flows; accessibility review for primary screens.
- Privacy, non-medical disclaimer, source/domain/editorial and asset-rights review as applicable.
- Kotlin/Gradle/Android checks when configured and available; never substitute Python content-tool results for app build/device validation.

## Out of Scope for MVP
- Friend nudges/social encouragement, milestone sharing and public social feeds.
- Performance competition, leaderboards and intensity-based rewards.
- Advanced ML personalization, a full non-expert content-authoring platform and large community features.
- Full-featured web workout logging and deep analytics beyond core metrics.

## MVP Readiness and Exit Criteria
- Users can set weekly goals, log workouts, complete required learning and see the two progress tracks independently.
- Weekly/deload behavior is fair, understandable, tested and consistent with approved policy.
- Any approved companion slice reviews progress without writing mobile-owned workout/learning state.
- Core flows are validated with real users, critical progression edge cases are tested, and accessibility/privacy/content approvals are recorded.
- No social feature, optional reading, intensity incentive or guilt mechanic is needed for core participation.

Owners, priorities and current acceptance evidence are maintained in the [backlog](backlog.md), [epics](epics.md) and [definition of done](definition-of-done.md).# PhysiApp MVP Scope

## Product Goal
Ship the smallest version of PhysiApp that proves the core thesis:
- users can build consistent workout habits
- users can learn core movement and training concepts through a structured curriculum
- users can see meaningful weekly progress without being punished for flexible schedules

**Implementation context:** Extend the existing Kotlin/Jetpack Compose Android app under [PD-008](../docs/product-decisions.md#pd-008-kotlin-app-and-vs-code--google-ai-studio-workflow), not a pending framework migration. The owner orchestrates in VS Code and builds/implements in Google AI Studio via the [development handoff workflow](../docs/development-workflow.md); no autonomous integration or successful build is inferred. Backend/cloud services remain proposed, and future web-companion technology remains undecided. Scope below is a readiness target, not a claim that all features are implemented.

## MVP Principles
- Consistency over intensity
- Flexible scheduling is a feature, not a bug
- Learning and training remain separate loops
- Android is primary; web is a companion dashboard
- Program and curriculum are curated and editorial; custom log entry is supportive, not primary

## In Scope for MVP

### 1. Learning track
- Daily learning node model
- Course and lesson structure with progression tracking
- Short lesson completion and knowledge check flow
- Learning streak logic (daily)
- Basic lesson progression and percent-complete state

### 2. Training track
- Weekly workout goal configuration (minimum 2 workouts/week)
- Manual workout logging on Android
- Weekly progress summary
- Deload week cycle every 6 weeks
- Basic adaptive split suggestion from simple rules

### 3. Progression map
- Daily path or board with states for workout, learning, and combo tasks
- Achievement and milestone celebrations without intensity reward logic
- Rest-day framing as recovery wins

### 4. Android app shell
- Extend and validate the existing Kotlin/Compose shell rather than scaffold a replacement client
- Onboarding
- Home or dashboard
- Workout logging flow
- Learning flow
- Settings and goal configuration

### 5. Companion web dashboard
- Future implementation technology to be decided separately; no shared Android/web framework assumed
- Progress summary
- Curriculum browsing overview
- Read-only review screens only
- Account and program settings

### 6. Quality gates
- QA pass for core flows
- Record native Gradle/Android results and device/AI Studio evidence separately; missing SDK/toolchain or unconfigured tests remain blockers, not passing checks
- Accessibility review for primary screens
- Privacy and disclaimer review for app launch

## Out of Scope for MVP
- All friend nudges and social encouragement flows
- Milestone sharing cards and social distribution surfaces
- Social leaderboard or performance competition
- Full public social feed
- Advanced ML personalization
- Advanced content authoring platform for non-experts
- Full-featured web workout logging
- Deep analytics dashboards beyond core product metrics
- Large-scale community features

## MVP Definition of Readiness
The MVP is ready when the following are true:
- A user can set a weekly training goal and log workouts in Android
- A user can complete a learning node and see daily progress
- The system can present a fair weekly goal and a deload week
- The progression logic is understandable, testable, and stable
- A user can view progress on companion web without writing workout data
- The app does not rely on intensity-based incentives or guilt mechanics
- No social feature is required to make the core learning or training loop work

## Exit Criteria
Only leave MVP when:
- core learning and training flows are validated with real users
- critical progression edge cases are tested and stable
- the product still matches the original consistency-first thesis