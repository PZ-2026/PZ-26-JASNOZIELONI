package pl.edu.ur.coopspace_backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.dto.IssueAssignRequest;
import pl.edu.ur.coopspace_backend.dto.IssueCategoryResponse;
import pl.edu.ur.coopspace_backend.dto.IssueCreateRequest;
import pl.edu.ur.coopspace_backend.dto.IssueImageResponse;
import pl.edu.ur.coopspace_backend.dto.IssueResponse;
import pl.edu.ur.coopspace_backend.dto.IssueStatusUpdateRequest;
import pl.edu.ur.coopspace_backend.entity.Issue;
import pl.edu.ur.coopspace_backend.entity.IssueAssignment;
import pl.edu.ur.coopspace_backend.entity.IssueCategory;
import pl.edu.ur.coopspace_backend.entity.IssueImage;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;
import pl.edu.ur.coopspace_backend.entity.IssueStatusHistory;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.IssueAssignmentRepository;
import pl.edu.ur.coopspace_backend.repository.IssueCategoryRepository;
import pl.edu.ur.coopspace_backend.repository.IssueImageRepository;
import pl.edu.ur.coopspace_backend.repository.IssueRepository;
import pl.edu.ur.coopspace_backend.repository.IssueStatusHistoryRepository;
import pl.edu.ur.coopspace_backend.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
/**
 * Glowny serwis logiki biznesowej dla obslugi zgloszen.
 *
 * <p>Obsluguje operacje cyklu zycia zgloszen, workflow przypisan,
 * slownik kategorii oraz operacje na zdjeciach z kontrola uprawnien dla danej roli.</p>
 */
public class IssueService {

    private final IssueRepository issueRepository;
    private final IssueCategoryRepository issueCategoryRepository;
    private final IssueStatusHistoryRepository issueStatusHistoryRepository;
    private final IssueAssignmentRepository issueAssignmentRepository;
    private final IssueImageRepository issueImageRepository;
    private final UserRepository userRepository;

    public IssueService(
            IssueRepository issueRepository,
            IssueCategoryRepository issueCategoryRepository,
            IssueStatusHistoryRepository issueStatusHistoryRepository,
            IssueAssignmentRepository issueAssignmentRepository,
            IssueImageRepository issueImageRepository,
            UserRepository userRepository
    ) {
        this.issueRepository = issueRepository;
        this.issueCategoryRepository = issueCategoryRepository;
        this.issueStatusHistoryRepository = issueStatusHistoryRepository;
        this.issueAssignmentRepository = issueAssignmentRepository;
        this.issueImageRepository = issueImageRepository;
        this.userRepository = userRepository;
    }

    /**
        * Zwraca zgloszenia utworzone przez aktualnego mieszkanca.
     */
    @Transactional(readOnly = true)
    public List<IssueResponse> getMyIssues(String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);

        return issueRepository.findByCreatedByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
        * Zwraca zgloszenia przypisane do aktualnego konserwatora.
     */
    @Transactional(readOnly = true)
    public List<IssueResponse> getAssignedIssues(String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);

        return issueRepository.findByMainAssigneeIdAndDeletedAtIsNullOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
        * Zwraca wszystkie zgloszenia w kontekście administratora z opcjonalnymi filtrami.
     */
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

    /**
        * Tworzy nowe zgloszenie dla biezacego kontekstu uzytkownika.
     */
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

    /**
        * Aktualizuje status zgloszenia po sprawdzeniu uprawnien modyfikujacego.
     */
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

    /**
        * Przypisuje zgloszenie do konserwatora i w razie potrzeby przechodzi do statusu IN_PROGRESS.
     */
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

    /**
        * Zwraca dostepne kategorie zgloszen.
     */
    @Transactional(readOnly = true)
    public List<IssueCategoryResponse> getCategories() {
        return issueCategoryRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(IssueCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .map(category -> new IssueCategoryResponse(category.getId(), category.getName()))
                .toList();
    }

    /**
        * Zwraca metadane zdjec przypisanych do zgloszenia.
     */
    @Transactional(readOnly = true)
    public List<IssueImageResponse> getIssueImages(String currentUserEmail, Integer issueId) {
        User currentUser = getCurrentUser(currentUserEmail);
        Issue issue = getIssueOrThrow(issueId);
        assertCanAccessIssue(currentUser, issue);

        return issueImageRepository.findByIssueId(issueId)
                .stream()
                .sorted(Comparator.comparing(IssueImage::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toImageResponse)
                .toList();
    }

    /**
        * Zapisuje wgrane zdjecie i laczy je ze zgloszeniem.
     */
    public IssueImageResponse addIssueImage(String currentUserEmail, Integer issueId, MultipartFile file) {
        User currentUser = getCurrentUser(currentUserEmail);
        Issue issue = getIssueOrThrow(issueId);
        assertCanAccessIssue(currentUser, issue);

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plik zdjecia jest wymagany");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String safeFileName = UUID.randomUUID() + extension;

        Path targetDirectory = getIssueImageDirectory(issueId);
        Path targetPath = targetDirectory.resolve(safeFileName).normalize();

        try {
            Files.createDirectories(targetPath.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udalo sie zapisac zdjecia");
        }

        IssueImage savedImage = issueImageRepository.save(IssueImage.builder()
                .issueId(issueId)
            .filePath(safeFileName)
                .createdAt(LocalDateTime.now())
                .build());

        return toImageResponse(savedImage);
    }

    /**
        * Zwraca metadane zdjecia po sprawdzeniu uprawnien dostepu do zgloszenia.
     */
    public IssueImage getIssueImage(String currentUserEmail, Integer issueId, Integer imageId) {
        User currentUser = getCurrentUser(currentUserEmail);
        Issue issue = getIssueOrThrow(issueId);
        assertCanAccessIssue(currentUser, issue);

        IssueImage image = issueImageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zdjecie nie istnieje"));

        if (!issueId.equals(image.getIssueId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Zdjecie nie istnieje dla tego zgloszenia");
        }

        return image;
    }

    /**
        * Usuwa metadane zdjecia i powiazany plik z dysku.
     */
    public void deleteIssueImage(String currentUserEmail, Integer issueId, Integer imageId) {
        IssueImage image = getIssueImage(currentUserEmail, issueId, imageId);
        Path path = resolveIssueImagePath(issueId, image.getFilePath());

        issueImageRepository.delete(image);

        try {
            Files.deleteIfExists(path);

            Path parentDirectory = path.getParent();
            if (parentDirectory != null && Files.exists(parentDirectory)) {
                try (var entries = Files.list(parentDirectory)) {
                    if (entries.findAny().isEmpty()) {
                        Files.deleteIfExists(parentDirectory);
                    }
                }
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udalo sie usunac pliku zdjecia");
        }
    }

    /**
        * Wyznacza fizyczna sciezke pliku dla zdjecia zgloszenia.
     */
    public Path getIssueImagePath(String currentUserEmail, Integer issueId, Integer imageId) {
        IssueImage image = getIssueImage(currentUserEmail, issueId, imageId);
        return resolveIssueImagePath(issueId, image.getFilePath());
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

        // For ADMIN or MAINTAINER, localId can be null for community-wide issues
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

    private void assertCanAccessIssue(User currentUser, Issue issue) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        if (currentUser.getRole() == UserRole.RESIDENT && issue.getCreatedByUserId().equals(currentUser.getId())) {
            return;
        }

        if (currentUser.getRole() == UserRole.MAINTAINER && issue.getMainAssigneeId() != null && issue.getMainAssigneeId().equals(currentUser.getId())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Brak uprawnien do tego zgloszenia");
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return ".jpg";
        }

        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return ".jpg";
        }

        return originalFilename.substring(index).toLowerCase();
    }

    private IssueImageResponse toImageResponse(IssueImage image) {
        return new IssueImageResponse(
                image.getId(),
                image.getIssueId(),
                image.getFilePath(),
                "/api/issues/" + image.getIssueId() + "/images/" + image.getId(),
                image.getCreatedAt()
        );
    }

    private Path getIssueImageDirectory(Integer issueId) {
        return Path.of("uploads", "issue-images", issueId.toString()).toAbsolutePath().normalize();
    }

    private Path resolveIssueImagePath(Integer issueId, String fileName) {
        return getIssueImageDirectory(issueId).resolve(fileName).normalize();
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
