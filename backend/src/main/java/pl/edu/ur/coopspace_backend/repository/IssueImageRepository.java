package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.IssueImage;

import java.util.List;

/**
 * Persistence access for issue image metadata.
 */
@Repository
public interface IssueImageRepository extends JpaRepository<IssueImage, Integer> {
    /**
     * Lists image metadata attached to an issue.
     *
     * @param issueId issue identifier
     * @return list of issue images
     */
    List<IssueImage> findByIssueId(Integer issueId);
}
