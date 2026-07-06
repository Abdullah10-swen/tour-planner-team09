package at.fh_technikum.group09.tourplanner.service.Impl;

import at.fh_technikum.group09.tourplanner.dal.TourDal;
import at.fh_technikum.group09.tourplanner.dal.TourLogDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import at.fh_technikum.group09.tourplanner.dal.entity.TourLogEntity;
import at.fh_technikum.group09.tourplanner.dal.exception.TourDalException;
import at.fh_technikum.group09.tourplanner.dto.TourExportDto;
import at.fh_technikum.group09.tourplanner.dto.TourLogExportDto;
import at.fh_technikum.group09.tourplanner.integration.openroute.OpenRouteTourRouteEnrichment;
import at.fh_technikum.group09.tourplanner.model.Tour;
import at.fh_technikum.group09.tourplanner.service.TourService;
import at.fh_technikum.group09.tourplanner.service.exception.TourNotFoundException;
import at.fh_technikum.group09.tourplanner.service.exception.TourServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class JpaTourService implements TourService {

    private static final Logger log = LoggerFactory.getLogger(JpaTourService.class);

    private final TourDal tourDal;
    private final TourLogDal tourLogDal;
    private final OpenRouteTourRouteEnrichment routeEnrichment;

    public JpaTourService(TourDal tourDal, TourLogDal tourLogDal, OpenRouteTourRouteEnrichment routeEnrichment) {
        this.tourDal = tourDal;
        this.tourLogDal = tourLogDal;
        this.routeEnrichment = routeEnrichment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tour> getAllTours(long userId) {
        try {
            List<Tour> result = new ArrayList<>();
            for (TourEntity e : tourDal.findAllByUserId(userId)) {
                result.add(toTour(e));
            }
            log.debug("getAllTours userId={} count={}", userId, result.size());
            return result;
        } catch (TourDalException ex) {
            log.error("Failed to retrieve tours for userId={}", userId, ex);
            throw new TourServiceException("Failed to retrieve tours for userId=" + userId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Tour getTourById(long id, long userId) {
        try {
            return tourDal.findByIdAndUserId(id, userId)
                    .map(this::toTour)
                    .orElseThrow(() -> new TourNotFoundException(id));
        } catch (TourDalException ex) {
            log.error("Failed to retrieve tour id={} userId={}", id, userId, ex);
            throw new TourServiceException("Failed to retrieve tour id=" + id, ex);
        }
    }

    @Override
    public Tour createTour(Tour tour, long userId) {
        try {
            TourEntity entity = new TourEntity();
            entity.setUserId(userId);
            copyTourFields(tour, entity);
            routeEnrichment.enrichIfPossible(entity);
            TourEntity saved = tourDal.save(entity);
            log.info("Tour created id={} name='{}' userId={}", saved.getId(), saved.getName(), userId);
            return toTour(saved);
        } catch (TourDalException ex) {
            log.error("Failed to create tour for userId={}", userId, ex);
            throw new TourServiceException("Failed to create tour", ex);
        }
    }

    @Override
    public Tour updateTour(long id, Tour updated, long userId) {
        try {
            TourEntity existing = tourDal.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new TourNotFoundException(id));
            copyTourFields(updated, existing);
            routeEnrichment.enrichIfPossible(existing);
            Tour result = toTour(tourDal.save(existing));
            log.info("Tour updated id={} userId={}", id, userId);
            return result;
        } catch (TourDalException ex) {
            log.error("Failed to update tour id={} userId={}", id, userId, ex);
            throw new TourServiceException("Failed to update tour id=" + id, ex);
        }
    }

    @Override
    public Tour updateImageUrl(long id, String imageUrl, long userId) {
        try {
            TourEntity existing = tourDal.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new TourNotFoundException(id));
            existing.setImageUrl(imageUrl);
            return toTour(tourDal.save(existing));
        } catch (TourDalException ex) {
            log.error("Failed to update imageUrl for tour id={} userId={}", id, userId, ex);
            throw new TourServiceException("Failed to update imageUrl for tour id=" + id, ex);
        }
    }

    @Override
    public void deleteTour(long id, long userId) {
        try {
            if (!tourDal.existsByIdAndUserId(id, userId)) {
                throw new TourNotFoundException(id);
            }
            tourDal.deleteById(id);
            log.info("Tour deleted id={} userId={}", id, userId);
        } catch (TourDalException ex) {
            log.error("Failed to delete tour id={} userId={}", id, userId, ex);
            throw new TourServiceException("Failed to delete tour id=" + id, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TourExportDto exportTourById(long id, long userId) {
        try {
            TourEntity entity = tourDal.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new TourNotFoundException(id));

            TourExportDto dto = new TourExportDto();
            dto.setName(entity.getName());
            dto.setDescription(entity.getDescription());
            dto.setFromLocation(entity.getFromLocation());
            dto.setToLocation(entity.getToLocation());
            dto.setTransportType(entity.getTransportType());
            dto.setDistance(entity.getDistance());
            dto.setEstimatedTime(entity.getEstimatedTime());
            dto.setImageUrl(entity.getImageUrl());
            dto.setRouteInfo(entity.getRouteInfo());

            List<TourLogExportDto> logDtos = new ArrayList<>();
            for (TourLogEntity log : tourLogDal.findByTourIdOrderByDateTimeAsc(entity.getId())) {
                TourLogExportDto logDto = new TourLogExportDto();
                logDto.setDateTime(log.getDateTime());
                logDto.setComment(log.getComment());
                logDto.setDifficulty(log.getDifficulty());
                logDto.setTotalDistance(log.getTotalDistance());
                logDto.setTotalTime(log.getTotalTime());
                logDto.setRating(log.getRating());
                logDtos.add(logDto);
            }
            dto.setLogs(logDtos);
            log.info("Tour exported id={} userId={} logCount={}", id, userId, logDtos.size());
            return dto;
        } catch (TourDalException ex) {
            log.error("Failed to export tour id={} userId={}", id, userId, ex);
            throw new TourServiceException("Failed to export tour id=" + id, ex);
        }
    }

    @Override
    public List<Tour> importTours(List<TourExportDto> exports, long userId) {
        log.info("Importing {} tours for userId={}", exports.size(), userId);
        try {
            List<Tour> imported = new ArrayList<>();
            for (TourExportDto dto : exports) {
                TourEntity entity = new TourEntity();
                entity.setUserId(userId);
                entity.setName(dto.getName());
                entity.setDescription(dto.getDescription());
                entity.setFromLocation(dto.getFromLocation());
                entity.setToLocation(dto.getToLocation());
                entity.setTransportType(dto.getTransportType());
                entity.setDistance(dto.getDistance());
                entity.setEstimatedTime(dto.getEstimatedTime());
                entity.setImageUrl(dto.getImageUrl());
                entity.setRouteInfo(dto.getRouteInfo());

                TourEntity saved = tourDal.save(entity);

                if (dto.getLogs() != null) {
                    for (TourLogExportDto logDto : dto.getLogs()) {
                        TourLogEntity log = new TourLogEntity();
                        log.setTour(saved);
                        log.setDateTime(logDto.getDateTime() != null ? logDto.getDateTime() : LocalDateTime.now());
                        log.setComment(logDto.getComment());
                        log.setDifficulty(logDto.getDifficulty());
                        log.setTotalDistance(logDto.getTotalDistance());
                        log.setTotalTime(logDto.getTotalTime());
                        log.setRating(logDto.getRating());
                        tourLogDal.save(log);
                    }
                }

                imported.add(toTour(saved));
            }
            log.info("Import complete: {} tours created for userId={}", imported.size(), userId);
            return imported;
        } catch (TourDalException ex) {
            log.error("Failed to import tours for userId={}", userId, ex);
            throw new TourServiceException("Failed to import tours", ex);
        }
    }

    private void copyTourFields(Tour from, TourEntity to) {
        to.setName(from.getName());
        to.setDescription(from.getDescription());
        to.setFromLocation(from.getFromLocation());
        to.setToLocation(from.getToLocation());
        to.setTransportType(from.getTransportType());
        to.setDistance(from.getDistance());
        to.setEstimatedTime(from.getEstimatedTime());
        to.setImageUrl(from.getImageUrl());
        to.setRouteInfo(from.getRouteInfo());
    }

    private Tour toTour(TourEntity e) {
        List<TourLogEntity> logs = tourLogDal.findByTourIdOrderByDateTimeAsc(e.getId());
        return toTour(e, logs);
    }

    private Tour toTour(TourEntity e, List<TourLogEntity> logs) {
        Tour t = new Tour();
        t.setId(e.getId());
        t.setName(e.getName());
        t.setDescription(e.getDescription());
        t.setFromLocation(e.getFromLocation());
        t.setToLocation(e.getToLocation());
        t.setTransportType(e.getTransportType());
        t.setDistance(e.getDistance());
        t.setEstimatedTime(e.getEstimatedTime());
        t.setImageUrl(e.getImageUrl());
        t.setRouteInfo(e.getRouteInfo());
        t.setPopularity(logs.size());
        t.setChildFriendliness(computeChildFriendliness(logs));
        return t;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tour> searchTours(String query, long userId) {
        if (query == null || query.isBlank()) {
            return getAllTours(userId);
        }
        try {
            String lower = query.toLowerCase().trim();
            List<Tour> result = new ArrayList<>();
            for (TourEntity entity : tourDal.findAllByUserId(userId)) {
                List<TourLogEntity> logs = tourLogDal.findByTourIdOrderByDateTimeAsc(entity.getId());
                Tour tour = toTour(entity, logs);
                if (matchesQuery(tour, logs, lower)) {
                    result.add(tour);
                }
            }
            log.debug("searchTours query='{}' userId={} hits={}", query, userId, result.size());
            return result;
        } catch (TourDalException ex) {
            log.error("Failed to search tours for userId={} query='{}'", userId, query, ex);
            throw new TourServiceException("Failed to search tours for userId=" + userId, ex);
        }
    }

    private boolean matchesQuery(Tour tour, List<TourLogEntity> logs, String lower) {
        if (contains(tour.getName(), lower)) return true;
        if (contains(tour.getDescription(), lower)) return true;
        if (contains(tour.getFromLocation(), lower)) return true;
        if (contains(tour.getToLocation(), lower)) return true;
        if (contains(tour.getTransportType(), lower)) return true;
        for (TourLogEntity log : logs) {
            if (contains(log.getComment(), lower)) return true;
        }
        if (lower.equals(popularityLabel(tour.getPopularity()))) return true;
        if (lower.equals(childFriendlinessLabel(tour.getChildFriendliness()))) return true;
        return false;
    }

    private boolean contains(String text, String lower) {
        return text != null && text.toLowerCase().contains(lower);
    }

    private String popularityLabel(int popularity) {
        if (popularity == 0) return "unpopular";
        if (popularity <= 3) return "popular";
        return "very popular";
    }

    private String childFriendlinessLabel(double score) {
        if (score < 0.33) return "not child-friendly";
        if (score < 0.67) return "child-friendly";
        return "very child-friendly";
    }

    
    private double computeChildFriendliness(List<TourLogEntity> logs) {
        if (logs.isEmpty()) {
            return 0.0;
        }
        //averages nehmen und dann die scores berechnen
        double avgDifficulty = logs.stream().mapToInt(TourLogEntity::getDifficulty).average().orElse(1);
        double avgTime       = logs.stream().mapToDouble(TourLogEntity::getTotalTime).average().orElse(0);
        double avgDistance   = logs.stream().mapToDouble(TourLogEntity::getTotalDistance).average().orElse(0);

        double diffScore = clamp(1.0 - (avgDifficulty - 1.0) / 2.0); //Difficulty geht von 1 bis 3
        double timeScore = clamp(1.0 - avgTime / 8.0); //8h wurde als maximum festgelegt weil mehr als das ist nicht kindgerecht.
        double distScore = clamp(1.0 - avgDistance / 30.0); //30km wurde als maximum festgelegt weil mehr als das ist nicht kindgerecht.

        double raw = (diffScore + timeScore + distScore) / 3.0; //durchschnitt der 3 scores
        return Math.round(raw * 100.0) / 100.0; 
    }

    //clamp wird benutzt um die scores zwischen 0 und 1 zu halten
    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
