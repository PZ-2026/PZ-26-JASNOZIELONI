package pl.edu.ur.coopspace_backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.dto.IssueAssignRequest;
import pl.edu.ur.coopspace_backend.dto.IssueCategoryResponse;
import pl.edu.ur.coopspace_backend.dto.IssueCreateRequest;
import pl.edu.ur.coopspace_backend.dto.IssueResponse;
import pl.edu.ur.coopspace_backend.dto.IssueStatusUpdateRequest;
import pl.edu.ur.coopspace_backend.entity.Issue;
import pl.edu.ur.coopspace_backend.entity.IssueAssignment;
import pl.edu.ur.coopspace_backend.entity.IssueCategory;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;
import pl.edu.ur.coopspace_backend.entity.IssueStatusHistory;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.IssueAssignmentRepository;
import pl.edu.ur.coopspace_backend.repository.IssueCategoryRepository;
import pl.edu.ur.coopspace_backend.repository.IssueRepository;
import pl.edu.ur.coopspace_backend.repository.IssueStatusHistoryRepository;
import pl.edu.ur.coopspace_backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class IssueService {

    private final IssueRepository issueRepository;
    private final IssueCategoryRepository issueCategoryRepository;
    private final IssueStatusHistoryRepository issueStatusHistoryRepository;
    private final IssueAssignmentRepository issueAssignmentRepository;
    private final UserRepository userRepository;

    public IssueService(
            IssueRepository issueRepository,
            IssueCategoryRepository issueCategoryRepository,
            IssueStatusHistoryRepository issueStatusHistoryRepository,
            IssueAssignmentRepository issueAssignmentRepository,
            UserRepository userRepository
    ) {
        this.issueRepository = issueRepository;
        this.issueCategoryRepository = issueCategoryRepository;
        this.issueStatusHistoryRepository = issueStatusHistoryRepository;
        this.issueAssignmentRepository = issueAssignmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> getMyIssues(String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);

        return issueRepository.findByCreatedByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> getAssignedIssues(String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);

        return issueRepository.findByMainAssigneeIdAndDeletedAtIsNullOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> getAllIssues(String currentUserEmail, IssueStatus status, Integer localId) {
        User currentUser = getCurrentUser(currentUserEmail);
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tylko administrator moze przegladac wszystkie zgloszenia");
        }

        return issueRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .filter(issue -> status == null || issue.getStatus() == status)
                .filter(issue -> localId == null || issue.getLocalId().equals(localId))
                .sorted(Comparator.comparing(Issue::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    public IssueResponse createIssue(String currentUserEmail, IssueCreateRequest request) {
        User currentUser = getCurrentUser(currentUserEmail);

        validateCreateRequest(request);
        resolveLocalIdForCreate(currentUser, request.getLocalId());

        if (!issueCategoryRepository.existsById(request.getCategoryId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wybrana kategoria nie istnieje");
        }

        LocalDateTime now = LocalDateTime.now();
        Issue issue = Issue.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .categoryId(request.getCategoryId())
                .localId(resolveLocalIdForCreate(currentUser, request.getLocalId()))
                .createdByUserId(currentUser.getId())
                .status(IssueStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Issue savedIssue = issueRepository.save(issue);
        saveStatusHistory(savedIssue.getId(), IssueStatus.OPEN, currentUser.getId(), now);

        return toResponse(savedIssue);
    }

    public IssueResponse updateIssueStatus(String currentUserEmail, Integer issueId, IssueStatusUpdateRequest request) {
        User currentUser = getCurrentUser(currentUserEmail);
        Issue issue = getIssueOrThrow(issueId);

        if (request == null || request.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status jest wymagany");
        }

        boolean canUpdateStatus = currentUser.getRole() == UserRole.ADMIN
                || (issue.getMainAssigneeId() != null && issue.getMainAssigneeId().equals(currentUser.getId()));
        if (!canUpdateStatus) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Brak uprawnien do zmiany statusu tego zgloszenia");
        }

        LocalDateTime now = LocalDateTime.now();
        issue.setStatus(request.getStatus());
        issue.setUpdatedAt(now);
        issue.setClosedAt(request.getStatus() == IssueStatus.CLOSED ? now : null);

        Issue savedIssue = issueRepository.save(issue);
        saveStatusHistory(savedIssue.getId(), savedIssue.getStatus(), currentUser.getId(), now);

        return toResponse(savedIssue);
    }

    public IssueResponse assignIssue(String currentUserEmail, Integer issueId, IssueAssignRequest request) {
        User currentUser = getCurrentUser(currentUserEmail);
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tylko administrator moze przypisywac zgloszenia");
        }

        if (request == null || request.getAssigneeUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assigneeUserId jest wymagane");
        }

        User assignee = userRepository.findById(request.getAssigneeUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wskazany uzytkownik nie istnieje"));
        if (assignee.getRole() != UserRole.MAINTAINER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zgloszenie mozna przypisac tylko do konserwatora");
        }

        Issue issue = getIssueOrThrow(issueId);
        LocalDateTime now = LocalDateTime.now();

        issue.setMainAssigneeId(assignee.getId());
        issue.setUpdatedAt(now);
        if (issue.getStatus() == IssueStatus.OPEN) {
            issue.setStatus(IssueStatus.IN_PROGRESS);
            saveStatusHistory(issue.getId(), IssueStatus.IN_PROGRESS, currentUser.getId(), now);
        }

        issueAssignmentRepository.save(IssueAssignment.builder()
                .issueId(issue.getId())
                .userId(assignee.getId())
                .assignedBy(currentUser.getId())
                .assignedAt(now)
                .build());

        return toResponse(issueRepository.save(issue));
    }

    @Transactional(readOnly = true)
    public List<IssueCategoryResponse> getCategories() {
        return issueCategoryRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(IssueCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .map(category -> new IssueCategoryResponse(category.getId(), category.getName()))
                .toList();
    }

    private void validateCreateRequest(IssueCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dane zgloszenia sa wymagane");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tytul jest wymagany");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opis jest wymagany");
        }
        if (request.getCategoryId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kategoria jest wymagana");
        }
    }

    private Integer resolveLocalIdForCreate(User currentUser, Integer requestedLocalId) {
        if (currentUser.getRole() == UserRole.RESIDENT) {
            if (currentUser.getLocalId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mieszkaniec nie ma przypisanego lokalu");
            }
            if (requestedLocalId != null && !requestedLocalId.equals(currentUser.getLocalId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nie mozna tworzyc zgloszen dla innego lokalu");
            }
            return currentUser.getLocalId();
        }

        if (requestedLocalId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "localId jest wymagane");
        }

        return requestedLocalId;
    }

    private Issue getIssueOrThrow(Integer issueId) {
        return issueRepository.findByIdAndDeletedAtIsNull(issueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zgloszenie nie istnieje"));
    }

    private User getCurrentUser(String currentUserEmail) {
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Uzytkownik niezalogowany"));
    }

    private void saveStatusHistory(Integer issueId, IssueStatus status, Integer changedBy, LocalDateTime changedAt) {
        issueStatusHistoryRepository.save(IssueStatusHistory.builder()
                .issueId(issueId)
                .status(status)
                .changedBy(changedBy)
                .changedAt(changedAt)
                .build());
    }

    private IssueResponse toResponse(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getCategoryId(),
                issue.getLocalId(),
                issue.getCreatedByUserId(),
                issue.getMainAssigneeId(),
                issue.getStatus(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getClosedAt()
        );
    }
}
