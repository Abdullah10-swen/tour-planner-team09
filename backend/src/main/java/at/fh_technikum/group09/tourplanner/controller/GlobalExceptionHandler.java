package at.fh_technikum.group09.tourplanner.controller;

import at.fh_technikum.group09.tourplanner.service.exception.TourLogNotFoundException;
import at.fh_technikum.group09.tourplanner.service.exception.TourNotFoundException;
import at.fh_technikum.group09.tourplanner.service.exception.TourServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Translates service-layer exceptions into HTTP responses.
 * No Spring or JPA exception types leak to the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TourNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTourNotFound(TourNotFoundException ex) {
        log.warn("Tour not found: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TourLogNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTourLogNotFound(TourLogNotFoundException ex) {
        log.warn("TourLog not found: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TourServiceException.class)
    public ResponseEntity<Map<String, Object>> handleServiceError(TourServiceException ex) {
        log.error("Service error: {}", ex.getMessage(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred.");
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        ));
    }
}
