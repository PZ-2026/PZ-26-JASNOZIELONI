package pl.edu.ur.coopspace_backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.dto.AdminCreateUserRequest;
import pl.edu.ur.coopspace_backend.dto.AdminUserResponse;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.LocalRepository;
import pl.edu.ur.coopspace_backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController - testy createUser")
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocalRepository localRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        when(authentication.getName()).thenReturn("admin@example.com");
    }

    @Test
    @DisplayName("createUser - sukces dla rezydenta")
    void testCreateUserSuccess() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        AdminCreateUserRequest req = new AdminCreateUserRequest();
        req.setEmail("new@example.com");
        req.setPassword("secret");
        req.setFirstName("First");
        req.setLastName("Last");
        req.setPhoneNumber("123");
        req.setRole(UserRole.RESIDENT);
        req.setLocalId(5);

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(localRepository.existsById(5)).thenReturn(true);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");

        User saved = User.builder()
                .id(42)
                .email("new@example.com")
                .firstName("First")
                .lastName("Last")
                .phoneNumber("123")
                .role(UserRole.RESIDENT)
                .localId(5)
                .isActive(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(saved);

        // When
        ResponseEntity<AdminUserResponse> resp = userController.createUser(authentication, req);

        // Then
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        AdminUserResponse body = resp.getBody();
        assertNotNull(body);
        assertEquals(42, body.getId());
        assertEquals("new@example.com", body.getEmail());
        assertEquals(UserRole.RESIDENT, body.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - istniejący email zwraca 400")
    void testCreateUserExistingEmailThrows() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        AdminCreateUserRequest req = new AdminCreateUserRequest();
        req.setEmail("exists@example.com");
        req.setPassword("secret");
        req.setFirstName("F");
        req.setLastName("L");
        req.setRole(UserRole.MAINTAINER);

        when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userController.createUser(authentication, req));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Email jest juz zarejestrowany"));
    }

    @Test
    @DisplayName("updateUserActiveState - sukces dla admina")
    void testUpdateUserActiveStateSuccess() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        User target = User.builder()
                .id(10)
                .email("user@example.com")
                .role(UserRole.RESIDENT)
                .isActive(false)
                .firstName("User")
                .lastName("Test")
                .build();
        when(userRepository.findById(10)).thenReturn(Optional.of(target));

        UserController.ActiveStateRequest request = new UserController.ActiveStateRequest();
        request.setIsActive(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResponseEntity<AdminUserResponse> response = userController.updateUserActiveState(authentication, 10, request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(10, response.getBody().getId());
        assertTrue(response.getBody().getIsActive());
        verify(userRepository).save(argThat(user -> Boolean.TRUE.equals(user.getIsActive())));
    }

    @Test
    @DisplayName("updateUserActiveState - null request zwraca 400")
    void testUpdateUserActiveStateNullRequestThrows() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userController.updateUserActiveState(authentication, 10, null));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Pole isActive jest wymagane"));
    }

    @Test
    @DisplayName("updateUserActiveState - brak użytkownika zwraca 404")
    void testUpdateUserActiveStateUserNotFoundThrows() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        UserController.ActiveStateRequest request = new UserController.ActiveStateRequest();
        request.setIsActive(true);
        when(userRepository.findById(10)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userController.updateUserActiveState(authentication, 10, request));
        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Uzytkownik nie istnieje"));
    }

    @Test
    @DisplayName("updateUserActiveState - nie można dezaktywować własnego konta")
    void testUpdateUserActiveStateCannotDeactivateOwnAccount() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        User self = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).isActive(true).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(self));

        UserController.ActiveStateRequest request = new UserController.ActiveStateRequest();
        request.setIsActive(false);

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userController.updateUserActiveState(authentication, 1, request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Nie mozna dezaktywowac wlasnego konta"));
    }

    @Test
    @DisplayName("updateUserActiveState - użytkownik bez roli admin dostaje 403")
    void testUpdateUserActiveStateNonAdminForbidden() {
        // Given
        User nonAdmin = User.builder().id(2).email("user@example.com").role(UserRole.RESIDENT).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(nonAdmin));

        UserController.ActiveStateRequest request = new UserController.ActiveStateRequest();
        request.setIsActive(true);

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userController.updateUserActiveState(authentication, 10, request));
        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Tylko administrator ma dostep do tego endpointu"));
    }
}
