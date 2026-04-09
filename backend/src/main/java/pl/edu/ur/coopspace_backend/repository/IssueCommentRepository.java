package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.IssueComment;

import java.util.List;

@Repository
public interface IssueCommentRepository extends JpaRepository<IssueComment, Integer> {
    List<IssueComment> findByIssueId(Integer issueId);
    List<IssueComment> findByUserId(Integer userId);
}
