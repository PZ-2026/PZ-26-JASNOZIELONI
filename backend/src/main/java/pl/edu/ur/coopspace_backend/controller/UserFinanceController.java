package pl.edu.ur.coopspace_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.entity.Charge;
import pl.edu.ur.coopspace_backend.entity.ChargeItem;
import pl.edu.ur.coopspace_backend.entity.ChargeItemType;
import pl.edu.ur.coopspace_backend.entity.Payment;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.UserRepository;
import pl.edu.ur.coopspace_backend.service.UserFinanceService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/user/finances")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
/**
 * Exposes resident-only finance endpoints for charges, payments, and charge item data.
 */
public class UserFinanceController {

    private final UserFinanceService userFinanceService;
    private final UserRepository userRepository;

    /**
     * Returns charges visible to the authenticated resident.
     *
     * @param authentication current authentication
     * @return list of charges for the resident's local
     */
    @GetMapping("/charges")
    public ResponseEntity<List<Charge>> getCharges(Authentication authentication) {
        User currentUser = requireResident(authentication);
        return ResponseEntity.ok(userFinanceService.getCharges(currentUser.getLocalId()));
    }

    /**
     * Returns payments visible to the authenticated resident.
     *
     * @param authentication current authentication
     * @return list of payments for the resident's local
     */
    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getPayments(Authentication authentication) {
        User currentUser = requireResident(authentication);
        return ResponseEntity.ok(userFinanceService.getPayments(currentUser.getLocalId()));
    }

    /**
     * Returns charge items visible to the authenticated resident.
     *
     * @param authentication current authentication
     * @return list of charge items for the resident's local
     */
    @GetMapping("/charge-items")
    public ResponseEntity<List<ChargeItem>> getChargeItems(Authentication authentication) {
        User currentUser = requireResident(authentication);
        return ResponseEntity.ok(userFinanceService.getChargeItems(currentUser.getLocalId()));
    }

    /**
     * Returns the catalog of charge item types.
     *
     * @param authentication current authentication
     * @return list of charge item types
     */
    @GetMapping("/charge-item-types")
    public ResponseEntity<List<ChargeItemType>> getChargeItemTypes(Authentication authentication) {
        requireResident(authentication);
        return ResponseEntity.ok(userFinanceService.getChargeItemTypes());
    }

    /**
     * Creates a payment for the authenticated resident.
     *
     * @param authentication current authentication
     * @param payment payment payload
     * @return persisted payment
     */
    @PostMapping("/payments")
    public ResponseEntity<Payment> makePayment(Authentication authentication, @RequestBody Payment payment) {
        User currentUser = requireResident(authentication);
        return ResponseEntity.ok(userFinanceService.makePayment(currentUser.getLocalId(), payment));
    }

    /**
     * Generates and returns a financial report for the authenticated resident.
     *
     * @param authentication current authentication
     * @return PDF report file
     */
    @GetMapping("/report")
    public ResponseEntity<byte[]> downloadReport(
            Authentication authentication,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        User currentUser = requireResident(authentication);
        byte[] pdfContent = userFinanceService.generateFinancialReport(currentUser, startDate, endDate);

        String filename = "Raport_Finansowy_" + currentUser.getLastName() + "_" + LocalDate.now() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    /**
     * Ensures the caller is an authenticated resident with an assigned local.
     *
     * @param authentication current authentication
     * @return resident user
     */
    private User requireResident(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Użytkownik niezalogowany");
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nie znaleziono użytkownika"));

        if (currentUser.getRole() != UserRole.RESIDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tylko mieszkaniec ma dostęp do tego endpointu");
        }

        if (currentUser.getLocalId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mieszkaniec nie ma przypisanego lokalu");
        }

        return currentUser;
    }
}
