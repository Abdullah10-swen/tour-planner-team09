package at.fh_technikum.group09.tourplanner.service.exception;

/** Thrown by the service layer when a requested tour log does not exist. */
public class TourLogNotFoundException extends TourServiceException {

    public TourLogNotFoundException(long logId, long tourId) {
        super("TourLog not found: logId=" + logId + ", tourId=" + tourId);
    }
}
