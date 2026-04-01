package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.IssueStatusHistory;

import java.util.List;

@Repository
public interface IssueStatusHistoryRepository extends JpaRepository<IssueStatusHistory, Integer> {
    List<IssueStatusHistory> findByIssueId(Integer issueId);
}
