# Data Model: Kimori Family Tree Builder

All data is scoped under the authenticated user's Firebase UID. Firestore path prefix for every entity below
is `users/{uid}/...`; no document is ever readable or writable by a different UID (FR-022a).

## FamilyTree

Path: `users/{uid}/trees/{treeId}`

| Field | Type | Rules |
|---|---|---|
| `id` | string (Firestore doc ID) | Generated; immutable |
| `ownerUid` | string | Set from authenticated principal; MUST equal request UID on every read/write |
| `name` | string | Required, 1–200 chars; duplicates across trees are allowed (Edge Case) but each tree keeps a distinct `id` |
| `createdAt` | timestamp | Set on creation, immutable |
| `updatedAt` | timestamp | Updated on any mutation to the tree or its people/relationships |

**Lifecycle**: created (FR-001) → renamed/updated → deleted (FR-020, cascades to all `people` and
`relationships` subcollections and their Storage files).

## Person

Path: `users/{uid}/trees/{treeId}/people/{personId}`

| Field | Type | Rules |
|---|---|---|
| `id` | string (Firestore doc ID) | Generated; immutable |
| `treeId` | string | Must match parent tree; denormalized for query convenience |
| `name` | string | Required, 1–200 chars (FR-004) |
| `dateOfBirth` | date (ISO 8601) or null | Optional (FR-004) |
| `dateOfDeath` | date (ISO 8601) or null | Optional; if set, MUST be ≥ `dateOfBirth` (FR-017); absence means presumed living |
| `pictureUrl` | string (Storage download URL) or null | Optional; set only after a successful upload (≤10MB, JPEG/PNG/WEBP — FR-024) |
| `comments` | string or null | Optional, free text, no enforced structure (Assumptions) |
| `birthCertificateUrl` | string (Storage download URL) or null | Optional; PDF only, ≤10MB (FR-006, FR-007, FR-024) |
| `deathCertificateUrl` | string (Storage download URL) or null | Optional; PDF only, ≤10MB (FR-006, FR-007, FR-024) |
| `createdAt` / `updatedAt` | timestamp | Standard audit fields |

**Validation rules**:
- `dateOfDeath >= dateOfBirth` when both present (FR-017).
- Certificate fields only accept files whose content-type resolves to `application/pdf` (FR-007).
- Picture fields only accept `image/jpeg`, `image/png`, `image/webp`, ≤10MB; certificates ≤10MB (FR-024).

**Lifecycle**: created via right-panel "add person" (FR-004, FR-015) → edited any time (FR-005) → deleted
(FR-019), which cascades removal/invalidation of any `Relationship` documents referencing this person.

## Relationship

Path: `users/{uid}/trees/{treeId}/relationships/{relationshipId}`

| Field | Type | Rules |
|---|---|---|
| `id` | string (Firestore doc ID) | Generated; immutable |
| `treeId` | string | Must match parent tree |
| `type` | enum: `PARENT_CHILD` \| `COUPLE` | Required |
| `parentId` | string (Person ref) | Required when `type = PARENT_CHILD`; the parent |
| `childId` | string (Person ref) | Required when `type = PARENT_CHILD`; the child |
| `partnerAId` / `partnerBId` | string (Person ref) | Required when `type = COUPLE`; unordered pair |
| `status` | enum: `CURRENT` \| `PAST` | Applies only to `COUPLE` relationships (FR-023); defaults to `CURRENT` |
| `createdAt` | timestamp | Standard audit field |

**Validation rules** (enforced server-side before persisting, FR-012 / FR-012a):
- A `PARENT_CHILD` relationship MUST NOT create a cycle (a person cannot be their own ancestor).
- A person MUST NOT be simultaneously recorded as parent and child of the same other person.
- A `childId` MUST NOT already have 2 `PARENT_CHILD` relationships where it is the child (max two parents).
- A `COUPLE` relationship's two partner IDs MUST differ and both MUST belong to the same tree.
- A person MAY have more than one `COUPLE` relationship over time; only relationships with `status = CURRENT`
  are treated as simultaneous partners in the diagram (FR-023).

**Derived rendering rule** (not stored, computed by the diagram layer): children shared by two people who have
a `COUPLE` relationship are grouped under that couple as a single visual union node (Clarification,
2026-08-30).

## Entity Relationship Summary

```text
FamilyTree (1) ──── (0..N) Person
FamilyTree (1) ──── (0..N) Relationship
Relationship(PARENT_CHILD) ──── references 2 Person (parentId, childId)
Relationship(COUPLE)       ──── references 2 Person (partnerAId, partnerBId)
Person (1) ──── (0..1) picture file, (0..1) birth certificate file, (0..1) death certificate file
```
