package at.fh_technikum.group09.tourplanner.service;

import at.fh_technikum.group09.tourplanner.dal.UserDal;
import at.fh_technikum.group09.tourplanner.dal.entity.UserEntity;
import at.fh_technikum.group09.tourplanner.dto.AuthResponse;
import at.fh_technikum.group09.tourplanner.dto.LoginRequest;
import at.fh_technikum.group09.tourplanner.dto.RegisterRequest;
import at.fh_technikum.group09.tourplanner.security.JwtTokenProvider;
import at.fh_technikum.group09.tourplanner.service.Impl.JpaAuthService;
import at.fh_technikum.group09.tourplanner.service.exception.InvalidCredentialsException;
import at.fh_technikum.group09.tourplanner.service.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaAuthServiceTest {

    @Mock private UserDal userDal;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private JpaAuthService service;

    // ── Register ─────────────────────────────────────────────────────

    /** Benutzername bereits vergeben → UserAlreadyExistsException */
    @Test
    void register_throwsUserAlreadyExists_whenUsernameIsTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("max");
        req.setEmail("max@example.com");
        req.setPassword("secret123");

        when(userDal.existsByUsername("max")).thenReturn(true);

        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("max");
    }

    /** E-Mail bereits vergeben → UserAlreadyExistsException */
    @Test
    void register_throwsUserAlreadyExists_whenEmailIsInUse() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("max");
        req.setEmail("max@example.com");
        req.setPassword("secret123");

        when(userDal.existsByUsername("max")).thenReturn(false);
        when(userDal.existsByEmail("max@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("max@example.com");
    }

    /** Erfolgreiche Registrierung → AuthResponse mit Token */
    @Test
    void register_returnsAuthResponse_onSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("max");
        req.setEmail("max@example.com");
        req.setPassword("secret123");

        UserEntity saved = new UserEntity();
        saved.setId(1L);
        saved.setUsername("max");
        saved.setEmail("max@example.com");
        saved.setPasswordHash("hashedPw");

        when(userDal.existsByUsername("max")).thenReturn(false);
        when(userDal.existsByEmail("max@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashedPw");
        when(userDal.save(any())).thenReturn(saved);
        when(jwtTokenProvider.generateToken("max", 1L)).thenReturn("jwt-token");

        AuthResponse response = service.register(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("max");
    }

    // ── Login ─────────────────────────────────────────────────────────

    /** Unbekannter Benutzername → InvalidCredentialsException */
    @Test
    void login_throwsInvalidCredentials_whenUserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setUsername("unknown");
        req.setPassword("pw");

        when(userDal.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(req))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    /** Erfolgreicher Login → AuthResponse mit Token */
    @Test
    void login_returnsAuthResponse_onSuccess() {
        LoginRequest req = new LoginRequest();
        req.setUsername("max");
        req.setPassword("correctpw");

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("max");
        user.setEmail("max@example.com");
        user.setPasswordHash("correctHash");

        when(userDal.findByUsername("max")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctpw", "correctHash")).thenReturn(true);
        when(jwtTokenProvider.generateToken("max", 1L)).thenReturn("jwt-token");

        AuthResponse response = service.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("max");
    }
}
