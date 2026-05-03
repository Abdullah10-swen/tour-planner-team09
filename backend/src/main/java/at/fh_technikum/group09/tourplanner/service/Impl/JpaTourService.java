package at.fh_technikum.group09.tourplanner.service.Impl;

import at.fh_technikum.group09.tourplanner.dal.TourDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
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

    public JpaTourService(TourDal tourDal) {
        this.tourDal = tourDal;
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
        TourEntity saved = tourDal.save(entity);
        return toTour(saved);
    }

    @Override
    public Tour updateTour(long id, Tour updated) {
        return tourDal.findById(id).map(existing -> {
            copyTourFields(updated, existing);
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
}
