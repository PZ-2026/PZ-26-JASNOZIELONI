package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload used to create a new issue.
 */
@Getter
@Setter
@AllArgsConstructor
public class IssueCreateRequest {
    /**
     * Creates an empty issue creation request.
     */
    public IssueCreateRequest() {
    }

    private String title;
    private String description;
    private Integer categoryId;
    private Integer localId;
}
