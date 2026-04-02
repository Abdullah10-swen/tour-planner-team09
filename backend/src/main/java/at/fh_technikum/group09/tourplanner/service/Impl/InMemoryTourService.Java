package at.fh_technikum.group09.tourplanner.service.Impl;

import at.fh_technikum.group09.tourplanner.model.Tour;
import at.fh_technikum.group09.tourplanner.model.TourLog;
import at.fh_technikum.group09.tourplanner.service.TourService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryTourService implements TourService {

    private final Map<Long, Tour> tours = new ConcurrentHashMap<>();
    private final Map<Long, List<TourLog>> logsByTourId = new ConcurrentHashMap<>();

    private final AtomicLong tourIdSequence = new AtomicLong(1);
    private final AtomicLong logIdSequence = new AtomicLong(1);

    @Override
    public List<Tour> getAllTours() {
        return new ArrayList<>(tours.values());
    }

    @Override
    public Tour getTourById(long id) {
        return tours.get(id);
    }

    @Override
    public Tour createTour(Tour tour) {
        long id = tourIdSequence.getAndIncrement();
        tour.setId(id);
        tours.put(id, tour);
        logsByTourId.putIfAbsent(id, new ArrayList<>());
        return tour;
    }

    @Override
    public Tour updateTour(long id, Tour updated) {
        Tour existing = tours.get(id);
        if (existing == null) {
            return null;
        }

        updated.setId(id);
        tours.put(id, updated);
        return updated;
    }

    @Override
    public boolean deleteTour(long id) {
        Tour removed = tours.remove(id);
        logsByTourId.remove(id);
        return removed != null;
    }

    @Override
    public List<TourLog> getLogsForTour(long tourId) {
        List<TourLog> logs = logsByTourId.get(tourId);
        if (logs == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(logs);
    }

    @Override
    public TourLog getLogById(long tourId, long logId) {
        List<TourLog> logs = logsByTourId.get(tourId);
        if (logs == null) {
            return null;
        }
        return logs.stream()
                .filter(l -> l.getId() == logId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public TourLog createLog(long tourId, TourLog log) {
        Tour tour = tours.get(tourId);
        if (tour == null) {
            return null;
        }

        long id = logIdSequence.getAndIncrement();
        log.setId(id);
        log.setTourId(tourId);
        if (log.getDateTime() == null) {
            log.setDateTime(LocalDateTime.now());
        }

        logsByTourId.computeIfAbsent(tourId, k -> new ArrayList<>()).add(log);
        return log;
    }

    @Override
    public TourLog updateLog(long tourId, long logId, TourLog updated) {
        List<TourLog> logs = logsByTourId.get(tourId);
        if (logs == null) {
            return null;
        }

        for (int i = 0; i < logs.size(); i++) {
            TourLog existing = logs.get(i);
            if (existing.getId() == logId) {
                updated.setId(logId);
                updated.setTourId(tourId);
                if (updated.getDateTime() == null) {
                    updated.setDateTime(existing.getDateTime());
                }
                logs.set(i, updated);
                return updated;
            }
        }
        return null;
    }

    @Override
    public boolean deleteLog(long tourId, long logId) {
        List<TourLog> logs = logsByTourId.get(tourId);
        if (logs == null) {
            return false;
        }
        return logs.removeIf(l -> l.getId() == logId);
    }
}
