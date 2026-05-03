package at.fh_technikum.group09.tourplanner.dal.repository;

import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourRepository extends JpaRepository<TourEntity, Long> {
}
