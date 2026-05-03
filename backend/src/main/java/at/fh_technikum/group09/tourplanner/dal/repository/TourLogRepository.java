package at.fh_technikum.group09.tourplanner.dal.repository;

import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourLogRepository extends JpaRepository<TourLogEntity, Long> {

    List<TourLogEntity> findByTour_IdOrderByDateTimeAsc(Long tourId);

    Optional<TourLogEntity> findByIdAndTour_Id(Long id, Long tourId);
}
