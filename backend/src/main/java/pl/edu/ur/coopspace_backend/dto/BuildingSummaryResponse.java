package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response model describing a building for selection lists.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuildingSummaryResponse {
    private Integer id;
    private String name;
    private String address;
}
