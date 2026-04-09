package pl.edu.ur.coopspace_backend.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.ur.coopspace_backend.dto.IssueAssignRequest;
import pl.edu.ur.coopspace_backend.dto.IssueCategoryResponse;
import pl.edu.ur.coopspace_backend.dto.IssueCreateRequest;
import pl.edu.ur.coopspace_backend.dto.IssueImageResponse;
import pl.edu.ur.coopspace_backend.dto.IssueResponse;
import pl.edu.ur.coopspace_backend.dto.IssueStatusUpdateRequest;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;
import pl.edu.ur.coopspace_backend.service.IssueService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
/**
 * REST API do obslugi cyklu zycia zgloszen.
 *
 * <p>Udostepnia listowanie, tworzenie, zmiane statusu, przypisanie
 * oraz zarzadzanie zdjeciami zgloszen.</p>
 */
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    /**
        * Zwraca wszystkie zgloszenia widoczne dla administratora z opcjonalnymi filtrami.
     */
    @GetMapping
    public ResponseEntity<List<IssueResponse>> getAllIssues(
            Authentication authentication,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Integer localId
    ) {
        return ResponseEntity.ok(issueService.getAllIssues(authentication.getName(), status, localId));
    }

    /**
        * Zwraca zgloszenia utworzone przez aktualnego uzytkownika.
     */
    @GetMapping("/my")
    public ResponseEntity<List<IssueResponse>> getMyIssues(Authentication authentication) {
        return ResponseEntity.ok(issueService.getMyIssues(authentication.getName()));
    }

    /**
        * Zwraca zgloszenia przypisane do aktualnego konserwatora.
     */
    @GetMapping("/assigned")
    public ResponseEntity<List<IssueResponse>> getAssignedIssues(Authentication authentication) {
        return ResponseEntity.ok(issueService.getAssignedIssues(authentication.getName()));
    }

    /**
        * Tworzy nowe zgloszenie.
     */
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(
            Authentication authentication,
            @RequestBody IssueCreateRequest request
    ) {
        return ResponseEntity.ok(issueService.createIssue(authentication.getName(), request));
    }

    /**
        * Zwraca metadane wszystkich zdjec przypisanych do wskazanego zgloszenia.
     */
    @GetMapping("/{issueId}/images")
    public ResponseEntity<List<IssueImageResponse>> getIssueImages(
            Authentication authentication,
            @PathVariable Integer issueId
    ) {
        return ResponseEntity.ok(issueService.getIssueImages(authentication.getName(), issueId));
    }

    /**
        * Wgrywa pojedynczy plik obrazu i przypina go do istniejacego zgloszenia.
     */
    @PostMapping(value = "/{issueId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IssueImageResponse> addIssueImage(
            Authentication authentication,
            @PathVariable Integer issueId,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(issueService.addIssueImage(authentication.getName(), issueId, file));
    }

    /**
        * Zwraca binarna zawartosc obrazu dla konkretnego zdjecia zgloszenia.
     */
    @GetMapping("/{issueId}/images/{imageId}")
    public ResponseEntity<Resource> getIssueImage(
            Authentication authentication,
            @PathVariable Integer issueId,
            @PathVariable Integer imageId
    ) throws Exception {
        Path path = issueService.getIssueImagePath(authentication.getName(), issueId, imageId);
        Resource resource = new FileSystemResource(path);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(path);
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    /**
        * Usuwa zalacznik obrazu ze zgloszenia.
     */
    @DeleteMapping("/{issueId}/images/{imageId}")
    public ResponseEntity<Void> deleteIssueImage(
            Authentication authentication,
            @PathVariable Integer issueId,
            @PathVariable Integer imageId
    ) {
        issueService.deleteIssueImage(authentication.getName(), issueId, imageId);
        return ResponseEntity.noContent().build();
    }

    /**
        * Zmienia status zgloszenia.
     */
    @PatchMapping("/{issueId}/status")
    public ResponseEntity<IssueResponse> updateIssueStatus(
            Authentication authentication,
            @PathVariable Integer issueId,
            @RequestBody IssueStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(issueService.updateIssueStatus(authentication.getName(), issueId, request));
    }

    /**
        * Przypisuje zgloszenie do konserwatora.
     */
    @PatchMapping("/{issueId}/assignee")
    public ResponseEntity<IssueResponse> assignIssue(
            Authentication authentication,
            @PathVariable Integer issueId,
            @RequestBody IssueAssignRequest request
    ) {
        return ResponseEntity.ok(issueService.assignIssue(authentication.getName(), issueId, request));
    }

    /**
        * Zwraca dostepne kategorie zgloszen.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<IssueCategoryResponse>> getIssueCategories() {
        return ResponseEntity.ok(issueService.getCategories());
    }
}
