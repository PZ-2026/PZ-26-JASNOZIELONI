package pl.edu.ur.coopspace_backend.service;

import org.example.RepairProtocolData;
import org.example.RepairProtocolGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.entity.Building;
import pl.edu.ur.coopspace_backend.entity.Issue;
import pl.edu.ur.coopspace_backend.entity.IssueCategory;
import pl.edu.ur.coopspace_backend.entity.IssueComment;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;
import pl.edu.ur.coopspace_backend.entity.IssueStatusHistory;
import pl.edu.ur.coopspace_backend.entity.Local;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.BuildingRepository;
import pl.edu.ur.coopspace_backend.repository.IssueCategoryRepository;
import pl.edu.ur.coopspace_backend.repository.IssueCommentRepository;
import pl.edu.ur.coopspace_backend.repository.IssueRepository;
import pl.edu.ur.coopspace_backend.repository.IssueStatusHistoryRepository;
import pl.edu.ur.coopspace_backend.repository.LocalRepository;
import pl.edu.ur.coopspace_backend.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
/**
 * Serwis odpowiedzialny za przygotowanie danych i generowanie protokolow naprawy.
 *
 * <p>Weryfikuje uprawnienia, waliduje status zgloszenia oraz mapuje dane do formatu
 * wymaganego przez biblioteke PDF.</p>
 */
public class RepairProtocolService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final IssueRepository issueRepository;
    private final IssueCategoryRepository issueCategoryRepository;
    private final IssueCommentRepository issueCommentRepository;
    private final IssueStatusHistoryRepository issueStatusHistoryRepository;
    private final LocalRepository localRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

    public RepairProtocolService(
            IssueRepository issueRepository,
            IssueCategoryRepository issueCategoryRepository,
            IssueCommentRepository issueCommentRepository,
            IssueStatusHistoryRepository issueStatusHistoryRepository,
            LocalRepository localRepository,
            BuildingRepository buildingRepository,
            UserRepository userRepository
    ) {
        this.issueRepository = issueRepository;
        this.issueCategoryRepository = issueCategoryRepository;
        this.issueCommentRepository = issueCommentRepository;
        this.issueStatusHistoryRepository = issueStatusHistoryRepository;
        this.localRepository = localRepository;
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
    }

    /**
        * Generuje protokol naprawy w formacie PDF dla wskazanego zgloszenia.
     */
    public RepairProtocolResult generateRepairProtocol(String currentUserEmail, Integer issueId) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Uzytkownik niezalogowany"));

        Issue issue = issueRepository.findByIdAndDeletedAtIsNull(issueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zgloszenie nie istnieje"));

        assertCanGenerate(currentUser, issue);

        if (issue.getStatus() != IssueStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Protokol mozna generowac tylko dla zamknietych zgloszen");
        }

        RepairProtocolData data = buildRepairProtocolData(issue, currentUser);
        Path outputPath = createProtocolFile(issue.getId());

        try {
            RepairProtocolGenerator generator = new RepairProtocolGenerator();
            generator.generateRepairPdf(outputPath.toString(), data);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udalo sie wygenerowac protokolu", exception);
        }

        return new RepairProtocolResult(outputPath, buildFileName(issue.getId()));
    }

    /**
        * Sprawdza czy biezacy uzytkownik moze generowac protokol dla danego zgloszenia.
     */
    private void assertCanGenerate(User currentUser, Issue issue) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        if (currentUser.getRole() == UserRole.MAINTAINER
                && issue.getMainAssigneeId() != null
                && issue.getMainAssigneeId().equals(currentUser.getId())) {
            return;
        }

        if (currentUser.getRole() == UserRole.RESIDENT
                && issue.getCreatedByUserId() != null
                && issue.getCreatedByUserId().equals(currentUser.getId())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Brak uprawnien do wygenerowania protokolu");
    }

    /**
        * Buduje obiekt danych wejsciowych dla generatora PDF.
     */
    private RepairProtocolData buildRepairProtocolData(Issue issue, User currentUser) {
        RepairProtocolData data = new RepairProtocolData();

        data.dataStworzenia = LocalDateTime.now().format(DATE_FORMAT);
        data.numerProtokolu = buildProtocolNumber(issue.getId());
        data.dataRozpoczecia = formatDateTime(resolveStartDate(issue));
        data.dataZakonczenia = formatDateTime(resolveEndDate(issue));
        data.konserwator = resolveMaintainerName(issue.getMainAssigneeId());
        data.adres = resolveAddress(issue.getLocalId());
        data.status = toProtocolStatus(issue.getStatus());
        data.osobaTworzaca = formatUserName(currentUser);
        data.kategoria = resolveCategoryName(issue.getCategoryId());
        data.tytul = issue.getTitle();
        data.opis = issue.getDescription();
        data.komentarze = buildComments(issue.getId());

        return data;
    }

    private String buildProtocolNumber(Integer issueId) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("PR/%d/%02d/%d", now.getYear(), now.getMonthValue(), issueId);
    }

    private String buildFileName(Integer issueId) {
        return "Protokol_Naprawy_" + issueId + ".pdf";
    }

    /**
        * Wyszukuje pierwsza date przejscia w status IN_PROGRESS.
     */
    private LocalDateTime resolveStartDate(Issue issue) {
        List<IssueStatusHistory> history = issueStatusHistoryRepository.findByIssueId(issue.getId());

        return history.stream()
                .filter(entry -> entry.getStatus() == IssueStatus.IN_PROGRESS)
                .map(IssueStatusHistory::getChangedAt)
                .filter(value -> value != null)
                .min(Comparator.naturalOrder())
                .orElse(issue.getCreatedAt());
    }

    /**
        * Wyszukuje date zakonczenia zgloszenia lub zwraca ostatnia aktualizacje.
     */
    private LocalDateTime resolveEndDate(Issue issue) {
        if (issue.getClosedAt() != null) {
            return issue.getClosedAt();
        }

        return issue.getUpdatedAt() != null ? issue.getUpdatedAt() : LocalDateTime.now();
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

        Local local = localRepository.findById(localId)
                .orElse(null);
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

    private String resolveCategoryName(Integer categoryId) {
        if (categoryId == null) {
            return "Brak";
        }

        IssueCategory category = issueCategoryRepository.findById(categoryId).orElse(null);
        if (category == null || category.getName() == null || category.getName().isBlank()) {
            return "Kategoria #" + categoryId;
        }

        return category.getName();
    }

    /**
        * Pobiera komentarze zgloszenia i mapuje na format tabeli protokolu.
     */
    private List<RepairProtocolData.CommentRow> buildComments(Integer issueId) {
        List<IssueComment> comments = issueCommentRepository.findByIssueId(issueId)
                .stream()
                .filter(comment -> comment.getDeletedAt() == null)
                .sorted(Comparator.comparing(IssueComment::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return buildRows(comments);
    }

    private List<RepairProtocolData.CommentRow> buildRows(List<IssueComment> comments) {
        int index = 1;
        java.util.ArrayList<RepairProtocolData.CommentRow> rows = new java.util.ArrayList<>();

        for (IssueComment comment : comments) {
            LocalDateTime date = comment.getCreatedAt() != null ? comment.getCreatedAt() : comment.getUpdatedAt();
            String dateString = date != null ? date.format(DATE_FORMAT) : "";
            String author = resolveCommentAuthor(comment.getUserId());
            rows.add(new RepairProtocolData.CommentRow(
                    String.valueOf(index),
                    dateString,
                    comment.getContent(),
                    author
            ));
            index++;
        }

        return rows;
    }

    private String resolveCommentAuthor(Integer userId) {
        if (userId == null) {
            return "System";
        }

        return userRepository.findById(userId)
                .map(this::formatUserName)
                .orElse("Nieznany");
    }

    private String formatUserName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "";
        }

        return value.format(DATE_TIME_FORMAT);
    }

    private String toProtocolStatus(IssueStatus status) {
        if (status == IssueStatus.CLOSED) {
            return "ZAKOŃCZONE";
        }

        if (status == IssueStatus.IN_PROGRESS) {
            return "W TRAKCIE";
        }

        return "NOWE";
    }

    /**
        * Tworzy tymczasowy plik wyjsciowy dla generatora PDF.
     */
    private Path createProtocolFile(Integer issueId) {
        try {
            Path path = Files.createTempFile("repair_protocol_" + issueId + "_", ".pdf");
            path.toFile().deleteOnExit();
            return path;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udalo sie przygotowac pliku", exception);
        }
    }

    public record RepairProtocolResult(Path filePath, String fileName) {
    }
}
