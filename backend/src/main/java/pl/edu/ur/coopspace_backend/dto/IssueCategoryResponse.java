package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response model for issue categories.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IssueCategoryResponse {
    private Integer id;
    private String name;
}
