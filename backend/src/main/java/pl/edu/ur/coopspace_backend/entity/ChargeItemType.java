package pl.edu.ur.coopspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Reference type describing a billable charge item.
 */
@Entity
@Table(name = "charge_item_type")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChargeItemType {

    /**
     * Creates an empty charge item type entity.
     */
    public ChargeItemType() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;
}
