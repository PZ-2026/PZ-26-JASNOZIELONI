package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Lightweight response model for local summaries.
 */
@Getter
@Setter
@AllArgsConstructor
public class LocalSummaryResponse {
    /**
     * Creates an empty local summary response.
     */
    public LocalSummaryResponse() {
    }

    private Integer id;
    private Integer buildingId;
    private String number;
    private String staircase;
}