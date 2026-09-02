# Feature Specification: Kimori Family Tree Builder

**Feature Branch**: `001-family-tree-builder`

**Created**: 2026-08-30

**Status**: Draft

**Input**: User description: "Build an application that can create a family tree that the user input. It is possible to create multiple trees and to give it a name. In a tree, you can add people. A person is a name, a date of birth, a date of death and a picture and additional comments. A person can have certificates of birth and death which are pdf. People have relationships between them, parents, children, couple or married. The application is one page with a navigable family tree which is a hierarchy diagram. On the header, it is possible to change or add a new tree or a new person on the current tree. A right panel expendable appears when clicking on an existing person with all the information about this person or when clicking on adding new person. Any change on the right panel directly impact the navigable tree. For the esthetic, the app is clean, modern, green and make think about nature. The name of the app is Kimori."

## Clarifications

### Session 2026-08-30

- Q: When two people are marked as a couple, should their shared children be visually grouped under that couple as a single unit in the diagram, or should each child link independently to each parent with no special couple-grouping? → A: Children are grouped under the couple as a single visual unit (standard genealogy chart style).
- Q: What is the largest family tree size (number of people) the app must comfortably support without becoming slow or hard to navigate? → A: Up to 500 people per tree.
- Q: Should the app enforce a maximum of two parent links per person, or allow more than two? → A: Cap at exactly two parent links per person (biological mother/father only).
- Q: Given the repo already includes Firestore/backend infrastructure, should this v1 still ship with no login, or does it need authentication? → A: Require Google Authentication to connect to the app.
- Q: Should Google Sign-In also isolate data per user, or just gate access to a shared pool of trees? → A: Per-user isolation — each Google account has its own private trees.

## User Scenarios & Testing *(mandatory)*

### User Story 0 - Sign in with Google (Priority: P1)

A visitor arrives at Kimori and must sign in with their Google account before seeing or creating any family tree; each signed-in account only ever sees its own trees.

**Why this priority**: Authentication gates every other capability and enforces data privacy between accounts; nothing else can be safely built or demonstrated without it.

**Independent Test**: Can be fully tested by attempting to load the app unauthenticated (confirming redirect to sign-in), signing in with two different Google accounts, and confirming each account sees only the trees it created.

**Acceptance Scenarios**:

1. **Given** a visitor is not signed in, **When** they open the application, **Then** they are redirected to a Google sign-in prompt and cannot view any tree data until authenticated.
2. **Given** a user successfully signs in with Google, **When** the application loads, **Then** only the trees belonging to that account are shown.
3. **Given** two different Google accounts have each created their own trees, **When** either account signs in, **Then** they cannot see, edit, or delete the other account's trees.

---

### User Story 1 - Create and switch between family trees (Priority: P1)

A user opens Kimori for the first time, creates a new named family tree, and can later create additional named trees and switch between them from the header.

**Why this priority**: Without the ability to create and select a tree, there is no place to add people or relationships — this is the foundation the rest of the app depends on.

**Independent Test**: Can be fully tested by creating two differently named trees, switching between them from the header, and confirming each tree's content stays separate and correctly labeled.

**Acceptance Scenarios**:

1. **Given** no trees exist yet, **When** the user creates a new tree and gives it a name, **Then** the tree is created, becomes the active tree, and its name is shown in the header.
2. **Given** at least one tree exists, **When** the user selects "add a new tree" from the header and provides a name, **Then** a new empty tree is created and becomes the active tree without affecting existing trees.
3. **Given** multiple trees exist, **When** the user chooses a different tree from the header, **Then** the navigable diagram updates to show only that tree's people and relationships.

---

### User Story 2 - Add and edit a person's details (Priority: P1)

A user adds a new person to the active tree by entering their name, date of birth, date of death (if applicable), a picture, and comments, and can later reopen that person to edit any of these fields.

**Why this priority**: People are the core data unit of a family tree; without adding and editing person records, no tree can be populated or kept accurate.

**Independent Test**: Can be fully tested by adding a person with all fields filled in, verifying they appear on the diagram, reopening their panel, editing a field, and confirming the diagram and stored data reflect the update immediately.

**Acceptance Scenarios**:

1. **Given** the active tree is open, **When** the user clicks "add new person" in the header, **Then** an expandable right panel opens with empty fields for name, date of birth, date of death, picture, and comments.
2. **Given** the add-person panel is open, **When** the user enters a name and saves, **Then** the person appears as a new node in the navigable diagram immediately.
3. **Given** an existing person node in the diagram, **When** the user clicks on it, **Then** the right panel expands showing all of that person's stored information.
4. **Given** the right panel is showing an existing person, **When** the user edits a field (e.g., changes the picture or adds a comment) and saves, **Then** the diagram and the person's stored record reflect the change without requiring a page reload.
5. **Given** a person has no recorded date of death, **When** their record is viewed, **Then** the person is displayed as presumed living without a death date shown.

---

### User Story 3 - Attach birth and death certificates (Priority: P2)

While viewing or editing a person, the user uploads a birth certificate and/or a death certificate as PDF files, and can later open or remove them.

**Why this priority**: Certificates are supporting documentation that enrich a person's record but are not required to create a usable tree, making this a valuable enhancement rather than a blocker.

**Independent Test**: Can be fully tested by opening a person's panel, uploading a PDF as a birth certificate, saving, reopening the panel, and confirming the certificate is listed and can be opened or removed.

**Acceptance Scenarios**:

1. **Given** a person's right panel is open, **When** the user uploads a PDF file as the birth certificate, **Then** the file is attached to that person's record and listed in the panel.
2. **Given** a person's right panel is open, **When** the user uploads a PDF file as the death certificate, **Then** the file is attached to that person's record and listed in the panel.
3. **Given** a person already has an attached certificate, **When** the user opens that person's panel, **Then** the certificate is available to view or remove.
4. **Given** the user attempts to upload a non-PDF file as a certificate, **When** the upload is submitted, **Then** the system rejects the file and shows a clear error message.

---

### User Story 4 - Define relationships between people (Priority: P1)

A user connects people in the active tree using parent/child and couple/married relationships, and the navigable hierarchy diagram updates to visually reflect these connections.

**Why this priority**: Relationships are what transform a list of individual people into an actual family tree; this capability delivers the core value proposition of the application.

**Independent Test**: Can be fully tested by adding two people, linking them as parent and child, confirming the hierarchy diagram draws the connection, then linking two people as a couple and confirming that connection is also visually distinct.

**Acceptance Scenarios**:

1. **Given** two existing people in the active tree, **When** the user sets one as the parent of the other via the right panel, **Then** the diagram redraws to show the hierarchical parent-child connection.
2. **Given** two existing people in the active tree, **When** the user marks them as a couple or married, **Then** the diagram shows a visually distinct couple connection between them.
3. **Given** a person has multiple children recorded, **When** the diagram renders, **Then** all children appear connected to that parent at the appropriate generational level.
4. **Given** two people are marked as a couple and have one or more shared children, **When** the diagram renders, **Then** the shared children are grouped under the couple as a single visual unit rather than drawn as separate independent links to each parent.
5. **Given** a relationship exists between two people, **When** the user removes that relationship from the right panel, **Then** the diagram immediately redraws without that connection.

---

### User Story 5 - Navigate a large tree visually (Priority: P2)

A user pans and zooms around the hierarchy diagram to explore ancestors, descendants, and extended family within a large tree.

**Why this priority**: Navigation quality directly affects usability once a tree grows beyond a handful of people, but a small tree is usable and demonstrable without advanced navigation.

**Independent Test**: Can be fully tested by populating a tree with several generations of people and confirming the user can pan, zoom, and still click any visible person to open their details.

**Acceptance Scenarios**:

1. **Given** a tree with multiple generations of people, **When** the user pans or zooms the diagram, **Then** the diagram remains responsive and all nodes remain clickable at any zoom level.
2. **Given** the diagram is zoomed or panned away from center, **When** the user clicks a visible person node, **Then** the right panel opens for that person without resetting the current view.

---

### Edge Cases

- What happens when the user tries to create a tree with a name that duplicates an existing tree's name? (System allows it but must keep trees distinguishable, e.g., by an internal identifier shown alongside duplicate names.)
- What happens when a person is deleted but still has recorded relationships to other people? (Related relationships must be removed or clearly invalidated so the diagram does not reference a missing person.)
- How does the system handle a proposed relationship that would create a logical impossibility (e.g., a person set as their own ancestor, or as both parent and child of the same person)? (System must reject the change and show an explanatory error.)
- What happens when a picture or certificate upload fails or exceeds an allowed size? (System shows a clear error and does not save a partial/corrupt record.)
- What happens when the last tree in the application is deleted? (Header must offer a clear path to create a new tree; the diagram shows an empty state.)
- What happens when a date of death is entered that is earlier than the date of birth? (System must reject the entry with a validation error.)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow users to create a new family tree by providing a name.
- **FR-002**: System MUST allow users to create and maintain multiple independent family trees, each with its own people and relationships.
- **FR-003**: System MUST allow users to switch the active tree from the header at any time, updating the navigable diagram to reflect the newly selected tree.
- **FR-004**: System MUST allow users to add a new person to the active tree with the following fields: name (required), date of birth (optional), date of death (optional), a picture (optional), and free-text comments (optional).
- **FR-005**: System MUST allow users to edit any previously entered field of an existing person.
- **FR-006**: System MUST allow users to attach a birth certificate and a death certificate to a person, each as a PDF file.
- **FR-007**: System MUST reject certificate uploads that are not in PDF format and show an error message explaining why.
- **FR-008**: System MUST allow users to view or remove a previously attached certificate.
- **FR-009**: System MUST allow users to define a parent/child relationship between two people in the same tree.
- **FR-010**: System MUST allow users to define a couple/married relationship between two people in the same tree.
- **FR-011**: System MUST allow users to remove a previously defined relationship between two people.
- **FR-012**: System MUST prevent relationship configurations that are logically impossible (e.g., a person being their own ancestor, or simultaneously a parent and child of the same person) and inform the user why the change was rejected.
- **FR-012a**: System MUST enforce a maximum of two parent links per person and reject attempts to add a third.
- **FR-013**: System MUST display the active tree as a navigable, zoomable, and pannable hierarchy diagram reflecting all people and relationships in that tree.
- **FR-014**: System MUST open an expandable right panel showing full details when a user clicks an existing person node in the diagram.
- **FR-015**: System MUST open an expandable right panel with empty input fields when the user chooses to add a new person from the header.
- **FR-016**: System MUST reflect any change saved in the right panel (new person, edited field, new or removed relationship) in the navigable diagram immediately, without requiring a manual refresh.
- **FR-017**: System MUST validate that a person's date of death, when provided, is not earlier than their date of birth.
- **FR-018**: System MUST persist all trees, people, relationships, pictures, and certificates so they remain available across sessions.
- **FR-019**: System MUST allow users to delete a person from the active tree, removing or invalidating any relationships that referenced that person.
- **FR-020**: System MUST allow users to delete an entire family tree, including all of its people, relationships, pictures, and certificates.
- **FR-021**: System MUST present the application under the name "Kimori" with a clean, modern, nature-inspired visual style using a predominantly green palette.

*Clarifications resolved during specification:*

- **FR-022**: System MUST require users to sign in with a Google account before accessing any part of the application; unauthenticated users MUST be redirected to sign-in.
- **FR-022a**: System MUST isolate data per authenticated user — each Google account MUST only see, create, edit, and delete trees, people, relationships, pictures, and certificates that belong to it, never another account's data.
- **FR-023**: A person MUST be able to have more than one couple/married relationship recorded over time (e.g., divorce and remarriage), and the diagram MUST visually distinguish multiple partners without implying they are simultaneous unless both are marked as current.
- **FR-024**: Picture uploads MUST be limited to standard image formats (JPEG, PNG, WEBP) up to 10 MB, and PDF certificate uploads MUST be limited to 10 MB; files exceeding these limits MUST be rejected with a clear error message.

### Key Entities

- **Family Tree**: A named, independent collection of people and their relationships. Attributes: name, creation date, list of member people.
- **Person**: An individual within a family tree. Attributes: name, date of birth, date of death (optional), picture (optional), comments (optional), attached birth certificate (optional PDF), attached death certificate (optional PDF).
- **Relationship**: A directed or mutual connection between two people within the same tree. Types: parent/child (directed, from parent to child) and couple/married (mutual, may include a current/past status). Attributes: type, the two linked people, status (e.g., current or past, for couple relationships).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A new user can create a named tree and add their first person to it in under 2 minutes without external help.
- **SC-002**: Users can switch between at least 10 distinct family trees from the header with the correct tree's content displayed every time.
- **SC-003**: 95% of edits made in the right panel (new person, field edit, relationship change) are reflected in the navigable diagram in under 1 second, with no manual refresh required.
- **SC-004**: A tree containing up to 500 people across 5+ generations remains navigable (pan/zoom) without noticeable lag, and every visible person remains clickable.
- **SC-005**: 100% of attempted non-PDF certificate uploads and out-of-range dates of death are rejected with an understandable error message, with zero corrupted or partial records saved.
- **SC-006**: Users rate the visual style as clean, modern, and nature-themed in post-use feedback, with a satisfaction score of at least 4 out of 5.
- **SC-007**: 100% of unauthenticated access attempts are redirected to Google sign-in, and no user can view or modify another account's trees.

## Assumptions

- Users authenticate with a Google account; there is no separate username/password system, and no account self-registration flow beyond what Google Sign-In provides.
- Each authenticated user's trees, people, relationships, pictures, and certificates are private to that account; there is no sharing or collaboration between accounts in this version.
- A person may have more than one couple/married relationship over time (e.g., due to divorce or remarriage); the diagram distinguishes these without assuming simultaneity unless explicitly marked current.
- Picture uploads are limited to standard image formats (JPEG, PNG, WEBP) up to 10 MB; PDF certificates are limited to 10 MB.
- The application is delivered as a single page focused on one active tree view at a time; there is no requirement for offline use or native mobile apps in this version.
- "Additional comments" is a single free-text field per person with no enforced structure or length limit beyond reasonable storage practices.
- No import/export of trees (e.g., GEDCOM) is required for this initial version.
- Each person has a maximum of two parent links (biological mother/father); step- or adoptive-parent tracking is out of scope for this version.
- A single tree is expected to comfortably hold up to 500 people; larger trees are out of scope for this version's performance target.
