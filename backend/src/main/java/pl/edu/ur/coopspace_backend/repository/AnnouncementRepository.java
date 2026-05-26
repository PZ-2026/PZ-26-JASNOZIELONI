package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.Announcement;

import java.util.List;

/**
 * Persistence access for announcement records.
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {
    /**
     * Finds announcements created by the given user id.
     *
     * @param userId creator user id
     * @return announcements created by user
     */
    List<Announcement> findByCreatedBy(Integer userId);
}
