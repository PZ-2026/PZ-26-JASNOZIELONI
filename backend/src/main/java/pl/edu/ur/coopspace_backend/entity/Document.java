package pl.edu.ur.coopspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * File metadata for uploaded documents.
 */
@Entity
@Table(name = "document")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Document {

    /**
     * Creates an empty document entity.
     */
    public Document() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String title;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "uploaded_by")
    private Integer uploadedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
