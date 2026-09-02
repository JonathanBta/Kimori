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
import kimori.kimori_api.model.Person;

/** Firestore access for Person documents at users/{uid}/trees/{treeId}/people/{personId}. */
@Repository
public class PersonRepository {

    private final FamilyTreeRepository familyTreeRepository;

    public PersonRepository(FamilyTreeRepository familyTreeRepository) {
        this.familyTreeRepository = familyTreeRepository;
    }

    public Person save(String uid, String treeId, Person person) {
        DocumentReference ref = person.getId() != null
                ? familyTreeRepository.peopleCollection(uid, treeId).document(person.getId())
                : familyTreeRepository.peopleCollection(uid, treeId).document();
        person.setId(ref.getId());
        person.setTreeId(treeId);
        try {
            ref.set(person).get();
            return person;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to save person", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to save person", e);
        }
    }

    public Optional<Person> findById(String uid, String treeId, String personId) {
        try {
            DocumentSnapshot snap = familyTreeRepository.peopleCollection(uid, treeId).document(personId).get().get();
            return snap.exists() ? Optional.ofNullable(snap.toObject(Person.class)) : Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to fetch person", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to fetch person", e);
        }
    }

    public List<Person> findAll(String uid, String treeId) {
        try {
            List<QueryDocumentSnapshot> docs = familyTreeRepository.peopleCollection(uid, treeId).get().get().getDocuments();
            List<Person> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                result.add(doc.toObject(Person.class));
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to list people", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to list people", e);
        }
    }

    public void delete(String uid, String treeId, String personId) {
        try {
            familyTreeRepository.peopleCollection(uid, treeId).document(personId).delete().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreOperationException("Failed to delete person", e);
        } catch (ExecutionException e) {
            throw new FirestoreOperationException("Failed to delete person", e);
        }
    }
}
