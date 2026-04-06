package pl.edu.ur.coopspace_backend.controller;

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
import pl.edu.ur.coopspace_backend.repository.UserRepository;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(Authentication authentication, @RequestBody AdminCreateUserRequest request) {
        User currentUser = requireAdmin(authentication);

        validateCreateRequest(request);
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email jest juz zarejestrowany");
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
        return ResponseEntity.ok(toAdminUserResponse(savedUser));
    }

    @PatchMapping("/{userId}/active")
    public ResponseEntity<AdminUserResponse> updateUserActiveState(
            Authentication authentication,
            @PathVariable Integer userId,
            @RequestBody ActiveStateRequest request
    ) {
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nie mozna dezaktywowac innego administratora tym endpointem");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Administrator moze utworzyc tylko konto mieszkanca lub konserwatora");
        }
        if (request.getRole() == UserRole.RESIDENT && request.getLocalId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dla mieszkanca localId jest wymagane");
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
                user.getIsActive()
        );
    }

    public static class ActiveStateRequest {
        private Boolean isActive;

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }
}
