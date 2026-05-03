package at.fh_technikum.group09.tourplanner.dal.Impl;

import at.fh_technikum.group09.tourplanner.dal.TourDal;
import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;
import at.fh_technikum.group09.tourplanner.dal.repository.TourRepository;
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
        return tourRepository.findAll();
    }

    @Override
    public Optional<TourEntity> findById(long id) {
        return tourRepository.findById(id);
    }

    @Override
    public boolean existsById(long id) {
        return tourRepository.existsById(id);
    }

    @Override
    public TourEntity save(TourEntity entity) {
        return tourRepository.save(entity);
    }

    @Override
    public void deleteById(long id) {
        tourRepository.deleteById(id);
    }
}
