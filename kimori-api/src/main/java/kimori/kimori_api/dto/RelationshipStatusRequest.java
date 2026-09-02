package kimori.kimori_api.dto;

import jakarta.validation.constraints.NotNull;

import kimori.kimori_api.model.RelationshipStatus;

/** FR-023: request body to update a COUPLE relationship's status (current/past). */
public record RelationshipStatusRequest(@NotNull RelationshipStatus status) {
}
