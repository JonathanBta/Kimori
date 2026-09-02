package kimori.kimori_api.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import kimori.kimori_api.exception.FirestoreOperationException;
import kimori.kimori_api.model.Relationship;

/** Firestore access for Relationship documents at users/{uid}/trees/{treeId}/relationships/{relationshipId}. */
@Repository
public class RelationshipRepository {

    private final FamilyTreeRepository familyTreeRepository;

    public RelationshipRepository(FamilyTreeRepository familyTreeRepository) {
        this.familyTreeRepository = familyTreeRepository;
    }

    public Relationship save(String uid, String treeId, Relationship relationship) {
        DocumentReference ref = relationship.getId() != null
                ? familyTreeRepository.relationshipsCollection(uid, treeId).document(relationship.getId())
                : familyTreeRepository.relationshipsCollection(uid, treeId).document();
        relationship.setId(ref.getId());
        relationship.setTreeId(treeId);
        try {
            ref.set(relationship).get();
            return relationship;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to save relationship", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to save relationship", e);
        }
    }

    public Optional<Relationship> findById(String uid, String treeId, String relationshipId) {
        try {
            DocumentSnapshot snap = familyTreeRepository.relationshipsCollection(uid, treeId)
                    .document(relationshipId).get().get();
            return snap.exists() ? Optional.ofNullable(snap.toObject(Relationship.class)) : Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to fetch relationship", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to fetch relationship", e);
        }
    }

    public List<Relationship> findAll(String uid, String treeId) {
        try {
            List<QueryDocumentSnapshot> docs = familyTreeRepository.relationshipsCollection(uid, treeId)
                    .get().get().getDocuments();
            List<Relationship> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                result.add(doc.toObject(Relationship.class));
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to list relationships", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to list relationships", e);
        }
    }

    public void delete(String uid, String treeId, String relationshipId) {
        try {
            familyTreeRepository.relationshipsCollection(uid, treeId).document(relationshipId).delete().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to delete relationship", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to delete relationship", e);
        }
    }
}
