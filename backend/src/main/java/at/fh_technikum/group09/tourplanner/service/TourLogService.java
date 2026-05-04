package at.fh_technikum.group09.tourplanner.service;

import at.fh_technikum.group09.tourplanner.model.TourLog;

import java.util.List;

public interface TourLogService {

    List<TourLog> findAllByTourId(long tourId);

    TourLog getByTourIdAndLogId(long tourId, long logId);

    TourLog create(long tourId, TourLog log);

    TourLog update(long tourId, long logId, TourLog updated);

    void delete(long tourId, long logId);
}
