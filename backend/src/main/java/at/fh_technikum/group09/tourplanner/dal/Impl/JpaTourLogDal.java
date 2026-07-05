package at.fh_technikum.group09.tourplanner.dal.Impl;

import at.fh_technikum.group09.tourplanner.dal.TourLogDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;
import at.fh_technikum.group09.tourplanner.dal.exception.TourDalException;
import at.fh_technikum.group09.tourplanner.dal.repository.TourLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTourLogDal implements TourLogDal {

    private static final Logger log = LoggerFactory.getLogger(JpaTourLogDal.class);

    private final TourLogRepository tourLogRepository;

    public JpaTourLogDal(TourLogRepository tourLogRepository) {
        this.tourLogRepository = tourLogRepository;
    }

    @Override
    public List<TourLogEntity> findByTourIdOrderByDateTimeAsc(long tourId) {
        try {
            return tourLogRepository.findByTour_IdOrderByDateTimeAsc(tourId);
        } catch (DataAccessException ex) {
            log.error("DB error retrieving logs for tourId={}", tourId, ex);
            throw new TourDalException("Failed to retrieve logs for tourId=" + tourId, ex);
        }
    }

    @Override
    public Optional<TourLogEntity> findByIdAndTourId(long logId, long tourId) {
        try {
            return tourLogRepository.findByIdAndTour_Id(logId, tourId);
        } catch (DataAccessException ex) {
            log.error("DB error finding log logId={} for tourId={}", logId, tourId, ex);
            throw new TourDalException("Failed to find log logId=" + logId + " for tourId=" + tourId, ex);
        }
    }

    @Override
    public TourLogEntity save(TourLogEntity entity) {
        try {
            TourLogEntity saved = tourLogRepository.save(entity);
            log.debug("TourLog entity saved id={}", saved.getId());
            return saved;
        } catch (DataAccessException ex) {
            log.error("DB error saving tour-log entity id={}", entity.getId(), ex);
            throw new TourDalException("Failed to save tour-log entity", ex);
        }
    }

    @Override
    public void delete(TourLogEntity entity) {
        try {
            tourLogRepository.delete(entity);
            log.debug("TourLog entity deleted id={}", entity.getId());
        } catch (DataAccessException ex) {
            log.error("DB error deleting tour-log entity id={}", entity.getId(), ex);
            throw new TourDalException("Failed to delete tour-log entity", ex);
        }
    }
}
