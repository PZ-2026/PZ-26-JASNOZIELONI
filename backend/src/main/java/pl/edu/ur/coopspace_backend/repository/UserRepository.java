package pl.edu.ur.coopspace_backend.repository;

import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence access for user records.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * Finds user by email address.
     *
     * @param email user email
     * @return optional user
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether an email is already registered.
     *
     * @param email email to check
     * @return true when exists
     */
    boolean existsByEmail(String email);

    /**
     * Lists users having the given role.
     *
     * @param role role to filter
     * @return list of users with the role
     */
    List<User> findByRole(UserRole role);

    /**
     * Lists active users with the given role.
     *
     * @param role role to filter
     * @return active users with the role
     */
    List<User> findByRoleAndIsActiveTrue(UserRole role);
}
