package kimori.kimori_api.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** FR-009–FR-012a, FR-023: a directed parent/child link or a mutual couple link between two people. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Relationship {
    private String id;
    private String treeId;
    private RelationshipType type;
    private String parentId;
    private String childId;
    private String partnerAId;
    private String partnerBId;
    private RelationshipStatus status;
    private Date createdAt;
}
