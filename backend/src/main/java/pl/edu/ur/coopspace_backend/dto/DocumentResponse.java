package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response model describing a stored document.
 */
@Data
@Builder
@AllArgsConstructor
public class DocumentResponse {
    /**
     * Creates an empty document response.
     */
    public DocumentResponse() {
    }

    private Integer id;
    private String title;
    private String filePath;
    private Integer uploadedBy;
    private LocalDateTime createdAt;
}
