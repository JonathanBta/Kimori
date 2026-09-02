import { Person, Relationship } from '../../core/models';
import { buildLayoutTree } from './diagram-layout';

function person(id: string, name = id): Person {
  return {
    id,
    treeId: 't1',
    name,
    dateOfBirth: null,
    dateOfDeath: null,
    pictureUrl: null,
    comments: null,
    birthCertificateUrl: null,
    deathCertificateUrl: null,
    createdAt: '',
    updatedAt: ''
  };
}

function parentChild(parentId: string, childId: string): Relationship {
  return {
    id: `${parentId}-${childId}`,
    treeId: 't1',
    type: 'PARENT_CHILD',
    parentId,
    childId,
    partnerAId: null,
    partnerBId: null,
    status: null,
    createdAt: ''
  };
}

function couple(a: string, b: string, status: 'CURRENT' | 'PAST' = 'CURRENT'): Relationship {
  return {
    id: `${a}-${b}`,
    treeId: 't1',
    type: 'COUPLE',
    parentId: null,
    childId: null,
    partnerAId: a,
    partnerBId: b,
    status,
    createdAt: ''
  };
}

describe('buildLayoutTree', () => {
  it('groups a shared child under a CURRENT couple as a single union node', () => {
    const people = [person('mom'), person('dad'), person('child')];
    const relationships = [couple('mom', 'dad'), parentChild('mom', 'child'), parentChild('dad', 'child')];

    const root = buildLayoutTree(people, relationships);

    expect(root.children.length).toBe(1);
    const union = root.children[0];
    expect(union.kind).toBe('union');
    expect(union.personIds.sort()).toEqual(['dad', 'mom']);
    expect(union.children.map((c) => c.key)).toEqual(['child']);
  });

  it('does not group a child under a PAST couple relationship', () => {
    const people = [person('mom'), person('dad'), person('child')];
    const relationships = [couple('mom', 'dad', 'PAST'), parentChild('mom', 'child'), parentChild('dad', 'child')];

    const root = buildLayoutTree(people, relationships);

    // Both parents surface as separate roots since there is no recognized CURRENT union.
    expect(root.children.map((c) => c.kind)).toEqual(['person', 'person']);
  });

  it('places people with no relationships as independent root nodes', () => {
    const people = [person('a'), person('b')];
    const root = buildLayoutTree(people, []);

    expect(root.children.length).toBe(2);
    expect(root.children.every((c) => c.kind === 'person')).toBe(true);
  });
});
