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
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("IssueController - testy jednostkowe")
class IssueControllerTest {

    @Mock
    private IssueService issueService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private IssueController issueController;

    @BeforeEach
    void setUp() {
        when(authentication.getName()).thenReturn("user@example.com");
    }

    @Test
    @DisplayName("getAllIssues deleguje do serwisu")
    void testGetAllIssuesDelegatesToService() {
        // Given
        IssueResponse issue = new IssueResponse(1, "T", "D", 2, 3, 4, null, IssueStatus.OPEN, null, null, null, null);
        when(issueService.getAllIssues("user@example.com", IssueStatus.OPEN, 10)).thenReturn(List.of(issue));

        // When
        ResponseEntity<List<IssueResponse>> response = issueController.getAllIssues(authentication, IssueStatus.OPEN, 10);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(issueService).getAllIssues("user@example.com", IssueStatus.OPEN, 10);
    }

    @Test
    @DisplayName("getMyIssues deleguje do serwisu")
    void testGetMyIssuesDelegatesToService() {
        // Given
        when(issueService.getMyIssues("user@example.com")).thenReturn(List.of());

        // When
        ResponseEntity<List<IssueResponse>> response = issueController.getMyIssues(authentication);

        // Then
        assertEquals(200, response.getStatusCode().value());
        verify(issueService).getMyIssues("user@example.com");
    }

    @Test
    @DisplayName("createIssue deleguje do serwisu")
    void testCreateIssueDelegatesToService() {
        // Given
        IssueCreateRequest request = new IssueCreateRequest("Tytuł", "Opis", 5, 7);
        IssueResponse issue = new IssueResponse(2, "Tytuł", "Opis", 5, 7, 1, null, IssueStatus.OPEN, null, null, null, null);
        when(issueService.createIssue("user@example.com", request)).thenReturn(issue);

        // When
        ResponseEntity<IssueResponse> response = issueController.createIssue(authentication, request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().getId());
        verify(issueService).createIssue("user@example.com", request);
    }

    @Test
    @DisplayName("updateIssueStatus deleguje do serwisu")
    void testUpdateIssueStatusDelegatesToService() {
        // Given
        IssueStatusUpdateRequest request = new IssueStatusUpdateRequest(IssueStatus.IN_PROGRESS, "komentarz");
        IssueResponse issue = new IssueResponse(3, "T", "D", 2, 3, 4, null, IssueStatus.IN_PROGRESS, null, null, null, "komentarz");
        when(issueService.updateIssueStatus("user@example.com", 77, request)).thenReturn(issue);

        // When
        ResponseEntity<IssueResponse> response = issueController.updateIssueStatus(authentication, 77, request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(IssueStatus.IN_PROGRESS, response.getBody().getStatus());
        verify(issueService).updateIssueStatus("user@example.com", 77, request);
    }

    @Test
    @DisplayName("assignIssue deleguje do serwisu")
    void testAssignIssueDelegatesToService() {
        // Given
        IssueAssignRequest request = new IssueAssignRequest(99);
        IssueResponse issue = new IssueResponse(4, "T", "D", 2, 3, 4, 99, IssueStatus.IN_PROGRESS, null, null, null, null);
        when(issueService.assignIssue("user@example.com", 88, request)).thenReturn(issue);

        // When
        ResponseEntity<IssueResponse> response = issueController.assignIssue(authentication, 88, request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(99, response.getBody().getMainAssigneeId());
        verify(issueService).assignIssue("user@example.com", 88, request);
    }

    @Test
    @DisplayName("deleteIssueImage zwraca 204 i deleguje do serwisu")
    void testDeleteIssueImageDelegatesToService() {
        // When
        ResponseEntity<Void> response = issueController.deleteIssueImage(authentication, 12, 34);

        // Then
        assertEquals(204, response.getStatusCode().value());
        verify(issueService).deleteIssueImage("user@example.com", 12, 34);
    }

    @Test
    @DisplayName("getIssueCategories deleguje do serwisu")
    void testGetIssueCategoriesDelegatesToService() {
        // Given
        when(issueService.getCategories()).thenReturn(List.of(new IssueCategoryResponse(1, "Elektryka")));

        // When
        ResponseEntity<List<IssueCategoryResponse>> response = issueController.getIssueCategories();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(issueService).getCategories();
    }

    @Test
    @DisplayName("getIssueImage zwraca 404 gdy plik nie istnieje")
    void testGetIssueImageReturns404WhenFileMissing() throws Exception {
        // Given
        Path tempDir = Files.createTempDirectory("issue-controller-test");
        Path missingFile = tempDir.resolve("missing.jpg");
        when(issueService.getIssueImagePath("user@example.com", 5, 6)).thenReturn(missingFile);

        // When
        ResponseEntity<Resource> response = issueController.getIssueImage(authentication, 5, 6);

        // Then
        assertEquals(404, response.getStatusCode().value());
        verify(issueService).getIssueImagePath("user@example.com", 5, 6);
    }

    @Test
    @DisplayName("addIssueImage deleguje do serwisu")
    void testAddIssueImageDelegatesToService() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        IssueImageResponse image = new IssueImageResponse(1, 2, "file.jpg", "/api/issues/2/images/1", null);
        when(issueService.addIssueImage("user@example.com", 2, file)).thenReturn(image);

        // When
        ResponseEntity<IssueImageResponse> response = issueController.addIssueImage(authentication, 2, file);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getId());
        verify(issueService).addIssueImage("user@example.com", 2, file);
    }
}