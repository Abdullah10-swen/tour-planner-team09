package at.fh_technikum.group09.tourplanner.service.Impl;

import at.fh_technikum.group09.tourplanner.dal.TourDal;
import at.fh_technikum.group09.tourplanner.dal.TourLogDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;
import at.fh_technikum.group09.tourplanner.dal.exception.TourDalException;
import at.fh_technikum.group09.tourplanner.model.TourLog;
import at.fh_technikum.group09.tourplanner.service.TourLogService;
import at.fh_technikum.group09.tourplanner.service.exception.TourLogNotFoundException;
import at.fh_technikum.group09.tourplanner.service.exception.TourNotFoundException;
import at.fh_technikum.group09.tourplanner.service.exception.TourServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class JpaTourLogService implements TourLogService {

    private static final Logger log = LoggerFactory.getLogger(JpaTourLogService.class);

    private final TourLogDal tourLogDal;
    private final TourDal tourDal;

    public JpaTourLogService(TourLogDal tourLogDal, TourDal tourDal) {
        this.tourLogDal = tourLogDal;
        this.tourDal = tourDal;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourLog> findAllByTourId(long tourId, long userId) {
        try {
            verifyTourOwnership(tourId, userId);
            List<TourLog> out = new ArrayList<>();
            for (TourLogEntity le : tourLogDal.findByTourIdOrderByDateTimeAsc(tourId)) {
                out.add(toTourLog(le, tourId));
            }
            log.debug("findAllByTourId tourId={} userId={} count={}", tourId, userId, out.size());
            return out;
        } catch (TourDalException ex) {
            log.error("Failed to retrieve logs for tourId={} userId={}", tourId, userId, ex);
            throw new TourServiceException("Failed to retrieve logs for tourId=" + tourId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TourLog getByTourIdAndLogId(long tourId, long logId, long userId) {
        try {
            verifyTourOwnership(tourId, userId);
            return tourLogDal.findByIdAndTourId(logId, tourId)
                    .map(le -> toTourLog(le, tourId))
                    .orElseThrow(() -> new TourLogNotFoundException(logId, tourId));
        } catch (TourDalException ex) {
            log.error("Failed to retrieve logId={} for tourId={} userId={}", logId, tourId, userId, ex);
            throw new TourServiceException("Failed to retrieve logId=" + logId + " for tourId=" + tourId, ex);
        }
    }

    @Override
    public TourLog create(long tourId, TourLog tourLog, long userId) {
        try {
            TourEntity tour = tourDal.findByIdAndUserId(tourId, userId)
                    .orElseThrow(() -> new TourNotFoundException(tourId));
            TourLogEntity entity = new TourLogEntity();
            entity.setTour(tour);
            copyLogFields(tourLog, entity);
            if (entity.getDateTime() == null) {
                entity.setDateTime(LocalDateTime.now());
            }
            TourLog created = toTourLog(tourLogDal.save(entity), tourId);
            log.info("TourLog created id={} tourId={} userId={}", created.getId(), tourId, userId);
            return created;
        } catch (TourDalException ex) {
            log.error("Failed to create log for tourId={} userId={}", tourId, userId, ex);
            throw new TourServiceException("Failed to create log for tourId=" + tourId, ex);
        }
    }

    @Override
    public TourLog update(long tourId, long logId, TourLog updated, long userId) {
        try {
            verifyTourOwnership(tourId, userId);
            TourLogEntity existing = tourLogDal.findByIdAndTourId(logId, tourId)
                    .orElseThrow(() -> new TourLogNotFoundException(logId, tourId));
            copyLogFields(updated, existing);
            if (existing.getDateTime() == null) {
                existing.setDateTime(LocalDateTime.now());
            }
            TourLog result = toTourLog(tourLogDal.save(existing), tourId);
            log.info("TourLog updated id={} tourId={} userId={}", logId, tourId, userId);
            return result;
        } catch (TourDalException ex) {
            log.error("Failed to update logId={} for tourId={} userId={}", logId, tourId, userId, ex);
            throw new TourServiceException("Failed to update logId=" + logId + " for tourId=" + tourId, ex);
        }
    }

    @Override
    public void delete(long tourId, long logId, long userId) {
        try {
            verifyTourOwnership(tourId, userId);
            TourLogEntity entity = tourLogDal.findByIdAndTourId(logId, tourId)
                    .orElseThrow(() -> new TourLogNotFoundException(logId, tourId));
            tourLogDal.delete(entity);
            log.info("TourLog deleted id={} tourId={} userId={}", logId, tourId, userId);
        } catch (TourDalException ex) {
            log.error("Failed to delete logId={} for tourId={} userId={}", logId, tourId, userId, ex);
            throw new TourServiceException("Failed to delete logId=" + logId + " for tourId=" + tourId, ex);
        }
    }

    private void verifyTourOwnership(long tourId, long userId) {
        if (!tourDal.existsByIdAndUserId(tourId, userId)) {
            throw new TourNotFoundException(tourId);
        }
    }

    private void copyLogFields(TourLog from, TourLogEntity to) {
        if (from.getDateTime() != null) {
            to.setDateTime(from.getDateTime());
        }
        to.setComment(from.getComment());
        to.setDifficulty(from.getDifficulty());
        to.setTotalDistance(from.getTotalDistance());
        to.setTotalTime(from.getTotalTime());
        to.setRating(from.getRating());
    }

    private TourLog toTourLog(TourLogEntity e, long tourId) {
        TourLog l = new TourLog();
        l.setId(e.getId());
        l.setTourId(tourId);
        l.setDateTime(e.getDateTime());
        l.setComment(e.getComment());
        l.setDifficulty(e.getDifficulty());
        l.setTotalDistance(e.getTotalDistance());
        l.setTotalTime(e.getTotalTime());
        l.setRating(e.getRating());
        return l;
    }
}
