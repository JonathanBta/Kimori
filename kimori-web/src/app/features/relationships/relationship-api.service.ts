import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiClientService } from '../../core/api-client.service';
import { Relationship, RelationshipStatus, RelationshipType } from '../../core/models';

export interface RelationshipRequest {
  type: RelationshipType;
  parentId?: string | null;
  childId?: string | null;
  partnerAId?: string | null;
  partnerBId?: string | null;
}

/** FR-009–FR-012a, FR-023: calls the /api/trees/{treeId}/relationships endpoints. */
@Injectable({ providedIn: 'root' })
export class RelationshipApiService {
  private readonly api = inject(ApiClientService);

  list(treeId: string): Observable<Relationship[]> {
    return this.api.get<Relationship[]>(`/trees/${treeId}/relationships`);
  }

  create(treeId: string, request: RelationshipRequest): Observable<Relationship> {
    return this.api.post<Relationship>(`/trees/${treeId}/relationships`, request);
  }

  updateStatus(treeId: string, relationshipId: string, status: RelationshipStatus): Observable<Relationship> {
    return this.api.patch<Relationship>(`/trees/${treeId}/relationships/${relationshipId}`, { status });
  }

  delete(treeId: string, relationshipId: string): Observable<void> {
    return this.api.delete<void>(`/trees/${treeId}/relationships/${relationshipId}`);
  }
}
