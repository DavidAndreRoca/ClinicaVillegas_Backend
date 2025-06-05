package com.clinicavillegas.app.auth.services;

import com.clinicavillegas.app.auth.dto.request.LoginRequest;
import com.clinicavillegas.app.auth.dto.request.RegisterRequest;
import com.clinicavillegas.app.auth.dto.response.AuthResponse;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.models.Usuario;

public interface AuthService {
    AuthResponse authenticateAndGenerateTokens(LoginRequest request, String ip, String userAgent);
    AuthResponse registerAndGenerateTokens(RegisterRequest request, String ip, String userAgent);
    UsuarioResponse getUserProfile(Usuario usuario);
    String refreshAccessToken(String refreshToken);
    void invalidateUserSessions(Usuario usuario);
}
