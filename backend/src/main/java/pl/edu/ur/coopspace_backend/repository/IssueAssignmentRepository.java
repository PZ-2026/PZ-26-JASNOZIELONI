package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.IssueAssignment;

import java.util.List;

@Repository
public interface IssueAssignmentRepository extends JpaRepository<IssueAssignment, Integer> {
    List<IssueAssignment> findByIssueId(Integer issueId);
    List<IssueAssignment> findByUserId(Integer userId);
}
