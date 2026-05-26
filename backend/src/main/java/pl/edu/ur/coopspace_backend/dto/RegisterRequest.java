package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for user registration.
 */
@Getter
@Setter
@AllArgsConstructor
public class RegisterRequest {
    /**
     * Creates an empty registration request.
     */
    public RegisterRequest() {
    }

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
