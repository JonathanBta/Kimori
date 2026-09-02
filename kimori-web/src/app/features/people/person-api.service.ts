import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiClientService } from '../../core/api-client.service';
import { Person } from '../../core/models';

export interface PersonRequest {
  name: string;
  dateOfBirth: string | null;
  dateOfDeath: string | null;
  comments: string | null;
}

/** FR-004–FR-008, FR-017, FR-019, FR-024: calls the person + picture/certificate endpoints. */
@Injectable({ providedIn: 'root' })
export class PersonApiService {
  private readonly api = inject(ApiClientService);

  list(treeId: string): Observable<Person[]> {
    return this.api.get<Person[]>(`/trees/${treeId}/people`);
  }

  create(treeId: string, request: PersonRequest): Observable<Person> {
    return this.api.post<Person>(`/trees/${treeId}/people`, request);
  }

  update(treeId: string, personId: string, request: PersonRequest): Observable<Person> {
    return this.api.patch<Person>(`/trees/${treeId}/people/${personId}`, request);
  }

  delete(treeId: string, personId: string): Observable<void> {
    return this.api.delete<void>(`/trees/${treeId}/people/${personId}`);
  }

  uploadPicture(treeId: string, personId: string, file: File): Observable<Person> {
    return this.api.postFile<Person>(`/trees/${treeId}/people/${personId}/picture`, file);
  }

  deletePicture(treeId: string, personId: string): Observable<void> {
    return this.api.delete<void>(`/trees/${treeId}/people/${personId}/picture`);
  }

  uploadCertificate(treeId: string, personId: string, kind: 'birth' | 'death', file: File): Observable<Person> {
    return this.api.postFile<Person>(`/trees/${treeId}/people/${personId}/certificates/${kind}`, file);
  }

  deleteCertificate(treeId: string, personId: string, kind: 'birth' | 'death'): Observable<void> {
    return this.api.delete<void>(`/trees/${treeId}/people/${personId}/certificates/${kind}`);
  }
}
