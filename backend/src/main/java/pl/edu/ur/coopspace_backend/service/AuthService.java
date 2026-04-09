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

@Service
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Nieprawidłowe hasło");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("Użytkownik jest nieaktywny");
        }

        // 3. Generujemy token podczas logowania
        String token = jwtService.generateToken(user.getEmail());

        return mapToAuthResponse(user, token);
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
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
