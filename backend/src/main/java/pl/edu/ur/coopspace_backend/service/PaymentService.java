package pl.edu.ur.coopspace_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.coopspace_backend.dto.UpdateRatesRequest;
import pl.edu.ur.coopspace_backend.entity.Charge;
import pl.edu.ur.coopspace_backend.entity.ChargeItem;
import pl.edu.ur.coopspace_backend.entity.ChargeItemType;
import pl.edu.ur.coopspace_backend.repository.ChargeItemRepository;
import pl.edu.ur.coopspace_backend.repository.ChargeItemTypeRepository;
import pl.edu.ur.coopspace_backend.repository.ChargeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ChargeRepository chargeRepository;
    private final ChargeItemRepository chargeItemRepository;
    private final ChargeItemTypeRepository chargeItemTypeRepository;

    @Transactional
    public void updateRates(UpdateRatesRequest request) {
        LocalDate now = LocalDate.now();
        List<Charge> activeCharges = chargeRepository.findActiveCharges(now);

        updateRateForType("Czynsz", request.getRentRate(), activeCharges);
        updateRateForType("Woda", request.getWaterRate(), activeCharges);
        updateRateForType("Prąd", request.getElectricityRate(), activeCharges);
        updateRateForType("Gaz", request.getGasRate(), activeCharges);
    }

    @Transactional(readOnly = true)
    public UpdateRatesRequest getCurrentRates() {
        return UpdateRatesRequest.builder()
                .rentRate(getLatestRateForType("Czynsz"))
                .waterRate(getLatestRateForType("Woda"))
                .electricityRate(getLatestRateForType("Prąd"))
                .gasRate(getLatestRateForType("Gaz"))
                .build();
    }

    private BigDecimal getLatestRateForType(String typeName) {
        return chargeItemTypeRepository.findByName(typeName)
                .flatMap(type -> chargeItemRepository.findFirstByTypeIdOrderByIdDesc(type.getId()))
                .map(ChargeItem::getUnitPrice)
                .orElse(BigDecimal.ZERO);
    }

    private void updateRateForType(String typeName, BigDecimal newRate, List<Charge> activeCharges) {
        if (newRate == null) {
            return; // No update requested for this rate
        }

        chargeItemTypeRepository.findByName(typeName).ifPresent(type -> {
            for (Charge charge : activeCharges) {
                // Znajdź istniejące wpisy dla danej szarży i typu
                List<ChargeItem> existingItems = chargeItemRepository.findByChargeId(charge.getId()).stream()
                        .filter(item -> item.getTypeId().equals(type.getId()))
                        // Posortujmy, aby pobrać ostatni (jeżeli jest ich wiele)
                        .sorted((a, b) -> b.getId().compareTo(a.getId()))
                        .toList();

                if (!existingItems.isEmpty()) {
                    ChargeItem latestItem = existingItems.get(0);
                    // Dodaj nowy rekord TYLKO jeśli stawka faktycznie uległa zmianie
                    if (latestItem.getUnitPrice() != null && latestItem.getUnitPrice().compareTo(newRate) != 0) {
                        
                        // Zgodnie z wytycznymi - poprzedni rekord: "musi mieć quantity nie null wtedy dopiero wyliczany jest total"
                        if (latestItem.getQuantity() != null) {
                            latestItem.setTotal(latestItem.getQuantity().multiply(latestItem.getUnitPrice()));
                            chargeItemRepository.save(latestItem);
                        }

                        ChargeItem newItem = ChargeItem.builder()
                                .chargeId(charge.getId())
                                .typeId(type.getId())
                                .unitPrice(newRate)
                                .quantity(null)
                                .total(null)
                                .unit(getUnitForType(typeName))
                                .build();

                        chargeItemRepository.save(newItem);
                    }
                }
            }
        });
    }

    private String getUnitForType(String typeName) {
        return switch (typeName) {
            case "Woda" -> "m3";
            case "Prąd" -> "kWh";
            case "Czynsz" -> "month";
            case "Gaz" -> "m3"; // guessing for Gaz
            default -> "unit";
        };
    }
}
