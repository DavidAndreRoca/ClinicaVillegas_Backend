package com.clinicavillegas.app.auth.controllers;

import com.clinicavillegas.app.auth.dto.request.LoginRequest;
import com.clinicavillegas.app.auth.dto.request.RegisterRequest;
import com.clinicavillegas.app.auth.dto.response.AuthResponse;
import com.clinicavillegas.app.auth.services.AuthService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.common.EndpointPaths;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EndpointPaths.AUTH_BASE)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        if (authResponse.getToken() != null){
            setTokenCookie(response, authResponse.getToken(), authResponse.getExpirationTime());
        }
        return ResponseEntity.ok(authResponse.getUsuarioResponse());
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        if (authResponse.getToken() != null){
            setTokenCookie(response, authResponse.getToken(), authResponse.getExpirationTime());
        }
        return ResponseEntity.ok(authResponse.getUsuarioResponse());
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(authService.me(userDetails));
    }
    
    private void setTokenCookie(HttpServletResponse response, String token, long expirationTime) {
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(expirationTime)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
