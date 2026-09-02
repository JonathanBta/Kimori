import { Component, OnInit, inject } from '@angular/core';

import { TreeStateService } from '../../features/trees/tree-state.service';
import { TreeSwitcherComponent } from '../../features/trees/tree-switcher.component';
import { PersonPanelComponent } from '../../features/people/person-panel.component';
import { DiagramComponent } from '../diagram/diagram.component';

/** FR-013–FR-016: the single-page shell — header, diagram canvas, and expandable right panel. */
@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [TreeSwitcherComponent, DiagramComponent, PersonPanelComponent],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.scss'
})
export class AppShellComponent implements OnInit {
  private readonly state = inject(TreeStateService);

  ngOnInit(): void {
    this.state.loadTrees();
  }
}
