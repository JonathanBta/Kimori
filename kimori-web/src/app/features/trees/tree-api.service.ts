import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiClientService } from '../../core/api-client.service';
import { FamilyTree } from '../../core/models';

/** FR-001–FR-003, FR-020: calls the /api/trees endpoints. */
@Injectable({ providedIn: 'root' })
export class TreeApiService {
  private readonly api = inject(ApiClientService);

  list(): Observable<FamilyTree[]> {
    return this.api.get<FamilyTree[]>('/trees');
  }

  create(name: string): Observable<FamilyTree> {
    return this.api.post<FamilyTree>('/trees', { name });
  }

  rename(treeId: string, name: string): Observable<FamilyTree> {
    return this.api.patch<FamilyTree>(`/trees/${treeId}`, { name });
  }

  delete(treeId: string): Observable<void> {
    return this.api.delete<void>(`/trees/${treeId}`);
  }
}
