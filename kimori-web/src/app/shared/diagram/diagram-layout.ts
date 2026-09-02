import { Person, Relationship } from '../../core/models';

export interface LayoutNode {
  key: string;
  kind: 'root' | 'union' | 'person';
  personIds: string[];
  children: LayoutNode[];
}

/**
 * Converts the flat people/relationships lists into a single-rooted tree structure suitable for
 * d3-hierarchy, grouping shared children under a couple's union node when both parents currently
 * form a COUPLE relationship (per the 2026-08-30 clarification on couple/children grouping).
 *
 * Simplification: if a child's two recorded parents are not a recognized CURRENT couple, the child
 * is attached under the first recorded parent only, to keep the result a strict tree (required by
 * d3-hierarchy) rather than a general DAG.
 */
export function buildLayoutTree(people: Person[], relationships: Relationship[]): LayoutNode {
  const unions = new Map<string, { partnerIds: string[] }>();
  const currentCoupleKeyByPerson = new Map<string, string>();

  relationships
    .filter((r) => r.type === 'COUPLE' && r.status === 'CURRENT' && r.partnerAId && r.partnerBId)
    .forEach((r) => {
      const key = [r.partnerAId!, r.partnerBId!].sort().join('__');
      unions.set(key, { partnerIds: [r.partnerAId!, r.partnerBId!] });
      currentCoupleKeyByPerson.set(r.partnerAId!, key);
      currentCoupleKeyByPerson.set(r.partnerBId!, key);
    });

  const nodeKeyForPerson = (personId: string): string => currentCoupleKeyByPerson.get(personId) ?? personId;

  const parentsOfChild = new Map<string, string[]>();
  relationships
    .filter((r) => r.type === 'PARENT_CHILD' && r.parentId && r.childId)
    .forEach((r) => {
      const list = parentsOfChild.get(r.childId!) ?? [];
      list.push(r.parentId!);
      parentsOfChild.set(r.childId!, list);
    });

  const allNodeKeys = new Set<string>(people.map((p) => nodeKeyForPerson(p.id)));
  const childKeysByNodeKey = new Map<string, Set<string>>();

  parentsOfChild.forEach((parents, childId) => {
    let attachTo: string;
    if (parents.length === 2) {
      const unionKey = [...parents].sort().join('__');
      attachTo = unions.has(unionKey) ? unionKey : nodeKeyForPerson(parents[0]);
    } else {
      attachTo = nodeKeyForPerson(parents[0]);
    }
    const set = childKeysByNodeKey.get(attachTo) ?? new Set<string>();
    set.add(nodeKeyForPerson(childId));
    childKeysByNodeKey.set(attachTo, set);
  });

  const childKeySet = new Set<string>();
  childKeysByNodeKey.forEach((set) => set.forEach((k) => childKeySet.add(k)));
  const rootKeys = [...allNodeKeys].filter((k) => !childKeySet.has(k));

  const built = new Set<string>();
  function buildNode(key: string): LayoutNode {
    built.add(key);
    const union = unions.get(key);
    const childKeys = [...(childKeysByNodeKey.get(key) ?? [])].filter((k) => !built.has(k));
    return {
      key,
      kind: union ? 'union' : 'person',
      personIds: union ? union.partnerIds : [key],
      children: childKeys.map(buildNode)
    };
  }

  return {
    key: '__root__',
    kind: 'root',
    personIds: [],
    children: rootKeys.map(buildNode)
  };
}
