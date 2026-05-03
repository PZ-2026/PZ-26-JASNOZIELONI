package pl.edu.ur.coopspace_backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "application.security.jwt.secret-key=c2VjdXJlLWp3dC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW11c3QtYmUtdmVyeS1sb25n",
        "application.security.jwt.expiration=3600000"
})
@DisplayName("JwtService - Testy jednostkowe dla serwisu JWT")
class JwtServiceTest {

    private JwtService jwtService;

    private String secretKey = "c2VjdXJlLWp3dC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW11c3QtYmUtdmVyeS1sb25n";
    private long expirationTime = 3600000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", secretKey);
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", expirationTime);
    }

    @Test
    @DisplayName("Wygasły token powinien być nieprawidłowy")
    void testExpiredTokenIsInvalid() throws InterruptedException {
        // Given: utwórz serwis z bardzo krótkim czasem życia tokenu
        JwtService shortLived = new JwtService();
        ReflectionTestUtils.setField(shortLived, "SECRET_KEY", secretKey);
        ReflectionTestUtils.setField(shortLived, "EXPIRATION_TIME", 1L);

        // When: wygenerujemy token i poczekamy aż wygaśnie
        String token = shortLived.generateToken("expired@example.com");
        Thread.sleep(10);

        // Then: parsowanie wygasłego tokenu powinno rzucić wyjątek
        assertThrows(ExpiredJwtException.class, () -> shortLived.isTokenValid(token));
    }

    @Test
    @DisplayName("Generowanie tokenu powinno zwrócić nie-pusty token")
    void testGenerateTokenReturnsNonEmptyToken() {
        // Given
        String email = "test@example.com";

        // When
        String token = jwtService.generateToken(email);

        // Then
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("Generowany token powinien zawierać trzy części oddzielone kropkami")
    void testGenerateTokenHasThreeParts() {
        // Given
        String email = "test@example.com";

        // When
        String token = jwtService.generateToken(email);

        // Then
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT token powinien mieć trzy części");
    }

    @Test
    @DisplayName("Wyciągnięty email z tokenu powinien być równy oryginalnemu emailowi")
    void testExtractUsernameFromToken() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        String extractedEmail = jwtService.extractUsername(token);

        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Nowo wygenerowany token powinien być ważny")
    void testIsTokenValidForNewToken() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Token z różnymi emailami powinien zwrócić odpowiednie email")
    void testExtractUsernameWithDifferentEmails() {
        // Given
        String[] emails = {
                "user1@example.com",
                "user2@example.com",
                "admin@company.com",
                "test+tag@domain.co.uk"
        };

        // When & Then
        for (String email : emails) {
            String token = jwtService.generateToken(email);
            String extracted = jwtService.extractUsername(token);
            assertEquals(email, extracted);
        }
    }

    @Test
    @DisplayName("Token powinien zawierać claim 'subject' równy emailowi")
    void testTokenContainsEmailAsSubject() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Then
        assertEquals(email, subject);
    }

    @Test
    @DisplayName("Token powinien mieć datę wydania")
    void testTokenHasIssuedAtDate() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

        // Then
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()));
        assertTrue(issuedAt.after(new Date(System.currentTimeMillis() - 1000))); // 1 sekunda tolerancji
    }

    @Test
    @DisplayName("Token powinien mieć datę wygaszenia w przyszłości")
    void testTokenExpirationIsInFuture() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Poprawiony token powinien być nieważny")
    void testModifiedTokenIsInvalid() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);
        String modifiedToken = token.substring(0, token.length() - 5) + "XXXXX"; // Zmiana ostatnich znaków

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(modifiedToken);
        });
    }

    @Test
    @DisplayName("Pusty token powinien rzucić wyjątek")
    void testEmptyTokenThrowsException() {
        // Given
        String emptyToken = "";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(emptyToken);
        });
    }

    @Test
    @DisplayName("Null token powinien rzucić wyjątek")
    void testNullTokenThrowsException() {
        // Given
        String nullToken = null;

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername(nullToken);
        });
    }

    @Test
    @DisplayName("Token z bardzo długim emailem powinien działać")
    void testTokenWithLongEmail() {
        // Given
        String longEmail = "verylongemailaddresswithalotofcharacters@subdomain.example.com";

        // When
        String token = jwtService.generateToken(longEmail);
        String extracted = jwtService.extractUsername(token);

        // Then
        assertEquals(longEmail, extracted);
    }

    @Test
    @DisplayName("Wyciągnięcie wielu claim z tego samego tokenu powinno być spójne")
    void testExtractingMultipleClaimsIsConsistent() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        String subject1 = jwtService.extractClaim(token, Claims::getSubject);
        Date issuedAt1 = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration1 = jwtService.extractClaim(token, Claims::getExpiration);

        String subject2 = jwtService.extractClaim(token, Claims::getSubject);
        Date issuedAt2 = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration2 = jwtService.extractClaim(token, Claims::getExpiration);

        // Then
        assertEquals(subject1, subject2);
        assertEquals(issuedAt1, issuedAt2);
        assertEquals(expiration1, expiration2);
    }

    @Test
    @DisplayName("Token powinien być ważny natychmiast po wygenerowaniu")
    void testTokenIsValidImmediatelyAfterGeneration() {
        // Given
        String email = "test@example.com";

        // When
        String token = jwtService.generateToken(email);
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Wyciągnięcie ekspiracji powinno działać dla wszystkich typów danych")
    void testExtractExpirationDate() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.getTime() > System.currentTimeMillis());
    }

    @Test
    @DisplayName("Token powinien zawierać Subject claim równy emailowi")
    void testTokenSubjectEqualsEmail() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        String subject = jwtService.extractUsername(token);

        // Then
        assertEquals(email, subject, "Subject tokenu powinien być równy emailowi");
    }

    @Test
    @DisplayName("Różni użytkownicy powinni mieć różne tokeny")
    void testDifferentEmailsHaveDifferentTokens() {
        // Given
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        // When
        String token1 = jwtService.generateToken(email1);
        String token2 = jwtService.generateToken(email2);

        // Then
        assertNotEquals(token1, token2);
        assertEquals(email1, jwtService.extractUsername(token1));
        assertEquals(email2, jwtService.extractUsername(token2));
    }
}
