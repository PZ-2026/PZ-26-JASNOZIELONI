package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.IssueAssignment;

import java.util.List;

/**
 * Persistence access for issue assignments.
 */
@Repository
public interface IssueAssignmentRepository extends JpaRepository<IssueAssignment, Integer> {
    /**
     * Lists assignments for a given issue.
     *
     * @param issueId issue id
     * @return list of assignments
     */
    List<IssueAssignment> findByIssueId(Integer issueId);

    /**
     * Lists assignments for a given user.
     *
     * @param userId user id
     * @return list of assignments
     */
    List<IssueAssignment> findByUserId(Integer userId);
}
