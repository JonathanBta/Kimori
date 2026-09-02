package kimori.kimori_api.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import kimori.kimori_api.exception.ApiException;
import kimori.kimori_api.model.FamilyTree;
import kimori.kimori_api.repository.FamilyTreeRepository;
import kimori.kimori_api.repository.PersonRepository;
import kimori.kimori_api.repository.RelationshipRepository;

/** FR-001–FR-003, FR-020, FR-022a: tree CRUD, scoped to the authenticated user's UID. */
@Service
public class TreeService {

    private final FamilyTreeRepository familyTreeRepository;
    private final PersonRepository personRepository;
    private final RelationshipRepository relationshipRepository;
    private final StorageService storageService;

    public TreeService(FamilyTreeRepository familyTreeRepository,
            PersonRepository personRepository,
            RelationshipRepository relationshipRepository,
            StorageService storageService) {
        this.familyTreeRepository = familyTreeRepository;
        this.personRepository = personRepository;
        this.relationshipRepository = relationshipRepository;
        this.storageService = storageService;
    }

    public List<FamilyTree> list(String uid) {
        return familyTreeRepository.findAll(uid);
    }

    public FamilyTree create(String uid, String name) {
        Date now = new Date();
        FamilyTree tree = FamilyTree.builder()
                .ownerUid(uid)
                .name(name)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return familyTreeRepository.save(uid, tree);
    }

    public FamilyTree get(String uid, String treeId) {
        return familyTreeRepository.findById(uid, treeId)
                .orElseThrow(() -> ApiException.notFound("Tree not found"));
    }

    public FamilyTree rename(String uid, String treeId, String name) {
        FamilyTree tree = get(uid, treeId);
        tree.setName(name);
        tree.setUpdatedAt(new Date());
        return familyTreeRepository.save(uid, tree);
    }

    /** FR-020: cascading delete — removes every person/relationship document and their Storage files. */
    public void delete(String uid, String treeId) {
        get(uid, treeId); // 404s if the tree does not belong to this user

        personRepository.findAll(uid, treeId).forEach(person -> {
            storageService.deleteFile(person.getPictureUrl());
            storageService.deleteFile(person.getBirthCertificateUrl());
            storageService.deleteFile(person.getDeathCertificateUrl());
            personRepository.delete(uid, treeId, person.getId());
        });
        relationshipRepository.findAll(uid, treeId)
                .forEach(relationship -> relationshipRepository.delete(uid, treeId, relationship.getId()));

        familyTreeRepository.delete(uid, treeId);
    }
}
