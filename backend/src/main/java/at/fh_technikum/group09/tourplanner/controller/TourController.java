package at.fh_technikum.group09.tourplanner.controller;

import at.fh_technikum.group09.tourplanner.dto.TourDto;
import at.fh_technikum.group09.tourplanner.model.Tour;
import at.fh_technikum.group09.tourplanner.service.TourService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tours")
@CrossOrigin
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping
    public List<TourDto> getAllTours() {
        return tourService.getAllTours()
                .stream()
                .map(this::toTourDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TourDto getTourById(@PathVariable long id) {
        return toTourDto(tourService.getTourById(id));
    }

    @PostMapping
    public ResponseEntity<TourDto> createTour(@RequestBody TourDto dto) {
        Tour created = tourService.createTour(toTour(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toTourDto(created));
    }

    @PutMapping("/{id}")
    public TourDto updateTour(@PathVariable long id, @RequestBody TourDto dto) {
        return toTourDto(tourService.updateTour(id, toTour(dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable long id) {
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
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
