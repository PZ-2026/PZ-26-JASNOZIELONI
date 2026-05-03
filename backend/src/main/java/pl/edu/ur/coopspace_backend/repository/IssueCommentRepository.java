package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.IssueComment;

import java.util.List;

/**
 * Persistence access for issue comments.
 */
@Repository
public interface IssueCommentRepository extends JpaRepository<IssueComment, Integer> {
    /**
     * Lists comments attached to an issue.
     *
     * @param issueId issue id
     * @return list of comments
     */
    List<IssueComment> findByIssueId(Integer issueId);

    /**
     * Lists comments created by a user.
     *
     * @param userId user id
     * @return list of comments
     */
    List<IssueComment> findByUserId(Integer userId);

    /**
     * Finds a comment for a given issue by the specified user.
     *
     * @param issueId issue id
     * @param userId user id
     * @return optional comment
     */
    java.util.Optional<IssueComment> findByIssueIdAndUserId(Integer issueId, Integer userId);
}
