package at.fh_technikum.group09.tourplanner.service.Impl;

import at.fh_technikum.group09.tourplanner.dal.TourDal;
import at.fh_technikum.group09.tourplanner.dal.TourLogDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;
import at.fh_technikum.group09.tourplanner.integration.openroute.OpenRouteTourRouteEnrichment;
import at.fh_technikum.group09.tourplanner.model.Tour;
import at.fh_technikum.group09.tourplanner.service.TourService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class JpaTourService implements TourService {

    private final TourDal tourDal;
    private final TourLogDal tourLogDal;
    private final OpenRouteTourRouteEnrichment routeEnrichment;

    public JpaTourService(TourDal tourDal, TourLogDal tourLogDal, OpenRouteTourRouteEnrichment routeEnrichment) {
        this.tourDal = tourDal;
        this.tourLogDal = tourLogDal;
        this.routeEnrichment = routeEnrichment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tour> getAllTours() {
        List<Tour> result = new ArrayList<>();
        for (TourEntity e : tourDal.findAll()) {
            result.add(toTour(e));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Tour getTourById(long id) {
        return tourDal.findById(id).map(this::toTour).orElse(null);
    }

    @Override
    public Tour createTour(Tour tour) {
        TourEntity entity = new TourEntity();
        copyTourFields(tour, entity);
        routeEnrichment.enrichIfPossible(entity);
        TourEntity saved = tourDal.save(entity);
        return toTour(saved);
    }

    @Override
    public Tour updateTour(long id, Tour updated) {
        return tourDal.findById(id).map(existing -> {
            copyTourFields(updated, existing);
            routeEnrichment.enrichIfPossible(existing);
            return toTour(tourDal.save(existing));
        }).orElse(null);
    }

    @Override
    public boolean deleteTour(long id) {
        if (!tourDal.existsById(id)) {
            return false;
        }
        tourDal.deleteById(id);
        return true;
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

    private Tour toTour(TourEntity e) {
        List<TourLogEntity> logs = tourLogDal.findByTourIdOrderByDateTimeAsc(e.getId());

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
        t.setPopularity(logs.size());
        t.setChildFriendliness(computeChildFriendliness(logs));
        return t;
    }

    /**
     * Derives a child-friendliness score in [0.0, 1.0] from the tour logs.
     *
     * <p>Three factors are averaged, each normalised to [0, 1] where 1 = best:
     * <ul>
     *   <li>difficulty  (scale 1–3; 1=easy → 1.0, 3=hard → 0.0)</li>
     *   <li>total time  (hours; 0 h → 1.0, ≥ 8 h → 0.0)</li>
     *   <li>total distance (km; 0 km → 1.0, ≥ 30 km → 0.0)</li>
     * </ul>
     * Returns 0.0 when no logs are available.
     */
    private double computeChildFriendliness(List<TourLogEntity> logs) {
        if (logs.isEmpty()) {
            return 0.0;
        }

        double avgDifficulty = logs.stream().mapToInt(TourLogEntity::getDifficulty).average().orElse(1);
        double avgTime       = logs.stream().mapToDouble(TourLogEntity::getTotalTime).average().orElse(0);
        double avgDistance   = logs.stream().mapToDouble(TourLogEntity::getTotalDistance).average().orElse(0);

        double diffScore = clamp(1.0 - (avgDifficulty - 1.0) / 2.0);
        double timeScore = clamp(1.0 - avgTime / 8.0);
        double distScore = clamp(1.0 - avgDistance / 30.0);

        double raw = (diffScore + timeScore + distScore) / 3.0;
        return Math.round(raw * 100.0) / 100.0;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
