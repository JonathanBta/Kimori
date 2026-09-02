import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild, effect, inject } from '@angular/core';
import * as d3Hierarchy from 'd3-hierarchy';
import * as d3Selection from 'd3-selection';
import * as d3Zoom from 'd3-zoom';

import { TreeStateService } from '../../features/trees/tree-state.service';
import { buildLayoutTree, LayoutNode } from './diagram-layout';

const NODE_WIDTH = 170;
const NODE_HEIGHT = 110;

/** FR-013, FR-014, FR-016: renders the active tree as a pannable/zoomable hierarchy diagram. */
@Component({
  selector: 'app-diagram',
  standalone: true,
  templateUrl: './diagram.component.html',
  styleUrl: './diagram.component.scss'
})
export class DiagramComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('svgRef', { static: true }) svgRef!: ElementRef<SVGSVGElement>;

  readonly state = inject(TreeStateService);
  private zoomBehavior?: d3Zoom.ZoomBehavior<SVGSVGElement, unknown>;

  constructor() {
    // FR-016: re-render whenever people/relationships change (e.g., after a right-panel save).
    effect(() => {
      this.state.people();
      this.state.relationships();
      this.render();
    });
  }

  ngOnInit(): void {
    // Initial render happens via the reactive effect once view children are ready.
  }

  ngAfterViewInit(): void {
    const svg = d3Selection.select(this.svgRef.nativeElement);
    this.zoomBehavior = d3Zoom.zoom<SVGSVGElement, unknown>()
      .scaleExtent([0.2, 2])
      .on('zoom', (event) => {
        svg.select('g.diagram-viewport').attr('transform', event.transform.toString());
      });
    svg.call(this.zoomBehavior);
    this.render();
  }

  ngOnDestroy(): void {
    if (this.zoomBehavior) {
      d3Selection.select(this.svgRef.nativeElement).on('.zoom', null);
    }
  }

  onNodeClick(personId: string): void {
    this.state.openPersonPanel(personId);
  }

  personName(personId: string): string {
    return this.state.people().find((p) => p.id === personId)?.name ?? '';
  }

  personPicture(personId: string): string | null {
    return this.state.people().find((p) => p.id === personId)?.pictureUrl ?? null;
  }

  private render(): void {
    if (!this.svgRef) {
      return;
    }
    const root = buildLayoutTree(this.state.people(), this.state.relationships());
    const svg = d3Selection.select(this.svgRef.nativeElement);
    const viewport = svg.select<SVGGElement>('g.diagram-viewport');
    viewport.selectAll('*').remove();

    if (root.children.length === 0) {
      return;
    }

    const hierarchy = d3Hierarchy.hierarchy<LayoutNode>(root, (d) => d.children);
    const layout = d3Hierarchy.tree<LayoutNode>().nodeSize([NODE_WIDTH, NODE_HEIGHT]);
    const positioned = layout(hierarchy);

    const linksGroup = viewport.append('g').attr('class', 'diagram-links');
    linksGroup
      .selectAll('path')
      .data(positioned.links().filter((link) => link.source.depth > 0))
      .enter()
      .append('path')
      .attr('class', 'diagram-link')
      .attr('d', (d) =>
        `M${d.source.x},${d.source.y} V${(d.source.y + d.target.y) / 2} H${d.target.x} V${d.target.y}`
      );

    const nodesGroup = viewport.append('g').attr('class', 'diagram-nodes');
    const nodeSelection = nodesGroup
      .selectAll('g')
      .data(positioned.descendants().filter((d) => d.depth > 0))
      .enter()
      .append('g')
      .attr('class', (d) => `diagram-node diagram-node--${d.data.kind}`)
      .attr('transform', (d) => `translate(${d.x},${d.y})`);

    nodeSelection.each((d, i, groups) => {
      const group = d3Selection.select(groups[i]);
      const ids = d.data.personIds;
      ids.forEach((personId, idx) => {
        const offsetX = ids.length > 1 ? (idx === 0 ? -45 : 5) : -40;
        const person = this.state.people().find((p) => p.id === personId);
        const card = group.append('g')
          .attr('class', 'diagram-person')
          .attr('transform', `translate(${offsetX},-30)`)
          .style('cursor', 'pointer')
          .on('click', () => this.onNodeClick(personId));

        card.append('rect').attr('width', 80).attr('height', 60).attr('rx', 10);
        card.append('text')
          .attr('x', 40)
          .attr('y', 30)
          .attr('text-anchor', 'middle')
          .attr('class', 'diagram-person__name')
          .text(person?.name ?? '');
      });
    });
  }
}
