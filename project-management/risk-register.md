# PhysiApp Risk Register

## Risk Triggers
This is the set of conditions that should trigger a pause, reassessment, or backlog cut.

### Product risk triggers
- Core learning path is not stable before new social features are introduced
- Training and learning goals are merged into one signal and become confusing
- A feature does not map to a clear user insight or KPI
- Growth features create pressure mechanics that conflict with consistency-first philosophy

### Technical risk triggers
- A task grows beyond the initial decomposition without a new plan
- Duplicate logic is emerging across several modules without helper extraction
- Refactors begin to exceed the scope of the task
- Repo health is poor enough to slow down inter-team work
- A third-party API becomes a runtime dependency for logging, learning completion, or progression
- Mobile or web clients contain provider secrets
- Offline mutations cannot be applied idempotently after retry

### UX and accessibility risk triggers
- Key interaction patterns are not testable or readable on mobile
- Touch targets and screen-reader flows are not reviewed before release
- The app begins to rely on color or urgency to communicate critical states

### Compliance risk triggers
- A feature starts collecting data without legal review
- A claim begins to imply diagnosis, treatment, or medical certainty
- Content or app messaging is not clear about non-medical status
- Imported content lacks a verified source, creator, license, attribution, or usage approval
- Provider content is cached, modified, redistributed, or retained beyond its license terms
- A provider changes pricing or terms without a documented reassessment

### Content risk triggers
- The curriculum grows without a sequence or prerequisite model
- Lessons become too long or too broad to track and validate
- Topic quality is not reviewed by the Physio Consultant before content is published
- External exercise or routine content enters a curated plan without physio and editorial review

## Risk Response Rule
When a trigger is reached:
1. document the risk
2. pause or narrow the work
3. update the backlog and roadmap
4. decide whether to fix, defer, or remove the scope

## Risk Ownership
Each risk should have an accountable owner, even when the response is deferral.
