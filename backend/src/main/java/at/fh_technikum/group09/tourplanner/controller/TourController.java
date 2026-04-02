package at.fh_technikum.group09.tourplanner.controller;

import at.fh_technikum.group09.tourplanner.dto.TourDto;
import at.fh_technikum.group09.tourplanner.dto.TourLogDto;
import at.fh_technikum.group09.tourplanner.model.Tour;
import at.fh_technikum.group09.tourplanner.model.TourLog;
import at.fh_technikum.group09.tourplanner.service.TourService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tours")
@CrossOrigin // für lokale Angular-Entwicklung
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    // region Tours

    @GetMapping
    public List<TourDto> getAllTours() {
        return tourService.getAllTours()
                .stream()
                .map(this::toTourDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourDto> getTourById(@PathVariable long id) {
        Tour tour = tourService.getTourById(id);
        if (tour == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toTourDto(tour));
    }

    @PostMapping
    public ResponseEntity<TourDto> createTour(@RequestBody TourDto dto) {
        Tour created = tourService.createTour(toTour(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toTourDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourDto> updateTour(@PathVariable long id, @RequestBody TourDto dto) {
        Tour updated = tourService.updateTour(id, toTour(dto));
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toTourDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable long id) {
        boolean deleted = tourService.deleteTour(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // endregion

    // region Logs

    @GetMapping("/{tourId}/logs")
    public ResponseEntity<List<TourLogDto>> getLogsForTour(@PathVariable long tourId) {
        Tour tour = tourService.getTourById(tourId);
        if (tour == null) {
            return ResponseEntity.notFound().build();
        }

        List<TourLogDto> logs = tourService.getLogsForTour(tourId)
                .stream()
                .map(this::toTourLogDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{tourId}/logs/{logId}")
    public ResponseEntity<TourLogDto> getLogById(@PathVariable long tourId, @PathVariable long logId) {
        TourLog log = tourService.getLogById(tourId, logId);
        if (log == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toTourLogDto(log));
    }

    @PostMapping("/{tourId}/logs")
    public ResponseEntity<TourLogDto> createLog(@PathVariable long tourId, @RequestBody TourLogDto dto) {
        TourLog created = tourService.createLog(tourId, toTourLog(dto));
        if (created == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(toTourLogDto(created));
    }

    @PutMapping("/{tourId}/logs/{logId}")
    public ResponseEntity<TourLogDto> updateLog(@PathVariable long tourId,
                                                @PathVariable long logId,
                                                @RequestBody TourLogDto dto) {
        TourLog updated = tourService.updateLog(tourId, logId, toTourLog(dto));
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toTourLogDto(updated));
    }

    @DeleteMapping("/{tourId}/logs/{logId}")
    public ResponseEntity<Void> deleteLog(@PathVariable long tourId, @PathVariable long logId) {
        boolean deleted = tourService.deleteLog(tourId, logId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // endregion

    private TourDto toTourDto(Tour tour) {
        TourDto dto = new TourDto();
        dto.setId(tour.getId());
        dto.setName(tour.getName());
        dto.setDescription(tour.getDescription());
        dto.setFromLocation(tour.getFromLocation());
        dto.setToLocation(tour.getToLocation());
        dto.setTransportType(tour.getTransportType());
        dto.setDistance(tour.getDistance());
        dto.setEstimatedTime(tour.getEstimatedTime());
        dto.setImageUrl(tour.getImageUrl());
        dto.setRouteInfo(tour.getRouteInfo());
        return dto;
    }

    private Tour toTour(TourDto dto) {
        Tour tour = new Tour();
        tour.setId(dto.getId());
        tour.setName(dto.getName());
        tour.setDescription(dto.getDescription());
        tour.setFromLocation(dto.getFromLocation());
        tour.setToLocation(dto.getToLocation());
        tour.setTransportType(dto.getTransportType());
        tour.setDistance(dto.getDistance());
        tour.setEstimatedTime(dto.getEstimatedTime());
        tour.setImageUrl(dto.getImageUrl());
        tour.setRouteInfo(dto.getRouteInfo());
        return tour;
    }

    private TourLogDto toTourLogDto(TourLog log) {
        TourLogDto dto = new TourLogDto();
        dto.setId(log.getId());
        dto.setTourId(log.getTourId());
        dto.setDateTime(log.getDateTime());
        dto.setComment(log.getComment());
        dto.setDifficulty(log.getDifficulty());
        dto.setTotalDistance(log.getTotalDistance());
        dto.setTotalTime(log.getTotalTime());
        dto.setRating(log.getRating());
        return dto;
    }

    private TourLog toTourLog(TourLogDto dto) {
        TourLog log = new TourLog();
        log.setId(dto.getId());
        log.setTourId(dto.getTourId());
        log.setDateTime(dto.getDateTime());
        log.setComment(dto.getComment());
        log.setDifficulty(dto.getDifficulty());
        log.setTotalDistance(dto.getTotalDistance());
        log.setTotalTime(dto.getTotalTime());
        log.setRating(dto.getRating());
        return log;
    }
}

