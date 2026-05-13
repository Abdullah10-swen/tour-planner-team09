package at.fh_technikum.group09.tourplanner.service;

import at.fh_technikum.group09.tourplanner.dto.TourExportDto;
import at.fh_technikum.group09.tourplanner.model.Tour;

import java.util.List;

public interface TourService {

    List<Tour> getAllTours();

    Tour getTourById(long id);

    Tour createTour(Tour tour);

    Tour updateTour(long id, Tour updated);

    void deleteTour(long id);

    Tour updateImageUrl(long id, String imageUrl);

    TourExportDto exportTourById(long id);

    List<Tour> importTours(List<TourExportDto> exports);
}
