package pl.edu.ur.coopspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a residential building.
 */
@Entity
@Table(name = "building")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Building {

    /**
     * Creates an empty building entity.
     */
    public Building() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private String address;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
