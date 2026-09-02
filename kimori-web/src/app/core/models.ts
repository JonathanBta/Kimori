/** Mirrors kimori-api's model/DTO shapes (see specs/001-family-tree-builder/data-model.md). */

export interface FamilyTree {
  id: string;
  ownerUid: string;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface Person {
  id: string;
  treeId: string;
  name: string;
  dateOfBirth: string | null;
  dateOfDeath: string | null;
  pictureUrl: string | null;
  comments: string | null;
  birthCertificateUrl: string | null;
  deathCertificateUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export type RelationshipType = 'PARENT_CHILD' | 'COUPLE';
export type RelationshipStatus = 'CURRENT' | 'PAST';

export interface Relationship {
  id: string;
  treeId: string;
  type: RelationshipType;
  parentId: string | null;
  childId: string | null;
  partnerAId: string | null;
  partnerBId: string | null;
  status: RelationshipStatus | null;
  createdAt: string;
}

/** Uniform error shape returned by kimori-api for all 4xx failures (contracts/api.md). */
export interface ApiErrorResponse {
  status: number;
  error: string;
  message: string;
  field: string | null;
}
