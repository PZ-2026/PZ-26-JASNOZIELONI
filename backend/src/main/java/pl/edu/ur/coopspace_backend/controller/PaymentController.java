package pl.edu.ur.coopspace_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.ur.coopspace_backend.dto.UpdateRatesRequest;
import pl.edu.ur.coopspace_backend.service.PaymentService;

/**
 * Exposes administrative endpoints for viewing and updating payment rates.
 */
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Updates payment rates used by the application (admin only).
     *
     * @param request new rates payload
     * @return empty success response
     */
    @PutMapping("/rates")
    public ResponseEntity<Void> updateRates(@RequestBody UpdateRatesRequest request) {
        paymentService.updateRates(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns the current payment rates configuration.
     *
     * @return current rates payload
     */
    @GetMapping("/rates")
    public ResponseEntity<UpdateRatesRequest> getCurrentRates() {
        return ResponseEntity.ok(paymentService.getCurrentRates());
    }
}
