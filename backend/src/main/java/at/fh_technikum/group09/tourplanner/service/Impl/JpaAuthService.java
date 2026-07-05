package at.fh_technikum.group09.tourplanner.service.Impl;

import at.fh_technikum.group09.tourplanner.dal.UserDal;
import at.fh_technikum.group09.tourplanner.dal.entity.UserEntity;
import at.fh_technikum.group09.tourplanner.dal.exception.UserDalException;
import at.fh_technikum.group09.tourplanner.dto.AuthResponse;
import at.fh_technikum.group09.tourplanner.dto.LoginRequest;
import at.fh_technikum.group09.tourplanner.dto.RegisterRequest;
import at.fh_technikum.group09.tourplanner.security.JwtTokenProvider;
import at.fh_technikum.group09.tourplanner.service.AuthService;
import at.fh_technikum.group09.tourplanner.service.exception.InvalidCredentialsException;
import at.fh_technikum.group09.tourplanner.service.exception.TourServiceException;
import at.fh_technikum.group09.tourplanner.service.exception.UserAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JpaAuthService implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(JpaAuthService.class);

    private final UserDal userDal;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public JpaAuthService(UserDal userDal,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.userDal = userDal;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user username='{}'", request.getUsername());
        try {
            if (userDal.existsByUsername(request.getUsername())) {
                log.warn("Registration rejected – username already taken: '{}'", request.getUsername());
                throw new UserAlreadyExistsException(
                        "Username already taken: " + request.getUsername());
            }
            if (userDal.existsByEmail(request.getEmail())) {
                log.warn("Registration rejected – email already in use: '{}'", request.getEmail());
                throw new UserAlreadyExistsException(
                        "Email already in use: " + request.getEmail());
            }

            UserEntity user = new UserEntity();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

            UserEntity saved = userDal.save(user);
            log.info("User registered successfully id={} username='{}'", saved.getId(), saved.getUsername());
            String token = jwtTokenProvider.generateToken(saved.getUsername(), saved.getId());
            return new AuthResponse(token, saved.getUsername(), saved.getEmail());
        } catch (UserAlreadyExistsException ex) {
            throw ex;
        } catch (UserDalException ex) {
            log.error("Failed to register user username='{}'", request.getUsername(), ex);
            throw new TourServiceException("Failed to register user", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt username='{}'", request.getUsername());
        try {
            UserEntity user = userDal.findByUsername(request.getUsername())
                    .orElseThrow(() -> {
                        log.warn("Login failed – unknown username='{}'", request.getUsername());
                        return new InvalidCredentialsException("Invalid username or password");
                    });

            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                log.warn("Login failed – wrong password for username='{}'", request.getUsername());
                throw new InvalidCredentialsException("Invalid username or password");
            }

            log.info("User authenticated id={} username='{}'", user.getId(), user.getUsername());
            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
            return new AuthResponse(token, user.getUsername(), user.getEmail());
        } catch (InvalidCredentialsException ex) {
            throw ex;
        } catch (UserDalException ex) {
            log.error("Failed to authenticate user username='{}'", request.getUsername(), ex);
            throw new TourServiceException("Failed to authenticate user", ex);
        }
    }
}
