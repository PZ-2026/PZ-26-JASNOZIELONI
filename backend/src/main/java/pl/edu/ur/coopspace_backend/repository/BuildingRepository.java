package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.Building;

/**
 * Persistence access for building records.
 */
@Repository
public interface BuildingRepository extends JpaRepository<Building, Integer> {
}
