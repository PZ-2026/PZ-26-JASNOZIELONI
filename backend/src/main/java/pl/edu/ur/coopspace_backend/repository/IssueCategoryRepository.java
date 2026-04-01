package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.IssueCategory;

@Repository
public interface IssueCategoryRepository extends JpaRepository<IssueCategory, Integer> {
}
