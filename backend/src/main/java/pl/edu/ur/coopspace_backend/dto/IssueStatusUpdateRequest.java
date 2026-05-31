package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;

/**
 * Request payload for updating the status of an issue.
 */
@Getter
@Setter
@AllArgsConstructor
public class IssueStatusUpdateRequest {
    /**
     * Creates an empty issue status update request.
     */
    public IssueStatusUpdateRequest() {
    }

    private IssueStatus status;
    private String maintainerComment;
}
