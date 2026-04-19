package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRatesRequest {
    private BigDecimal rentRate;
    private BigDecimal waterRate;
    private BigDecimal electricityRate;
    private BigDecimal gasRate;
}
