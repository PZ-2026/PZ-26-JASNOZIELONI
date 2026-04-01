package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.IssueImage;

import java.util.List;

@Repository
public interface IssueImageRepository extends JpaRepository<IssueImage, Integer> {
    List<IssueImage> findByIssueId(Integer issueId);
}
