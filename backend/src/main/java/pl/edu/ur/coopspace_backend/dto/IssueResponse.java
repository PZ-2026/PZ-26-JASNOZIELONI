package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;

import java.time.LocalDateTime;

/**
 * Response model describing an issue.
 */
@Getter
@Setter
@AllArgsConstructor
public class IssueResponse {
    /**
     * Creates an empty issue response.
     */
    public IssueResponse() {
    }

    private Integer id;
    private String title;
    private String description;
    private Integer categoryId;
    private Integer localId;
    private Integer createdByUserId;
    private Integer mainAssigneeId;
    private IssueStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private String maintainerComment;
}
