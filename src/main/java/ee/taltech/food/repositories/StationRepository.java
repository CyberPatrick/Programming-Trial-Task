package ee.taltech.food.repositories;

import ee.taltech.food.entities.StationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<StationEntity, Integer> {
}
