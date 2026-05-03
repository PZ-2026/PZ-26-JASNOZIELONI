package pl.edu.ur.coopspace_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.coopspace_backend.entity.Charge;
import pl.edu.ur.coopspace_backend.entity.ChargeItem;
import pl.edu.ur.coopspace_backend.entity.ChargeItemType;
import pl.edu.ur.coopspace_backend.entity.Payment;
import pl.edu.ur.coopspace_backend.repository.ChargeItemRepository;
import pl.edu.ur.coopspace_backend.repository.ChargeItemTypeRepository;
import pl.edu.ur.coopspace_backend.repository.ChargeRepository;
import pl.edu.ur.coopspace_backend.repository.PaymentRepository;

import java.util.List;
import java.util.Comparator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
/**
 * Provides resident-facing finance operations for charges, payments, and charge item metadata.
 */
public class UserFinanceService {

    private final ChargeRepository chargeRepository;
    private final PaymentRepository paymentRepository;
    private final ChargeItemRepository chargeItemRepository;
    private final ChargeItemTypeRepository chargeItemTypeRepository;

    /**
     * Returns charges assigned to the given local.
     *
     * @param localId local identifier
     * @return list of charges for the local
     */
    @Transactional(readOnly = true)
    public List<Charge> getCharges(Integer localId) {
        return chargeRepository.findByLocalId(localId);
    }

    /**
     * Returns payments made for all charges linked to the given local.
     *
     * @param localId local identifier
     * @return list of payments for the local
     */
    @Transactional(readOnly = true)
    public List<Payment> getPayments(Integer localId) {
        List<Charge> charges = chargeRepository.findByLocalId(localId);
        List<Integer> chargeIds = charges.stream().map(Charge::getId).toList();
        return chargeIds.isEmpty() ? List.of() : chargeIds.stream()
                .flatMap(chargeId -> paymentRepository.findByChargeId(chargeId).stream())
                .toList();
    }

    /**
     * Returns all charge items for charges linked to the given local.
     *
     * @param localId local identifier
     * @return list of charge items for the local
     */
    @Transactional(readOnly = true)
    public List<ChargeItem> getChargeItems(Integer localId) {
        List<Charge> charges = chargeRepository.findByLocalId(localId);
        List<Integer> chargeIds = charges.stream().map(Charge::getId).toList();
        return chargeIds.isEmpty() ? List.of() : chargeIds.stream()
                .flatMap(chargeId -> chargeItemRepository.findByChargeId(chargeId).stream())
                .toList();
    }

    /**
     * Returns all available charge item types.
     *
     * @return list of charge item types
     */
    @Transactional(readOnly = true)
    public List<ChargeItemType> getChargeItemTypes() {
        return chargeItemTypeRepository.findAll();
    }

    /**
     * Stores a new payment for a local, resolving the target charge when needed.
     *
     * @param localId local identifier
     * @param payment payment payload
     * @return persisted payment
     */
    @Transactional
    public Payment makePayment(Integer localId, Payment payment) {
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kwota musi być większa od zera");
        }
        
        Integer targetChargeId = payment.getChargeId();
        
        if (targetChargeId == null) {
            List<Charge> charges = chargeRepository.findByLocalId(localId);
            charges.sort(Comparator.comparing(Charge::getPeriodStart));
            
            for (Charge c : charges) {
                BigDecimal totalAmount = c.getTotalAmount() != null ? c.getTotalAmount() : BigDecimal.ZERO;
                BigDecimal paidAmount = paymentRepository.findByChargeId(c.getId()).stream()
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (paidAmount.compareTo(totalAmount) < 0) {
                    targetChargeId = c.getId();
                    break;
                }
            }
            
            if (targetChargeId == null) {
                if (!charges.isEmpty()) {
                    targetChargeId = charges.get(charges.size() - 1).getId();
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak opłat do zapłaty");
                }
            }
        } else {
            Charge charge = chargeRepository.findById(targetChargeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opłata nie istnieje"));
            if (!charge.getLocalId().equals(localId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Brak dostępu do tej opłaty");
            }
        }
        
        Payment newPayment = Payment.builder()
                .chargeId(targetChargeId)
                .amount(payment.getAmount())
                .paymentDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();
                
        return paymentRepository.save(newPayment);
    }
}
