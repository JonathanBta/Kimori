package kimori.kimori_api.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** FR-001, FR-002, FR-020: a named, independent collection of people and relationships, owned by one user. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyTree {
    private String id;
    private String ownerUid;
    private String name;
    private Date createdAt;
    private Date updatedAt;
}
