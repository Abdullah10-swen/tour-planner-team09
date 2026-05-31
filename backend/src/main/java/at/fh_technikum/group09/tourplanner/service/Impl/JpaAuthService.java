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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JpaAuthService implements AuthService {

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
        try {
            if (userDal.existsByUsername(request.getUsername())) {
                throw new UserAlreadyExistsException(
                        "Username already taken: " + request.getUsername());
            }
            if (userDal.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException(
                        "Email already in use: " + request.getEmail());
            }

            UserEntity user = new UserEntity();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

            UserEntity saved = userDal.save(user);
            String token = jwtTokenProvider.generateToken(saved.getUsername(), saved.getId());
            return new AuthResponse(token, saved.getUsername(), saved.getEmail());
        } catch (UserAlreadyExistsException ex) {
            throw ex;
        } catch (UserDalException ex) {
            throw new TourServiceException("Failed to register user", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            UserEntity user = userDal.findByUsername(request.getUsername())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new InvalidCredentialsException("Invalid username or password");
            }

            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
            return new AuthResponse(token, user.getUsername(), user.getEmail());
        } catch (InvalidCredentialsException ex) {
            throw ex;
        } catch (UserDalException ex) {
            throw new TourServiceException("Failed to authenticate user", ex);
        }
    }
}
