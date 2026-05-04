package at.fh_technikum.group09.tourplanner.dal.exception;

/** Thrown by the DAL when a tour-log entity cannot be found. */
public class TourLogEntityNotFoundException extends TourDalException {

    public TourLogEntityNotFoundException(long logId, long tourId) {
        super("TourLog entity not found for logId=" + logId + ", tourId=" + tourId);
    }
}
