package pl.edu.ur.coopspace_backend.service;

import org.example.MaintenanceReportData;
import org.example.MaintenanceReportGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.entity.Building;
import pl.edu.ur.coopspace_backend.entity.Issue;
import pl.edu.ur.coopspace_backend.entity.IssueCategory;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;
import pl.edu.ur.coopspace_backend.entity.IssueStatusHistory;
import pl.edu.ur.coopspace_backend.entity.Local;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.BuildingRepository;
import pl.edu.ur.coopspace_backend.repository.IssueCategoryRepository;
import pl.edu.ur.coopspace_backend.repository.IssueRepository;
import pl.edu.ur.coopspace_backend.repository.IssueStatusHistoryRepository;
import pl.edu.ur.coopspace_backend.repository.LocalRepository;
import pl.edu.ur.coopspace_backend.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * Service responsible for generating maintenance issue reports.
 */
@Service
public class MaintenanceReportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final IssueRepository issueRepository;
    private final IssueCategoryRepository issueCategoryRepository;
    private final IssueStatusHistoryRepository issueStatusHistoryRepository;
    private final LocalRepository localRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

    /**
     * Creates a maintenance report service.
     *
     * @param issueRepository issue persistence access
     * @param issueCategoryRepository issue category persistence access
     * @param issueStatusHistoryRepository issue status history persistence access
     * @param localRepository local persistence access
     * @param buildingRepository building persistence access
     * @param userRepository user persistence access
     */
    public MaintenanceReportService(
            IssueRepository issueRepository,
            IssueCategoryRepository issueCategoryRepository,
            IssueStatusHistoryRepository issueStatusHistoryRepository,
            LocalRepository localRepository,
            BuildingRepository buildingRepository,
            UserRepository userRepository
    ) {
        this.issueRepository = issueRepository;
        this.issueCategoryRepository = issueCategoryRepository;
        this.issueStatusHistoryRepository = issueStatusHistoryRepository;
        this.localRepository = localRepository;
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Generates a maintenance issue report in PDF format.
     *
     * @param currentUserEmail current user email
     * @param months optional period length in months
     * @param status optional issue status filter
     * @param categoryId optional issue category filter
     * @param maintainerId optional maintainer filter
     * @param buildingId optional building filter
     * @return generated report metadata with file path and file name
     */
    public MaintenanceReportResult generateReport(
            String currentUserEmail,
            Integer months,
            IssueStatus status,
            Integer categoryId,
            Integer maintainerId,
            Integer buildingId
    ) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Uzytkownik niezalogowany"));

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tylko administrator moze generowac raport");
        }

        int monthsValue = months != null && months > 0 ? months : 6;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateFrom = now.minusMonths(monthsValue);

        List<Issue> issues = issueRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .filter(issue -> issue.getCreatedAt() != null && !issue.getCreatedAt().isBefore(dateFrom))
                .filter(issue -> status == null || issue.getStatus() == status)
                .filter(issue -> categoryId == null || Objects.equals(issue.getCategoryId(), categoryId))
                .filter(issue -> maintainerId == null || Objects.equals(issue.getMainAssigneeId(), maintainerId))
                .filter(issue -> matchesBuilding(issue, buildingId))
                .sorted(Comparator.comparing(Issue::getCreatedAt))
                .toList();

        MaintenanceReportData data = buildReportData(issues, dateFrom, now);
        Path outputPath = createReportFile();

        try {
            MaintenanceReportGenerator generator = new MaintenanceReportGenerator();
            generator.generateMaintenanceReportPdf(outputPath.toString(), data);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udalo sie wygenerowac raportu", exception);
        }

        return new MaintenanceReportResult(outputPath, buildFileName(now));
    }

    private boolean matchesBuilding(Issue issue, Integer buildingId) {
        if (buildingId == null) {
            return true;
        }

        if (issue.getLocalId() == null) {
            return false;
        }

        return localRepository.findById(issue.getLocalId())
                .map(Local::getBuildingId)
                .map(id -> Objects.equals(id, buildingId))
                .orElse(false);
    }

    private MaintenanceReportData buildReportData(List<Issue> issues, LocalDateTime dateFrom, LocalDateTime dateTo) {
        MaintenanceReportData data = new MaintenanceReportData();
        data.dataStworzenia = dateTo.format(DATE_FORMAT);
        data.numerRaportu = buildReportNumber(dateTo);
        data.dataOd = dateFrom.format(DATE_FORMAT);
        data.dataDo = dateTo.format(DATE_FORMAT);
        data.pozycje = new java.util.ArrayList<>();

        int index = 1;
        for (Issue issue : issues) {
            data.pozycje.add(new MaintenanceReportData.MaintenanceRow(
                    String.valueOf(index),
                    formatDate(issue.getCreatedAt()),
                    resolveCategoryName(issue.getCategoryId()),
                    resolveAddress(issue.getLocalId()),
                    resolveMaintainerName(issue.getMainAssigneeId()),
                    formatDuration(issue),
                    toReportStatus(issue.getStatus())
            ));
            index++;
        }

        return data;
    }

    private String buildReportNumber(LocalDateTime now) {
        return String.format("RK/%d/%02d/%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }

    private String buildFileName(LocalDateTime now) {
        return String.format("Raport_Zgloszen_Konserwatorskich_%d%02d%02d.pdf", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }

    private String resolveCategoryName(Integer categoryId) {
        if (categoryId == null) {
            return "Brak";
        }

        IssueCategory category = issueCategoryRepository.findById(categoryId).orElse(null);
        if (category == null || category.getName() == null || category.getName().isBlank()) {
            return "Kategoria #" + categoryId;
        }

        return normalizeText(category.getName());
    }

    private String resolveMaintainerName(Integer maintainerId) {
        if (maintainerId == null) {
            return "Nieprzypisano";
        }

        return userRepository.findById(maintainerId)
                .map(this::formatUserName)
                .orElse("Nieznany konserwator");
    }

    private String resolveAddress(Integer localId) {
        if (localId == null) {
            return "Brak lokalu";
        }

        Local local = localRepository.findById(localId).orElse(null);
        if (local == null) {
            return "Lokal " + localId;
        }

        String localSuffix = local.getNumber() != null ? "Lokal " + local.getNumber() : "Lokal " + localId;
        Building building = buildingRepository.findById(local.getBuildingId()).orElse(null);
        if (building == null || building.getAddress() == null || building.getAddress().isBlank()) {
            return localSuffix;
        }

        return building.getAddress() + ", " + localSuffix;
    }

    private String formatDuration(Issue issue) {
        LocalDateTime start = resolveWorkStart(issue.getId());
        LocalDateTime end = Optional.ofNullable(issue.getClosedAt())
            .orElse(issue.getUpdatedAt());

        if (start == null || end == null) {
            return "-";
        }

        long minutes = Duration.between(start, end).toMinutes();
        if (minutes < 0) {
            return "-";
        }

        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        if (hours == 0) {
            return remainingMinutes + "m";
        }

        return hours + "h " + remainingMinutes + "m";
    }

    private LocalDateTime resolveWorkStart(Integer issueId) {
        List<IssueStatusHistory> history = issueStatusHistoryRepository.findByIssueId(issueId);

        return history.stream()
                .filter(entry -> entry.getStatus() == IssueStatus.IN_PROGRESS)
                .map(IssueStatusHistory::getChangedAt)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private String formatDate(LocalDateTime value) {
        if (value == null) {
            return "";
        }

        return value.format(DATE_FORMAT);
    }

    private String toReportStatus(IssueStatus status) {
        if (status == IssueStatus.CLOSED) {
            return normalizeText("ZAKONCZONE");
        }

        if (status == IssueStatus.IN_PROGRESS) {
            return normalizeText("W TRAKCIE");
        }

        return normalizeText("NOWE");
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private String formatUserName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }

    private Path createReportFile() {
        try {
            Path path = Files.createTempFile("maintenance_report_", ".pdf");
            path.toFile().deleteOnExit();
            return path;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udalo sie przygotowac pliku", exception);
        }
    }

    /**
     * Result metadata for a generated maintenance report.
     *
     * @param filePath generated report path
     * @param fileName generated report file name
     */
    public record MaintenanceReportResult(Path filePath, String fileName) {
    }
}
