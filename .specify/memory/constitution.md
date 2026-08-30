<!--
Sync Impact Report
- Version change: uninitialized scaffold -> 1.0.0
- Modified principles: scaffold placeholders -> Code Quality; Testing; User Experience;
	Performance; Maintainability
- Added sections: Quality Gates; Delivery Workflow; Governance
- Removed sections: none
- Follow-up TODOs: none
-->
# Kimori Constitution

## Core Principles

### I. Code Quality Is Non-Negotiable
Production code MUST be clear, cohesive, and consistent with established project patterns.
Changes MUST keep public contracts explicit, validate inputs at boundaries, and avoid dead code,
duplicated business rules, and unreviewed dependencies. Complexity MUST be justified in the pull
request or change description because maintainability is a product requirement.

### II. Tests Prove Behavior
Every behavior change MUST include automated tests at the lowest practical level. Bug fixes MUST
include a regression test that fails before the fix. Changes to API contracts, persistence,
authentication, or service integrations MUST include integration or contract coverage. The
relevant test suite MUST pass before merging because unverified behavior is not deliverable.

### III. Consistent User Experience
User-facing behavior MUST be predictable and consistent across equivalent screens, endpoints, and
states. New flows MUST define success, loading, empty, validation-error, and failure behavior as
applicable. Names, messages, response shapes, and interaction patterns MUST reuse established
conventions; deviations require an explicit product rationale.

### IV. Performance Is a Feature
Changes MUST avoid unnecessary network calls, repeated database work, unbounded reads, and
blocking operations on request paths. New or materially changed user-facing operations MUST define
an expected latency or throughput target and be measured when they affect a known hot path.
Performance regressions MUST be corrected or explicitly accepted with an owner and follow-up plan.

### V. Maintainability Through Small Changes
Work MUST be delivered in small, reviewable units with a single clear purpose. New abstractions
MUST solve a demonstrated local need and preserve straightforward debugging and testing. Technical
debt introduced deliberately MUST be documented with its impact and planned remediation.

## Quality Gates

Before merge, changes MUST compile, pass their relevant automated tests, and have no unresolved
errors from configured static analysis. Reviewers MUST verify applicable core principles, API and
data-contract compatibility, error handling, and performance implications. A failing quality gate
requires a documented exception approved by the code owner.

## Delivery Workflow

Requirements MUST be expressed as observable behavior before implementation. Implementations MUST
be reviewed by at least one contributor other than the author when the repository workflow permits.
Release-impacting changes MUST include migration, rollback, and monitoring considerations when
applicable. Documentation and examples MUST be updated when externally observable behavior changes.

## Governance

This constitution supersedes conflicting development conventions. Amendments MUST identify the
affected principles, rationale, migration impact, and semantic version increment. MAJOR versions
remove or redefine governing requirements; MINOR versions add principles or materially expand
guidance; PATCH versions clarify wording without changing obligations. Each change review MUST
verify compliance with this constitution, and recurring exceptions MUST result in either corrective
work or a proposed amendment.

**Version**: 1.0.0 | **Ratified**: 2026-08-30 | **Last Amended**: 2026-08-30
