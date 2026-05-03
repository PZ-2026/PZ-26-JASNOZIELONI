package pl.edu.ur.coopspace_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

import java.util.List;

@RestController
@RequestMapping("/api/user/finances")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserFinanceController {

    private final UserFinanceService userFinanceService;
    private final UserRepository userRepository;

    @GetMapping("/charges")
    public ResponseEntity<List<Charge>> getCharges(Authentication authentication) {
        User currentUser = requireResident(authentication);
        return ResponseEntity.ok(userFinanceService.getCharges(currentUser.getLocalId()));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getPayments(Authentication authentication) {
        User currentUser = requireResident(authentication);
        return ResponseEntity.ok(userFinanceService.getPayments(currentUser.getLocalId()));
    }

    @GetMapping("/charge-items")
    public ResponseEntity<List<ChargeItem>> getChargeItems(Authentication authentication) {
        User currentUser = requireResident(authentication);
        return ResponseEntity.ok(userFinanceService.getChargeItems(currentUser.getLocalId()));
    }

    @GetMapping("/charge-item-types")
    public ResponseEntity<List<ChargeItemType>> getChargeItemTypes(Authentication authentication) {
        requireResident(authentication);
        return ResponseEntity.ok(userFinanceService.getChargeItemTypes());
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> makePayment(Authentication authentication, @RequestBody Payment payment) {
        User currentUser = requireResident(authentication);
        return ResponseEntity.ok(userFinanceService.makePayment(currentUser.getLocalId(), payment));
    }

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
