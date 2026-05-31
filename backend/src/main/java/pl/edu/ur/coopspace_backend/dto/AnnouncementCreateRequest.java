package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Request payload for creating a new announcement.
 */
@Data
@Builder
@AllArgsConstructor
public class AnnouncementCreateRequest {
    /**
     * Creates an empty announcement creation request.
     */
    public AnnouncementCreateRequest() {
    }

    private String title;
    private String content;
}
