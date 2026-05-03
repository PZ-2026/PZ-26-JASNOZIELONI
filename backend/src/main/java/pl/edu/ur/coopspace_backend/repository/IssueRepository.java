package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.Issue;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;

import java.util.List;
import java.util.Optional;

/**
 * Persistence access for issue records.
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Integer> {
    /**
     * Lists issues for a given local.
     *
     * @param localId local identifier
     * @return list of issues
     */
    List<Issue> findByLocalId(Integer localId);

    /**
     * Lists issues created by a specific user.
     *
     * @param userId user identifier
     * @return list of issues
     */
    List<Issue> findByCreatedByUserId(Integer userId);

    /**
     * Lists issues assigned to a given maintainer.
     *
     * @param userId maintainer user id
     * @return list of issues
     */
    List<Issue> findByMainAssigneeId(Integer userId);

    /**
     * Lists issues filtered by status.
     *
     * @param status issue status
     * @return list of issues
     */
    List<Issue> findByStatus(IssueStatus status);

    /**
     * Lists non-deleted issues ordered by creation time descending.
     *
     * @return list of issues
     */
    List<Issue> findByDeletedAtIsNullOrderByCreatedAtDesc();

    /**
     * Lists non-deleted issues created by a user ordered by creation time descending.
     *
     * @param userId creator id
     * @return list of issues
     */
    List<Issue> findByCreatedByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Integer userId);

    /**
     * Lists non-deleted issues assigned to a user ordered by creation time descending.
     *
     * @param userId maintainer id
     * @return list of issues
     */
    List<Issue> findByMainAssigneeIdAndDeletedAtIsNullOrderByCreatedAtDesc(Integer userId);

    /**
     * Lists non-deleted issues assigned to a user with a specific status ordered by creation time descending.
     *
     * @param userId maintainer id
     * @param status issue status
     * @return list of issues
     */
    List<Issue> findByMainAssigneeIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(Integer userId, IssueStatus status);

    /**
     * Finds an issue by id if it is not deleted.
     *
     * @param id issue id
     * @return optional issue
     */
    Optional<Issue> findByIdAndDeletedAtIsNull(Integer id);
}
