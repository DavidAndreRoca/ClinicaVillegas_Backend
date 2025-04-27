package com.clinicavillegas.app.auth.services;

import com.clinicavillegas.app.auth.dto.request.LoginRequest;
import com.clinicavillegas.app.auth.dto.request.RegisterRequest;
import com.clinicavillegas.app.auth.dto.response.JwtResponse;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    JwtResponse register(RegisterRequest request);
}
