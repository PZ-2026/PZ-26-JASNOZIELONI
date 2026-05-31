package pl.edu.ur.coopspace_backend.service;

import pl.edu.ur.coopspace_backend.dto.AuthResponse;
import pl.edu.ur.coopspace_backend.dto.LoginRequest;
import pl.edu.ur.coopspace_backend.dto.RegisterRequest;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements authentication and registration workflows.
 */
@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    /**
     * Creates an authentication service.
     *
     * @param userRepository user persistence access
     * @param passwordEncoder password hashing service
     * @param jwtService JWT token service
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Authenticates a user with provided credentials.
     *
     * @param request login request containing email and password
     * @return authentication response with JWT and user details
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed – user not found: {}", request.getEmail());
                    return new RuntimeException("Użytkownik nie znaleziony");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed – invalid password for email: {}", request.getEmail());
            throw new RuntimeException("Nieprawidłowe hasło");
        }

        if (!user.getIsActive()) {
            log.warn("Login failed – inactive account for email: {}", request.getEmail());
            throw new RuntimeException("Użytkownik jest nieaktywny");
        }

        // 3. Generujemy token podczas logowania
        String token = jwtService.generateToken(user.getEmail());

        log.info("Login successful for user id {} (email: {})", user.getId(), user.getEmail());
        return mapToAuthResponse(user, token);
    }


    /**
     * Registers a new resident account and returns an authentication response.
     *
     * @param request registration data for the new user
     * @return authentication response with JWT and new user details
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed – email already exists: {}", request.getEmail());
            throw new RuntimeException("Email jest już zarejestrowany");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.RESIDENT)
                .isActive(true)
                .build();

        userRepository.save(user);

        // 4. Generujemy token również przy rejestracji (dzięki temu user jest od razu zalogowany)
        String token = jwtService.generateToken(user.getEmail());

        log.info("Registration successful for user id {} (email: {})", user.getId(), user.getEmail());
        return mapToAuthResponse(user, token);
    }

    private AuthResponse mapToAuthResponse(User user, String token) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getLocalId()
        );
    }
}
