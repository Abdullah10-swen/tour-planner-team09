package at.fh_technikum.group09.tourplanner.dal.repository;

import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourRepository extends JpaRepository<TourEntity, Long> {

    List<TourEntity> findAllByUserId(long userId);

    Optional<TourEntity> findByIdAndUserId(long id, long userId);

    boolean existsByIdAndUserId(long id, long userId);
}
