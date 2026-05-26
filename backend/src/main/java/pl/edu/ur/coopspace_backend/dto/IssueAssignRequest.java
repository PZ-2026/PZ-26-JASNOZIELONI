package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for assigning an issue to a maintainer.
 */
@Getter
@Setter
@AllArgsConstructor
public class IssueAssignRequest {
    /**
     * Creates an empty issue assignment request.
     */
    public IssueAssignRequest() {
    }

    private Integer assigneeUserId;
}
