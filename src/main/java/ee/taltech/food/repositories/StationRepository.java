package ee.taltech.food.repositories;

import ee.taltech.food.entities.StationEntity;
import ee.taltech.food.utils.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface StationRepository extends JpaRepository<StationEntity, Integer> {
    Optional<StationEntity> findFirstByNameContainingIgnoreCaseOrderByTimestampDesc(City name);

    Optional<StationEntity> findFirstByNameContainingIgnoreCaseAndTimestampBeforeOrderByTimestampDesc(City name, Date timestamp);
}
