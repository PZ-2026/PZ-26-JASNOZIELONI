package pl.edu.ur.coopspace_backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.dto.LocalSummaryResponse;
import pl.edu.ur.coopspace_backend.entity.Local;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.LocalRepository;
import pl.edu.ur.coopspace_backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LocalController - testy jednostkowe")
class LocalControllerTest {

    @Mock
    private LocalRepository localRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LocalController localController;

    @BeforeEach
    void setUp() {
        when(authentication.getName()).thenReturn("admin@example.com");
    }

    @Test
    @DisplayName("getLocals zwraca aktywne lokale posortowane i tylko dla admina")
    void testGetLocalsSuccessSortedAndFiltered() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        Local deleted = Local.builder()
                .id(1)
                .buildingId(2)
                .number("10")
                .staircase("B")
                .deletedAt(LocalDateTime.now())
                .build();

        Local first = Local.builder()
                .id(2)
                .buildingId(1)
                .number("2")
                .staircase("B")
                .build();

        Local second = Local.builder()
                .id(3)
                .buildingId(1)
                .number("2")
                .staircase(null)
                .build();

        Local third = Local.builder()
                .id(4)
                .buildingId(1)
                .number("10")
                .staircase("A")
                .build();

        when(localRepository.findAll()).thenReturn(List.of(deleted, first, second, third));

        // When
        ResponseEntity<List<LocalSummaryResponse>> response = localController.getLocals(authentication);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(3, response.getBody().size());
        assertEquals(4, response.getBody().get(0).getId());
        assertEquals(3, response.getBody().get(1).getId());
        assertEquals(2, response.getBody().get(2).getId());
        verify(localRepository).findAll();
    }

    @Test
    @DisplayName("getLocals zwraca pustą listę gdy nie ma aktywnych lokali")
    void testGetLocalsReturnsEmptyList() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(localRepository.findAll()).thenReturn(List.of());

        // When
        ResponseEntity<List<LocalSummaryResponse>> response = localController.getLocals(authentication);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("getLocals zwraca 401 gdy użytkownik nie istnieje")
    void testGetLocalsUnauthorizedWhenUserMissing() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> localController.getLocals(authentication));
        assertEquals(401, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Uzytkownik niezalogowany"));
        verify(localRepository, never()).findAll();
    }

    @Test
    @DisplayName("getLocals zwraca 403 gdy użytkownik nie jest adminem")
    void testGetLocalsForbiddenForNonAdmin() {
        // Given
        User resident = User.builder().id(2).email("admin@example.com").role(UserRole.RESIDENT).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(resident));

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> localController.getLocals(authentication));
        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Tylko administrator ma dostep do tego endpointu"));
        verify(localRepository, never()).findAll();
    }
}