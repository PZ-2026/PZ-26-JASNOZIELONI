package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response model for issue image metadata.
 */
@Getter
@Setter
@AllArgsConstructor
public class IssueImageResponse {
    /**
     * Creates an empty issue image response.
     */
    public IssueImageResponse() {
    }

    private Integer id;
    private Integer issueId;
    private String filePath;
    private String downloadUrl;
    private LocalDateTime createdAt;
}