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
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kimori.kimori_api.dto.RelationshipRequest;
import kimori.kimori_api.dto.RelationshipStatusRequest;
import kimori.kimori_api.model.Relationship;
import kimori.kimori_api.service.RelationshipService;
import kimori.kimori_api.util.CurrentUser;

/** FR-009–FR-012a, FR-023: relationship CRUD endpoints, per contracts/api.md. */
@RestController
@RequestMapping("/api/trees/{treeId}/relationships")
public class RelationshipController {

    private final RelationshipService relationshipService;

    public RelationshipController(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @GetMapping
    public List<Relationship> list(@PathVariable String treeId) {
        return relationshipService.list(CurrentUser.uid(), treeId);
    }

    @PostMapping
    public ResponseEntity<Relationship> create(@PathVariable String treeId,
            @Valid @RequestBody RelationshipRequest request) {
        Relationship relationship = relationshipService.create(CurrentUser.uid(), treeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(relationship);
    }

    @PatchMapping("/{relationshipId}")
    public Relationship updateStatus(@PathVariable String treeId, @PathVariable String relationshipId,
            @Valid @RequestBody RelationshipStatusRequest request) {
        return relationshipService.updateStatus(CurrentUser.uid(), treeId, relationshipId, request.status());
    }

    @DeleteMapping("/{relationshipId}")
    public ResponseEntity<Void> delete(@PathVariable String treeId, @PathVariable String relationshipId) {
        relationshipService.delete(CurrentUser.uid(), treeId, relationshipId);
        return ResponseEntity.noContent().build();
    }
}
