package at.fh_technikum.group09.tourplanner.controller;

import at.fh_technikum.group09.tourplanner.dto.AuthResponse;
import at.fh_technikum.group09.tourplanner.dto.LoginRequest;
import at.fh_technikum.group09.tourplanner.dto.RegisterRequest;
import at.fh_technikum.group09.tourplanner.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for username='{}'", request.getUsername());
        AuthResponse response = authService.register(request);
        log.info("User registered successfully username='{}'", response.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username='{}'", request.getUsername());
        AuthResponse response = authService.login(request);
        log.info("User logged in successfully username='{}'", response.getUsername());
        return ResponseEntity.ok(response);
    }
}
