package at.fh_technikum.group09.tourplanner.service;

import at.fh_technikum.group09.tourplanner.dto.AuthResponse;
import at.fh_technikum.group09.tourplanner.dto.LoginRequest;
import at.fh_technikum.group09.tourplanner.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
