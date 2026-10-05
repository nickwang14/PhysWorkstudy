# PhysiApp MVP Scope

## Product Goal
Ship the smallest version of PhysiApp that proves the core thesis:
- users can build consistent workout habits
- users can learn core movement and training concepts through a structured curriculum
- users can see meaningful weekly progress without being punished for flexible schedules

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
- Onboarding
- Home or dashboard
- Workout logging flow
- Learning flow
- Settings and goal configuration

### 5. Companion web dashboard
- Progress summary
- Curriculum browsing overview
- Read-only review screens only
- Account and program settings

### 6. Quality gates
- QA pass for core flows
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