package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.Charge;
import pl.edu.ur.coopspace_backend.entity.ChargeStatus;

import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Integer> {
    List<Charge> findByLocalId(Integer localId);
    List<Charge> findByCreatedBy(Integer userId);
    List<Charge> findByStatus(ChargeStatus status);
}
