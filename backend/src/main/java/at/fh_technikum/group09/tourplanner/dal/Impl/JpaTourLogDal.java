package at.fh_technikum.group09.tourplanner.dal.Impl;

import at.fh_technikum.group09.tourplanner.dal.TourLogDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;
import at.fh_technikum.group09.tourplanner.dal.repository.TourLogRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTourLogDal implements TourLogDal {

    private final TourLogRepository tourLogRepository;

    public JpaTourLogDal(TourLogRepository tourLogRepository) {
        this.tourLogRepository = tourLogRepository;
    }

    @Override
    public List<TourLogEntity> findByTourIdOrderByDateTimeAsc(long tourId) {
        return tourLogRepository.findByTour_IdOrderByDateTimeAsc(tourId);
    }

    @Override
    public Optional<TourLogEntity> findByIdAndTourId(long logId, long tourId) {
        return tourLogRepository.findByIdAndTour_Id(logId, tourId);
    }

    @Override
    public TourLogEntity save(TourLogEntity entity) {
        return tourLogRepository.save(entity);
    }

    @Override
    public void delete(TourLogEntity entity) {
        tourLogRepository.delete(entity);
    }
}
