package at.fh_technikum.group09.tourplanner.service;

import at.fh_technikum.group09.tourplanner.dto.TourExportDto;
import at.fh_technikum.group09.tourplanner.model.Tour;

import java.util.List;

public interface TourService {

    List<Tour> getAllTours(long userId);

    Tour getTourById(long id, long userId);

    Tour createTour(Tour tour, long userId);

    Tour updateTour(long id, Tour updated, long userId);

    void deleteTour(long id, long userId);

    Tour updateImageUrl(long id, String imageUrl, long userId);

    TourExportDto exportTourById(long id, long userId);

    List<Tour> importTours(List<TourExportDto> exports, long userId);

    List<Tour> searchTours(String query, long userId);
}
