package com.clinicavillegas.app.auth.controllers;

import com.clinicavillegas.app.auth.dto.request.LoginRequest;
import com.clinicavillegas.app.auth.dto.request.RegisterRequest;
import com.clinicavillegas.app.auth.dto.response.AuthResponse;
import com.clinicavillegas.app.auth.services.AuthService;
import com.clinicavillegas.app.auth.services.CookieService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.common.EndpointPaths;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.models.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(EndpointPaths.AUTH_BASE)
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, CookieService cookieService, JwtService jwtService) {
        this.authService = authService;
        this.cookieService = cookieService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest bodyRequest, HttpServletRequest request, HttpServletResponse response) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        AuthResponse authResponse = authService.authenticateAndGenerateTokens(bodyRequest, ip, userAgent);

        cookieService.setTokenCookie(response, authResponse.getToken(), authResponse.getExpirationTime());
        cookieService.setRefreshTokenCookie(response, authResponse.getRefreshToken(), authResponse.getExpirationTimeRefresh());

        return ResponseEntity.ok(authResponse.getUsuarioResponse());
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@RequestBody RegisterRequest bodyRequest, HttpServletRequest request, HttpServletResponse response) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        AuthResponse authResponse = authService.registerAndGenerateTokens(bodyRequest, ip, userAgent);

        cookieService.setTokenCookie(response, authResponse.getToken(), authResponse.getExpirationTime());
        cookieService.setRefreshTokenCookie(response, authResponse.getRefreshToken(), authResponse.getExpirationTimeRefresh());

        return ResponseEntity.ok(authResponse.getUsuarioResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshTokenFromRequest(request);
        String newAccessToken = authService.refreshAccessToken(refreshToken);

        cookieService.setTokenCookie(response, newAccessToken, jwtService.getEXPIRATION_TIME());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            authService.invalidateUserSessions(usuario);
        }
        cookieService.unsetTokenCookie(response);
        cookieService.unsetRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new IllegalStateException("Authentication principal no es del tipo Usuario.");
        }
        return ResponseEntity.ok(authService.getUserProfile(usuario));
    }
}