package at.fh_technikum.group09.tourplanner.controller;

import at.fh_technikum.group09.tourplanner.dto.TourLogDto;
import at.fh_technikum.group09.tourplanner.model.TourLog;
import at.fh_technikum.group09.tourplanner.service.TourLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tours/{tourId}/logs")
@CrossOrigin
public class TourLogController {

    private final TourLogService tourLogService;

    public TourLogController(TourLogService tourLogService) {
        this.tourLogService = tourLogService;
    }

    @GetMapping
    public ResponseEntity<List<TourLogDto>> getLogsForTour(@PathVariable long tourId) {
        return tourLogService.findAllByTourId(tourId)
                .map(logs -> logs.stream().map(this::toTourLogDto).collect(Collectors.toList()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{logId}")
    public ResponseEntity<TourLogDto> getLogById(@PathVariable long tourId, @PathVariable long logId) {
        TourLog log = tourLogService.getByTourIdAndLogId(tourId, logId);
        if (log == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toTourLogDto(log));
    }

    @PostMapping
    public ResponseEntity<TourLogDto> createLog(@PathVariable long tourId, @RequestBody TourLogDto dto) {
        TourLog created = tourLogService.create(tourId, toTourLog(dto));
        if (created == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(toTourLogDto(created));
    }

    @PutMapping("/{logId}")
    public ResponseEntity<TourLogDto> updateLog(@PathVariable long tourId,
                                                @PathVariable long logId,
                                                @RequestBody TourLogDto dto) {
        TourLog updated = tourLogService.update(tourId, logId, toTourLog(dto));
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toTourLogDto(updated));
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(@PathVariable long tourId, @PathVariable long logId) {
        boolean deleted = tourLogService.delete(tourId, logId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
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
