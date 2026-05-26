package pl.edu.ur.coopspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payment record associated with a charge.
 */
@Entity
@Table(name = "payment")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Payment {

    /**
     * Creates an empty payment entity.
     */
    public Payment() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "charge_id")
    private Integer chargeId;

    @Column
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
