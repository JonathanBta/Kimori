package kimori.kimori_api.service;

import java.util.Date;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;

import kimori.kimori_api.dto.RelationshipRequest;
import kimori.kimori_api.exception.ApiException;
import kimori.kimori_api.model.Relationship;
import kimori.kimori_api.model.RelationshipStatus;
import kimori.kimori_api.model.RelationshipType;
import kimori.kimori_api.repository.RelationshipRepository;

/** FR-009–FR-012a, FR-023: relationship CRUD with cycle/parent-cap/self-reference validation. */
@Service
public class RelationshipService {

    private static final int MAX_PARENTS = 2;

    private final RelationshipRepository relationshipRepository;
    private final PersonService personService;
    private final TreeService treeService;

    public RelationshipService(RelationshipRepository relationshipRepository,
            PersonService personService,
            TreeService treeService) {
        this.relationshipRepository = relationshipRepository;
        this.personService = personService;
        this.treeService = treeService;
    }

    public List<Relationship> list(String uid, String treeId) {
        treeService.get(uid, treeId);
        return relationshipRepository.findAll(uid, treeId);
    }

    public Relationship create(String uid, String treeId, RelationshipRequest request) {
        treeService.get(uid, treeId);
        return switch (request.type()) {
            case PARENT_CHILD -> createParentChild(uid, treeId, request.parentId(), request.childId());
            case COUPLE -> createCouple(uid, treeId, request.partnerAId(), request.partnerBId());
        };
    }

    public Relationship updateStatus(String uid, String treeId, String relationshipId, RelationshipStatus status) {
        treeService.get(uid, treeId);
        Relationship relationship = relationshipRepository.findById(uid, treeId, relationshipId)
                .orElseThrow(() -> ApiException.notFound("Relationship not found"));
        if (relationship.getType() != RelationshipType.COUPLE) {
            throw ApiException.validation("Only COUPLE relationships have a status", "status");
        }
        relationship.setStatus(status);
        return relationshipRepository.save(uid, treeId, relationship);
    }

    public void delete(String uid, String treeId, String relationshipId) {
        treeService.get(uid, treeId);
        relationshipRepository.findById(uid, treeId, relationshipId)
                .orElseThrow(() -> ApiException.notFound("Relationship not found"));
        relationshipRepository.delete(uid, treeId, relationshipId);
    }

    private Relationship createParentChild(String uid, String treeId, String parentId, String childId) {
        if (parentId == null || childId == null) {
            throw ApiException.validation("parentId and childId are required", "parentId");
        }
        if (parentId.equals(childId)) {
            throw ApiException.validation("A person cannot be their own parent", "parentId");
        }
        personService.get(uid, treeId, parentId);
        personService.get(uid, treeId, childId);

        List<Relationship> existing = relationshipRepository.findAll(uid, treeId);

        long parentCount = existing.stream()
                .filter(r -> r.getType() == RelationshipType.PARENT_CHILD && childId.equals(r.getChildId()))
                .count();
        if (parentCount >= MAX_PARENTS) {
            throw ApiException.validation("A person cannot have more than two parents", "childId");
        }

        if (ancestorsOf(parentId, existing).contains(childId)) {
            throw ApiException.validation(
                    "This would make a person their own ancestor or descendant", "childId");
        }

        Relationship relationship = Relationship.builder()
                .treeId(treeId)
                .type(RelationshipType.PARENT_CHILD)
                .parentId(parentId)
                .childId(childId)
                .createdAt(new Date())
                .build();
        return relationshipRepository.save(uid, treeId, relationship);
    }

    private Relationship createCouple(String uid, String treeId, String partnerAId, String partnerBId) {
        if (partnerAId == null || partnerBId == null) {
            throw ApiException.validation("partnerAId and partnerBId are required", "partnerAId");
        }
        if (partnerAId.equals(partnerBId)) {
            throw ApiException.validation("A person cannot be their own partner", "partnerAId");
        }
        personService.get(uid, treeId, partnerAId);
        personService.get(uid, treeId, partnerBId);

        Relationship relationship = Relationship.builder()
                .treeId(treeId)
                .type(RelationshipType.COUPLE)
                .partnerAId(partnerAId)
                .partnerBId(partnerBId)
                .status(RelationshipStatus.CURRENT)
                .createdAt(new Date())
                .build();
        return relationshipRepository.save(uid, treeId, relationship);
    }

    /** BFS over existing PARENT_CHILD edges to collect every ancestor (parent, grandparent, ...) of personId. */
    private Set<String> ancestorsOf(String personId, List<Relationship> existing) {
        Set<String> ancestors = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(personId);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (Relationship r : existing) {
                if (r.getType() == RelationshipType.PARENT_CHILD
                        && Objects.equals(r.getChildId(), current)
                        && ancestors.add(r.getParentId())) {
                    queue.add(r.getParentId());
                }
            }
        }
        return ancestors;
    }
}
