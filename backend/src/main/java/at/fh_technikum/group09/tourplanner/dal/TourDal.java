package at.fh_technikum.group09.tourplanner.dal;

import at.fh_technikum.group09.tourplanner.dal.entity.TourEntity;

import java.util.List;
import java.util.Optional;

public interface TourDal {

    List<TourEntity> findAll();

    Optional<TourEntity> findById(long id);

    boolean existsById(long id);

    TourEntity save(TourEntity entity);

    void deleteById(long id);
}
