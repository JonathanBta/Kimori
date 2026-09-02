package kimori.kimori_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** FR-004, FR-005: request body to create or edit a person. */
public record PersonRequest(
        @NotBlank(message = "Name is required") @Size(max = 200) String name,
        String dateOfBirth,
        String dateOfDeath,
        String comments) {
}
