package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.ChargeItemType;

import java.util.Optional;

/**
 * Persistence access for charge item type dictionary entries.
 */
@Repository
public interface ChargeItemTypeRepository extends JpaRepository<ChargeItemType, Integer> {
    /**
     * Finds a charge item type by its name.
     *
     * @param name type name
     * @return optional type
     */
    Optional<ChargeItemType> findByName(String name);
}
