package at.fh_technikum.group09.tourplanner.service.Impl;

import at.fh_technikum.group09.tourplanner.dal.TourDal;
import at.fh_technikum.group09.tourplanner.dal.TourLogDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;
import at.fh_technikum.group09.tourplanner.model.TourLog;
import at.fh_technikum.group09.tourplanner.service.TourLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class JpaTourLogService implements TourLogService {

    private final TourLogDal tourLogDal;
    private final TourDal tourDal;

    public JpaTourLogService(TourLogDal tourLogDal, TourDal tourDal) {
        this.tourLogDal = tourLogDal;
        this.tourDal = tourDal;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<List<TourLog>> findAllByTourId(long tourId) {
        if (!tourDal.existsById(tourId)) {
            return Optional.empty();
        }
        List<TourLog> out = new ArrayList<>();
        for (TourLogEntity le : tourLogDal.findByTourIdOrderByDateTimeAsc(tourId)) {
            out.add(toTourLog(le, tourId));
        }
        return Optional.of(out);
    }

    @Override
    @Transactional(readOnly = true)
    public TourLog getByTourIdAndLogId(long tourId, long logId) {
        return tourLogDal.findByIdAndTourId(logId, tourId).map(le -> toTourLog(le, tourId)).orElse(null);
    }

    @Override
    public TourLog create(long tourId, TourLog log) {
        TourEntity tour = tourDal.findById(tourId).orElse(null);
        if (tour == null) {
            return null;
        }
        TourLogEntity entity = new TourLogEntity();
        entity.setTour(tour);
        copyLogFields(log, entity);
        if (entity.getDateTime() == null) {
            entity.setDateTime(LocalDateTime.now());
        }
        TourLogEntity saved = tourLogDal.save(entity);
        return toTourLog(saved, tourId);
    }

    @Override
    public TourLog update(long tourId, long logId, TourLog updated) {
        TourLogEntity existing = tourLogDal.findByIdAndTourId(logId, tourId).orElse(null);
        if (existing == null) {
            return null;
        }
        copyLogFields(updated, existing);
        if (existing.getDateTime() == null) {
            existing.setDateTime(LocalDateTime.now());
        }
        return toTourLog(tourLogDal.save(existing), tourId);
    }

    @Override
    public boolean delete(long tourId, long logId) {
        return tourLogDal.findByIdAndTourId(logId, tourId).map(entity -> {
            tourLogDal.delete(entity);
            return true;
        }).orElse(false);
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
