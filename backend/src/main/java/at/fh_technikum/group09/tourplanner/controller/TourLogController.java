package at.fh_technikum.group09.tourplanner.controller;

import at.fh_technikum.group09.tourplanner.dto.TourLogDto;
import at.fh_technikum.group09.tourplanner.model.TourLog;
import at.fh_technikum.group09.tourplanner.service.TourLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public List<TourLogDto> getLogsForTour(@PathVariable long tourId) {
        return tourLogService.findAllByTourId(tourId, currentUserId())
                .stream()
                .map(this::toTourLogDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{logId}")
    public TourLogDto getLogById(@PathVariable long tourId, @PathVariable long logId) {
        return toTourLogDto(tourLogService.getByTourIdAndLogId(tourId, logId, currentUserId()));
    }

    @PostMapping
    public ResponseEntity<TourLogDto> createLog(@PathVariable long tourId, @RequestBody TourLogDto dto) {
        TourLog created = tourLogService.create(tourId, toTourLog(dto), currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toTourLogDto(created));
    }

    @PutMapping("/{logId}")
    public TourLogDto updateLog(@PathVariable long tourId,
                                @PathVariable long logId,
                                @RequestBody TourLogDto dto) {
        return toTourLogDto(tourLogService.update(tourId, logId, toTourLog(dto), currentUserId()));
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(@PathVariable long tourId, @PathVariable long logId) {
        tourLogService.delete(tourId, logId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private long currentUserId() {
        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getDetails();
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
