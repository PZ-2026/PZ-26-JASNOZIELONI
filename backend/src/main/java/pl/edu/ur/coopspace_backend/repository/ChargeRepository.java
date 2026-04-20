package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.Charge;
import pl.edu.ur.coopspace_backend.entity.ChargeStatus;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Integer> {
    List<Charge> findByLocalId(Integer localId);
    List<Charge> findByCreatedBy(Integer userId);
    List<Charge> findByStatus(ChargeStatus status);
    
    @Query("SELECT c FROM Charge c WHERE c.periodEnd IS NULL OR c.periodEnd >= :currentDate")
    List<Charge> findActiveCharges(@Param("currentDate") LocalDate currentDate);
}
