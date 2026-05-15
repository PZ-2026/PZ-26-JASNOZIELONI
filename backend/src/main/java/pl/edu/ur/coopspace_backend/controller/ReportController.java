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

/**
 * Kontroler do endpointow API dla raportow generowanych w postaci plikow PDF.
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final MaintenanceReportService maintenanceReportService;

    public ReportController(MaintenanceReportService maintenanceReportService) {
        this.maintenanceReportService = maintenanceReportService;
    }

    /**
     * Generuje raport zgloszen konserwatorskich w formacie PDF.
     */
    @GetMapping(value = "/maintenance", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> downloadMaintenanceReport(
            Authentication authentication,
            @RequestParam(required = false) Integer months,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer maintainerId,
            @RequestParam(required = false) Integer buildingId
    ) {
        MaintenanceReportService.MaintenanceReportResult result = maintenanceReportService.generateReport(
                authentication.getName(),
                months,
                status,
                categoryId,
                maintainerId,
                buildingId
        );

        Resource resource = new FileSystemResource(result.filePath());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .body(resource);
    }
}
