package com.clinicavillegas.app.auth.dto.response;

import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private long expirationTime;
    private long expirationTimeRefresh;
    private UsuarioResponse usuarioResponse;
}
