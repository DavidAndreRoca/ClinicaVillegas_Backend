package com.clinicavillegas.app.auth.controllers;

import com.clinicavillegas.app.auth.dto.request.LoginRequest;
import com.clinicavillegas.app.auth.dto.request.RegisterRequest;
import com.clinicavillegas.app.auth.dto.response.AuthResponse;
import com.clinicavillegas.app.auth.repositories.SessionRepository;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(EndpointPaths.AUTH_BASE)
public class AuthController {

    private final AuthService authService;

    private final CookieService cookieService;

    private final JwtService jwtService;

    private final SessionRepository refreshTokenRepository;

    public AuthController(AuthService authService, JwtService jwtService, CookieService cookieService, SessionRepository refreshTokenRepository) {
        this.authService = authService;
        this.cookieService = cookieService;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest bodyRequest, HttpServletRequest request, HttpServletResponse response) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        AuthResponse authResponse = authService.login(bodyRequest, ip, userAgent);
        if (authResponse.getToken() != null){
            cookieService.setTokenCookie(response, authResponse.getToken(), authResponse.getExpirationTime());
            cookieService.setRefreshTokenCookie(response, authResponse.getRefreshToken(), authResponse.getExpirationTimeRefresh());
        }
        return ResponseEntity.ok(authResponse.getUsuarioResponse());
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@RequestBody RegisterRequest bodyRequest, HttpServletRequest request, HttpServletResponse response) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        AuthResponse authResponse = authService.register(bodyRequest, ip, userAgent);
        if (authResponse.getToken() != null){
            cookieService.setTokenCookie(response, authResponse.getToken(), authResponse.getExpirationTime());
            cookieService.setRefreshTokenCookie(response, authResponse.getRefreshToken(), authResponse.getExpirationTimeRefresh());
        }
        return ResponseEntity.ok(authResponse.getUsuarioResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshTokenFromRequest(request);
        String newAccessToken = authService.refreshToken(refreshToken);

        cookieService.setTokenCookie(response, newAccessToken, jwtService.getEXPIRATION_TIME());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            refreshTokenRepository.deleteByUsuario(usuario);
        }
        cookieService.unsetTokenCookie(response);
        cookieService.unsetRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(authService.me(userDetails));
    }

}
