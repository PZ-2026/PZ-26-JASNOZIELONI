package pl.edu.ur.coopspace_backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.coopspace_backend.dto.AuthResponse;
import pl.edu.ur.coopspace_backend.dto.LoginRequest;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Testy dla kontrolera autentykacji")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        // Przygotowanie żądania logowania
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Przygotowanie odpowiedzi autentykacji
        authResponse = new AuthResponse();
        authResponse.setToken("jwt-token-123");
        authResponse.setId(1);
        authResponse.setEmail("test@example.com");
        authResponse.setFirstName("Jan");
        authResponse.setLastName("Kowalski");
        authResponse.setPhoneNumber("123456789");
        authResponse.setRole(UserRole.RESIDENT);
        authResponse.setLocalId(1);
    }

    @Test
    @DisplayName("Login powinien zwrócić prawidłową odpowiedź")
    void testLoginSuccess() {
        // Given
        when(authService.login(any())).thenReturn(authResponse);

        // When
        org.springframework.http.ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("jwt-token-123", response.getBody().getToken());
        assertEquals("test@example.com", response.getBody().getEmail());
        verify(authService, times(1)).login(any());
    }

    @Test
    @DisplayName("Login powinien zwrócić AuthResponse ze wszystkimi danymi")
    void testLoginReturnsCompleteAuthResponse() {
        // Given
        when(authService.login(any())).thenReturn(authResponse);

        // When
        org.springframework.http.ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        // Then
        assertNotNull(response.getBody());
        AuthResponse body = response.getBody();
        assertEquals("jwt-token-123", body.getToken());
        assertEquals(1, body.getId());
        assertEquals("test@example.com", body.getEmail());
        assertEquals("Jan", body.getFirstName());
        assertEquals("Kowalski", body.getLastName());
        assertEquals("123456789", body.getPhoneNumber());
        assertEquals(UserRole.RESIDENT, body.getRole());
        assertEquals(1, body.getLocalId());
    }

    @Test
    @DisplayName("Login powinien przekazać żądanie do serwisu")
    void testLoginPassesRequestToService() {
        // Given
        when(authService.login(any())).thenReturn(authResponse);

        // When
        authController.login(loginRequest);

        // Then
        verify(authService, times(1)).login(argThat(request ->
                request.getEmail().equals("test@example.com") &&
                request.getPassword().equals("password123")
        ));
    }

    @Test
    @DisplayName("Login z różnymi rolami powinien zwrócić odpowiednią rolę")
    void testLoginWithDifferentRoles() {
        // Given
        AuthResponse adminResponse = new AuthResponse();
        adminResponse.setToken("jwt-token-admin");
        adminResponse.setEmail("admin@example.com");
        adminResponse.setRole(UserRole.ADMIN);

        when(authService.login(any())).thenReturn(adminResponse);

        // When
        org.springframework.http.ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        // Then
        assertEquals(UserRole.ADMIN, response.getBody().getRole());
    }

    @Test
    @DisplayName("Login powinien wrzucić wyjątek gdy serwis go wrzuci")
    void testLoginThrowsExceptionWhenServiceThrows() {
        // Given
        when(authService.login(any())).thenThrow(new RuntimeException("Nieprawidłowe hasło"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authController.login(loginRequest);
        });

        verify(authService, times(1)).login(any());
    }

    @Test
    @DisplayName("Login powinien zwrócić ResponseEntity")
    void testLoginReturnsResponseEntity() {
        // Given
        when(authService.login(any())).thenReturn(authResponse);

        // When
        var response = authController.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Login nie powinien być null")
    void testLoginReturnsNonNull() {
        // Given
        when(authService.login(any())).thenReturn(authResponse);

        // When
        var response = authController.login(loginRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Login powinien zawierać token")
    void testLoginResponseContainsToken() {
        // Given
        when(authService.login(any())).thenReturn(authResponse);

        // When
        var response = authController.login(loginRequest);

        // Then
        assertNotNull(response.getBody().getToken());
        assertFalse(response.getBody().getToken().isBlank());
    }

    @Test
    @DisplayName("Login powinien zawierać email użytkownika")
    void testLoginResponseContainsEmail() {
        // Given
        when(authService.login(any())).thenReturn(authResponse);

        // When
        var response = authController.login(loginRequest);

        // Then
        assertNotNull(response.getBody().getEmail());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    @Test
    @DisplayName("Kontroler powinien być właściwie zainiojalizowany")
    void testControllerIsInitialized() {
        assertNotNull(authController);
        assertNotNull(authService);
    }

    @Test
    @DisplayName("Login z pustym/popsutym żądaniem powinien rzucić wyjątek (bad request)")
    void testLoginWithMalformedRequestThrows() {
        // Given
        when(authService.login(null)).thenThrow(new IllegalArgumentException("Malformed request"));

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authController.login(null));
        assertEquals("Malformed request", ex.getMessage());
        verify(authService, times(1)).login(null);
    }
}
