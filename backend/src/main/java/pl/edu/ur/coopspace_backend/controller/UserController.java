package pl.edu.ur.coopspace_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.dto.AdminCreateUserRequest;
import pl.edu.ur.coopspace_backend.dto.AdminUserResponse;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.LocalRepository;
import pl.edu.ur.coopspace_backend.repository.UserRepository;

import java.util.Comparator;
import java.util.List;

/**
 * Administrative endpoints for user management.
 *
 * <p>Allows administrators to list residents and maintainers,
 * create accounts, and update account activation state.</p>
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final LocalRepository localRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a user administration controller.
     *
     * @param userRepository user persistence access
     * @param localRepository local persistence access
     * @param passwordEncoder password hashing service
     */
    public UserController(UserRepository userRepository, LocalRepository localRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.localRepository = localRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Returns all resident accounts visible to the administrator.
     *
     * @param authentication current user authentication
     * @return sorted list of residents
     */
    @GetMapping("/residents")
    public ResponseEntity<List<AdminUserResponse>> getResidents(Authentication authentication) {
        User currentUser = requireAdmin(authentication);

        List<AdminUserResponse> residents = userRepository.findByRole(UserRole.RESIDENT)
                .stream()
                .sorted(Comparator.comparing(User::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(User::getFirstName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toAdminUserResponse)
                .toList();

        return ResponseEntity.ok(residents);
    }

    /**
     * Returns all maintainer accounts visible to the administrator.
     *
     * @param authentication current user authentication
     * @return sorted list of maintainers
     */
    @GetMapping("/maintainers")
    public ResponseEntity<List<AdminUserResponse>> getMaintainers(Authentication authentication) {
        User currentUser = requireAdmin(authentication);

        List<AdminUserResponse> maintainers = userRepository.findByRole(UserRole.MAINTAINER)
                .stream()
                .sorted(Comparator.comparing(User::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(User::getFirstName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toAdminUserResponse)
                .toList();

        return ResponseEntity.ok(maintainers);
    }

    /**
     * Creates a resident or maintainer account.
     *
     * @param authentication current user authentication
     * @param request new user data
     * @return created user data
     */
    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(Authentication authentication,
            @RequestBody AdminCreateUserRequest request) {
        User currentUser = requireAdmin(authentication);
        log.info("Admin {} attempts to create {} user with email {}", authentication.getName(), request.getRole(),
                request.getEmail());

        validateCreateRequest(request);
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Admin {} failed to create user – email already exists: {}", authentication.getName(),
                    request.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email jest juz zarejestrowany");
        }

        if (request.getRole() == UserRole.RESIDENT) {
            validateResidentLocalId(request.getLocalId());
        }

        User user = User.builder()
                .email(request.getEmail().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .localId(request.getRole() == UserRole.RESIDENT ? request.getLocalId() : null)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Admin {} created new {} user with id {}", authentication.getName(), request.getRole(),
                savedUser.getId());
        return ResponseEntity.ok(toAdminUserResponse(savedUser));
    }

    /**
     * Changes the active state of the specified user account.
     *
     * @param authentication current user authentication
     * @param userId target user identifier
     * @param request target active state
     * @return updated user data
     */
    @PatchMapping("/{userId}/active")
    public ResponseEntity<AdminUserResponse> updateUserActiveState(
            Authentication authentication,
            @PathVariable Integer userId,
            @RequestBody ActiveStateRequest request) {
        User currentUser = requireAdmin(authentication);

        if (request == null || request.getIsActive() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pole isActive jest wymagane");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uzytkownik nie istnieje"));

        if (user.getRole() == UserRole.ADMIN && user.getId().equals(currentUser.getId()) && !request.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nie mozna dezaktywowac wlasnego konta");
        }

        if (user.getRole() == UserRole.ADMIN && !user.getId().equals(currentUser.getId()) && !request.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nie mozna dezaktywowac innego administratora tym endpointem");
        }

        user.setIsActive(request.getIsActive());
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(toAdminUserResponse(savedUser));
    }

    private User requireAdmin(Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Uzytkownik niezalogowany"));

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tylko administrator ma dostep do tego endpointu");
        }

        return currentUser;
    }

    private void validateCreateRequest(AdminCreateUserRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dane uzytkownika sa wymagane");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email jest wymagany");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Haslo jest wymagane");
        }
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imie jest wymagane");
        }
        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nazwisko jest wymagane");
        }
        if (request.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rola jest wymagana");
        }
        if (request.getRole() != UserRole.RESIDENT && request.getRole() != UserRole.MAINTAINER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Administrator moze utworzyc tylko konto mieszkanca lub konserwatora");
        }
        if (request.getRole() == UserRole.RESIDENT && request.getLocalId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dla mieszkanca localId jest wymagane");
        }
    }

    private void validateResidentLocalId(Integer localId) {
        if (localId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dla mieszkanca localId jest wymagane");
        }

        if (!localRepository.existsById(localId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wskazany lokal nie istnieje");
        }
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getLocalId(),
                user.getIsActive());
    }

    /**
     * Request payload describing the target active state for a user account.
     */
    public static class ActiveStateRequest {
        private Boolean isActive;

        /**
         * Creates an empty active-state request.
         */
        public ActiveStateRequest() {
        }

        /**
         * Returns requested account active state.
         *
         * @return requested active flag
         */
        public Boolean getIsActive() {
            return isActive;
        }

        /**
         * Sets requested account active state.
         *
         * @param isActive requested active flag
         */
        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }
}
