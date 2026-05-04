package at.fh_technikum.group09.tourplanner.dal.Impl;

import at.fh_technikum.group09.tourplanner.dal.TourDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import at.fh_technikum.group09.tourplanner.dal.exception.TourDalException;
import at.fh_technikum.group09.tourplanner.dal.repository.TourRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTourDal implements TourDal {

    private final TourRepository tourRepository;

    public JpaTourDal(TourRepository tourRepository) {
        this.tourRepository = tourRepository;
    }

    @Override
    public List<TourEntity> findAll() {
        try {
            return tourRepository.findAll();
        } catch (DataAccessException ex) {
            throw new TourDalException("Failed to retrieve all tours", ex);
        }
    }

    @Override
    public Optional<TourEntity> findById(long id) {
        try {
            return tourRepository.findById(id);
        } catch (DataAccessException ex) {
            throw new TourDalException("Failed to find tour by id=" + id, ex);
        }
    }

    @Override
    public boolean existsById(long id) {
        try {
            return tourRepository.existsById(id);
        } catch (DataAccessException ex) {
            throw new TourDalException("Failed to check existence for tour id=" + id, ex);
        }
    }

    @Override
    public TourEntity save(TourEntity entity) {
        try {
            return tourRepository.save(entity);
        } catch (DataAccessException ex) {
            throw new TourDalException("Failed to save tour entity", ex);
        }
    }

    @Override
    public void deleteById(long id) {
        try {
            tourRepository.deleteById(id);
        } catch (DataAccessException ex) {
            throw new TourDalException("Failed to delete tour id=" + id, ex);
        }
    }
}
