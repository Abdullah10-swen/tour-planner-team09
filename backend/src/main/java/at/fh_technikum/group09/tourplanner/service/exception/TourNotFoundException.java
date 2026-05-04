package at.fh_technikum.group09.tourplanner.service.exception;

/** Thrown by the service layer when a requested tour does not exist. */
public class TourNotFoundException extends TourServiceException {

    public TourNotFoundException(long id) {
        super("Tour not found: id=" + id);
    }
}
