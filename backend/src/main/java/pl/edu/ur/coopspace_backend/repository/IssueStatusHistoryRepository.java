package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.IssueStatusHistory;

import java.util.List;

/**
 * Persistence access for issue status history entries.
 */
@Repository
public interface IssueStatusHistoryRepository extends JpaRepository<IssueStatusHistory, Integer> {
    /**
     * Lists status history entries for an issue.
     *
     * @param issueId issue id
     * @return history entries
     */
    List<IssueStatusHistory> findByIssueId(Integer issueId);
}
