package pl.edu.ur.coopspace_backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import pl.edu.ur.coopspace_backend.dto.UpdateRatesRequest;
import pl.edu.ur.coopspace_backend.service.PaymentService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// konfiguracja testu
@ExtendWith(MockitoExtension.class) // uzycie Mockito do tworzenia mockow
@DisplayName("PaymentController - testy jednostkowe")
class PaymentControllerTest {

    @Mock // to tworzy sztuczny obiekt paymentService
    private PaymentService paymentService;

    @InjectMocks // tworzy testowany obiekt paymentController i wstrzykuje do niego mocki
    private PaymentController paymentController;

    /**
     * Sprawdzane jest czy kontroler przyjmuje dane, wywoluje odpowiedni serwis i zwraca kod odpowiedzi HTTP 200.
     */
    @Test
    @DisplayName("updateRates deleguje do PaymentService i zwraca 200")
    void testUpdateRatesDelegatesToService() {
        // Given - przygotowanie danych, utworzenie danych do symulacji requesta HTTP
        UpdateRatesRequest request = UpdateRatesRequest.builder()
                .rentRate(new BigDecimal("1000.00"))
                .waterRate(new BigDecimal("10.50"))
                .electricityRate(new BigDecimal("1.25"))
                .gasRate(new BigDecimal("4.80"))
                .build();

        // When - wywolaniue metody, bez serwera/http
        ResponseEntity<Void> response = paymentController.updateRates(request);

        // Then - sprawdzanie asercji, czyli czy dostajemy spodziewany wynik
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(paymentService).updateRates(same(request)); // czy kontroler na pewno wywolal metode serwisu z tym obiektem
    }

    @Test
    @DisplayName("getCurrentRates zwraca stawki z PaymentService")
    void testGetCurrentRatesReturnsServiceResult() {
        // Given
        UpdateRatesRequest rates = UpdateRatesRequest.builder()
                .rentRate(new BigDecimal("900.00"))
                .waterRate(new BigDecimal("9.50"))
                .electricityRate(new BigDecimal("1.10"))
                .gasRate(new BigDecimal("4.20"))
                .build();
        when(paymentService.getCurrentRates()).thenReturn(rates);

        // When
        ResponseEntity<UpdateRatesRequest> response = paymentController.getCurrentRates();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(new BigDecimal("900.00"), response.getBody().getRentRate());
        assertEquals(new BigDecimal("9.50"), response.getBody().getWaterRate());
        assertEquals(new BigDecimal("1.10"), response.getBody().getElectricityRate());
        assertEquals(new BigDecimal("4.20"), response.getBody().getGasRate());
        verify(paymentService).getCurrentRates();
    }
}