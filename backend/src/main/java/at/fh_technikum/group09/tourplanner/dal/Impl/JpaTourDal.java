package at.fh_technikum.group09.tourplanner.dal.Impl;

import at.fh_technikum.group09.tourplanner.dal.TourDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import at.fh_technikum.group09.tourplanner.dal.exception.TourDalException;
import at.fh_technikum.group09.tourplanner.dal.repository.TourRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTourDal implements TourDal {

    private static final Logger log = LoggerFactory.getLogger(JpaTourDal.class);

    private final TourRepository tourRepository;

    public JpaTourDal(TourRepository tourRepository) {
        this.tourRepository = tourRepository;
    }

    @Override
    public List<TourEntity> findAllByUserId(long userId) {
        try {
            return tourRepository.findAllByUserId(userId);
        } catch (DataAccessException ex) {
            log.error("DB error retrieving tours for userId={}", userId, ex);
            throw new TourDalException("Failed to retrieve tours for userId=" + userId, ex);
        }
    }

    @Override
    public Optional<TourEntity> findByIdAndUserId(long id, long userId) {
        try {
            return tourRepository.findByIdAndUserId(id, userId);
        } catch (DataAccessException ex) {
            log.error("DB error finding tour id={} for userId={}", id, userId, ex);
            throw new TourDalException("Failed to find tour id=" + id + " for userId=" + userId, ex);
        }
    }

    @Override
    public boolean existsByIdAndUserId(long id, long userId) {
        try {
            return tourRepository.existsByIdAndUserId(id, userId);
        } catch (DataAccessException ex) {
            log.error("DB error checking existence for tour id={} userId={}", id, userId, ex);
            throw new TourDalException("Failed to check existence for tour id=" + id + " userId=" + userId, ex);
        }
    }

    @Override
    public TourEntity save(TourEntity entity) {
        try {
            TourEntity saved = tourRepository.save(entity);
            log.debug("Tour entity saved id={}", saved.getId());
            return saved;
        } catch (DataAccessException ex) {
            log.error("DB error saving tour entity id={}", entity.getId(), ex);
            throw new TourDalException("Failed to save tour entity", ex);
        }
    }

    @Override
    public void deleteById(long id) {
        try {
            tourRepository.deleteById(id);
            log.debug("Tour entity deleted id={}", id);
        } catch (DataAccessException ex) {
            log.error("DB error deleting tour id={}", id, ex);
            throw new TourDalException("Failed to delete tour id=" + id, ex);
        }
    }
}
