package kimori.kimori_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import kimori.kimori_api.dto.PersonRequest;
import kimori.kimori_api.exception.ApiException;
import kimori.kimori_api.model.Person;
import kimori.kimori_api.service.PersonService;
import kimori.kimori_api.util.CurrentUser;

/** FR-004–FR-008, FR-017, FR-019, FR-024: person CRUD + picture/certificate upload endpoints. */
@RestController
@RequestMapping("/api/trees/{treeId}/people")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public List<Person> list(@PathVariable String treeId) {
        return personService.list(CurrentUser.uid(), treeId);
    }

    @PostMapping
    public ResponseEntity<Person> create(@PathVariable String treeId, @Valid @RequestBody PersonRequest request) {
        Person person = personService.create(CurrentUser.uid(), treeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    @GetMapping("/{personId}")
    public Person get(@PathVariable String treeId, @PathVariable String personId) {
        return personService.get(CurrentUser.uid(), treeId, personId);
    }

    @PatchMapping("/{personId}")
    public Person update(@PathVariable String treeId, @PathVariable String personId,
            @Valid @RequestBody PersonRequest request) {
        return personService.update(CurrentUser.uid(), treeId, personId, request);
    }

    @DeleteMapping("/{personId}")
    public ResponseEntity<Void> delete(@PathVariable String treeId, @PathVariable String personId) {
        personService.delete(CurrentUser.uid(), treeId, personId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{personId}/picture")
    public Person uploadPicture(@PathVariable String treeId, @PathVariable String personId,
            @RequestParam("file") MultipartFile file) {
        return personService.uploadPicture(CurrentUser.uid(), treeId, personId, file);
    }

    @DeleteMapping("/{personId}/picture")
    public ResponseEntity<Void> deletePicture(@PathVariable String treeId, @PathVariable String personId) {
        personService.deletePicture(CurrentUser.uid(), treeId, personId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{personId}/certificates/{kind}")
    public Person uploadCertificate(@PathVariable String treeId, @PathVariable String personId,
            @PathVariable String kind, @RequestParam("file") MultipartFile file) {
        validateKind(kind);
        return personService.uploadCertificate(CurrentUser.uid(), treeId, personId, kind, file);
    }

    @DeleteMapping("/{personId}/certificates/{kind}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable String treeId, @PathVariable String personId,
            @PathVariable String kind) {
        validateKind(kind);
        personService.deleteCertificate(CurrentUser.uid(), treeId, personId, kind);
        return ResponseEntity.noContent().build();
    }

    private void validateKind(String kind) {
        if (!"birth".equals(kind) && !"death".equals(kind)) {
            throw ApiException.validation("kind must be 'birth' or 'death'", "kind");
        }
    }
}
