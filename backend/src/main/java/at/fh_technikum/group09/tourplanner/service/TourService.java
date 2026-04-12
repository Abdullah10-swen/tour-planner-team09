package at.fh_technikum.group09.tourplanner.service;

import at.fh_technikum.group09.tourplanner.model.Tour;
import at.fh_technikum.group09.tourplanner.model.TourLog;

import java.util.List;

public interface TourService {

    List<Tour> getAllTours();

    Tour getTourById(long id);

    Tour createTour(Tour tour);

    Tour updateTour(long id, Tour updated);

    boolean deleteTour(long id);

    List<TourLog> getLogsForTour(long tourId);

    TourLog getLogById(long tourId, long logId);

    TourLog createLog(long tourId, TourLog log);

    TourLog updateLog(long tourId, long logId, TourLog updated);

    boolean deleteLog(long tourId, long logId);
}
