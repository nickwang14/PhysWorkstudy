# Definition of Done

A feature is not done until all required checks are complete.

## Required Checks

### Product and scope
- [ ] The work matches the approved scope and owner
- [ ] The task still fits the active MVP / V1 / V2 slice
- [ ] No scope drift was introduced without a decision record

### Design and UX
- [ ] The design matches the target interaction flow
- [ ] User states are defined: empty, loading, success, error, locked, complete
- [ ] Accessibility review is completed for the affected screen or flow
- [ ] Copy is reviewed for tone and compliance boundaries

### Engineering and logic
- [ ] The implementation matches the spec or documented deviation
- [ ] App changes follow PD-008's Kotlin/Compose direction and the owner-mediated [development handoff workflow](../docs/development-workflow.md); future web/services are not treated as implemented by assumption
- [ ] Validation around state transitions is written or at least planned
- [ ] Error handling, retry behavior, and offline behavior are considered
- [ ] No duplicate logic or copy-paste code was introduced without justification
- [ ] Third-party integrations keep credentials server-side and have quota, timeout, retry, and fallback behavior

### QA and validation
- [ ] Core happy path is tested
- [ ] Actual Gradle/Android checks and device/AI Studio evidence are recorded separately; unavailable SDK/toolchain, unconfigured tests and unrun checks remain explicit blockers
- [ ] Edge cases are reviewed
- [ ] Regression risk is noted
- [ ] Any bug fix includes a specific validation check

### Compliance and trust
- [ ] Medical and non-medical boundary language is respected
- [ ] Consent and data disclosures are reviewed where relevant
- [ ] Privacy and app policy impact was checked for the feature
- [ ] Imported content records its source, creator, license, attribution, review status, and allowed retention

### Release readiness
- [ ] Analytics events and KPI tracking are defined
- [ ] Feature is ready for review by the PM and relevant specialist
- [ ] Any remaining work is clearly captured and triaged

## Definition of Done Default Rule
A feature is only considered done when it is:
- built,
- validated,
- documented,
- measurable,
- and consistent with the product philosophy.

Anything less is work in progress, not done.
