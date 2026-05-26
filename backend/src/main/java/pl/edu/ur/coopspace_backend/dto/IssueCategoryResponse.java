package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for issue categories.
 */
@Getter
@Setter
@AllArgsConstructor
public class IssueCategoryResponse {
    /**
     * Creates an empty issue category response.
     */
    public IssueCategoryResponse() {
    }

    private Integer id;
    private String name;
}
