package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.ChargeItemType;

import java.util.Optional;

@Repository
public interface ChargeItemTypeRepository extends JpaRepository<ChargeItemType, Integer> {
    Optional<ChargeItemType> findByName(String name);
}
