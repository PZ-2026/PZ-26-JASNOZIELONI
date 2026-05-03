package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.Payment;

import java.util.List;

/**
 * Persistence access for payment records.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    /**
     * Finds payments associated with a charge.
     *
     * @param chargeId charge identifier
     * @return list of payments
     */
    List<Payment> findByChargeId(Integer chargeId);
}
