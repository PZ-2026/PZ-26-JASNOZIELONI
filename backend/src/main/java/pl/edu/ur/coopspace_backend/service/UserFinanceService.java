package pl.edu.ur.coopspace_backend.service;

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

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.entity.*;
import pl.edu.ur.coopspace_backend.repository.*;
import org.example.FinancialReportGenerator;
import org.example.FinancialReportData;

import java.io.File;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provides resident-facing finance operations for charges, payments, and charge item metadata.
 */
@Service
public class UserFinanceService {

    private final ChargeRepository chargeRepository;
    private final PaymentRepository paymentRepository;
    private final ChargeItemRepository chargeItemRepository;
    private final ChargeItemTypeRepository chargeItemTypeRepository;
    private final LocalRepository localRepository;
    private final BuildingRepository buildingRepository;

    /**
     * Creates a user finance service.
     *
     * @param chargeRepository charge persistence access
     * @param paymentRepository payment persistence access
     * @param chargeItemRepository charge item persistence access
     * @param chargeItemTypeRepository charge item type persistence access
     * @param localRepository local persistence access
     * @param buildingRepository building persistence access
     */
    public UserFinanceService(ChargeRepository chargeRepository, PaymentRepository paymentRepository,
            ChargeItemRepository chargeItemRepository, ChargeItemTypeRepository chargeItemTypeRepository,
            LocalRepository localRepository, BuildingRepository buildingRepository) {
        this.chargeRepository = chargeRepository;
        this.paymentRepository = paymentRepository;
        this.chargeItemRepository = chargeItemRepository;
        this.chargeItemTypeRepository = chargeItemTypeRepository;
        this.localRepository = localRepository;
        this.buildingRepository = buildingRepository;
    }

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

        // Walidacja nadpłaty
        BigDecimal chargeTotal = chargeRepository.findById(targetChargeId).orElseThrow().getTotalAmount();
        if (chargeTotal == null) chargeTotal = BigDecimal.ZERO;
        
        BigDecimal alreadyPaid = paymentRepository.findByChargeId(targetChargeId).stream()
                .map(Payment::getAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal remaining = chargeTotal.subtract(alreadyPaid);
        if (payment.getAmount().compareTo(remaining) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nie można zapłacić więcej niż pozostało do spłaty (" + remaining + " zł)");
        }
        
        Payment newPayment = Payment.builder()
                .chargeId(targetChargeId)
                .amount(payment.getAmount())
                .paymentDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();
                
        Payment savedPayment = paymentRepository.save(newPayment);

        // Aktualizacja statusu opłaty
        Charge charge = chargeRepository.findById(targetChargeId).orElseThrow();
        BigDecimal totalAmount = charge.getTotalAmount() != null ? charge.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paidAmount = paymentRepository.findByChargeId(targetChargeId).stream()
                .map(Payment::getAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (paidAmount.compareTo(totalAmount) >= 0) {
            charge.setStatus(ChargeStatus.PAID);
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            charge.setStatus(ChargeStatus.PARTIALLY_PAID);
        } else {
            charge.setStatus(ChargeStatus.UNPAID);
        }
        charge.setUpdatedAt(LocalDateTime.now());
        chargeRepository.save(charge);

        return savedPayment;
    }

    /**
     * Generates a financial PDF report for the provided resident and period.
     *
     * @param user current resident
     * @param startDate optional report period start date
     * @param endDate optional report period end date
     * @return generated PDF content as bytes
     */
    @Transactional(readOnly = true)
    public byte[] generateFinancialReport(User user, LocalDate startDate, LocalDate endDate) {
        Local local = localRepository.findById(user.getLocalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono lokalu"));
        Building building = buildingRepository.findById(local.getBuildingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono budynku"));

        List<Charge> chargesList = chargeRepository.findByLocalId(user.getLocalId());
        
        if (startDate != null) {
            chargesList = chargesList.stream()
                    .filter(c -> !c.getPeriodStart().isBefore(startDate))
                    .toList();
        }
        if (endDate != null) {
            chargesList = chargesList.stream()
                    .filter(c -> !c.getPeriodEnd().isAfter(endDate))
                    .toList();
        }
        
        List<Charge> finalCharges = new ArrayList<>(chargesList);
        finalCharges.sort(Comparator.comparing(Charge::getPeriodStart));

        FinancialReportData data = new FinancialReportData();
        data.dataStworzenia = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        data.numerRaportu = "RF/" + LocalDate.now().getYear() + "/" + LocalDate.now().getMonthValue() + "/" + user.getId();
        
        data.dataOd = startDate != null ? startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : 
                     (finalCharges.isEmpty() ? "-" : finalCharges.get(0).getPeriodStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        data.dataDo = endDate != null ? endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : 
                     (finalCharges.isEmpty() ? "-" : finalCharges.get(finalCharges.size() - 1).getPeriodEnd().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        data.imieNazwisko = user.getFirstName() + " " + user.getLastName();
        data.adresMieszkania = building.getAddress() + ", m. " + local.getNumber();
        data.email = user.getEmail();

        data.pozycje = new ArrayList<>();
        int index = 1;
        for (Charge charge : finalCharges) {
            List<ChargeItem> items = chargeItemRepository.findByChargeId(charge.getId());
            BigDecimal water = BigDecimal.ZERO;
            BigDecimal electricity = BigDecimal.ZERO;
            BigDecimal rent = BigDecimal.ZERO;
            BigDecimal gas = BigDecimal.ZERO;

            for (ChargeItem item : items) {
                BigDecimal total = item.getTotal() != null ? item.getTotal() : BigDecimal.ZERO;
                if (item.getTypeId() == 1) water = water.add(total);
                else if (item.getTypeId() == 2) electricity = electricity.add(total);
                else if (item.getTypeId() == 3) rent = rent.add(total);
                else if (item.getTypeId() == 4) gas = gas.add(total);
            }

            BigDecimal paidAmount = paymentRepository.findByChargeId(charge.getId()).stream()
                    .map(Payment::getAmount)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String statusStr = switch (charge.getStatus()) {
                case PAID -> "OPŁACONE";
                case UNPAID -> "NIEOPŁACONE";
                case PARTIALLY_PAID -> "CZĘŚCIOWO OPŁACONE";
            };

            BigDecimal totalChargeAmount = charge.getTotalAmount() != null ? charge.getTotalAmount() : BigDecimal.ZERO;

            data.pozycje.add(new FinancialReportData.FinanceRow(
                    String.valueOf(index++),
                    charge.getPeriodStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    rent.toPlainString(),
                    water.toPlainString(),
                    electricity.toPlainString(),
                    gas.toPlainString(),
                    totalChargeAmount.toPlainString(),
                    paidAmount.toPlainString(),
                    statusStr
            ));
        }

        String tempFilePath = "temp_report_" + user.getId() + "_" + System.currentTimeMillis() + ".pdf";
        FinancialReportGenerator generator = new FinancialReportGenerator();
        try {
            generator.generateFinancialReportPdf(tempFilePath, data);
            File pdfFile = new File(tempFilePath);
            if (!pdfFile.exists()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Plik PDF nie został wygenerowany");
            }
            byte[] content = Files.readAllBytes(pdfFile.toPath());
            pdfFile.delete();
            return content;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd podczas generowania PDF: " + e.getMessage(), e);
        }
    }
}
