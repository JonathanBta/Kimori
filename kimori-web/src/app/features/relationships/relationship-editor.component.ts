import { Component, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Relationship } from '../../core/models';
import { TreeStateService } from '../trees/tree-state.service';

type LinkKind = 'PARENT_OF' | 'CHILD_OF' | 'COUPLE';

/** FR-009–FR-012a, FR-023: link/unlink parent-child and couple relationships for a person. */
@Component({
  selector: 'app-relationship-editor',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './relationship-editor.component.html',
  styleUrl: './relationship-editor.component.scss'
})
export class RelationshipEditorComponent {
  readonly personId = input.required<string>();

  readonly state = inject(TreeStateService);

  readonly linkKind = signal<LinkKind>('PARENT_OF');
  readonly otherPersonId = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly otherPeople = computed(() => this.state.people().filter((p) => p.id !== this.personId()));

  readonly relatedRelationships = computed<Relationship[]>(() =>
    this.state.relationships().filter((r) =>
      r.parentId === this.personId() ||
      r.childId === this.personId() ||
      r.partnerAId === this.personId() ||
      r.partnerBId === this.personId()
    )
  );

  describe(r: Relationship): string {
    const nameOf = (id: string | null) => this.state.people().find((p) => p.id === id)?.name ?? 'Unknown';
    if (r.type === 'PARENT_CHILD') {
      return r.parentId === this.personId()
        ? `Parent of ${nameOf(r.childId)}`
        : `Child of ${nameOf(r.parentId)}`;
    }
    const otherId = r.partnerAId === this.personId() ? r.partnerBId : r.partnerAId;
    return `${r.status === 'PAST' ? 'Formerly partnered with' : 'Partnered with'} ${nameOf(otherId)}`;
  }

  async addLink(): Promise<void> {
    const otherId = this.otherPersonId();
    if (!otherId) {
      return;
    }
    this.errorMessage.set(null);
    if (this.linkKind() === 'COUPLE') {
      await this.state.addRelationship({ type: 'COUPLE', partnerAId: this.personId(), partnerBId: otherId });
    } else if (this.linkKind() === 'PARENT_OF') {
      await this.state.addRelationship({ type: 'PARENT_CHILD', parentId: this.personId(), childId: otherId });
    } else {
      await this.state.addRelationship({ type: 'PARENT_CHILD', parentId: otherId, childId: this.personId() });
    }
    this.otherPersonId.set(null);
  }

  async removeLink(relationshipId: string): Promise<void> {
    await this.state.removeRelationship(relationshipId);
  }
}
