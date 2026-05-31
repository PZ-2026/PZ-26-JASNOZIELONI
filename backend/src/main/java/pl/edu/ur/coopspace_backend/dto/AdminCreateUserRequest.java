package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.edu.ur.coopspace_backend.entity.UserRole;

/**
 * Request payload used by administrators to create a new user.
 */
@Getter
@Setter
@AllArgsConstructor
public class AdminCreateUserRequest {
    /**
     * Creates an empty request payload.
     */
    public AdminCreateUserRequest() {
    }

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private Integer localId;
}
