package pl.edu.ur.coopspace_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.coopspace_backend.entity.Local;

import java.util.List;

@Repository
public interface LocalRepository extends JpaRepository<Local, Integer> {
    List<Local> findByBuildingId(Integer buildingId);
}
