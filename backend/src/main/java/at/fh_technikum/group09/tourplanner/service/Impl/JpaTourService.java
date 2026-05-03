package at.fh_technikum.group09.tourplanner.service.Impl;

import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;
import at.fh_technikum.group09.tourplanner.dal.repository.TourLogRepository;
import at.fh_technikum.group09.tourplanner.dal.repository.TourRepository;
import at.fh_technikum.group09.tourplanner.model.Tour;
import at.fh_technikum.group09.tourplanner.model.TourLog;
import at.fh_technikum.group09.tourplanner.service.TourService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class JpaTourService implements TourService {

    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;

    public JpaTourService(TourRepository tourRepository, TourLogRepository tourLogRepository) {
        this.tourRepository = tourRepository;
        this.tourLogRepository = tourLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tour> getAllTours() {
        List<Tour> result = new ArrayList<>();
        for (TourEntity e : tourRepository.findAll()) {
            result.add(toTour(e));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Tour getTourById(long id) {
        return tourRepository.findById(id).map(this::toTour).orElse(null);
    }

    @Override
    public Tour createTour(Tour tour) {
        TourEntity entity = new TourEntity();
        copyTourFields(tour, entity);
        TourEntity saved = tourRepository.save(entity);
        return toTour(saved);
    }

    @Override
    public Tour updateTour(long id, Tour updated) {
        return tourRepository.findById(id).map(existing -> {
            copyTourFields(updated, existing);
            return toTour(tourRepository.save(existing));
        }).orElse(null);
    }

    @Override
    public boolean deleteTour(long id) {
        if (!tourRepository.existsById(id)) {
            return false;
        }
        tourRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourLog> getLogsForTour(long tourId) {
        if (!tourRepository.existsById(tourId)) {
            return Collections.emptyList();
        }
        List<TourLog> out = new ArrayList<>();
        for (TourLogEntity le : tourLogRepository.findByTour_IdOrderByDateTimeAsc(tourId)) {
            out.add(toTourLog(le, tourId));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public TourLog getLogById(long tourId, long logId) {
        return tourLogRepository.findByIdAndTour_Id(logId, tourId).map(le -> toTourLog(le, tourId)).orElse(null);
    }

    @Override
    public TourLog createLog(long tourId, TourLog log) {
        TourEntity tour = tourRepository.findById(tourId).orElse(null);
        if (tour == null) {
            return null;
        }
        TourLogEntity entity = new TourLogEntity();
        entity.setTour(tour);
        copyLogFields(log, entity);
        if (entity.getDateTime() == null) {
            entity.setDateTime(LocalDateTime.now());
        }
        TourLogEntity saved = tourLogRepository.save(entity);
        return toTourLog(saved, tourId);
    }

    @Override
    public TourLog updateLog(long tourId, long logId, TourLog updated) {
        TourLogEntity existing = tourLogRepository.findByIdAndTour_Id(logId, tourId).orElse(null);
        if (existing == null) {
            return null;
        }
        copyLogFields(updated, existing);
        if (existing.getDateTime() == null) {
            existing.setDateTime(LocalDateTime.now());
        }
        return toTourLog(tourLogRepository.save(existing), tourId);
    }

    @Override
    public boolean deleteLog(long tourId, long logId) {
        return tourLogRepository.findByIdAndTour_Id(logId, tourId).map(entity -> {
            tourLogRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    private void copyTourFields(Tour from, TourEntity to) {
        to.setName(from.getName());
        to.setDescription(from.getDescription());
        to.setFromLocation(from.getFromLocation());
        to.setToLocation(from.getToLocation());
        to.setTransportType(from.getTransportType());
        to.setDistance(from.getDistance());
        to.setEstimatedTime(from.getEstimatedTime());
        to.setImageUrl(from.getImageUrl());
        to.setRouteInfo(from.getRouteInfo());
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

    private Tour toTour(TourEntity e) {
        Tour t = new Tour();
        t.setId(e.getId());
        t.setName(e.getName());
        t.setDescription(e.getDescription());
        t.setFromLocation(e.getFromLocation());
        t.setToLocation(e.getToLocation());
        t.setTransportType(e.getTransportType());
        t.setDistance(e.getDistance());
        t.setEstimatedTime(e.getEstimatedTime());
        t.setImageUrl(e.getImageUrl());
        t.setRouteInfo(e.getRouteInfo());
        return t;
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
