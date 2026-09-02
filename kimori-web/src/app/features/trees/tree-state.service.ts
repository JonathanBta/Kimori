import { Injectable, computed, inject, signal } from '@angular/core';

import { FamilyTree, Person, Relationship } from '../../core/models';
import { NotificationService } from '../../core/notifications/notification.service';
import { PersonApiService, PersonRequest } from '../people/person-api.service';
import { RelationshipApiService, RelationshipRequest } from '../relationships/relationship-api.service';
import { TreeApiService } from './tree-api.service';

export type PanelMode = 'closed' | 'view' | 'add';

/**
 * Central client-side state for the active tree, its people/relationships, and the right panel.
 * FR-016: every mutation here re-fetches the affected list so the diagram re-renders immediately.
 */
@Injectable({ providedIn: 'root' })
export class TreeStateService {
  private readonly treeApi = inject(TreeApiService);
  private readonly personApi = inject(PersonApiService);
  private readonly relationshipApi = inject(RelationshipApiService);
  private readonly notifications = inject(NotificationService);

  readonly trees = signal<FamilyTree[]>([]);
  readonly activeTreeId = signal<string | null>(null);
  readonly people = signal<Person[]>([]);
  readonly relationships = signal<Relationship[]>([]);
  readonly panelMode = signal<PanelMode>('closed');
  readonly selectedPersonId = signal<string | null>(null);
  readonly loadingTrees = signal(false);
  readonly loadingTreeContent = signal(false);

  readonly activeTree = computed(() => this.trees().find((t) => t.id === this.activeTreeId()) ?? null);
  readonly selectedPerson = computed(() => this.people().find((p) => p.id === this.selectedPersonId()) ?? null);

  async loadTrees(): Promise<void> {
    this.loadingTrees.set(true);
    try {
      const trees = await this.firstValue(this.treeApi.list());
      this.trees.set(trees);
      if (!this.activeTreeId() && trees.length > 0) {
        await this.selectTree(trees[0].id);
      }
    } catch {
      this.notifications.error('Could not load your family trees. Please try again.');
    } finally {
      this.loadingTrees.set(false);
    }
  }

  async createTree(name: string): Promise<void> {
    try {
      const tree = await this.firstValue(this.treeApi.create(name));
      this.trees.update((list) => [...list, tree]);
      await this.selectTree(tree.id);
    } catch {
      this.notifications.error('Could not create the tree. Please check the name and try again.');
    }
  }

  async deleteTree(treeId: string): Promise<void> {
    try {
      await this.firstValue(this.treeApi.delete(treeId));
      this.trees.update((list) => list.filter((t) => t.id !== treeId));
      if (this.activeTreeId() === treeId) {
        const remaining = this.trees();
        this.activeTreeId.set(null);
        this.people.set([]);
        this.relationships.set([]);
        if (remaining.length > 0) {
          await this.selectTree(remaining[0].id);
        }
      }
    } catch {
      this.notifications.error('Could not delete the tree. Please try again.');
    }
  }

  async selectTree(treeId: string): Promise<void> {
    this.activeTreeId.set(treeId);
    this.closePanel();
    await this.refreshTreeContent();
  }

  async refreshTreeContent(): Promise<void> {
    const treeId = this.activeTreeId();
    if (!treeId) {
      return;
    }
    this.loadingTreeContent.set(true);
    try {
      const [people, relationships] = await Promise.all([
        this.firstValue(this.personApi.list(treeId)),
        this.firstValue(this.relationshipApi.list(treeId))
      ]);
      this.people.set(people);
      this.relationships.set(relationships);
    } catch {
      this.notifications.error('Could not load this tree. Please try again.');
    } finally {
      this.loadingTreeContent.set(false);
    }
  }

  openAddPersonPanel(): void {
    this.selectedPersonId.set(null);
    this.panelMode.set('add');
  }

  openPersonPanel(personId: string): void {
    this.selectedPersonId.set(personId);
    this.panelMode.set('view');
  }

  closePanel(): void {
    this.panelMode.set('closed');
    this.selectedPersonId.set(null);
  }

  async addPerson(request: PersonRequest): Promise<Person | null> {
    const treeId = this.activeTreeId();
    if (!treeId) {
      return null;
    }
    try {
      const person = await this.firstValue(this.personApi.create(treeId, request));
      await this.refreshTreeContent();
      this.openPersonPanel(person.id);
      return person;
    } catch (err) {
      this.notifications.error(this.messageOf(err));
      return null;
    }
  }

  async updatePerson(personId: string, request: PersonRequest): Promise<void> {
    const treeId = this.activeTreeId();
    if (!treeId) {
      return;
    }
    try {
      await this.firstValue(this.personApi.update(treeId, personId, request));
      await this.refreshTreeContent();
    } catch (err) {
      this.notifications.error(this.messageOf(err));
    }
  }

  async deletePerson(personId: string): Promise<void> {
    const treeId = this.activeTreeId();
    if (!treeId) {
      return;
    }
    try {
      await this.firstValue(this.personApi.delete(treeId, personId));
      this.closePanel();
      await this.refreshTreeContent();
    } catch {
      this.notifications.error('Could not delete this person. Please try again.');
    }
  }

  async addRelationship(request: RelationshipRequest): Promise<void> {
    const treeId = this.activeTreeId();
    if (!treeId) {
      return;
    }
    try {
      await this.firstValue(this.relationshipApi.create(treeId, request));
      await this.refreshTreeContent();
    } catch (err) {
      this.notifications.error(this.messageOf(err));
    }
  }

  async removeRelationship(relationshipId: string): Promise<void> {
    const treeId = this.activeTreeId();
    if (!treeId) {
      return;
    }
    try {
      await this.firstValue(this.relationshipApi.delete(treeId, relationshipId));
      await this.refreshTreeContent();
    } catch {
      this.notifications.error('Could not remove this relationship. Please try again.');
    }
  }

  private messageOf(err: unknown): string {
    const httpError = err as { error?: { message?: string } };
    return httpError?.error?.message ?? 'Something went wrong. Please try again.';
  }

  private firstValue<T>(obs: import('rxjs').Observable<T>): Promise<T> {
    return new Promise((resolve, reject) => {
      obs.subscribe({ next: (v) => resolve(v), error: (e) => reject(e) });
    });
  }
}
