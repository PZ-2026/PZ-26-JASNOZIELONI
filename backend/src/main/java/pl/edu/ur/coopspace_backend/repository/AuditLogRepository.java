package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.AuditLog;

import java.util.List;

/**
 * Persistence access for audit log entries.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    /**
     * Returns audit logs for the specified user.
     *
     * @param userId user identifier
     * @return list of audit log entries
     */
    List<AuditLog> findByUserId(Integer userId);
}
