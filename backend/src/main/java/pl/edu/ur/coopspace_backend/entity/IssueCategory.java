package pl.edu.ur.coopspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Dictionary entry for an issue category.
 */
@Entity
@Table(name = "issue_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;
}
