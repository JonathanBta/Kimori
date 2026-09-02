# Implementation Plan: Kimori Family Tree Builder

**Branch**: `001-family-tree-builder` | **Date**: 2026-08-30 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-family-tree-builder/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Kimori lets a Google-authenticated user create and switch between multiple named family trees, populate
each tree with people (name, dates, picture, comments, PDF birth/death certificates), and connect people
with parent/child and couple/married relationships. The active tree renders as a single-page, pannable and
zoomable hierarchy diagram; a right-hand panel opens on person selection or "add person" and every save
immediately re-renders the diagram. Technical approach: an Angular single-page frontend calls a Spring Boot
REST API; the API validates Google-issued ID tokens, enforces per-user data isolation, and persists trees,
people, and relationships in Firestore, with pictures and PDF certificates stored in Firebase Storage.

## Technical Context

**Language/Version**: TypeScript (Angular 18+) for frontend; Java 21 (Spring Boot 4.1) for backend

**Primary Dependencies**: Angular (standalone components, Angular Router), an Angular-compatible hierarchy/graph
rendering library (e.g., d3-hierarchy or a dedicated org-chart/tree component) for the diagram, Angular Fire or
Google Identity Services for Google Sign-In on the client; Spring Boot Web MVC, Spring Security (OAuth2
resource server / Firebase ID token verification), firebase-admin SDK (Firestore + Storage), springdoc-openapi

**Storage**: Firestore (trees, people, relationships — document/collection model, scoped per authenticated user
UID) and Firebase Storage (person pictures and PDF certificates)

**Testing**: Jasmine/Karma (or Jest, if the team prefers) + Angular Testing Library for frontend unit/component
tests; JUnit 5 + Spring Boot Test (MockMvc, `@SpringBootTest`) and Testcontainers/Firestore emulator for backend
unit and integration tests

**Target Platform**: Modern evergreen web browsers (desktop-first, responsive), backend deployed as a
containerized Spring Boot service

**Project Type**: Web application (Angular frontend + Spring Boot backend API)

**Performance Goals**: Right-panel edits reflect in the diagram in under 1s (SC-003); diagram remains responsive
(pan/zoom, all nodes clickable) for trees up to 500 people across 5+ generations (SC-004)

**Constraints**: All API endpoints require a valid Google ID token; every query/write MUST be scoped to the
authenticated user's UID (no cross-account data access); picture uploads ≤10MB (JPEG/PNG/WEBP), PDF certificate
uploads ≤10MB; non-PDF certificate uploads and invalid dates MUST be rejected with clear errors (FR-007, FR-017)

**Scale/Scope**: Single-user-per-account data model; each tree comfortably supports up to 500 people; supports
an unbounded number of trees per user shown via header switcher (validated for at least 10 in SC-002)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Code Quality Is Non-Negotiable**: Frontend (Angular services/components) and backend (controller →
  service → repository layering) keep clear boundaries; API contracts documented via OpenAPI (springdoc).
  PASS.
- **II. Tests Prove Behavior**: Each user story maps to acceptance scenarios in spec.md that will drive
  contract tests (backend) and component/integration tests (frontend) before/alongside implementation. PASS.
- **III. Consistent User Experience**: Right panel behavior (open on select/add, immediate diagram sync),
  loading/empty/error states are defined per user story and edge case in the spec. PASS.
- **IV. Performance Is a Feature**: SC-003 (<1s diagram sync) and SC-004 (500 people, 5+ generations, no lag)
  are explicit targets carried into Success Criteria and will be validated in quickstart.md. PASS.
- **V. Maintainability Through Small Changes**: Feature decomposes into independently testable user stories
  (auth, tree CRUD, person CRUD, certificates, relationships, navigation) suitable for incremental delivery via
  tasks.md. PASS.

No violations requiring justification; Complexity Tracking table left empty.

## Project Structure

### Documentation (this feature)

```text
specs/001-family-tree-builder/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
kimori-api/                                  # existing Spring Boot backend
├── src/main/java/kimori/kimori_api/
│   ├── config/                              # FirebaseConfig, SecurityConfig (Google ID token verification)
│   ├── controller/                          # TreeController, PersonController, RelationshipController
│   ├── service/                             # TreeService, PersonService, RelationshipService, StorageService
│   ├── model/                               # FamilyTree, Person, Relationship (+ DTOs/mappers)
│   └── repository/                          # Firestore-backed repositories/data access
└── src/test/java/kimori/kimori_api/
    ├── controller/                          # MockMvc contract tests per endpoint group
    └── service/                             # unit tests for business rules (relationship validation, etc.)

kimori-web/                                   # new Angular frontend
├── src/app/
│   ├── core/                                # auth guard/interceptor, API client services
│   ├── features/
│   │   ├── trees/                           # tree list/switcher, create-tree dialog
│   │   ├── people/                           # person right-panel form, person node rendering
│   │   └── relationships/                   # relationship editing UI
│   ├── shared/                              # design system (green/nature theme), diagram component
│   └── app.routes.ts
└── src/app/**/*.spec.ts                     # component/service unit tests
```

**Structure Decision**: Web application split into the existing `kimori-api/` (Spring Boot REST API, extended
with model/repository layers for trees, people, and relationships plus Firebase-token-based security) and a new
`kimori-web/` Angular single-page app at the repository root, calling `kimori-api` over HTTPS/JSON.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No entries — Constitution Check passed without violations.

