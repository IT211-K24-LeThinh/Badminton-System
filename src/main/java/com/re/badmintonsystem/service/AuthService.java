package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.request.LoginRequest;
import com.re.badmintonsystem.dto.request.RegisterRequest;
import com.re.badmintonsystem.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String accessToken);
}
