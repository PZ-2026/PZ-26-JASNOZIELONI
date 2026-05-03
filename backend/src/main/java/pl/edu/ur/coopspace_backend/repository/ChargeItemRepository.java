package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.ChargeItem;

import java.util.List;

import java.util.Optional;

/**
 * Persistence access for charge item records.
 */
@Repository
public interface ChargeItemRepository extends JpaRepository<ChargeItem, Integer> {
    /**
     * Lists charge items for a specific charge.
     *
     * @param chargeId charge identifier
     * @return list of items
     */
    List<ChargeItem> findByChargeId(Integer chargeId);

    /**
     * Finds the most recent charge item for a given type.
     *
     * @param typeId type identifier
     * @return optional latest item
     */
    Optional<ChargeItem> findFirstByTypeIdOrderByIdDesc(Integer typeId);
}
