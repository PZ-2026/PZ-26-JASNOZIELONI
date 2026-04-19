package pl.edu.ur.coopspace_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.dto.AnnouncementCreateRequest;
import pl.edu.ur.coopspace_backend.dto.AnnouncementResponse;
import pl.edu.ur.coopspace_backend.dto.DocumentResponse;
import pl.edu.ur.coopspace_backend.entity.Announcement;
import pl.edu.ur.coopspace_backend.entity.Document;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.AnnouncementRepository;
import pl.edu.ur.coopspace_backend.repository.DocumentRepository;
import pl.edu.ur.coopspace_backend.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    public AnnouncementController(AnnouncementRepository announcementRepository, UserRepository userRepository, DocumentRepository documentRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
    }

    @PostMapping
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            Authentication authentication,
            @RequestBody AnnouncementCreateRequest request) {
        
        User currentUser = requireAdmin(authentication);

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tytuł jest wymagany");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Treść jest wymagana");
        }

        Announcement announcement = Announcement.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .createdBy(currentUser.getId())
                .createdAt(LocalDateTime.now())
                .build();

        Announcement savedAnnouncement = announcementRepository.save(announcement);

        AnnouncementResponse response = AnnouncementResponse.builder()
                .id(savedAnnouncement.getId())
                .title(savedAnnouncement.getTitle())
                .content(savedAnnouncement.getContent())
                .createdBy(savedAnnouncement.getCreatedBy())
                .createdAt(savedAnnouncement.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(Authentication authentication) {
        requireAdmin(authentication);

        List<AnnouncementResponse> responses = announcementRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Announcement::getCreatedAt).reversed())
                .map(a -> AnnouncementResponse.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .content(a.getContent())
                        .createdBy(a.getCreatedBy())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> getAnnouncementById(
            Authentication authentication,
            @PathVariable Integer id) {
        requireAdmin(authentication);

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ogłoszenie nie istnieje"));

        AnnouncementResponse response = AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .createdBy(announcement.getCreatedBy())
                .createdAt(announcement.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAnnouncements(
            Authentication authentication,
            @RequestParam List<Integer> ids) {
        requireAdmin(authentication);

        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lista identyfikatorów nie może być pusta");
        }

        announcementRepository.deleteAllById(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments(Authentication authentication) {
        requireAdmin(authentication);

        List<DocumentResponse> responses = documentRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Document::getCreatedAt).reversed())
                .map(d -> DocumentResponse.builder()
                        .id(d.getId())
                        .title(d.getTitle())
                        .filePath(d.getFilePath())
                        .uploadedBy(d.getUploadedBy())
                        .createdAt(d.getCreatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping(value = "/documents", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            Authentication authentication,
            @RequestPart("file") MultipartFile file) {
        User currentUser = requireAdmin(authentication);

        if (file.isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Plik jest pusty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String safeFileName = UUID.randomUUID() + extension;

        Path targetDirectory = getDocumentDirectory();
        Path targetPath = targetDirectory.resolve(safeFileName).normalize();

        try {
            Files.createDirectories(targetPath.getParent());
            try (java.io.InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Błąd podczas zapisywania pliku", e);
        }

        Document document = Document.builder()
                .title(originalFilename)
                .filePath(safeFileName)
                .uploadedBy(currentUser.getId())
                .createdAt(LocalDateTime.now())
                .build();

        Document savedDocument = documentRepository.save(document);

        DocumentResponse response = DocumentResponse.builder()
                .id(savedDocument.getId())
                .title(savedDocument.getTitle())
                .filePath(savedDocument.getFilePath())
                .uploadedBy(savedDocument.getUploadedBy())
                .createdAt(savedDocument.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    private User requireAdmin(Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Użytkownik niezalogowany"));

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tylko administrator może dodawać ogłoszenia");
        }

        return currentUser;
    }

    /**
     * Zwraca katalog do przechowywania dokumentów.
     */
    private Path getDocumentDirectory() {
        return Path.of("uploads", "docs").toAbsolutePath().normalize();
    }

    /**
     * Wyodrębnia rozszerzenie pliku z nazwy original.
     */
    private String extractExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return ".pdf";
        }

        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return ".pdf";
        }

        return originalFilename.substring(index).toLowerCase();
    }
}
