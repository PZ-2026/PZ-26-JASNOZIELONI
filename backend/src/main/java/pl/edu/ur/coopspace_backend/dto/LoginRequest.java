package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for user login.
 */
@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {
    /**
     * Creates an empty login request.
     */
    public LoginRequest() {
    }

    private String email;
    private String password;
}
