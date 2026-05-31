package at.fh_technikum.group09.tourplanner.dal;

import at.fh_technikum.group09.tourplanner.dal.entity.UserEntity;

import java.util.Optional;

public interface UserDal {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    UserEntity save(UserEntity entity);
}
