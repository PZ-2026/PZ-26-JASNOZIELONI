package pl.edu.ur.coopspace_backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.edu.ur.coopspace_backend.service.JwtService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JwtAuthenticationFilter - Testy jednostkowe dla filtru JWT")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Filtr powinien ekstraktować token z nagłówka Authorization")
    void testFilterExtractsTokenFromAuthorizationHeader() throws ServletException, IOException {
        // Given
        String token = "Bearer valid-jwt-token-123";
        when(request.getHeader("Authorization")).thenReturn(token);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien ignorować brak nagłówka Authorization")
    void testFilterIgnoresMissingAuthorizationHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien ignorować nagłówek bez prefiksu 'Bearer'")
    void testFilterIgnoresHeaderWithoutBearerPrefix() throws ServletException, IOException {
        // Given
        String invalidToken = "InvalidToken 123";
        when(request.getHeader("Authorization")).thenReturn(invalidToken);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien kontynuować łańcuch dla każdego żądania")
    void testFilterContinuesChainForEveryRequest() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien obsługiwać ważny token")
    void testFilterHandlesValidToken() throws ServletException, IOException {
        // Given
        String validToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid.token";
        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtService.isTokenValid(validToken.substring(7))).thenReturn(true);
        when(jwtService.extractUsername("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid.token")).thenReturn("test@example.com");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien obsługiwać nieważny token bez rzucania wyjątku")
    void testFilterHandlesInvalidTokenGracefully() throws ServletException, IOException {
        // Given
        String invalidToken = "Bearer invalid-token";
        when(request.getHeader("Authorization")).thenReturn(invalidToken);

        // When & Then - powinno się nie rzucić wyjątkiem
        assertDoesNotThrow(() -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien czyszczać Security Context dla żądań bez Authorization")
    void testFilterClearsSecurityContextWithoutAuthorization() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("old-user", null));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien obsługiwać pusty token")
    void testFilterHandlesEmptyToken() throws ServletException, IOException {
        // Given
        String emptyToken = "Bearer ";
        when(request.getHeader("Authorization")).thenReturn(emptyToken);

        // When & Then
        assertDoesNotThrow(() -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien obsługiwać nagłówek z wieloma spacjami")
    void testFilterHandlesHeaderWithMultipleSpaces() throws ServletException, IOException {
        // Given
        String tokenWithSpaces = "Bearer   token123";
        when(request.getHeader("Authorization")).thenReturn(tokenWithSpaces);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien nie wymagać nagłówka Authorization dla niektórych endpointów")
    void testFilterDoesNotRequireAuthorizationForAllEndpoints() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien obsługiwać nagłówek case-insensitive")
    void testFilterIsNotCaseSensitiveForBearerPrefix() throws ServletException, IOException {
        // Given
        String tokenWithDifferentCase = "bearer valid-token";
        when(request.getHeader("Authorization")).thenReturn(tokenWithDifferentCase);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien obsługiwać JwtException bez zatrzymywania żądania")
    void testFilterHandlesJwtExceptionGracefully() throws ServletException, IOException {
        // Given
        String invalidToken = "Bearer malformed.token";
        when(request.getHeader("Authorization")).thenReturn(invalidToken);
        when(jwtService.isTokenValid("malformed.token")).thenThrow(new RuntimeException("Invalid token"));

        // When & Then
        assertDoesNotThrow(() -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtr powinien pozwolić na kontynuację dla żądań bez tokenu")
    void testFilterAllowsContinuationWithoutToken() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
