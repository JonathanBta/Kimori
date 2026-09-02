package kimori.kimori_api.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** FR-004, FR-005, FR-006: a person within a tree; dates stored as ISO-8601 strings ("yyyy-MM-dd"). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {
    private String id;
    private String treeId;
    private String name;
    private String dateOfBirth;
    private String dateOfDeath;
    private String pictureUrl;
    private String comments;
    private String birthCertificateUrl;
    private String deathCertificateUrl;
    private Date createdAt;
    private Date updatedAt;
}
