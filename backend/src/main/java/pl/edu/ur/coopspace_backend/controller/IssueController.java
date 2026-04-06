package pl.edu.ur.coopspace_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.coopspace_backend.dto.IssueAssignRequest;
import pl.edu.ur.coopspace_backend.dto.IssueCategoryResponse;
import pl.edu.ur.coopspace_backend.dto.IssueCreateRequest;
import pl.edu.ur.coopspace_backend.dto.IssueResponse;
import pl.edu.ur.coopspace_backend.dto.IssueStatusUpdateRequest;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;
import pl.edu.ur.coopspace_backend.service.IssueService;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public ResponseEntity<List<IssueResponse>> getAllIssues(
            Authentication authentication,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Integer localId
    ) {
        return ResponseEntity.ok(issueService.getAllIssues(authentication.getName(), status, localId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<IssueResponse>> getMyIssues(Authentication authentication) {
        return ResponseEntity.ok(issueService.getMyIssues(authentication.getName()));
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<IssueResponse>> getAssignedIssues(Authentication authentication) {
        return ResponseEntity.ok(issueService.getAssignedIssues(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(
            Authentication authentication,
            @RequestBody IssueCreateRequest request
    ) {
        return ResponseEntity.ok(issueService.createIssue(authentication.getName(), request));
    }

    @PatchMapping("/{issueId}/status")
    public ResponseEntity<IssueResponse> updateIssueStatus(
            Authentication authentication,
            @PathVariable Integer issueId,
            @RequestBody IssueStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(issueService.updateIssueStatus(authentication.getName(), issueId, request));
    }

    @PatchMapping("/{issueId}/assignee")
    public ResponseEntity<IssueResponse> assignIssue(
            Authentication authentication,
            @PathVariable Integer issueId,
            @RequestBody IssueAssignRequest request
    ) {
        return ResponseEntity.ok(issueService.assignIssue(authentication.getName(), issueId, request));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<IssueCategoryResponse>> getIssueCategories() {
        return ResponseEntity.ok(issueService.getCategories());
    }
}
