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
import kimori.kimori_api.dto.TreeRequest;
import kimori.kimori_api.model.FamilyTree;
import kimori.kimori_api.service.TreeService;
import kimori.kimori_api.util.CurrentUser;

/** FR-001–FR-003, FR-020: tree CRUD endpoints, per contracts/api.md. */
@RestController
@RequestMapping("/api/trees")
public class TreeController {

    private final TreeService treeService;

    public TreeController(TreeService treeService) {
        this.treeService = treeService;
    }

    @GetMapping
    public List<FamilyTree> list() {
        return treeService.list(CurrentUser.uid());
    }

    @PostMapping
    public ResponseEntity<FamilyTree> create(@Valid @RequestBody TreeRequest request) {
        FamilyTree tree = treeService.create(CurrentUser.uid(), request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(tree);
    }

    @GetMapping("/{treeId}")
    public FamilyTree get(@PathVariable String treeId) {
        return treeService.get(CurrentUser.uid(), treeId);
    }

    @PatchMapping("/{treeId}")
    public FamilyTree rename(@PathVariable String treeId, @Valid @RequestBody TreeRequest request) {
        return treeService.rename(CurrentUser.uid(), treeId, request.name());
    }

    @DeleteMapping("/{treeId}")
    public ResponseEntity<Void> delete(@PathVariable String treeId) {
        treeService.delete(CurrentUser.uid(), treeId);
        return ResponseEntity.noContent().build();
    }
}
