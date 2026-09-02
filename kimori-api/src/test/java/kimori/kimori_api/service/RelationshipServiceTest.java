package kimori.kimori_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kimori.kimori_api.dto.RelationshipRequest;
import kimori.kimori_api.exception.ApiException;
import kimori.kimori_api.model.FamilyTree;
import kimori.kimori_api.model.Person;
import kimori.kimori_api.model.Relationship;
import kimori.kimori_api.model.RelationshipType;
import kimori.kimori_api.repository.RelationshipRepository;

/** Unit tests for FR-012/FR-012a: cycle detection, parent cap, and self-reference rejection. */
class RelationshipServiceTest {

    private RelationshipRepository relationshipRepository;
    private PersonService personService;
    private TreeService treeService;
    private RelationshipService relationshipService;
    private final List<Relationship> store = new ArrayList<>();

    private static final String UID = "uid-1";
    private static final String TREE_ID = "tree-1";

    @BeforeEach
    void setUp() {
        relationshipRepository = mock(RelationshipRepository.class);
        personService = mock(PersonService.class);
        treeService = mock(TreeService.class);
        relationshipService = new RelationshipService(relationshipRepository, personService, treeService);

        store.clear();
        when(treeService.get(anyString(), anyString()))
                .thenReturn(FamilyTree.builder().id(TREE_ID).ownerUid(UID).name("Test").build());
        when(personService.get(anyString(), anyString(), anyString()))
                .thenAnswer(inv -> Person.builder().id(inv.getArgument(2)).build());
        when(relationshipRepository.findAll(anyString(), anyString())).thenAnswer(inv -> new ArrayList<>(store));
        when(relationshipRepository.save(anyString(), anyString(), any(Relationship.class))).thenAnswer(inv -> {
            Relationship r = inv.getArgument(2);
            r.setId("rel-" + store.size());
            store.add(r);
            return r;
        });
    }

    @Test
    void rejectsSelfReferenceAsParent() {
        RelationshipRequest request = new RelationshipRequest(RelationshipType.PARENT_CHILD, "p1", "p1", null, null);

        assertThatThrownBy(() -> relationshipService.create(UID, TREE_ID, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("own parent");
    }

    @Test
    void rejectsThirdParentForAChild() {
        addParentChild("mom", "child");
        addParentChild("dad", "child");

        RelationshipRequest request = new RelationshipRequest(RelationshipType.PARENT_CHILD, "stepdad", "child", null,
                null);

        assertThatThrownBy(() -> relationshipService.create(UID, TREE_ID, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("more than two parents");
    }

    @Test
    void rejectsCycleWhereChildWouldBecomeOwnAncestor() {
        addParentChild("grandparent", "parent");
        addParentChild("parent", "child");

        // Attempting to make "child" a parent of "grandparent" would create a cycle.
        RelationshipRequest request = new RelationshipRequest(RelationshipType.PARENT_CHILD, "child", "grandparent",
                null, null);

        assertThatThrownBy(() -> relationshipService.create(UID, TREE_ID, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("ancestor");
    }

    @Test
    void allowsValidParentChildRelationship() {
        RelationshipRequest request = new RelationshipRequest(RelationshipType.PARENT_CHILD, "mom", "child", null,
                null);

        Relationship result = relationshipService.create(UID, TREE_ID, request);

        assertThat(result.getType()).isEqualTo(RelationshipType.PARENT_CHILD);
        assertThat(result.getParentId()).isEqualTo("mom");
        assertThat(result.getChildId()).isEqualTo("child");
    }

    @Test
    void rejectsCoupleSelfReference() {
        RelationshipRequest request = new RelationshipRequest(RelationshipType.COUPLE, null, null, "p1", "p1");

        assertThatThrownBy(() -> relationshipService.create(UID, TREE_ID, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("own partner");
    }

    private void addParentChild(String parentId, String childId) {
        store.add(Relationship.builder()
                .id("seed-" + store.size())
                .treeId(TREE_ID)
                .type(RelationshipType.PARENT_CHILD)
                .parentId(parentId)
                .childId(childId)
                .createdAt(new Date())
                .build());
    }
}
