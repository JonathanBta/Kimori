package kimori.kimori_api.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import kimori.kimori_api.exception.FirestoreOperationException;
import kimori.kimori_api.model.FamilyTree;

/** Firestore access for FamilyTree documents at users/{uid}/trees/{treeId} (FR-002, FR-022a). */
@Repository
public class FamilyTreeRepository {

    private final Firestore firestore;

    public FamilyTreeRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public CollectionReference treesCollection(String uid) {
        return firestore.collection("users").document(uid).collection("trees");
    }

    public CollectionReference peopleCollection(String uid, String treeId) {
        return treesCollection(uid).document(treeId).collection("people");
    }

    public CollectionReference relationshipsCollection(String uid, String treeId) {
        return treesCollection(uid).document(treeId).collection("relationships");
    }

    public FamilyTree save(String uid, FamilyTree tree) {
        DocumentReference ref = tree.getId() != null
                ? treesCollection(uid).document(tree.getId())
                : treesCollection(uid).document();
        tree.setId(ref.getId());
        try {
            ref.set(tree).get();
            return tree;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to save tree", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to save tree", e);
        }
    }

    public Optional<FamilyTree> findById(String uid, String treeId) {
        try {
            DocumentSnapshot snap = treesCollection(uid).document(treeId).get().get();
            return snap.exists() ? Optional.ofNullable(snap.toObject(FamilyTree.class)) : Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to fetch tree", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to fetch tree", e);
        }
    }

    public List<FamilyTree> findAll(String uid) {
        try {
            List<QueryDocumentSnapshot> docs = treesCollection(uid).get().get().getDocuments();
            List<FamilyTree> result = new ArrayList<>();
            System.out.println(docs);
            for (QueryDocumentSnapshot doc : docs) {
                result.add(doc.toObject(FamilyTree.class));
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to list trees", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to list trees", e);
        }
    }

    public void delete(String uid, String treeId) {
        try {
            treesCollection(uid).document(treeId).delete().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to delete tree", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to delete tree", e);
        }
    }
}
