package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response model for announcement details.
 */
@Data
@Builder
@AllArgsConstructor
public class AnnouncementResponse {
    /**
     * Creates an empty announcement response.
     */
    public AnnouncementResponse() {
    }

    private Integer id;
    private String title;
    private String content;
    private Integer createdBy;
    private LocalDateTime createdAt;
}
