package at.fh_technikum.group09.tourplanner.dal.exception;

/** Base exception for all DAL-layer errors. */
public class TourDalException extends RuntimeException {

    public TourDalException(String message) {
        super(message);
    }

    public TourDalException(String message, Throwable cause) {
        super(message, cause);
    }
}
