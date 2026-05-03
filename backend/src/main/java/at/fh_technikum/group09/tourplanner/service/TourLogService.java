package at.fh_technikum.group09.tourplanner.service;

import at.fh_technikum.group09.tourplanner.model.TourLog;

import java.util.List;
import java.util.Optional;

public interface TourLogService {

    Optional<List<TourLog>> findAllByTourId(long tourId);

    TourLog getByTourIdAndLogId(long tourId, long logId);

    TourLog create(long tourId, TourLog log);

    TourLog update(long tourId, long logId, TourLog updated);

    boolean delete(long tourId, long logId);
}
