package at.fh_technikum.group09.tourplanner.dal;

import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;

import java.util.List;
import java.util.Optional;

public interface TourLogDal {

    List<TourLogEntity> findByTourIdOrderByDateTimeAsc(long tourId);

    Optional<TourLogEntity> findByIdAndTourId(long logId, long tourId);

    TourLogEntity save(TourLogEntity entity);

    void delete(TourLogEntity entity);
}
