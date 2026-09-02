package kimori.kimori_api.service;

import java.util.Date; 
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kimori.kimori_api.dto.PersonRequest;
import kimori.kimori_api.exception.ApiException;
import kimori.kimori_api.model.Person;
import kimori.kimori_api.repository.PersonRepository;
import kimori.kimori_api.repository.RelationshipRepository;

/** FR-004, FR-005, FR-017, FR-019, FR-024: person CRUD, date validation, and cascade delete. */
@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final RelationshipRepository relationshipRepository;
    private final TreeService treeService;
    private final StorageService storageService;

    public PersonService(PersonRepository personRepository,
            RelationshipRepository relationshipRepository,
            TreeService treeService,
            StorageService storageService) {
        this.personRepository = personRepository;
        this.relationshipRepository = relationshipRepository;
        this.treeService = treeService;
        this.storageService = storageService;
    }

    public List<Person> list(String uid, String treeId) {
        treeService.get(uid, treeId);
        return personRepository.findAll(uid, treeId);
    }

    public Person get(String uid, String treeId, String personId) {
        treeService.get(uid, treeId);
        return personRepository.findById(uid, treeId, personId)
                .orElseThrow(() -> ApiException.notFound("Person not found"));
    }

    public Person create(String uid, String treeId, PersonRequest request) {
        treeService.get(uid, treeId);
        validateDates(request.dateOfBirth(), request.dateOfDeath());
        Date now = new Date();
        Person person = Person.builder()
                .treeId(treeId)
                .name(request.name())
                .dateOfBirth(request.dateOfBirth())
                .dateOfDeath(request.dateOfDeath())
                .comments(request.comments())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return personRepository.save(uid, treeId, person);
    }

    public Person update(String uid, String treeId, String personId, PersonRequest request) {
        Person person = get(uid, treeId, personId);
        validateDates(request.dateOfBirth(), request.dateOfDeath());
        person.setName(request.name());
        person.setDateOfBirth(request.dateOfBirth());
        person.setDateOfDeath(request.dateOfDeath());
        person.setComments(request.comments());
        person.setUpdatedAt(new Date());
        return personRepository.save(uid, treeId, person);
    }

    /** FR-019: removes/invalidates every relationship referencing this person before deleting them. */
    public void delete(String uid, String treeId, String personId) {
        Person person = get(uid, treeId, personId);

        relationshipRepository.findAll(uid, treeId).stream()
                .filter(rel -> personId.equals(rel.getParentId())
                        || personId.equals(rel.getChildId())
                        || personId.equals(rel.getPartnerAId())
                        || personId.equals(rel.getPartnerBId()))
                .forEach(rel -> relationshipRepository.delete(uid, treeId, rel.getId()));

        storageService.deleteFile(person.getPictureUrl());
        storageService.deleteFile(person.getBirthCertificateUrl());
        storageService.deleteFile(person.getDeathCertificateUrl());

        personRepository.delete(uid, treeId, personId);
    }

    public Person uploadPicture(String uid, String treeId, String personId, MultipartFile file) {
        Person person = get(uid, treeId, personId);
        if (person.getPictureUrl() != null) {
            storageService.deleteFile(person.getPictureUrl());
        }
        person.setPictureUrl(storageService.uploadPicture(uid, treeId, personId, file));
        person.setUpdatedAt(new Date());
        return personRepository.save(uid, treeId, person);
    }

    public void deletePicture(String uid, String treeId, String personId) {
        Person person = get(uid, treeId, personId);
        storageService.deleteFile(person.getPictureUrl());
        person.setPictureUrl(null);
        person.setUpdatedAt(new Date());
        personRepository.save(uid, treeId, person);
    }

    public Person uploadCertificate(String uid, String treeId, String personId, String kind, MultipartFile file) {
        Person person = get(uid, treeId, personId);
        String existing = "birth".equals(kind) ? person.getBirthCertificateUrl() : person.getDeathCertificateUrl();
        if (existing != null) {
            storageService.deleteFile(existing);
        }
        String stored = storageService.uploadCertificate(uid, treeId, personId, kind, file);
        if ("birth".equals(kind)) {
            person.setBirthCertificateUrl(stored);
        } else {
            person.setDeathCertificateUrl(stored);
        }
        person.setUpdatedAt(new Date());
        return personRepository.save(uid, treeId, person);
    }

    public void deleteCertificate(String uid, String treeId, String personId, String kind) {
        Person person = get(uid, treeId, personId);
        String existing = "birth".equals(kind) ? person.getBirthCertificateUrl() : person.getDeathCertificateUrl();
        storageService.deleteFile(existing);
        if ("birth".equals(kind)) {
            person.setBirthCertificateUrl(null);
        } else {
            person.setDeathCertificateUrl(null);
        }
        person.setUpdatedAt(new Date());
        personRepository.save(uid, treeId, person);
    }

    /** FR-017: date of death, when present, must not be earlier than date of birth. */
    private void validateDates(String dateOfBirth, String dateOfDeath) {
        if (dateOfDeath == null || dateOfDeath.isBlank()) {
            return;
        }
        try {
            LocalDate death = LocalDate.parse(dateOfDeath);
            if (dateOfBirth != null && !dateOfBirth.isBlank()) {
                LocalDate birth = LocalDate.parse(dateOfBirth);
                if (death.isBefore(birth)) {
                    throw ApiException.validation("Date of death cannot be earlier than date of birth", "dateOfDeath");
                }
            }
        } catch (DateTimeParseException e) {
            throw ApiException.validation("Dates must be in ISO-8601 format (yyyy-MM-dd)", "dateOfDeath");
        }
    }
}
