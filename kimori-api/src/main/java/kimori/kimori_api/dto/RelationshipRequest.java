package kimori.kimori_api.dto;

import jakarta.validation.constraints.NotNull;

import kimori.kimori_api.model.RelationshipType;

/** FR-009, FR-010: request body to create a PARENT_CHILD or COUPLE relationship. */
public record RelationshipRequest(
        @NotNull RelationshipType type,
        String parentId,
        String childId,
        String partnerAId,
        String partnerBId) {
}
