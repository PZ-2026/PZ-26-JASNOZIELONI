package pl.edu.ur.coopspace_backend.dto;

import pl.edu.ur.coopspace_backend.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Authentication response containing the JWT token and basic user info.
 */
@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    /**
     * Creates an empty authentication response.
     */
    public AuthResponse() {
    }

    private String token; // token JWT
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private Integer localId;
}
