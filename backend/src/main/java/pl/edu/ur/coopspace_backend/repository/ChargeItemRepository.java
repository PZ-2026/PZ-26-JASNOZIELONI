package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.ChargeItem;

import java.util.List;

@Repository
public interface ChargeItemRepository extends JpaRepository<ChargeItem, Integer> {
    List<ChargeItem> findByChargeId(Integer chargeId);
}
