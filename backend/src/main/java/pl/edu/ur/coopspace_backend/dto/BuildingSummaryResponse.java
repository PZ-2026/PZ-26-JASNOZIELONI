package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model describing a building for selection lists.
 */
@Getter
@Setter
@AllArgsConstructor
public class BuildingSummaryResponse {
    /**
     * Creates an empty building summary response.
     */
    public BuildingSummaryResponse() {
    }

    private Integer id;
    private String name;
    private String address;
}
