package pl.edu.ur.coopspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Metadata for an image attached to an issue.
 */
@Entity
@Table(name = "issue_image")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class IssueImage {

    /**
     * Creates an empty issue image entity.
     */
    public IssueImage() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "issue_id")
    private Integer issueId;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
