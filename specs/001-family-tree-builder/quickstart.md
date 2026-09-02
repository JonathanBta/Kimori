# Quickstart: Kimori Family Tree Builder

Validation guide for confirming the feature works end-to-end. See [data-model.md](./data-model.md) for entity
details and [contracts/api.md](./contracts/api.md) for the full API surface.

## Prerequisites

- Java 21, Maven (or the included `mvnw`/`mvnw.cmd`) for `kimori-api`
- Node.js LTS + Angular CLI for `kimori-web`
- Firebase CLI, with the Firestore/Storage emulators available (see `firestore/firebase.json`)
- A Google Cloud / Firebase project (`kimori-499114`, per `application.yml`) with Google Sign-In enabled

## Setup

```powershell
# Backend: start Firestore/Storage emulators, then run the API
firebase emulators:start --only firestore
cd kimori-api
./mvnw.cmd spring-boot:run

# Frontend: install and serve the Angular app
cd kimori-web
npm install
ng serve
```

## Validation scenarios (map to spec.md user stories)

1. **Sign in with Google (User Story 0)**
   - Open the app while signed out → confirm redirect to Google sign-in.
   - Sign in with Google Account A → confirm the tree list is empty (or shows only A's prior trees).
   - Sign in with Google Account B in a separate session → confirm B cannot see A's trees (SC-007).

2. **Create and switch trees (User Story 1)**
   - Create tree "Smith Family" → confirm it becomes active and appears in the header.
   - Create a second tree "Doe Family" → confirm both trees are listed and switching between them shows the
     correct people/relationships for each (SC-002).

3. **Add and edit a person (User Story 2)**
   - Click "add new person" → fill name, date of birth, picture, comments → save → confirm the person appears
     as a new diagram node within 1 second (SC-003).
   - Click the new node → edit the comments field → save → confirm the diagram and stored record update
     immediately.
   - Leave date of death blank → confirm the person displays as presumed living.

4. **Attach certificates (User Story 3)**
   - Open a person's panel → upload a PDF as birth certificate → confirm it is listed and can be reopened.
   - Attempt to upload a `.docx` file as a certificate → confirm rejection with a clear error (FR-007, SC-005).
   - Attempt to upload an oversized (>10MB) PDF → confirm rejection (FR-024).

5. **Define relationships (User Story 4)**
   - Add two people, link as parent/child → confirm the diagram draws a hierarchical connection.
   - Mark two people as a couple, then add a shared child → confirm the child renders grouped under the couple
     as a single union node, not as two independent links (Clarification, 2026-08-30).
   - Attempt to set a person as their own ancestor → confirm rejection with an explanatory error (FR-012).
   - Attempt to add a third parent to a child who already has two → confirm rejection (FR-012a).
   - Remove a relationship → confirm the diagram redraws without it immediately.

6. **Navigate a large tree (User Story 5)**
   - Seed a tree with ~500 people across 5+ generations (e.g., via a seed script hitting the API).
   - Pan and zoom across the diagram → confirm responsiveness and that all visible nodes remain clickable
     (SC-004).

## Expected outcome

All scenarios above pass without manual data cleanup, cross-account data leakage, or unhandled errors,
confirming the feature satisfies spec.md's functional requirements and success criteria.
