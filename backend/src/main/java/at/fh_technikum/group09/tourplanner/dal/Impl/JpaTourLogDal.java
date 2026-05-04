package at.fh_technikum.group09.tourplanner.dal.Impl;

import at.fh_technikum.group09.tourplanner.dal.TourLogDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;
import at.fh_technikum.group09.tourplanner.dal.exception.TourDalException;
import at.fh_technikum.group09.tourplanner.dal.repository.TourLogRepository;
import org.springframework.dao.DataAccessException;
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
        try {
            return tourLogRepository.findByTour_IdOrderByDateTimeAsc(tourId);
        } catch (DataAccessException ex) {
            throw new TourDalException("Failed to retrieve logs for tourId=" + tourId, ex);
        }
    }

    @Override
    public Optional<TourLogEntity> findByIdAndTourId(long logId, long tourId) {
        try {
            return tourLogRepository.findByIdAndTour_Id(logId, tourId);
        } catch (DataAccessException ex) {
            throw new TourDalException("Failed to find log logId=" + logId + " for tourId=" + tourId, ex);
        }
    }

    @Override
    public TourLogEntity save(TourLogEntity entity) {
        try {
            return tourLogRepository.save(entity);
        } catch (DataAccessException ex) {
            throw new TourDalException("Failed to save tour-log entity", ex);
        }
    }

    @Override
    public void delete(TourLogEntity entity) {
        try {
            tourLogRepository.delete(entity);
        } catch (DataAccessException ex) {
            throw new TourDalException("Failed to delete tour-log entity", ex);
        }
    }
}
