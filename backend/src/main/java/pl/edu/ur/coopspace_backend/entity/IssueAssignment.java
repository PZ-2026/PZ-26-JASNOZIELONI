package pl.edu.ur.coopspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "issue_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "issue_id")
    private Integer issueId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "assigned_by")
    private Integer assignedBy;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
}
