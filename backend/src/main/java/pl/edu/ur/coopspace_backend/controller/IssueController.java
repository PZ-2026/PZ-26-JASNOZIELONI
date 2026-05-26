package pl.edu.ur.coopspace_backend.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
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
import pl.edu.ur.coopspace_backend.service.RepairProtocolService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * REST API for issue lifecycle operations.
 *
 * <p>Provides issue listing, creation, assignment, status transitions,
 * and image/document-related operations.</p>
 */
@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    private final IssueService issueService;
    private final RepairProtocolService repairProtocolService;

    /**
     * Creates an issue controller.
     *
     * @param issueService issue business service
     * @param repairProtocolService repair protocol generation service
     */
    public IssueController(IssueService issueService, RepairProtocolService repairProtocolService) {
        this.issueService = issueService;
        this.repairProtocolService = repairProtocolService;
    }

    /**
     * Returns all issues visible to the current administrator with optional filters.
     *
     * @param authentication current user authentication
     * @param status optional status filter
     * @param localId optional local identifier filter
     * @return filtered list of issues
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
     * Returns issues created by the current user.
     *
     * @param authentication current user authentication
     * @return list of issues created by the user
     */
    @GetMapping("/my")
    public ResponseEntity<List<IssueResponse>> getMyIssues(Authentication authentication) {
        return ResponseEntity.ok(issueService.getMyIssues(authentication.getName()));
    }

    /**
     * Returns issues assigned to the current maintainer.
     *
     * @param authentication current user authentication
     * @param status optional status filter
     * @param localId optional local identifier filter
     * @return filtered list of assigned issues
     */
    @GetMapping("/assigned")
    public ResponseEntity<List<IssueResponse>> getAssignedIssues(
            Authentication authentication,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Integer localId
    ) {
        return ResponseEntity.ok(issueService.getAssignedIssues(authentication.getName(), status, localId));
    }

    /**
        * Creates a new issue.
        *
        * @param authentication current user authentication
        * @param request issue creation payload
        * @return created issue
     */
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(
            Authentication authentication,
            @RequestBody IssueCreateRequest request
    ) {
        return ResponseEntity.ok(issueService.createIssue(authentication.getName(), request));
    }

    /**
     * Returns metadata of all images assigned to the specified issue.
     *
     * @param authentication current user authentication
     * @param issueId issue identifier
     * @return list of issue image metadata
     */
    @GetMapping("/{issueId}/images")
    public ResponseEntity<List<IssueImageResponse>> getIssueImages(
            Authentication authentication,
            @PathVariable Integer issueId
    ) {
        return ResponseEntity.ok(issueService.getIssueImages(authentication.getName(), issueId));
    }

    /**
     * Uploads a single image and attaches it to an existing issue.
     *
     * @param authentication current user authentication
     * @param issueId issue identifier
     * @param file uploaded image file
     * @return metadata of the stored image
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
     * Returns the binary content of a specific issue image.
     *
     * @param authentication current user authentication
     * @param issueId issue identifier
     * @param imageId image identifier
     * @return image resource response
     * @throws Exception when content type detection fails
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
     * Removes an image attachment from an issue.
     *
     * @param authentication current user authentication
     * @param issueId issue identifier
     * @param imageId image identifier
     * @return empty response on successful deletion
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
     * Updates an issue status.
     *
     * @param authentication current user authentication
     * @param issueId issue identifier
     * @param request status update payload
     * @return updated issue
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
     * Assigns an issue to a maintainer.
     *
     * @param authentication current user authentication
     * @param issueId issue identifier
     * @param request assignment payload
     * @return updated issue
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
    * Generates a repair protocol in PDF format.
    *
    * @param authentication current user authentication
    * @param issueId issue identifier
    * @return generated PDF file as a downloadable resource
     */
    @GetMapping(value = "/{issueId}/repair-protocol", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> downloadRepairProtocol(
        Authentication authentication,
        @PathVariable Integer issueId
    ) {
    RepairProtocolService.RepairProtocolResult result = repairProtocolService
        .generateRepairProtocol(authentication.getName(), issueId);

    Resource resource = new FileSystemResource(result.filePath());
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
        .body(resource);
    }

    /**
     * Returns available issue categories.
     *
     * @return list of issue categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<IssueCategoryResponse>> getIssueCategories() {
        return ResponseEntity.ok(issueService.getCategories());
    }
}
