package at.fh_technikum.group09.tourplanner.dal.Impl;

import at.fh_technikum.group09.tourplanner.dal.UserDal;
import at.fh_technikum.group09.tourplanner.dal.entity.UserEntity;
import at.fh_technikum.group09.tourplanner.dal.exception.UserDalException;
import at.fh_technikum.group09.tourplanner.dal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserDal implements UserDal {

    private static final Logger log = LoggerFactory.getLogger(JpaUserDal.class);

    private final UserRepository userRepository;

    public JpaUserDal(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (DataAccessException ex) {
            log.error("DB error finding user by username='{}'", username, ex);
            throw new UserDalException("Failed to find user by username=" + username, ex);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try {
            return userRepository.existsByUsername(username);
        } catch (DataAccessException ex) {
            log.error("DB error checking existence for username='{}'", username, ex);
            throw new UserDalException("Failed to check existence for username=" + username, ex);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            return userRepository.existsByEmail(email);
        } catch (DataAccessException ex) {
            log.error("DB error checking existence for email='{}'", email, ex);
            throw new UserDalException("Failed to check existence for email=" + email, ex);
        }
    }

    @Override
    public UserEntity save(UserEntity entity) {
        try {
            UserEntity saved = userRepository.save(entity);
            log.debug("User entity saved id={} username='{}'", saved.getId(), saved.getUsername());
            return saved;
        } catch (DataAccessException ex) {
            log.error("DB error saving user entity username='{}'", entity.getUsername(), ex);
            throw new UserDalException("Failed to save user entity", ex);
        }
    }
}
