package pl.edu.ur.coopspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Announcement published to all users.
 */
@Entity
@Table(name = "announcement")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Announcement {

    /**
     * Creates an empty announcement entity.
     */
    public Announcement() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
