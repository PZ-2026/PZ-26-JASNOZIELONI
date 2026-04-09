package pl.edu.ur.coopspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;

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
