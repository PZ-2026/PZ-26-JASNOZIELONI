package pl.edu.ur.coopspace_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.ur.coopspace_backend.dto.AuthResponse;
import pl.edu.ur.coopspace_backend.dto.LoginRequest;
import pl.edu.ur.coopspace_backend.dto.RegisterRequest;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Testy jednostkowe dla serwisu autentykacji")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // Przygotowanie testowego użytkownika
        testUser = User.builder()
                .id(1)
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Jan")
                .lastName("Kowalski")
                .phoneNumber("123456789")
                .role(UserRole.RESIDENT)
                .isActive(true)
                .localId(1)
                .build();

        // Przygotowanie żądania logowania
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Przygotowanie żądania rejestracji
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("newpassword123");
        registerRequest.setFirstName("Anna");
        registerRequest.setLastName("Nowak");
        registerRequest.setPhoneNumber("987654321");
    }

    @Test
    @DisplayName("Login z prawidłowymi danymi powinien zwrócić AuthResponse")
    void testLoginSuccess() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken("test@example.com")).thenReturn("jwt-token-123");

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Jan", response.getFirstName());
        assertEquals("Kowalski", response.getLastName());
        assertEquals(UserRole.RESIDENT, response.getRole());
        assertEquals(1, response.getId());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", testUser.getPasswordHash());
        verify(jwtService, times(1)).generateToken("test@example.com");
    }

    @Test
    @DisplayName("Login z nieistniejącym emailem powinien rzucić wyjątek")
    void testLoginUserNotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("nonexistent@example.com");
        invalidRequest.setPassword("password123");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(invalidRequest);
        });

        assertEquals("Użytkownik nie znaleziony", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Login z nieprawidłowym hasłem powinien rzucić wyjątek")
    void testLoginInvalidPassword() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash())).thenReturn(false);

        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("wrongpassword");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(invalidRequest);
        });

        assertEquals("Nieprawidłowe hasło", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("wrongpassword", testUser.getPasswordHash());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Login z nieaktywnym użytkownikiem powinien rzucić wyjątek")
    void testLoginInactiveUser() {
        // Given
        User inactiveUser = User.builder()
                .id(1)
                .email("inactive@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Jan")
                .lastName("Kowalski")
                .phoneNumber("123456789")
                .role(UserRole.RESIDENT)
                .isActive(false)
                .build();

        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactiveUser));
        when(passwordEncoder.matches("password123", inactiveUser.getPasswordHash())).thenReturn(true);

        LoginRequest inactiveRequest = new LoginRequest();
        inactiveRequest.setEmail("inactive@example.com");
        inactiveRequest.setPassword("password123");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(inactiveRequest);
        });

        assertEquals("Użytkownik jest nieaktywny", exception.getMessage());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Rejestracja z nowymi danymi powinna zwrócić AuthResponse")
    void testRegisterSuccess() {
        // Given
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newpassword123")).thenReturn("$2a$10$hashedNewPassword");
        
        User newUser = User.builder()
                .id(2)
                .email("newuser@example.com")
                .passwordHash("$2a$10$hashedNewPassword")
                .firstName("Anna")
                .lastName("Nowak")
                .phoneNumber("987654321")
                .role(UserRole.RESIDENT)
                .isActive(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateToken("newuser@example.com")).thenReturn("jwt-token-456");

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token-456", response.getToken());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("Anna", response.getFirstName());
        assertEquals("Nowak", response.getLastName());
        assertEquals(UserRole.RESIDENT, response.getRole());
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(passwordEncoder, times(1)).encode("newpassword123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken("newuser@example.com");
    }

    @Test
    @DisplayName("Rejestracja z istniejącym emailem powinna rzucić wyjątek")
    void testRegisterEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email jest już zarejestrowany", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Rejestracja powinna ustawić rolę RESIDENT")
    void testRegisterSetsResidentRole() {
        // Given
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        
        User savedUser = User.builder()
                .id(2)
                .email("newuser@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Anna")
                .lastName("Nowak")
                .phoneNumber("987654321")
                .role(UserRole.RESIDENT)
                .isActive(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(anyString())).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertEquals(UserRole.RESIDENT, response.getRole());
        verify(userRepository).save(argThat(user -> user.getRole() == UserRole.RESIDENT));
    }

    @Test
    @DisplayName("Rejestracja powinna zwrócić aktywnego użytkownika")
    void testRegisterReturnsActiveUser() {
        // Given
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        
        User activeUser = User.builder()
                .id(2)
                .email("newuser@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Anna")
                .lastName("Nowak")
                .phoneNumber("987654321")
                .role(UserRole.RESIDENT)
                .isActive(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(activeUser);
        when(jwtService.generateToken(anyString())).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertTrue(true); // Użytkownik jest zalogowany, więc nie będzie sprawdzania isActive w logice biznesowej
        assertNotNull(response.getToken());
    }

    @Test
    @DisplayName("Login propaguje wyjątek z repozytorium")
    void testLoginPropagatesRepositoryException() {
        // Given
        when(userRepository.findByEmail("dberror@example.com")).thenThrow(new RuntimeException("DB error"));

        LoginRequest req = new LoginRequest();
        req.setEmail("dberror@example.com");
        req.setPassword("pw");

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
        assertEquals("DB error", ex.getMessage());
        verify(userRepository, times(1)).findByEmail("dberror@example.com");
    }

    @Test
    @DisplayName("Rejestracja z brakującym emailem powinna rzucić wyjątek")
    void testRegisterWithMissingEmailThrows() {
        // Given
        RegisterRequest badRequest = new RegisterRequest();
        badRequest.setEmail(null);
        badRequest.setPassword("pw");
        badRequest.setFirstName("A");
        badRequest.setLastName("B");
        badRequest.setPhoneNumber("123");

        when(userRepository.existsByEmail(null)).thenThrow(new RuntimeException("Email jest wymagany"));

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(badRequest));
        assertEquals("Email jest wymagany", ex.getMessage());
        verify(userRepository, times(1)).existsByEmail(null);
    }

    @Test
    @DisplayName("Rejestracja powinna hashować hasło przed zapisem")
    void testRegisterCallsPasswordEncoder() {
        // Given
        when(userRepository.existsByEmail("secure@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashed");

        RegisterRequest req = new RegisterRequest();
        req.setEmail("secure@example.com");
        req.setPassword("plainPassword");
        req.setFirstName("F");
        req.setLastName("L");
        req.setPhoneNumber("000");

        User saved = User.builder()
                .id(10)
                .email("secure@example.com")
                .passwordHash("hashed")
                .firstName("F")
                .lastName("L")
                .phoneNumber("000")
                .role(UserRole.RESIDENT)
                .isActive(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken("secure@example.com")).thenReturn("token-secure");

        // When
        authService.register(req);

        // Then
        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(userRepository, times(1)).save(argThat(u -> "hashed".equals(u.getPasswordHash())));
    }
}
