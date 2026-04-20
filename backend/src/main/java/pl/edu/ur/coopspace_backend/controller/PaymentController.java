package pl.edu.ur.coopspace_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.ur.coopspace_backend.dto.UpdateRatesRequest;
import pl.edu.ur.coopspace_backend.service.PaymentService;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PutMapping("/rates")
    public ResponseEntity<Void> updateRates(@RequestBody UpdateRatesRequest request) {
        paymentService.updateRates(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rates")
    public ResponseEntity<UpdateRatesRequest> getCurrentRates() {
        return ResponseEntity.ok(paymentService.getCurrentRates());
    }
}
