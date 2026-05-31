package at.fh_technikum.group09.tourplanner.service;

import at.fh_technikum.group09.tourplanner.model.TourLog;

import java.util.List;

public interface TourLogService {

    List<TourLog> findAllByTourId(long tourId, long userId);

    TourLog getByTourIdAndLogId(long tourId, long logId, long userId);

    TourLog create(long tourId, TourLog log, long userId);

    TourLog update(long tourId, long logId, TourLog updated, long userId);

    void delete(long tourId, long logId, long userId);
}
