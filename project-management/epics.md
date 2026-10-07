# PhysiApp Epics

**Implementation context:** [PD-008](../docs/product-decisions.md#pd-008-kotlin-app-and-vs-code--google-ai-studio-workflow) confirms the existing Kotlin/Compose Android app. Android Developer prepares and reviews owner-mediated Google AI Studio work through the [VS Code handoff workflow](../docs/development-workflow.md). Backend/cloud work remains proposed; future web technology is undecided, not a shared-framework requirement.

## Epic 1: Learning and Knowledge System
**Goal:** Build a structured learning loop that teaches human kinetics and movement reasoning.

**Key work:**
- curriculum hierarchy
- daily learning nodes
- knowledge checks
- learning streaks
- time-in-app logic

**Owner:** Content Strategist + Physio Consultant + Android Developer + Backend Engineer

**Acceptance criteria:**
- Users can complete daily learning content and track progress
- Lessons map to structured curriculum
- Daily learning progress is measurable and reviewable

---

## Epic 2: Training Program and Weekly Goals
**Goal:** Build a flexible workout path that measures consistency and adapts to real schedules.

**Key work:**
- weekly goal tracking
- training splits
- custom workout logging
- deload weeks
- adaptive split rules

**Owner:** Physio Consultant + Backend Engineer + Android Developer + QA Engineer

**Acceptance criteria:**
- Users can set x workouts/week as a minimum goal
- Deload cycle is automatically applied every 6th week
- Weekly progress is visible and explainable

---

## Epic 3: Progression Map and Daily Journey
**Goal:** Deliver a candy-crush-style daily experience without sacrificing safety or clarity.

**Key work:**
- progression map logic
- node unlocking and state transitions
- celebration and rest-day handling
- learning + training node combinations

**Owner:** Gamification Designer + UX Designer + Android Developer

**Acceptance criteria:**
- Daily map reflects actual learning and training progress
- Completion state transitions are accurate and testable
- Rest and recovery are framed as wins

---

## Epic 4: Companion Web Dashboard
**Goal:** Provide a read-oriented companion experience for desktop users and progress sharing.

**Key work:**
- web dashboard shell
- progress review screens
- curriculum browsing
- shareable milestone cards

**Owner:** Android Developer (Android data/interface handoff) + UX Designer + Solutions Architect; assign the web implementation owner when its technology is decided.

**Technology:** Undecided future web stack; preserve the companion's read-oriented role. Milestone sharing remains post-MVP, not a new MVP requirement.

**Acceptance criteria:**
- Web experience is read-only for active progress tasks
- Users can see status and next steps without writing workout data
- Milestone cards are shareable and stable

---

## Epic 5: Platform and Delivery Infrastructure
**Goal:** Keep the repo and product structure operational and maintainable.

**Key work:**
- repo hygiene
- CI/CD setup
- environment health
- accessibility and QA quality gates
- dependency maintenance

**Owner:** DevOps Engineer + Repository Maintainer + QA Engineer

**Acceptance criteria:**
- Basic CI pipeline is functioning
- Quality and accessibility gates are explicit
- Repo remains lean and maintainable
