package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.Issue;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Integer> {
    List<Issue> findByLocalId(Integer localId);
    List<Issue> findByCreatedByUserId(Integer userId);
    List<Issue> findByMainAssigneeId(Integer userId);
    List<Issue> findByStatus(IssueStatus status);

    List<Issue> findByDeletedAtIsNullOrderByCreatedAtDesc();
    List<Issue> findByCreatedByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Integer userId);
    List<Issue> findByMainAssigneeIdAndDeletedAtIsNullOrderByCreatedAtDesc(Integer userId);
    Optional<Issue> findByIdAndDeletedAtIsNull(Integer id);
}
