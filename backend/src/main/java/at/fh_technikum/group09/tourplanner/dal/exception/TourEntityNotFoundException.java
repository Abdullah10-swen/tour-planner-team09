package at.fh_technikum.group09.tourplanner.dal.exception;

/** Thrown by the DAL when a tour entity cannot be found by its ID. */
public class TourEntityNotFoundException extends TourDalException {

    public TourEntityNotFoundException(long id) {
        super("Tour entity not found for id=" + id);
    }
}
