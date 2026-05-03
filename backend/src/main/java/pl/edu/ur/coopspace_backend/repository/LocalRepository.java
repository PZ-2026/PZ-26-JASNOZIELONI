package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.Local;

import java.util.List;

/**
 * Persistence access for local records.
 */
@Repository
public interface LocalRepository extends JpaRepository<Local, Integer> {
    /**
     * Lists locals for a given building.
     *
     * @param buildingId building id
     * @return locals in the building
     */
    List<Local> findByBuildingId(Integer buildingId);

    /**
     * Lists locals that are not deleted.
     *
     * @return active locals
     */
    List<Local> findByDeletedAtIsNull();
}
