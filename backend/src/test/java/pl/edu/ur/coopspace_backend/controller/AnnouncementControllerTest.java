package pl.edu.ur.coopspace_backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.dto.AnnouncementCreateRequest;
import pl.edu.ur.coopspace_backend.dto.AnnouncementResponse;
import pl.edu.ur.coopspace_backend.entity.Announcement;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.AnnouncementRepository;
import pl.edu.ur.coopspace_backend.repository.DocumentRepository;
import pl.edu.ur.coopspace_backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnnouncementController - testy jednostkowe")
class AnnouncementControllerTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AnnouncementController announcementController;

    @BeforeEach
    void setUp() {
        when(authentication.getName()).thenReturn("admin@example.com");
    }

    @Test
    @DisplayName("createAnnouncement zapisuje ogłoszenie dla admina")
    void testCreateAnnouncementSuccess() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        AnnouncementCreateRequest request = AnnouncementCreateRequest.builder()
                .title("  Ważny komunikat  ")
                .content("  Treść ogłoszenia  ")
                .build();

        Announcement saved = Announcement.builder()
                .id(11)
                .title("Ważny komunikat")
                .content("Treść ogłoszenia")
                .createdBy(1)
                .createdAt(LocalDateTime.of(2026, 5, 3, 10, 0))
                .build();

        when(announcementRepository.save(any(Announcement.class))).thenReturn(saved);

        // When
        ResponseEntity<AnnouncementResponse> response = announcementController.createAnnouncement(authentication, request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(11, response.getBody().getId());
        assertEquals("Ważny komunikat", response.getBody().getTitle());
        assertEquals("Treść ogłoszenia", response.getBody().getContent());
        assertEquals(1, response.getBody().getCreatedBy());
        verify(announcementRepository).save(argThat(announcement ->
                "Ważny komunikat".equals(announcement.getTitle())
                        && "Treść ogłoszenia".equals(announcement.getContent())
                        && Integer.valueOf(1).equals(announcement.getCreatedBy())
        ));
    }

    @Test
    @DisplayName("createAnnouncement zwraca 400 gdy tytuł jest pusty")
    void testCreateAnnouncementBlankTitleThrows() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        AnnouncementCreateRequest request = AnnouncementCreateRequest.builder()
                .title("   ")
                .content("Treść")
                .build();

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> announcementController.createAnnouncement(authentication, request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Tytuł jest wymagany"));
        verify(announcementRepository, never()).save(any(Announcement.class));
    }

    @Test
    @DisplayName("createAnnouncement zwraca 400 gdy treść jest pusta")
    void testCreateAnnouncementBlankContentThrows() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        AnnouncementCreateRequest request = AnnouncementCreateRequest.builder()
                .title("Tytuł")
                .content("   ")
                .build();

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> announcementController.createAnnouncement(authentication, request));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Treść jest wymagana"));
        verify(announcementRepository, never()).save(any(Announcement.class));
    }

    @Test
    @DisplayName("createAnnouncement odrzuca nie-admina")
    void testCreateAnnouncementNonAdminForbidden() {
        // Given
        User resident = User.builder().id(2).email("user@example.com").role(UserRole.RESIDENT).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(resident));

        AnnouncementCreateRequest request = AnnouncementCreateRequest.builder()
                .title("Tytuł")
                .content("Treść")
                .build();

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> announcementController.createAnnouncement(authentication, request));
        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Tylko administrator posiada uprawnienia do tej operacji"));
    }

    @Test
    @DisplayName("getAnnouncements zwraca ogłoszenia od najnowszego")
    void testGetAnnouncementsSortedNewestFirst() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        Announcement older = Announcement.builder()
                .id(1)
                .title("Stare")
                .content("1")
                .createdBy(1)
                .createdAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();
        Announcement newer = Announcement.builder()
                .id(2)
                .title("Nowe")
                .content("2")
                .createdBy(1)
                .createdAt(LocalDateTime.of(2026, 5, 3, 10, 0))
                .build();

        when(announcementRepository.findAll()).thenReturn(List.of(older, newer));

        // When
        ResponseEntity<List<AnnouncementResponse>> response = announcementController.getAnnouncements(authentication);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        assertEquals(2, response.getBody().get(0).getId());
        assertEquals(1, response.getBody().get(1).getId());
    }

    @Test
    @DisplayName("getAnnouncementById zwraca 404 gdy ogłoszenie nie istnieje")
    void testGetAnnouncementByIdNotFoundThrows() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(announcementRepository.findById(99)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> announcementController.getAnnouncementById(authentication, 99));
        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Ogłoszenie nie istnieje"));
    }

    @Test
    @DisplayName("deleteAnnouncements usuwa listę ogłoszeń i zwraca 204")
    void testDeleteAnnouncementsSuccess() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        // When
        ResponseEntity<Void> response = announcementController.deleteAnnouncements(authentication, List.of(1, 2, 3));

        // Then
        assertEquals(204, response.getStatusCode().value());
        verify(announcementRepository).deleteAllById(List.of(1, 2, 3));
    }

    @Test
    @DisplayName("deleteAnnouncements zwraca 400 dla pustej listy")
    void testDeleteAnnouncementsEmptyListThrows() {
        // Given
        User admin = User.builder().id(1).email("admin@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> announcementController.deleteAnnouncements(authentication, List.of()));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Lista identyfikatorów nie może być pusta"));
        verify(announcementRepository, never()).deleteAllById(anyList());
    }
}