package at.fh_technikum.group09.tourplanner.service.exception;

/** Base exception for all service-layer errors. */
public class TourServiceException extends RuntimeException {

    public TourServiceException(String message) {
        super(message);
    }

    public TourServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
