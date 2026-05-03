package pl.edu.ur.coopspace_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.entity.Charge;
import pl.edu.ur.coopspace_backend.entity.ChargeItem;
import pl.edu.ur.coopspace_backend.entity.ChargeItemType;
import pl.edu.ur.coopspace_backend.entity.Payment;
import pl.edu.ur.coopspace_backend.repository.ChargeItemRepository;
import pl.edu.ur.coopspace_backend.repository.ChargeItemTypeRepository;
import pl.edu.ur.coopspace_backend.repository.ChargeRepository;
import pl.edu.ur.coopspace_backend.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFinanceService - testy jednostkowe serwisu finansowego mieszkańca")
class UserFinanceServiceTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ChargeItemRepository chargeItemRepository;

    @Mock
    private ChargeItemTypeRepository chargeItemTypeRepository;

    @InjectMocks
    private UserFinanceService userFinanceService;

    private Charge firstCharge;
    private Charge secondCharge;

    @BeforeEach
    void setUp() {
        firstCharge = Charge.builder()
                .id(10)
                .localId(5)
                .periodStart(LocalDate.of(2026, 1, 1))
                .periodEnd(LocalDate.of(2026, 1, 31))
                .totalAmount(new BigDecimal("100.00"))
                .build();

        secondCharge = Charge.builder()
                .id(11)
                .localId(5)
                .periodStart(LocalDate.of(2026, 2, 1))
                .periodEnd(LocalDate.of(2026, 2, 28))
                .totalAmount(new BigDecimal("200.00"))
                .build();
    }

    @Test
    @DisplayName("getCharges powinno zwrócić opłaty dla lokalu")
    void testGetCharges() {
        when(chargeRepository.findByLocalId(5)).thenReturn(List.of(firstCharge, secondCharge));

        List<Charge> result = userFinanceService.getCharges(5);

        assertEquals(2, result.size());
        assertEquals(10, result.get(0).getId());
        verify(chargeRepository).findByLocalId(5);
    }

    @Test
    @DisplayName("getPayments powinno scalić płatności z wszystkich opłat lokalu")
    void testGetPayments() {
        when(chargeRepository.findByLocalId(5)).thenReturn(List.of(firstCharge, secondCharge));

        Payment payment1 = Payment.builder().id(100).chargeId(10).amount(new BigDecimal("25.00")).build();
        Payment payment2 = Payment.builder().id(101).chargeId(11).amount(new BigDecimal("50.00")).build();

        when(paymentRepository.findByChargeId(10)).thenReturn(List.of(payment1));
        when(paymentRepository.findByChargeId(11)).thenReturn(List.of(payment2));

        List<Payment> result = userFinanceService.getPayments(5);

        assertEquals(2, result.size());
        verify(paymentRepository).findByChargeId(10);
        verify(paymentRepository).findByChargeId(11);
    }

    @Test
    @DisplayName("getChargeItems powinno scalić pozycje opłat dla lokalu")
    void testGetChargeItems() {
        when(chargeRepository.findByLocalId(5)).thenReturn(List.of(firstCharge, secondCharge));

        ChargeItem item1 = ChargeItem.builder().id(1).chargeId(10).typeId(1).unitPrice(new BigDecimal("10.00")).build();
        ChargeItem item2 = ChargeItem.builder().id(2).chargeId(11).typeId(2).unitPrice(new BigDecimal("20.00")).build();

        when(chargeItemRepository.findByChargeId(10)).thenReturn(List.of(item1));
        when(chargeItemRepository.findByChargeId(11)).thenReturn(List.of(item2));

        List<ChargeItem> result = userFinanceService.getChargeItems(5);

        assertEquals(2, result.size());
        verify(chargeItemRepository).findByChargeId(10);
        verify(chargeItemRepository).findByChargeId(11);
    }

    @Test
    @DisplayName("getChargeItemTypes powinno zwrócić słownik typów")
    void testGetChargeItemTypes() {
        when(chargeItemTypeRepository.findAll()).thenReturn(List.of(
                ChargeItemType.builder().id(1).name("Woda").build(),
                ChargeItemType.builder().id(2).name("Prąd").build()
        ));

        List<ChargeItemType> result = userFinanceService.getChargeItemTypes();

        assertEquals(2, result.size());
        verify(chargeItemTypeRepository).findAll();
    }

    @Test
    @DisplayName("makePayment bez chargeId powinno wybrać najstarszą nieopłaconą opłatę")
    void testMakePaymentSelectsFirstUnpaidCharge() {
        when(chargeRepository.findByLocalId(5)).thenReturn(new ArrayList<>(List.of(secondCharge, firstCharge)));
        when(paymentRepository.findByChargeId(10)).thenReturn(List.of(
                Payment.builder().id(1).chargeId(10).amount(new BigDecimal("100.00")).build()
        ));
        when(paymentRepository.findByChargeId(11)).thenReturn(List.of(
                Payment.builder().id(2).chargeId(11).amount(new BigDecimal("50.00")).build()
        ));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment request = Payment.builder().amount(new BigDecimal("25.00")).build();

        Payment result = userFinanceService.makePayment(5, request);

        assertNotNull(result);
        assertEquals(11, result.getChargeId());
        assertEquals(new BigDecimal("25.00"), result.getAmount());
        verify(paymentRepository).save(argThat(payment -> payment.getChargeId().equals(11)));
    }

    @Test
    @DisplayName("makePayment z pustą listą opłat powinno zwrócić 400")
    void testMakePaymentNoChargesThrows() {
        when(chargeRepository.findByLocalId(5)).thenReturn(new ArrayList<>());

        Payment request = Payment.builder().amount(new BigDecimal("25.00")).build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userFinanceService.makePayment(5, request));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Brak opłat do zapłaty"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("makePayment z kwotą <= 0 powinno zwrócić 400")
    void testMakePaymentInvalidAmountThrows() {
        Payment request = Payment.builder().amount(BigDecimal.ZERO).build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userFinanceService.makePayment(5, request));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Kwota musi być większa od zera"));
        verifyNoInteractions(chargeRepository, paymentRepository, chargeItemRepository, chargeItemTypeRepository);
    }

    @Test
    @DisplayName("makePayment z nieistniejącą opłatą powinno zwrócić 404")
    void testMakePaymentChargeNotFoundThrows() {
        Payment request = Payment.builder().chargeId(99).amount(new BigDecimal("25.00")).build();
        when(chargeRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userFinanceService.makePayment(5, request));

        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Opłata nie istnieje"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("makePayment z opłatą z innego lokalu powinno zwrócić 403")
    void testMakePaymentChargeFromOtherLocalThrows() {
        Charge otherLocalCharge = Charge.builder().id(99).localId(7).periodStart(LocalDate.now()).build();
        Payment request = Payment.builder().chargeId(99).amount(new BigDecimal("25.00")).build();
        when(chargeRepository.findById(99)).thenReturn(Optional.of(otherLocalCharge));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userFinanceService.makePayment(5, request));

        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Brak dostępu do tej opłaty"));
        verify(paymentRepository, never()).save(any());
    }
}