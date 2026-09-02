# API Contract: Kimori Family Tree Builder

All endpoints are served by `kimori-api`, require a `Authorization: Bearer <Google/Firebase ID token>` header,
and operate only on data owned by the authenticated user's UID (401 if missing/invalid token, 403/404 if the
resource belongs to another user — never leak existence of another user's resource). Full machine-readable
schemas are published via the project's existing springdoc-openapi endpoint (`/v3/api-docs`,
`/swagger-ui.html`); this document summarizes the contract surface for planning purposes.

## Trees

| Method | Path | Description | Success | Errors |
|---|---|---|---|---|
| GET | `/api/trees` | List all trees owned by the caller | 200, `FamilyTree[]` | 401 |
| POST | `/api/trees` | Create a tree (FR-001) | 201, `FamilyTree` | 400 (missing/invalid name), 401 |
| GET | `/api/trees/{treeId}` | Get one tree (with summary counts) | 200, `FamilyTree` | 401, 404 |
| PATCH | `/api/trees/{treeId}` | Rename a tree | 200, `FamilyTree` | 400, 401, 404 |
| DELETE | `/api/trees/{treeId}` | Delete a tree and all its people/relationships/files (FR-020) | 204 | 401, 404 |

## People

| Method | Path | Description | Success | Errors |
|---|---|---|---|---|
| GET | `/api/trees/{treeId}/people` | List people in a tree | 200, `Person[]` | 401, 404 (tree) |
| POST | `/api/trees/{treeId}/people` | Add a person (FR-004) | 201, `Person` | 400 (validation, e.g. FR-017), 401, 404 |
| GET | `/api/trees/{treeId}/people/{personId}` | Get one person | 200, `Person` | 401, 404 |
| PATCH | `/api/trees/{treeId}/people/{personId}` | Edit a person's fields (FR-005) | 200, `Person` | 400, 401, 404 |
| DELETE | `/api/trees/{treeId}/people/{personId}` | Delete a person, cascading relationship cleanup (FR-019) | 204 | 401, 404 |
| POST | `/api/trees/{treeId}/people/{personId}/picture` | Upload/replace profile picture, `multipart/form-data` (FR-024) | 200, `Person` | 400 (bad type/size), 401, 404 |
| DELETE | `/api/trees/{treeId}/people/{personId}/picture` | Remove profile picture | 204 | 401, 404 |
| POST | `/api/trees/{treeId}/people/{personId}/certificates/{kind}` | Upload birth/death certificate PDF, `kind ∈ {birth, death}`, `multipart/form-data` (FR-006, FR-007) | 200, `Person` | 400 (not PDF / too large), 401, 404 |
| DELETE | `/api/trees/{treeId}/people/{personId}/certificates/{kind}` | Remove a certificate (FR-008) | 204 | 401, 404 |

## Relationships

| Method | Path | Description | Success | Errors |
|---|---|---|---|---|
| GET | `/api/trees/{treeId}/relationships` | List relationships in a tree (for diagram rendering) | 200, `Relationship[]` | 401, 404 |
| POST | `/api/trees/{treeId}/relationships` | Create a `PARENT_CHILD` or `COUPLE` relationship (FR-009, FR-010) | 201, `Relationship` | 400 (cycle, parent cap, self-reference, duplicate — FR-012/FR-012a), 401, 404 |
| PATCH | `/api/trees/{treeId}/relationships/{relationshipId}` | Update a `COUPLE` relationship's `status` (e.g., mark past — FR-023) | 200, `Relationship` | 400, 401, 404 |
| DELETE | `/api/trees/{treeId}/relationships/{relationshipId}` | Remove a relationship (FR-011) | 204 | 401, 404 |

## Common error shape

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Human-readable explanation of what was rejected and why",
  "field": "dateOfDeath"
}
```

Used consistently for all 400-class validation failures (invalid dates, non-PDF certificate, oversized file,
impossible relationship, parent cap exceeded) so the frontend can surface FR-007/FR-012/FR-017/SC-005 style
errors uniformly.
