package kimori.kimori_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kimori.kimori_api.dto.PersonRequest;
import kimori.kimori_api.exception.ApiException;
import kimori.kimori_api.model.FamilyTree;
import kimori.kimori_api.model.Person;
import kimori.kimori_api.model.Relationship;
import kimori.kimori_api.model.RelationshipType;
import kimori.kimori_api.repository.PersonRepository;
import kimori.kimori_api.repository.RelationshipRepository;

/** Unit tests for FR-017 (date validation) and FR-019 (cascade delete of relationships). */
class PersonServiceTest {

    private static final String UID = "uid-1";
    private static final String TREE_ID = "tree-1";

    private PersonRepository personRepository;
    private RelationshipRepository relationshipRepository;
    private TreeService treeService;
    private StorageService storageService;
    private PersonService personService;

    @BeforeEach
    void setUp() {
        personRepository = mock(PersonRepository.class);
        relationshipRepository = mock(RelationshipRepository.class);
        treeService = mock(TreeService.class);
        storageService = mock(StorageService.class);
        personService = new PersonService(personRepository, relationshipRepository, treeService, storageService);

        when(treeService.get(anyString(), anyString()))
                .thenReturn(FamilyTree.builder().id(TREE_ID).ownerUid(UID).name("Test").build());
        when(personRepository.save(anyString(), anyString(), any(Person.class)))
                .thenAnswer(inv -> inv.getArgument(2));
    }

    @Test
    void rejectsDateOfDeathBeforeDateOfBirth() {
        PersonRequest request = new PersonRequest("Ada", "2000-01-01", "1999-01-01", null);

        assertThatThrownBy(() -> personService.create(UID, TREE_ID, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("earlier than date of birth");
    }

    @Test
    void allowsMissingDateOfDeath() {
        PersonRequest request = new PersonRequest("Ada", "2000-01-01", null, null);

        Person result = personService.create(UID, TREE_ID, request);

        assertThat(result.getName()).isEqualTo("Ada");
        assertThat(result.getDateOfDeath()).isNull();
    }

    @Test
    void deletingPersonRemovesRelationshipsThatReferenceThem() {
        when(personRepository.findById(UID, TREE_ID, "child"))
                .thenReturn(Optional.of(Person.builder().id("child").treeId(TREE_ID).name("Child").build()));
        Relationship asChild = Relationship.builder().id("rel-1").treeId(TREE_ID)
                .type(RelationshipType.PARENT_CHILD).parentId("mom").childId("child").build();
        Relationship unrelated = Relationship.builder().id("rel-2").treeId(TREE_ID)
                .type(RelationshipType.PARENT_CHILD).parentId("dad").childId("other").build();
        when(relationshipRepository.findAll(UID, TREE_ID)).thenReturn(List.of(asChild, unrelated));

        personService.delete(UID, TREE_ID, "child");

        verify(relationshipRepository).delete(UID, TREE_ID, "rel-1");
        verify(relationshipRepository, never()).delete(eq(UID), eq(TREE_ID), eq("rel-2"));
        verify(personRepository).delete(UID, TREE_ID, "child");
    }
}
