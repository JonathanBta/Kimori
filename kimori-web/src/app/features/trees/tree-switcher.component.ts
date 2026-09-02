import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { TreeStateService } from './tree-state.service';

/** FR-001–FR-003, FR-015, Edge Case (empty state): header tree switcher + add-tree/add-person actions. */
@Component({
  selector: 'app-tree-switcher',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './tree-switcher.component.html',
  styleUrl: './tree-switcher.component.scss'
})
export class TreeSwitcherComponent {
  readonly state = inject(TreeStateService);

  readonly creatingTree = signal(false);
  readonly newTreeName = signal('');

  onSelectTree(treeId: string): void {
    this.state.selectTree(treeId);
  }

  startCreateTree(): void {
    this.newTreeName.set('');
    this.creatingTree.set(true);
  }

  cancelCreateTree(): void {
    this.creatingTree.set(false);
  }

  async confirmCreateTree(): Promise<void> {
    const name = this.newTreeName().trim();
    if (!name) {
      return;
    }
    await this.state.createTree(name);
    this.creatingTree.set(false);
  }

  onAddPerson(): void {
    this.state.openAddPersonPanel();
  }
}
