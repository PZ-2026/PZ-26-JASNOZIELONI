package pl.edu.ur.coopspace_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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

/**
 * Provides announcement and document endpoints for authenticated users and administrators.
 */
@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    /**
     * Creates a controller for announcement and document operations.
     *
     * @param announcementRepository announcement persistence access
     * @param userRepository user persistence access
     * @param documentRepository document persistence access
     */
    public AnnouncementController(AnnouncementRepository announcementRepository, UserRepository userRepository, DocumentRepository documentRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * Creates a new announcement. Administrator-only endpoint.
     *
     * @param authentication current user authentication
     * @param request announcement creation data
     * @return created announcement metadata
     */
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

    /**
     * Downloads a stored document file.
     *
     * @param authentication current user authentication
     * @param id document identifier
     * @return document file as a downloadable resource
     * @throws IOException when content type detection fails
     */
    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            Authentication authentication,
            @PathVariable Integer id
    ) throws IOException {
        getCurrentUser(authentication);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dokument nie istnieje"));

        Path path = resolveDocumentPath(document.getFilePath());
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plik dokumentu nie istnieje");
        }

        String contentType = Files.probeContentType(path);
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "attachment; filename=\"" + path.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Deletes a stored document. Administrator-only endpoint.
     *
     * @param authentication current user authentication
     * @param id document identifier
     * @return no content on success
     */
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(
            Authentication authentication,
            @PathVariable Integer id
    ) {
        requireAdmin(authentication);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dokument nie istnieje"));

        Path path = resolveDocumentPath(document.getFilePath());
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udalo sie usunac pliku dokumentu");
        }

        documentRepository.delete(document);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns all announcements visible to the authenticated user.
     *
     * @param authentication current user authentication
     * @return list of announcements
     */
    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(Authentication authentication) {
        getCurrentUser(authentication);

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

    /**
     * Returns a single announcement by id.
     *
     * @param authentication current user authentication
     * @param id announcement identifier
     * @return announcement details
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> getAnnouncementById(
            Authentication authentication,
            @PathVariable Integer id) {
        getCurrentUser(authentication);

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

    /**
     * Deletes multiple announcements by id list. Administrator-only endpoint.
     *
     * @param authentication current user authentication
     * @param ids list of announcement ids to delete
     * @return no content on success
     */
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

        /**
         * Creates a controller for announcement and document operations.
         *
         * @param announcementRepository announcement persistence access
         * @param userRepository user persistence access
         * @param documentRepository document persistence access
         */
    /**
     * Lists stored document metadata for authenticated users.
     *
     * @param authentication current user authentication
     * @return list of documents
     */
    @GetMapping("/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments(Authentication authentication) {
        getCurrentUser(authentication);

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

    /**
     * Uploads a document file and stores its metadata. Administrator-only endpoint.
     *
     * @param authentication current user authentication
     * @param file multipart file to upload
     * @return stored document metadata
     */
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
        /**
         * Downloads a stored document file.
         *
         * @param authentication current user authentication
         * @param id document identifier
         * @return document file as a downloadable resource
         * @throws IOException when content type detection fails
         */

        return ResponseEntity.ok(response);
    }

    /**
     * Sprawdza czy uzytkownik jest administratorem.
     * Jest to uzywane do weryfikacji uprawnien do danych akcji.
     */
    private User requireAdmin(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tylko administrator posiada uprawnienia do tej operacji");
        }

        return currentUser;
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Użytkownik niezalogowany"));
    }

    /**
     * Zwraca katalog do przechowywania dokumentów.
     */
    private Path getDocumentDirectory() {
        return Path.of("uploads", "docs").toAbsolutePath().normalize();
    }

    private Path resolveDocumentPath(String storedFilePath) {
        Path documentsDirectory = getDocumentDirectory();
        Path path = documentsDirectory.resolve(storedFilePath).normalize();

        if (!path.startsWith(documentsDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nieprawidlowy plik dokumentu");
        }

        return path;
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
