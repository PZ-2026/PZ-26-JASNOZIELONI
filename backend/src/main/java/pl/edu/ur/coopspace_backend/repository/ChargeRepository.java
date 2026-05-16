package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.Charge;
import pl.edu.ur.coopspace_backend.entity.ChargeStatus;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for charge records.
 */
@Repository
public interface ChargeRepository extends JpaRepository<Charge, Integer> {
    /**
     * Finds charges for a local.
     *
     * @param localId local identifier
     * @return list of charges
     */
    List<Charge> findByLocalId(Integer localId);

    /**
     * Finds charges created by a user.
     *
     * @param userId creator id
     * @return list of charges
     */
    List<Charge> findByCreatedBy(Integer userId);

    /**
     * Finds charges by status.
     *
     * @param status charge status
     * @return list of charges
     */
    List<Charge> findByStatus(ChargeStatus status);
    
    @Query("SELECT c FROM Charge c WHERE c.periodEnd IS NULL OR c.periodEnd >= :currentDate")
    /**
     * Finds charges that are currently active (period not ended).
     *
     * @param currentDate cutoff date
     * @return active charges
     */
    List<Charge> findActiveCharges(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT c FROM Charge c WHERE c.periodStart IS NOT NULL AND c.periodStart <= :endDate AND (c.periodEnd IS NULL OR c.periodEnd >= :startDate)")
    /**
     * Finds charges that overlap the given period.
     *
     * @param startDate period start cutoff
     * @param endDate period end cutoff
     * @return charges overlapping the period
     */
    List<Charge> findChargesOverlappingPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
