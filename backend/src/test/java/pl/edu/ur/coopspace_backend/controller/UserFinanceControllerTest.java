package pl.edu.ur.coopspace_backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.ur.coopspace_backend.entity.Charge;
import pl.edu.ur.coopspace_backend.entity.ChargeItem;
import pl.edu.ur.coopspace_backend.entity.ChargeItemType;
import pl.edu.ur.coopspace_backend.entity.Payment;
import pl.edu.ur.coopspace_backend.entity.User;
import pl.edu.ur.coopspace_backend.entity.UserRole;
import pl.edu.ur.coopspace_backend.repository.UserRepository;
import pl.edu.ur.coopspace_backend.service.UserFinanceService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFinanceController - testy jednostkowe kontrolera finansów mieszkańca")
class UserFinanceControllerTest {

    @Mock
    private UserFinanceService userFinanceService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserFinanceController userFinanceController;

    @BeforeEach
    void setUp() {
        lenient().when(authentication.getName()).thenReturn("resident@example.com");
    }

    @Test
    @DisplayName("getCharges powinno zwrócić opłaty mieszkańca")
    void testGetCharges() {
        User resident = User.builder().id(1).email("resident@example.com").role(UserRole.RESIDENT).localId(5).build();
        when(userRepository.findByEmail("resident@example.com")).thenReturn(Optional.of(resident));
        when(userFinanceService.getCharges(5)).thenReturn(List.of(
                Charge.builder().id(10).localId(5).periodStart(LocalDate.of(2026, 1, 1)).build()
        ));

        ResponseEntity<List<Charge>> response = userFinanceController.getCharges(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(userFinanceService).getCharges(5);
    }

    @Test
    @DisplayName("getPayments powinno zwrócić płatności mieszkańca")
    void testGetPayments() {
        User resident = User.builder().id(1).email("resident@example.com").role(UserRole.RESIDENT).localId(5).build();
        when(userRepository.findByEmail("resident@example.com")).thenReturn(Optional.of(resident));
        when(userFinanceService.getPayments(5)).thenReturn(List.of(
                Payment.builder().id(100).chargeId(10).amount(new BigDecimal("25.00")).build()
        ));

        ResponseEntity<List<Payment>> response = userFinanceController.getPayments(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(userFinanceService).getPayments(5);
    }

    @Test
    @DisplayName("getChargeItems powinno zwrócić pozycje opłat mieszkańca")
    void testGetChargeItems() {
        User resident = User.builder().id(1).email("resident@example.com").role(UserRole.RESIDENT).localId(5).build();
        when(userRepository.findByEmail("resident@example.com")).thenReturn(Optional.of(resident));
        when(userFinanceService.getChargeItems(5)).thenReturn(List.of(
                ChargeItem.builder().id(1).chargeId(10).typeId(2).unitPrice(new BigDecimal("10.00")).build()
        ));

        ResponseEntity<List<ChargeItem>> response = userFinanceController.getChargeItems(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(userFinanceService).getChargeItems(5);
    }

    @Test
    @DisplayName("getChargeItemTypes powinno zwrócić słownik typów")
    void testGetChargeItemTypes() {
        User resident = User.builder().id(1).email("resident@example.com").role(UserRole.RESIDENT).localId(5).build();
        when(userRepository.findByEmail("resident@example.com")).thenReturn(Optional.of(resident));
        when(userFinanceService.getChargeItemTypes()).thenReturn(List.of(
                ChargeItemType.builder().id(1).name("Woda").build()
        ));

        ResponseEntity<List<ChargeItemType>> response = userFinanceController.getChargeItemTypes(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(userFinanceService).getChargeItemTypes();
    }

    @Test
    @DisplayName("makePayment powinno przekazać płatność do serwisu")
    void testMakePayment() {
        User resident = User.builder().id(1).email("resident@example.com").role(UserRole.RESIDENT).localId(5).build();
        when(userRepository.findByEmail("resident@example.com")).thenReturn(Optional.of(resident));

        Payment request = Payment.builder().amount(new BigDecimal("25.00")).build();
        Payment saved = Payment.builder().id(200).chargeId(10).amount(new BigDecimal("25.00")).build();
        when(userFinanceService.makePayment(5, request)).thenReturn(saved);

        ResponseEntity<Payment> response = userFinanceController.makePayment(authentication, request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getId());
        verify(userFinanceService).makePayment(5, request);
    }

    @Test
    @DisplayName("brak zalogowanego użytkownika powinien zwrócić 401")
    void testUnauthorizedWhenAuthenticationMissing() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userFinanceController.getCharges(null));

        assertEquals(401, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Użytkownik niezalogowany"));
        verifyNoInteractions(userFinanceService);
    }

    @Test
    @DisplayName("użytkownik bez roli resident powinien dostać 403")
    void testForbiddenForNonResident() {
        User admin = User.builder().id(1).email("resident@example.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("resident@example.com")).thenReturn(Optional.of(admin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userFinanceController.getPayments(authentication));

        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Tylko mieszkaniec ma dostęp do tego endpointu"));
        verifyNoInteractions(userFinanceService);
    }

    @Test
    @DisplayName("mieszkaniec bez przypisanego lokalu powinien dostać 400")
    void testBadRequestWhenResidentHasNoLocal() {
        User residentWithoutLocal = User.builder().id(1).email("resident@example.com").role(UserRole.RESIDENT).localId(null).build();
        when(userRepository.findByEmail("resident@example.com")).thenReturn(Optional.of(residentWithoutLocal));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userFinanceController.getChargeItems(authentication));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Mieszkaniec nie ma przypisanego lokalu"));
        verifyNoInteractions(userFinanceService);
    }
}