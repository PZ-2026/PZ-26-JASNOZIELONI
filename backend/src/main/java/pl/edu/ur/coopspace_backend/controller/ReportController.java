package pl.edu.ur.coopspace_backend.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;
import pl.edu.ur.coopspace_backend.service.MaintenanceReportService;
import pl.edu.ur.coopspace_backend.service.StatisticReportService;

/**
 * Kontroler do endpointow API dla raportow generowanych w postaci plikow PDF.
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

        private final MaintenanceReportService maintenanceReportService;
        private final StatisticReportService statisticReportService;

        /**
         * Creates a report controller.
         *
         * @param maintenanceReportService maintenance report service
         * @param statisticReportService statistic report service
         */
        public ReportController(MaintenanceReportService maintenanceReportService,
                        StatisticReportService statisticReportService) {
                this.maintenanceReportService = maintenanceReportService;
                this.statisticReportService = statisticReportService;
        }

        /**
         * Generates a maintenance issue report in PDF format.
         *
         * @param authentication current user authentication
         * @param months optional period length in months
         * @param status optional issue status filter
         * @param categoryId optional category filter
         * @param maintainerId optional maintainer filter
         * @param buildingId optional building filter
         * @return generated maintenance report as downloadable PDF resource
         */
        @GetMapping(value = "/maintenance", produces = MediaType.APPLICATION_PDF_VALUE)
        public ResponseEntity<Resource> downloadMaintenanceReport(
                        Authentication authentication,
                        @RequestParam(required = false) Integer months,
                        @RequestParam(required = false) IssueStatus status,
                        @RequestParam(required = false) Integer categoryId,
                        @RequestParam(required = false) Integer maintainerId,
                        @RequestParam(required = false) Integer buildingId) {
                MaintenanceReportService.MaintenanceReportResult result = maintenanceReportService.generateReport(
                                authentication.getName(),
                                months,
                                status,
                                categoryId,
                                maintainerId,
                                buildingId);

                Resource resource = new FileSystemResource(result.filePath());
                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_PDF)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + result.fileName() + "\"")
                                .body(resource);
        }

        /**
         * Generates a cooperative statistic report in PDF format.
         *
         * @param authentication current user authentication
         * @param months optional period length in months
         * @param includeRevenue whether to include revenue section
         * @param includeIssuesCount whether to include issue count section
         * @param includeAvgResolutionTime whether to include average resolution time section
         * @param includeResidentsCount whether to include resident count section
         * @return generated statistic report as downloadable PDF resource
         */
        @GetMapping(value = "/statistic", produces = MediaType.APPLICATION_PDF_VALUE)
        public ResponseEntity<Resource> downloadStatisticReport(
                        Authentication authentication,
                        @RequestParam(required = false) Integer months,
                        @RequestParam(required = false, defaultValue = "true") Boolean includeRevenue,
                        @RequestParam(required = false, defaultValue = "true") Boolean includeIssuesCount,
                        @RequestParam(required = false, defaultValue = "true") Boolean includeAvgResolutionTime,
                        @RequestParam(required = false, defaultValue = "true") Boolean includeResidentsCount) {
                StatisticReportService.StatisticReportResult result = statisticReportService.generateReport(
                                authentication.getName(),
                                months,
                                includeRevenue,
                                includeIssuesCount,
                                includeAvgResolutionTime,
                                includeResidentsCount);

                Resource resource = new FileSystemResource(result.filePath());
                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_PDF)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + result.fileName() + "\"")
                                .body(resource);
        }
}
