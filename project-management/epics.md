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

---

## Epic 6: Content Maintenance and Migration
**Goal:** Index every authored lesson with Firebase-storable metadata pointing to its local app asset, collect lesson-specific user feedback, and let authorized admins make the final publication decision.

**Status:** Backlog — owner-requested scope, 2026-10-08; planning only, not implementation or approval evidence.

**Owners:** Content Strategist (inventory/review workflow), Backend Engineer (metadata, feedback and authorization), Android Developer (asset integration and feedback), QA Engineer (migration/access checks). Product Manager owns scope; domain/safety and privacy/rights review involve Physio Consultant and Legal Compliance as required by the existing content workflow.

### User stories
- As a content maintainer, I want all authored lessons indexed with local asset pointers so that I can maintain real learning content without counting scaffolds as lessons.
- As a learner, I want to leave feedback on a lesson so that maintainers can improve its clarity and usefulness.
- As an authorized admin, I want to approve, reject, or request improvements to a lesson so that I control whether its reviewed revision can be published.

### Metadata requirements
Product requirements below do not prescribe a Firebase schema or a new implementation stack.

| Record | Required information |
|---|---|
| Lesson index | Stable lesson ID, title, program/chapter/subchapter and order, objective, summary, category, prerequisites, estimated duration, authored/scaffold classification, draft/published lifecycle, relative local app-asset pointer, content revision, authoritative lesson/source references |
| User feedback | Lesson ID and revision seen, feedback signal, submission time, submitter reference under approved privacy rules; optional comment if privacy-approved |
| Admin approval | Lesson ID and reviewed revision, decision, reviewer identity, review time, decision notes and supporting review/rights evidence; prior revision decisions remain traceable |

**Admin decisions:** **Approve**, **Reject**, **Needs improvement**. New entries have **no decision / not yet reviewed**, never inferred approval.

**Publication rule — owner-confirmed 2026-10-08:** Admin approval is the final authorization to publish the reviewed revision and encompasses rights clearance and the required content reviews. There is no separate post-approval rights-clearance gate. The admin records the supporting domain, editorial, safety, source and intended-use rights evidence as part of approval; an admin role or index entry alone is not evidence of those checks. **Approve → may publish**; **Reject / Needs improvement / no decision → may not newly publish**. Approval permits publication; indexing or submitting feedback does not publish content.

### Acceptance criteria
1. Every authored lesson, including drafts, has one current index entry with a unique ID and a pointer resolving to the matching packaged local asset. Scaffold/outline-only material is excluded. Eligibility comes from authoritative content records, not filename, chapter number or manifest count alone.
2. Metadata can be stored in Firebase and reconciled with the authored inventory. Lesson bodies remain local assets; absolute workstation paths and ignored textbook PDFs are not app pointers.
3. Re-indexing an unchanged revision creates no duplicates and preserves feedback, admin decisions and supporting evidence. Missing, moved, changed and retired assets are reported rather than silently deleted or approved.
4. Migration verifies identity, placement, teaching/check metadata and knowledge-check answer keys against authored records. Asset existence alone is not a content-parity check.
5. Users can submit feedback for a lesson and see success/failure. Authorized maintainers can review feedback associated with the revision seen; users cannot access another user's identifiable feedback. Feedback does not change approval, completion, gates, streaks or training goals.
6. Only authorized admins can record Approve, Reject or Needs improvement. Rejection/improvement decisions include actionable notes. Ordinary users cannot change shared curriculum metadata or approval decisions.
7. Admins can find unreviewed, rejected, improvement-needed and feedback-bearing lessons. Approval covers the identified revision only; a substantive content change requires renewed approval and does not inherit the old revision's publication authorization.
8. A recorded approval with its required review/rights evidence authorizes publication without another clearance decision. Other decisions block new publication. The [content lifecycle](content-operations.md) supplies the checks consolidated by admin approval, not an additional post-approval gate.

### Roadmap and delivery sequence
1. **MVP foundation:** authored/scaffold inventory and metadata contract, then authored-only indexing, asset/source parity and safe migration. Extends existing content-delivery work; basic asset-backed loading and Firestore indexing exist in source but do not establish these acceptance criteria.
2. **V1:** authorized admin review/publication decisions, followed by a minimal learner-feedback and maintainer-review flow. No new CMS or admin-web technology is selected here.

Follow the [curriculum indexing workflow](../theory-and-knowledge/knowledge/curriculum/foundations-of-movement/README.md#content-indexing-textbook-parsing-and-inclusion), existing content review requirements and owner-mediated [AI Studio handoff](../docs/development-workflow.md). Preserve source references and actual review evidence; do not regenerate textbooks or claim approval during indexing.

**Open decisions before implementation:** admin-role assignment and review access surface; feedback signal format, comment limits, identity visibility, retention/deletion and abuse handling with Legal Compliance; handling already-published content when approval is revoked or a replacement revision is rejected, including learner visibility and existing progress. These do not change the confirmed rule that approval authorizes new publication.

### Out of scope
- App, Firebase, asset or curriculum implementation in this planning update; writing lessons or converting scaffolds into authored content.
- Uploading lesson bodies/source PDFs to Firebase, public ratings, social features or a general user-generated-content platform.
- Automatic approval, mandatory feedback, or feedback/review-driven progression and rewards. Optional readings remain outside required gates, streaks and texting.
- Legacy lesson/progress migration for the current curriculum replacement, as owner-approved. Future destructive migrations require explicit approval.

### KPI and risk register
**KPIs:** 100% of eligible authored lessons indexed with valid pointers; zero scaffold/duplicate current entries; zero newly published revisions without current admin approval. Track feedback submission success and time to maintainer triage; agree operational targets after a baseline rather than rewarding feedback volume.

| Assumption / risk | Validation experiment |
|---|---|
| Asset manifest may include scaffolds or disagree with authored metadata/answer keys | Compare a representative authored/scaffold sample against authoritative records, then validate the full eligible inventory |
| Refresh may overwrite review evidence or misassociate feedback | Re-index unchanged and changed sample lessons with existing feedback/decisions; verify preservation and revision identity |
| Ordinary clients may edit the shared index or expose feedback | Test ordinary-user, admin and unauthenticated permissions; review approval evidence and feedback privacy before rollout |

Prioritized stories: [content maintenance backlog](backlog.md#epic-6--content-maintenance-and-migration).
