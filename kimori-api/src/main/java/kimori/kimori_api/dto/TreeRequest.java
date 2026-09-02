package kimori.kimori_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** FR-001: request body to create or rename a tree. */
public record TreeRequest(
        @NotBlank(message = "Tree name is required") @Size(max = 200) String name) {
}
