package pl.edu.ur.coopspace_backend.service;

import org.example.StatisticReportGenerator;
import org.example.StatsReportData;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.entity.*;
import pl.edu.ur.coopspace_backend.repository.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for generating cooperative statistic reports.
 *
 * <p>Validates permissions, computes report metrics from persisted data,
 * and maps values to the PDF generation model.</p>
 */
@Service
public class StatisticReportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final IssueRepository issueRepository;
    private final IssueStatusHistoryRepository issueStatusHistoryRepository;
    private final LocalRepository localRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final ChargeRepository chargeRepository;
    private final PaymentRepository paymentRepository;
    private final ChargeItemRepository chargeItemRepository;

    /**
     * Creates a statistic report service.
     *
     * @param issueRepository issue persistence access
     * @param issueStatusHistoryRepository issue status history persistence access
     * @param localRepository local persistence access
     * @param buildingRepository building persistence access
     * @param userRepository user persistence access
     * @param chargeRepository charge persistence access
     * @param paymentRepository payment persistence access
     * @param chargeItemRepository charge item persistence access
     */
    public StatisticReportService(
            IssueRepository issueRepository,
            IssueStatusHistoryRepository issueStatusHistoryRepository,
            LocalRepository localRepository,
            BuildingRepository buildingRepository,
            UserRepository userRepository,
            ChargeRepository chargeRepository,
            PaymentRepository paymentRepository,
            ChargeItemRepository chargeItemRepository
    ) {
        this.issueRepository = issueRepository;
        this.issueStatusHistoryRepository = issueStatusHistoryRepository;
        this.localRepository = localRepository;
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
        this.chargeRepository = chargeRepository;
        this.paymentRepository = paymentRepository;
        this.chargeItemRepository = chargeItemRepository;
    }

    /**
     * Generates a cooperative statistic report in PDF format for the parameters provided
     * by the user.
     *
     * @param currentUserEmail current user email
     * @param months optional period length in months
     * @param includeRevenue whether to include revenue metrics
     * @param includeIssuesCount whether to include issue count metrics
     * @param includeAvgResolutionTime whether to include average resolution time metrics
     * @param includeResidentsCount whether to include resident count metrics
     * @return metadata of the generated statistic report file
     */
    public StatisticReportResult generateReport(
            String currentUserEmail,
            Integer months,
            Boolean includeRevenue,
            Boolean includeIssuesCount,
            Boolean includeAvgResolutionTime,
            Boolean includeResidentsCount
    ) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Uzytkownik niezalogowany"));

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tylko administrator moze generowac raport");
        }

        int monthsValue = (months != null && months > 0) ? months : 6;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateFrom = now.minusMonths(monthsValue);

        StatsReportData data = new StatsReportData();
        data.dataStworzenia = now.format(DATE_FORMAT);
        data.numerRaportu = String.format("STAT/%02d/%d", now.getMonthValue(), now.getYear());
        data.dataOd = dateFrom.format(DATE_FORMAT);
        data.dataDo = now.format(DATE_FORMAT);

        if (Boolean.TRUE.equals(includeRevenue)) {
            BigDecimal totalRevenue = paymentRepository.findAll().stream()
                    .filter(p -> p.getPaymentDate() != null && !p.getPaymentDate().isBefore(dateFrom.toLocalDate()))
                    .map(Payment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            data.sumaPrzychodow = String.format("%.2f PLN", totalRevenue);
        }

        List<Issue> periodIssues = issueRepository.findByDeletedAtIsNullOrderByCreatedAtDesc().stream()
                .filter(issue -> issue.getCreatedAt() != null && !issue.getCreatedAt().isBefore(dateFrom))
                .toList();

        if (Boolean.TRUE.equals(includeIssuesCount)) {
            data.ogolnaLiczbaUsterek = String.valueOf(periodIssues.size());
        }

        if (Boolean.TRUE.equals(includeAvgResolutionTime)) {
            List<Issue> closedIssues = periodIssues.stream()
                    .filter(i -> i.getStatus() == IssueStatus.CLOSED)
                    .toList();
            
            if (closedIssues.isEmpty()) {
                data.sredniCzasRozwiazania = "0d 0h";
            } else {
                long totalMinutes = 0;
                int count = 0;
                for (Issue issue : closedIssues) {
                    LocalDateTime start = resolveWorkStart(issue.getId());
                    LocalDateTime end = issue.getClosedAt() != null ? issue.getClosedAt() : issue.getUpdatedAt();
                    if (start != null && end != null) {
                        long minutes = Duration.between(start, end).toMinutes();
                        if (minutes > 0) {
                            totalMinutes += minutes;
                            count++;
                        }
                    }
                }
                if (count == 0) {
                    data.sredniCzasRozwiazania = "0d 0h";
                } else {
                    long avgMinutes = totalMinutes / count;
                    long days = avgMinutes / (24 * 60);
                    long hours = (avgMinutes % (24 * 60)) / 60;
                    data.sredniCzasRozwiazania = days + "d " + hours + "h";
                }
            }
        }

        if (Boolean.TRUE.equals(includeResidentsCount)) {
            long residentsCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.RESIDENT)
                    .count();
            data.iloscMieszkancow = String.valueOf(residentsCount);
        }

        // mieszkancy dopisani do wspoldzielni to tacy, co maja status isActive na true i role RESIDENT
        long activeResidents = userRepository.findAll().stream()
            .filter(u -> u.getRole() == UserRole.RESIDENT)
            .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
            .count();

        // mieszkancy usunieci to usuniete konta, ale nadal przechowywane w celach archiwizacji
        // przez co maja status isActive na false
        long inactiveResidents = userRepository.findAll().stream()
            .filter(u -> u.getRole() == UserRole.RESIDENT)
            .filter(u -> Boolean.FALSE.equals(u.getIsActive()))
            .count();

        data.mieszkancyDopisani = String.valueOf(activeResidents);
        data.mieszkancyUsunieci = String.valueOf(inactiveResidents);

        data.pozycje = new ArrayList<>();
        int index = 1;
        List<Charge> charges = chargeRepository.findChargesOverlappingPeriod(
                dateFrom.toLocalDate(),
                now.toLocalDate()
            ).stream()
                .sorted(Comparator.comparing(Charge::getPeriodStart))
                .toList();

        for (Charge charge : charges) {
            List<ChargeItem> items = chargeItemRepository.findByChargeId(charge.getId());
            BigDecimal rent = BigDecimal.ZERO;
            BigDecimal water = BigDecimal.ZERO;
            BigDecimal electricity = BigDecimal.ZERO;
            BigDecimal gas = BigDecimal.ZERO;

            for (ChargeItem item : items) {
                BigDecimal total = item.getTotal() != null ? item.getTotal() : BigDecimal.ZERO;
                if (item.getTypeId() == 3) rent = rent.add(total);
                else if (item.getTypeId() == 1) water = water.add(total);
                else if (item.getTypeId() == 2) electricity = electricity.add(total);
                else if (item.getTypeId() == 4) gas = gas.add(total);
            }

            BigDecimal paidAmount = paymentRepository.findByChargeId(charge.getId()).stream()
                    .map(Payment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalAmount = charge.getTotalAmount() != null ? charge.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal remaining = totalAmount.subtract(paidAmount);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

            String localName = resolveAddress(charge.getLocalId());

            data.pozycje.add(new StatsReportData.StatsRow(
                    String.valueOf(index++),
                    charge.getPeriodStart().format(DATE_FORMAT),
                    localName,
                    rent.toPlainString(),
                    water.toPlainString(),
                    electricity.toPlainString(),
                    gas.toPlainString(),
                    remaining.toPlainString(),
                    paidAmount.toPlainString()
            ));
        }

        Path outputPath = createReportFile();
        try {
            StatisticReportGenerator generator = new StatisticReportGenerator();
            generator.generateStatsReportPdf(outputPath.toString(), data);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udalo sie wygenerowac raportu statystycznego", exception);
        }

        return new StatisticReportResult(outputPath, buildFileName(now));
    }

    private LocalDateTime resolveWorkStart(Integer issueId) {
        return issueStatusHistoryRepository.findByIssueId(issueId).stream()
                .filter(entry -> entry.getStatus() == IssueStatus.IN_PROGRESS)
                .map(IssueStatusHistory::getChangedAt)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private String resolveAddress(Integer localId) {
        if (localId == null) return "Brak";
        Local local = localRepository.findById(localId).orElse(null);
        if (local == null) return "Lokal " + localId;
        Building building = buildingRepository.findById(local.getBuildingId()).orElse(null);
        String localSuffix = local.getNumber() != null ? local.getNumber() : String.valueOf(localId);
        if (building == null || building.getAddress() == null || building.getAddress().isBlank()) return localSuffix;
        return building.getAddress() + " m. " + localSuffix;
    }

    /**
     * Creates a temporary report file and deletes it after the response is sent.
     *
     * @return path to the report file
     * @throws ResponseStatusException when the report file cannot be prepared
     */
    private Path createReportFile() {
        try {
            Path path = Files.createTempFile("statistic_report_", ".pdf");
            path.toFile().deleteOnExit();
            return path;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udalo sie przygotowac pliku", exception);
        }
    }

    /**
     * Builds the filename for the cooperative statistic report.
     *
     * @param now current date and time
     * @return report file name
     */
    private String buildFileName(LocalDateTime now) {
        return String.format("Raport_Statystyczny_%d%02d%02d.pdf", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }

    /**
     * Result metadata for a generated statistic report.
     *
     * @param filePath generated report path
     * @param fileName generated report file name
     */
    public record StatisticReportResult(Path filePath, String fileName) {}
}
