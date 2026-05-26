package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Lightweight response model for a user summary.
 */
@Getter
@Setter
@AllArgsConstructor
public class UserSummaryResponse {
    /**
     * Creates an empty user summary response.
     */
    public UserSummaryResponse() {
    }

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
}
