package at.fh_technikum.group09.tourplanner.dal.Impl;

import at.fh_technikum.group09.tourplanner.dal.UserDal;
import at.fh_technikum.group09.tourplanner.dal.entity.UserEntity;
import at.fh_technikum.group09.tourplanner.dal.exception.UserDalException;
import at.fh_technikum.group09.tourplanner.dal.repository.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserDal implements UserDal {

    private final UserRepository userRepository;

    public JpaUserDal(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (DataAccessException ex) {
            throw new UserDalException("Failed to find user by username=" + username, ex);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try {
            return userRepository.existsByUsername(username);
        } catch (DataAccessException ex) {
            throw new UserDalException("Failed to check existence for username=" + username, ex);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            return userRepository.existsByEmail(email);
        } catch (DataAccessException ex) {
            throw new UserDalException("Failed to check existence for email=" + email, ex);
        }
    }

    @Override
    public UserEntity save(UserEntity entity) {
        try {
            return userRepository.save(entity);
        } catch (DataAccessException ex) {
            throw new UserDalException("Failed to save user entity", ex);
        }
    }
}
