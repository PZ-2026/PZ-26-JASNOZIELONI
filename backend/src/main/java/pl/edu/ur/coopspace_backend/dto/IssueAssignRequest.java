package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request payload for assigning an issue to a maintainer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IssueAssignRequest {
    private Integer assigneeUserId;
}
