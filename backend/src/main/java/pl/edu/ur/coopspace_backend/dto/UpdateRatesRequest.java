package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request and response model for payment rate values.
 */
@Data
@Builder
@AllArgsConstructor
public class UpdateRatesRequest {
    /**
     * Creates an empty payment rates request.
     */
    public UpdateRatesRequest() {
    }

    private BigDecimal rentRate;
    private BigDecimal waterRate;
    private BigDecimal electricityRate;
    private BigDecimal gasRate;
}
