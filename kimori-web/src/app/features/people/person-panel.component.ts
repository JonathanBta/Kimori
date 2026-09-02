import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { PersonApiService } from './person-api.service';
import { RelationshipEditorComponent } from '../relationships/relationship-editor.component';
import { TreeStateService } from '../trees/tree-state.service';

/** FR-004–FR-008, FR-014–FR-017, FR-019, FR-024: right-panel add/edit person form. */
@Component({
  selector: 'app-person-panel',
  standalone: true,
  imports: [FormsModule, RelationshipEditorComponent],
  templateUrl: './person-panel.component.html',
  styleUrl: './person-panel.component.scss'
})
export class PersonPanelComponent {
  readonly state = inject(TreeStateService);
  private readonly personApi = inject(PersonApiService);

  readonly name = signal('');
  readonly dateOfBirth = signal('');
  readonly dateOfDeath = signal('');
  readonly comments = signal('');

  readonly saving = signal(false);
  readonly uploadingPicture = signal(false);
  readonly uploadingCertificate = signal<'birth' | 'death' | null>(null);
  readonly validationError = signal<string | null>(null);

  readonly isOpen = computed(() => this.state.panelMode() !== 'closed');
  readonly isAddMode = computed(() => this.state.panelMode() === 'add');

  constructor() {
    // Reset/populate the form whenever the panel target changes.
    effect(() => {
      const person = this.state.selectedPerson();
      if (this.state.panelMode() === 'add') {
        this.name.set('');
        this.dateOfBirth.set('');
        this.dateOfDeath.set('');
        this.comments.set('');
        this.validationError.set(null);
      } else if (person) {
        this.name.set(person.name);
        this.dateOfBirth.set(person.dateOfBirth ?? '');
        this.dateOfDeath.set(person.dateOfDeath ?? '');
        this.comments.set(person.comments ?? '');
        this.validationError.set(null);
      }
    });
  }

  close(): void {
    this.state.closePanel();
  }

  async save(): Promise<void> {
    if (!this.name().trim()) {
      this.validationError.set('Name is required.');
      return;
    }
    this.saving.set(true);
    this.validationError.set(null);
    const request = {
      name: this.name().trim(),
      dateOfBirth: this.dateOfBirth() || null,
      dateOfDeath: this.dateOfDeath() || null,
      comments: this.comments() || null
    };
    try {
      if (this.isAddMode()) {
        await this.state.addPerson(request);
      } else {
        const person = this.state.selectedPerson();
        if (person) {
          await this.state.updatePerson(person.id, request);
        }
      }
    } finally {
      this.saving.set(false);
    }
  }

  async deletePerson(): Promise<void> {
    const person = this.state.selectedPerson();
    if (person) {
      await this.state.deletePerson(person.id);
    }
  }

  async onPictureSelected(event: Event): Promise<void> {
    const person = this.state.selectedPerson();
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!person || !file) {
      return;
    }
    this.uploadingPicture.set(true);
    try {
      await this.persistFile(() => this.personApi.uploadPicture(person.treeId, person.id, file));
    } finally {
      this.uploadingPicture.set(false);
    }
  }

  async removePicture(): Promise<void> {
    const person = this.state.selectedPerson();
    if (!person) {
      return;
    }
    await this.persistFile(() => this.personApi.deletePicture(person.treeId, person.id));
  }

  async onCertificateSelected(kind: 'birth' | 'death', event: Event): Promise<void> {
    const person = this.state.selectedPerson();
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!person || !file) {
      return;
    }
    this.uploadingCertificate.set(kind);
    try {
      if (file.type !== 'application/pdf') {
        this.validationError.set('Certificates must be PDF files.');
        return;
      }
      await this.persistFile(() => this.personApi.uploadCertificate(person.treeId, person.id, kind, file));
    } finally {
      this.uploadingCertificate.set(null);
    }
  }

  async removeCertificate(kind: 'birth' | 'death'): Promise<void> {
    const person = this.state.selectedPerson();
    if (!person) {
      return;
    }
    await this.persistFile(() => this.personApi.deleteCertificate(person.treeId, person.id, kind));
  }

  private async persistFile(action: () => import('rxjs').Observable<unknown>): Promise<void> {
    try {
      await new Promise<void>((resolve, reject) => {
        action().subscribe({ next: () => resolve(), error: (e) => reject(e) });
      });
      await this.state.refreshTreeContent();
    } catch (err) {
      const httpError = err as { error?: { message?: string } };
      this.validationError.set(httpError?.error?.message ?? 'Upload failed. Please try again.');
    }
  }
}
