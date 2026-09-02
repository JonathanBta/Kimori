---

description: "Task list template for feature implementation"
---

# Tasks: Kimori Family Tree Builder

**Input**: Design documents from `/specs/001-family-tree-builder/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/api.md](./contracts/api.md), [quickstart.md](./quickstart.md)

**Tests**: Included. The project constitution (Principle II, "Tests Prove Behavior") requires automated tests
for every behavior change, so each user story phase includes contract/unit/component tests alongside
implementation.

**Organization**: Tasks are grouped by user story (per spec.md) to enable independent implementation and
testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US0–US5)
- File paths are relative to the repository root (`kimori-api/`, `kimori-web/`)

## Path Conventions

Web application per [plan.md](./plan.md#project-structure): `kimori-api/src/main/java/kimori/kimori_api/`
(backend) and `kimori-web/src/app/` (Angular frontend), with backend tests under
`kimori-api/src/test/java/kimori/kimori_api/` and frontend spec files colocated as `*.spec.ts`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic tooling for both apps

- [X] T001 Create the Angular application shell in `kimori-web/` (standalone components, routing enabled) via Angular CLI
- [X] T002 [P] Add `model`, `repository`, `service`, `controller` packages under `kimori-api/src/main/java/kimori/kimori_api/`
- [X] T003 [P] Configure Firebase/Google Auth client SDK dependency in `kimori-web/package.json` and environment files (`kimori-web/src/environments/environment.ts`)
- [X] T004 [P] Define green/nature-themed design tokens (colors, spacing, typography) in `kimori-web/src/styles.scss`
- [X] T005 [P] Document local dev workflow (emulators + `mvnw` + `ng serve`) in `kimori-web/README.md` and update `kimori-api/HELP.md` per [quickstart.md](./quickstart.md)

**Checkpoint**: Both projects build and run locally with emulators before any feature work begins.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core entities, cross-cutting infrastructure, and shared UI shell required by every user story

**⚠️ CRITICAL**: No user story phase can be completed until this phase is done

- [X] T006 [P] Implement `FamilyTree` model class in `kimori-api/src/main/java/kimori/kimori_api/model/FamilyTree.java` and its Firestore-backed repository in `kimori-api/src/main/java/kimori/kimori_api/repository/FamilyTreeRepository.java`, scoped to `users/{uid}/trees`
- [X] T007 [P] Implement `Person` model class in `kimori-api/src/main/java/kimori/kimori_api/model/Person.java` and its repository in `kimori-api/src/main/java/kimori/kimori_api/repository/PersonRepository.java`, scoped to `users/{uid}/trees/{treeId}/people`
- [X] T008 [P] Implement `Relationship` model class in `kimori-api/src/main/java/kimori/kimori_api/model/Relationship.java` and its repository in `kimori-api/src/main/java/kimori/kimori_api/repository/RelationshipRepository.java`, scoped to `users/{uid}/trees/{treeId}/relationships`
- [X] T009 Implement the shared `ApiError` DTO and a `@ControllerAdvice` exception handler in `kimori-api/src/main/java/kimori/kimori_api/config/ApiExceptionHandler.java` producing the error shape from [contracts/api.md](./contracts/api.md#common-error-shape)
- [X] T010 [P] Implement `ApiClientService` (HttpClient wrapper with base URL/config) in `kimori-web/src/app/core/api-client.service.ts`
- [X] T011 [P] Implement the app shell (`AppShellComponent`) with header bar, right-panel container, and diagram canvas placeholder in `kimori-web/src/app/shared/shell/app-shell.component.ts`
- [X] T012 Wire the single-route app shell in `kimori-web/src/app/app.routes.ts`

**Checkpoint**: Foundational entities, error handling, and UI shell exist — user story implementation can now begin in parallel where independent.

---

## Phase 3: User Story 0 - Sign in with Google (Priority: P1) 🎯 MVP prerequisite

**Goal**: Gate the entire application behind Google Sign-In and enforce per-user data isolation (FR-022, FR-022a).

**Independent Test**: Load the app signed out and confirm redirect to sign-in; sign in with two different Google accounts and confirm neither sees the other's data.

### Tests for User Story 0

- [ ] T013 [P] [US0] Contract test: unauthenticated request to `GET /api/trees` returns 401 in `kimori-api/src/test/java/kimori/kimori_api/controller/AuthContractTest.java`
- [ ] T014 [P] [US0] Contract test: request with a token for user A cannot read/modify a tree owned by user B (403/404) in `kimori-api/src/test/java/kimori/kimori_api/controller/AuthContractTest.java`
- [X] T015 [P] [US0] Component test: unauthenticated route access redirects to sign-in in `kimori-web/src/app/core/auth/auth.guard.spec.ts`

### Implementation for User Story 0

- [X] T016 [US0] Implement `FirebaseTokenAuthFilter` verifying Google/Firebase ID tokens via `firebase-admin` in `kimori-api/src/main/java/kimori/kimori_api/config/FirebaseTokenAuthFilter.java`
- [X] T017 [US0] Implement `SecurityConfig` requiring authentication on `/api/**` and registering the filter in `kimori-api/src/main/java/kimori/kimori_api/config/SecurityConfig.java`
- [X] T018 [US0] Implement `AuthService` (Google Sign-In, current-user UID/token access) in `kimori-web/src/app/core/auth/auth.service.ts`
- [X] T019 [US0] Implement `authGuard` redirecting unauthenticated users to sign-in in `kimori-web/src/app/core/auth/auth.guard.ts`
- [X] T020 [US0] Implement an HTTP interceptor attaching the Bearer ID token to outgoing API requests in `kimori-web/src/app/core/auth/auth.interceptor.ts`

**Checkpoint**: Application requires Google Sign-In and every backend endpoint enforces per-user isolation.

---

## Phase 4: User Story 1 - Create and switch between family trees (Priority: P1)

**Goal**: Let a signed-in user create named trees and switch the active tree from the header (FR-001–FR-003).

**Independent Test**: Create two differently named trees, switch between them, confirm each shows only its own (empty) content.

### Tests for User Story 1

- [ ] T021 [P] [US1] Contract tests for `POST/GET/PATCH/DELETE /api/trees` (create, list, rename, delete, name validation) in `kimori-api/src/test/java/kimori/kimori_api/controller/TreeControllerTest.java`
- [ ] T021a [P] [US1] Contract/integration test: deleting a tree removes all its people, relationships, pictures, and certificate files (FR-020) in `kimori-api/src/test/java/kimori/kimori_api/controller/TreeControllerTest.java`

### Implementation for User Story 1

- [X] T022 [US1] Implement `TreeService` (create/list/rename/delete, uid-scoped) in `kimori-api/src/main/java/kimori/kimori_api/service/TreeService.java`
- [X] T022a [US1] Implement cascading tree delete in `TreeService` — remove all `people` and `relationships` subcollection documents and delete their associated pictures/certificates from Firebase Storage (FR-020) in `kimori-api/src/main/java/kimori/kimori_api/service/TreeService.java`
- [X] T023 [US1] Implement `TreeController` exposing the tree endpoints in `kimori-api/src/main/java/kimori/kimori_api/controller/TreeController.java`
- [X] T024 [P] [US1] Implement `TreeApiService` calling the tree endpoints in `kimori-web/src/app/features/trees/tree-api.service.ts`
- [X] T025 [US1] Implement the header tree switcher + "add new tree" dialog in `kimori-web/src/app/features/trees/tree-switcher.component.ts`
- [X] T025a [US1] Implement the "no trees exist" empty state in the header/diagram area with a clear call-to-action to create a new tree (Edge Case: last tree deleted) in `kimori-web/src/app/features/trees/tree-switcher.component.ts`
- [ ] T026 [P] [US1] Component test: creating and switching trees updates the active tree and diagram scope across at least 10 distinct trees (SC-002) in `kimori-web/src/app/features/trees/tree-switcher.component.spec.ts`

**Checkpoint**: Users can create, rename, delete, and switch between trees from the header, with cascading delete and an empty state when no trees remain.

---

## Phase 5: User Story 2 - Add and edit a person's details (Priority: P1)

**Goal**: Let a user add/edit a person (name, DOB, DOD, picture, comments) via the right panel, reflected immediately in the diagram (FR-004, FR-005, FR-014–FR-017).

**Independent Test**: Add a person with all fields, confirm the diagram node appears, edit a field, confirm both diagram and stored record update immediately without reload.

### Tests for User Story 2

- [ ] T027 [P] [US2] Contract tests for `POST/GET/PATCH/DELETE /api/trees/{treeId}/people` including date-of-death validation (FR-017) in `kimori-api/src/test/java/kimori/kimori_api/controller/PersonControllerTest.java`
- [ ] T027a [P] [US2] Contract/integration test: deleting a person removes or invalidates any relationships that referenced them, and the diagram no longer references the missing person (FR-019) in `kimori-api/src/test/java/kimori/kimori_api/controller/PersonControllerTest.java`
- [ ] T028 [P] [US2] Contract test for picture upload/delete endpoints (valid type/size, rejection cases) in `kimori-api/src/test/java/kimori/kimori_api/controller/PersonControllerTest.java`

### Implementation for User Story 2

- [X] T029 [US2] Implement `PersonService` (CRUD + `dateOfDeath >= dateOfBirth` validation) in `kimori-api/src/main/java/kimori/kimori_api/service/PersonService.java`
- [X] T029a [US2] Implement cascading person delete in `PersonService`/`RelationshipRepository` — remove all `Relationship` documents where the deleted person is a parent, child, or partner (FR-019) in `kimori-api/src/main/java/kimori/kimori_api/service/PersonService.java`
- [X] T030 [US2] Implement `PersonController` exposing person CRUD endpoints in `kimori-api/src/main/java/kimori/kimori_api/controller/PersonController.java`
- [X] T031 [US2] Implement picture upload/delete endpoints (type/size validation, Firebase Storage write) in `kimori-api/src/main/java/kimori/kimori_api/controller/PersonController.java` and `kimori-api/src/main/java/kimori/kimori_api/service/StorageService.java`
- [X] T032 [P] [US2] Implement `PersonApiService` calling person + picture endpoints in `kimori-web/src/app/features/people/person-api.service.ts`
- [X] T033 [US2] Implement the right-panel add/edit person form (name, DOB, DOD, picture upload, comments) in `kimori-web/src/app/features/people/person-panel.component.ts`
- [X] T033a [US2] Define and implement the right panel's loading, empty, validation-error, and save-failure states per Constitution Principle III in `kimori-web/src/app/features/people/person-panel.component.ts`
- [X] T034 [US2] Implement the diagram person-node rendering and click-to-open-panel wiring in `kimori-web/src/app/shared/diagram/diagram.component.ts`
- [X] T035 [US2] Wire panel save to trigger an immediate diagram refresh (SC-003: <1s) in `kimori-web/src/app/features/people/person-panel.component.ts`
- [ ] T036 [P] [US2] Component test: saving panel edits updates the diagram without a page reload in `kimori-web/src/app/features/people/person-panel.component.spec.ts`

**Checkpoint**: People can be added, edited, and viewed as diagram nodes with live sync from the right panel, and deleting a person leaves no dangling relationships.

---

## Phase 6: User Story 4 - Define relationships between people (Priority: P1)

**Goal**: Let a user create/remove parent-child and couple relationships, rendered correctly in the hierarchy diagram, with invalid configurations rejected (FR-009–FR-012a, FR-023).

**Independent Test**: Link two people as parent/child and confirm the diagram connection; mark two people as a couple with a shared child and confirm the child groups under the couple as one unit.

### Tests for User Story 4

- [ ] T037 [P] [US4] Contract tests for `POST/PATCH/DELETE /api/trees/{treeId}/relationships` covering valid parent/child and couple creation, cycle rejection, parent-cap rejection (max 2), and self-reference rejection in `kimori-api/src/test/java/kimori/kimori_api/controller/RelationshipControllerTest.java`
- [X] T038 [P] [US4] Unit tests for relationship validation rules (cycle detection, parent cap, couple status) in `kimori-api/src/test/java/kimori/kimori_api/service/RelationshipServiceTest.java`

### Implementation for User Story 4

- [X] T039 [US4] Implement `RelationshipService` with cycle detection, two-parent cap, and couple/status validation in `kimori-api/src/main/java/kimori/kimori_api/service/RelationshipService.java`
- [X] T040 [US4] Implement `RelationshipController` exposing relationship endpoints in `kimori-api/src/main/java/kimori/kimori_api/controller/RelationshipController.java`
- [X] T041 [P] [US4] Implement `RelationshipApiService` calling relationship endpoints in `kimori-web/src/app/features/relationships/relationship-api.service.ts`
- [X] T042 [US4] Implement relationship editing UI (link parent/child, mark couple, remove) within the right panel in `kimori-web/src/app/features/relationships/relationship-editor.component.ts`
- [X] T042a [US4] Define and implement validation-error and failure states for rejected relationships (cycle, parent cap, self-reference) per Constitution Principle III in `kimori-web/src/app/features/relationships/relationship-editor.component.ts`
- [X] T043 [US4] Implement the hierarchy layout algorithm (d3-hierarchy based) including couple-union grouping of shared children in `kimori-web/src/app/shared/diagram/diagram-layout.ts`
- [ ] T044 [P] [US4] Component test: adding/removing a relationship redraws the diagram immediately, and couple children render grouped under the union node in `kimori-web/src/app/shared/diagram/diagram.component.spec.ts`

**Checkpoint**: Full family trees can be built with parent/child and couple relationships, correctly visualized.

---

## Phase 7: User Story 3 - Attach birth and death certificates (Priority: P2)

**Goal**: Let a user upload, view, and remove PDF birth/death certificates on a person (FR-006–FR-008, FR-024).

**Independent Test**: Upload a PDF as a birth certificate, confirm it is listed and reopenable; attempt a non-PDF upload and confirm rejection.

### Tests for User Story 3

- [ ] T045 [P] [US3] Contract tests for certificate upload/delete endpoints (valid PDF, non-PDF rejection, oversized rejection) in `kimori-api/src/test/java/kimori/kimori_api/controller/PersonControllerTest.java`

### Implementation for User Story 3

- [X] T046 [US3] Implement certificate upload/delete endpoints (`.../certificates/{kind}`) with PDF/size validation in `kimori-api/src/main/java/kimori/kimori_api/controller/PersonController.java` and `kimori-api/src/main/java/kimori/kimori_api/service/StorageService.java`
- [X] T047 [P] [US3] Extend `PersonApiService` with certificate upload/delete calls in `kimori-web/src/app/features/people/person-api.service.ts`
- [X] T048 [US3] Implement certificate upload/list/remove UI within the right panel in `kimori-web/src/app/features/people/person-panel.component.ts`
- [X] T048a [US3] Define and implement upload-progress (loading) and rejection (validation-error) states for certificate uploads per Constitution Principle III in `kimori-web/src/app/features/people/person-panel.component.ts`
- [ ] T049 [P] [US3] Component test: non-PDF and oversized uploads show a clear rejection error in `kimori-web/src/app/features/people/person-panel.component.spec.ts`

**Checkpoint**: Certificates can be attached to and removed from a person's record.

---

## Phase 8: User Story 5 - Navigate a large tree visually (Priority: P2)

**Goal**: Let a user pan and zoom a large tree (up to 500 people, 5+ generations) while keeping all nodes clickable (FR-013, SC-004).

**Independent Test**: Populate a tree with several generations of people and confirm pan/zoom stays responsive with every visible node clickable.

### Tests for User Story 5

- [ ] T050 [P] [US5] Component test: pan/zoom interactions keep nodes clickable and the right panel opens without resetting the view in `kimori-web/src/app/shared/diagram/diagram.component.spec.ts`

### Implementation for User Story 5

- [X] T051 [US5] Implement pan/zoom controls (drag + wheel, e.g. `d3-zoom`) on the diagram canvas in `kimori-web/src/app/shared/diagram/diagram.component.ts`
- [ ] T052 [P] [US5] Add a dev-only seed script/utility to populate a tree with ~500 people across 5+ generations for performance validation in `kimori-web/scripts/seed-large-tree.ts`
- [ ] T053 [US5] Validate and tune rendering performance for 500-node trees against SC-004 using the seed script from [quickstart.md](./quickstart.md#6-navigate-a-large-tree-user-story-5)

**Checkpoint**: Large trees remain navigable and performant.

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Consistency, resilience, and final validation across all user stories

- [ ] T054 [P] Review and reconcile the loading/empty/error states implemented per-story (T025a, T033a, T042a, T048a) for consistent look-and-feel across header, right panel, and diagram per Constitution Principle III in `kimori-web/src/app/shared/`
- [X] T055 [P] Add a global toast/error-notification service surfacing the uniform `ApiError` shape in `kimori-web/src/app/core/notifications/notification.service.ts`
- [ ] T056 [P] Review generated OpenAPI docs (`/v3/api-docs`, `/swagger-ui.html`) for completeness against [contracts/api.md](./contracts/api.md)
- [ ] T057 Run through all validation scenarios in [quickstart.md](./quickstart.md) end-to-end and record results

---

## Dependencies & Execution Order

- **Phase 1 (Setup)** → **Phase 2 (Foundational)**: strictly blocking; no user story work starts before Phase 2 completes.
- **Phase 3 (US0 Sign in)**: depends only on Phase 2; blocks all other user story phases in practice (every endpoint requires auth), but is itself independently testable.
- **Phase 4 (US1 Trees)**: depends on Phase 2 + US0's auth wiring being in place to test end-to-end, but its tree CRUD logic can be built in parallel with US0 once models exist.
- **Phase 5 (US2 People)**: depends on Phase 4 (people live inside a tree).
- **Phase 6 (US4 Relationships)**: depends on Phase 5 (relationships link existing people).
- **Phase 7 (US3 Certificates)**: depends on Phase 5 (certificates attach to existing people); independent of Phase 6.
- **Phase 8 (US5 Navigation)**: depends on Phase 6 (needs a renderable hierarchy with relationships to stress-test).
- **Phase 9 (Polish)**: depends on all prior phases.

## Parallel Execution Examples

- Within Phase 2: T006, T007, T008 (distinct model/repository files) can run in parallel; T010 and T011 (frontend) can run in parallel with the backend tasks.
- Within Phase 3: T013, T014, T015 (test files) can be written in parallel before T016–T020 implementation.
- Within Phase 4: T021 and T021a (contract tests) can run in parallel before T022/T022a.
- Within Phase 5: T027, T027a, and T028 (contract tests) can run in parallel; T032 (frontend API service) can run in parallel with T029–T031 (backend) once contracts are agreed.
- Within Phase 6: T037 and T038 (backend tests) can run in parallel; T041 (frontend API service) can run in parallel with T039–T040 (backend).

## Implementation Strategy

**Suggested MVP**: Phases 1–6 (Setup, Foundational, US0 Sign-in, US1 Trees, US2 People, US4 Relationships) —
this delivers a fully authenticated, working family tree builder with the core value proposition (people +
relationships rendered as a diagram). Phases 7–8 (US3 Certificates, US5 large-tree navigation) are valuable
P2 enhancements that can ship incrementally afterward. Phase 9 polish should be revisited after each phase, not
only at the end.
