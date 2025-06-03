package com.clinicavillegas.app.auth.services;

import com.clinicavillegas.app.auth.dto.request.LoginRequest;
import com.clinicavillegas.app.auth.dto.request.RegisterRequest;
import com.clinicavillegas.app.auth.dto.response.AuthResponse;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    UsuarioResponse me(UserDetails userDetails);
}
