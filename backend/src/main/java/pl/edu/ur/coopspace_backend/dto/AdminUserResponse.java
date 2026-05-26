package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.edu.ur.coopspace_backend.entity.UserRole;

/**
 * Administrative view of a user record.
 */
@Getter
@Setter
@AllArgsConstructor
public class AdminUserResponse {
    /**
     * Creates an empty administrative user response.
     */
    public AdminUserResponse() {
    }

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private Integer localId;
    private Boolean isActive;
}
