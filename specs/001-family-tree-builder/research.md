# Phase 0 Research: Kimori Family Tree Builder

## 1. Frontend framework & structure

- **Decision**: Angular (standalone components, Angular CLI project `kimori-web/`) with the Angular Router for
  a single routed shell (one page, no multi-page navigation) and reactive forms for the right-panel
  create/edit form.
- **Rationale**: Explicitly required by the user. Standalone components avoid NgModule boilerplate and fit a
  single-page app with a small number of features (trees, people, relationships, diagram).
- **Alternatives considered**: NgModule-based architecture (rejected — more boilerplate for no added benefit
  at this scope).

## 2. Backend framework & structure

- **Decision**: Extend the existing Spring Boot 4.1 / Java 21 `kimori-api` project with `model`, `repository`,
  `service`, and `controller` packages for `FamilyTree`, `Person`, and `Relationship`. Expose a REST API
  documented via the already-configured springdoc-openapi.
- **Rationale**: Matches existing project (`kimori-api/pom.xml` already has `spring-boot-starter-webmvc`,
  `spring-boot-starter-security`, `spring-boot-starter-validation`, `firebase-admin`, `springdoc-openapi`).
- **Alternatives considered**: Reactive stack (WebFlux) — rejected, no requirement for high-concurrency
  streaming and adds complexity; the existing project already uses the Servlet-based `webmvc` starter.

## 3. Data storage

- **Decision**: Firestore as the primary data store, with one collection per authenticated user for trees
  (`users/{uid}/trees/{treeId}`), and subcollections for people and relationships
  (`users/{uid}/trees/{treeId}/people/{personId}`, `.../relationships/{relationshipId}`). Firebase Storage
  holds person pictures and PDF certificates, keyed under the same `uid/treeId/personId` path, with the
  Firestore person document storing only the storage download reference.
- **Rationale**: `firebase-admin` is already a backend dependency and `firestore/firebase.json` already
  configures a Firestore emulator; nesting data under `users/{uid}` gives natural per-user isolation (FR-022a)
  enforceable both by backend authorization checks and Firestore security rules.
- **Alternatives considered**: A single flat `trees` collection with a `ownerUid` field on every document
  (rejected as primary structure — works but requires an extra filter on every query/rule instead of relying on
  path-based isolation; can still be introduced later if cross-tree querying is needed).

## 4. Authentication (Google Sign-In, per-user isolation)

- **Decision**: Frontend uses Google Identity Services (or Firebase Authentication JS SDK) to sign the user in
  and obtain a Firebase ID token, sent as a `Bearer` token on every API request. Backend uses Spring Security
  with a custom filter that verifies the ID token via `FirebaseAuth.getInstance().verifyIdToken(token)` (from
  the already-included `firebase-admin` SDK) and sets the resulting Firebase UID as the authenticated
  principal. Every repository/service method requires and scopes by this UID.
- **Rationale**: Reuses the existing `firebase-admin` dependency and `spring-boot-starter-security` starter
  already declared in `pom.xml`; avoids introducing a second identity provider or token format.
- **Alternatives considered**: Full OAuth2 resource-server config pointed at Google's OIDC discovery document
  (rejected — more generic but requires extra library wiring when `firebase-admin` already provides a
  purpose-built, already-present verification path).

## 5. Hierarchy diagram rendering

- **Decision**: Render the family tree using an SVG-based hierarchy layout (e.g., `d3-hierarchy`'s tree/cluster
  layout driven from Angular, or a maintained Angular org-chart/family-tree component) wrapped in a pannable/
  zoomable container (e.g., CSS transform + drag/wheel handlers, or `d3-zoom`). Couples are rendered as a
  merged node/union point with shared children hanging from that union (per Clarification: children grouped
  under the couple as a single visual unit).
- **Rationale**: d3-hierarchy is the standard, well-tested approach for tree/org-chart layouts and integrates
  cleanly with Angular by keeping D3 responsible only for layout math while Angular manages DOM/data binding.
- **Alternatives considered**: A generic graph library (e.g., Cytoscape.js) — rejected as heavier and
  general-purpose graph libraries are less suited to strictly hierarchical, generation-based layouts than
  `d3-hierarchy`.

## 6. File upload handling (pictures & PDF certificates)

- **Decision**: Frontend uploads pictures/certificates as `multipart/form-data` to dedicated backend endpoints;
  backend validates MIME type/extension and size (≤10MB) before streaming the file to Firebase Storage, then
  stores the resulting storage path/URL on the Person document. Reject with `400` and a descriptive error
  before any Storage write on validation failure (per FR-007, FR-024, Edge Cases).
- **Rationale**: Keeps validation authoritative on the backend (never trust client-side checks alone) and
  avoids partially-written records when validation fails.
- **Alternatives considered**: Direct client-to-Storage upload with Storage security rules doing validation
  (rejected as primary path — harder to produce the specific, user-friendly error messages required by
  FR-007/SC-005 from client-side Storage rule failures alone; can be layered in later as defense-in-depth).

## 7. Testing approach

- **Decision**: Backend — JUnit 5 + `@WebMvcTest`/`@SpringBootTest` with MockMvc for controller contract
  tests, plain JUnit for service-level relationship validation rules (e.g., cycle/parent-cap checks), and the
  Firestore emulator (already configured in `firestore/firebase.json`) for repository-level integration tests.
  Frontend — Angular's default Jasmine/Karma (or Jest if migrated) for component/service unit tests, with a
  small number of integration tests around the right-panel-to-diagram sync behavior (SC-003).
- **Rationale**: Matches existing tooling already present in the repo (Firestore emulator config, Spring Boot
  Test starter available transitively) without introducing new test frameworks.
- **Alternatives considered**: End-to-end browser testing (e.g., Playwright/Cypress) for the diagram —
  deferred; valuable but not required to prove the functional requirements at this stage, can be added later
  without blocking this feature.

## Outcome

All unknowns from the Technical Context are resolved above; no `NEEDS CLARIFICATION` markers remain.
