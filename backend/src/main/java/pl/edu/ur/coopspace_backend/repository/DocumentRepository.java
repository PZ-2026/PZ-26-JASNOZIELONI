package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.Document;

import java.util.List;

/**
 * Persistence access for document records.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    /**
     * Lists documents uploaded by a specific user.
     *
     * @param userId uploader id
     * @return list of documents
     */
    List<Document> findByUploadedBy(Integer userId);
}
