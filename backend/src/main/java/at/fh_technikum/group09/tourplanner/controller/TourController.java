package at.fh_technikum.group09.tourplanner.controller;

import at.fh_technikum.group09.tourplanner.dto.TourDto;
import at.fh_technikum.group09.tourplanner.dto.TourExportDto;
import at.fh_technikum.group09.tourplanner.model.Tour;
import at.fh_technikum.group09.tourplanner.service.TourService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tours")
@CrossOrigin
public class TourController {

    private static final Logger log = LoggerFactory.getLogger(TourController.class);

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping
    public List<TourDto> getAllTours() {
        long userId = currentUserId();
        log.debug("GET /api/tours userId={}", userId);
        return tourService.getAllTours(userId)
                .stream()
                .map(this::toTourDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<TourDto> searchTours(@RequestParam("q") String query) {
        long userId = currentUserId();
        log.debug("GET /api/tours/search q='{}' userId={}", query, userId);
        return tourService.searchTours(query, userId)
                .stream()
                .map(this::toTourDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TourDto getTourById(@PathVariable long id) {
        long userId = currentUserId();
        log.debug("GET /api/tours/{} userId={}", id, userId);
        return toTourDto(tourService.getTourById(id, userId));
    }

    @PostMapping
    public ResponseEntity<TourDto> createTour(@RequestBody TourDto dto) {
        long userId = currentUserId();
        log.info("POST /api/tours name='{}' userId={}", dto.getName(), userId);
        Tour created = tourService.createTour(toTour(dto), userId);
        log.info("Tour created id={} userId={}", created.getId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toTourDto(created));
    }

    @PutMapping("/{id}")
    public TourDto updateTour(@PathVariable long id, @RequestBody TourDto dto) {
        long userId = currentUserId();
        log.info("PUT /api/tours/{} userId={}", id, userId);
        return toTourDto(tourService.updateTour(id, toTour(dto), userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable long id) {
        long userId = currentUserId();
        log.info("DELETE /api/tours/{} userId={}", id, userId);
        tourService.deleteTour(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<TourExportDto> exportTour(@PathVariable long id) {
        long userId = currentUserId();
        log.info("GET /api/tours/{}/export userId={}", id, userId);
        TourExportDto data = tourService.exportTourById(id, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("tour-" + id + "-export.json")
                        .build());
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @PostMapping("/import")
    public ResponseEntity<List<TourDto>> importTours(@RequestBody List<TourExportDto> exports) {
        long userId = currentUserId();
        log.info("POST /api/tours/import count={} userId={}", exports.size(), userId);
        List<Tour> imported = tourService.importTours(exports, userId);
        log.info("Tours imported count={} userId={}", imported.size(), userId);
        List<TourDto> result = imported.stream().map(this::toTourDto).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    private long currentUserId() {
        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getDetails();
    }

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
        dto.setPopularity(tour.getPopularity());
        dto.setChildFriendliness(tour.getChildFriendliness());
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
}
