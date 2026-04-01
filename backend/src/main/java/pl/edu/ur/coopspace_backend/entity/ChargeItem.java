package pl.edu.ur.coopspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "charge_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "charge_id")
    private Integer chargeId;

    @Column(name = "type_id")
    private Integer typeId;

    @Column
    private BigDecimal quantity;

    @Column
    private String unit;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column
    private BigDecimal total;
}
