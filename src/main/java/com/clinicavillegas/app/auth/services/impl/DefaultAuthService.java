package com.clinicavillegas.app.auth.services.impl;

import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.repositories.DentistaRepository;
import com.clinicavillegas.app.appointment.specifications.DentistaSpecification;
import com.clinicavillegas.app.auth.dto.request.LoginRequest;
import com.clinicavillegas.app.auth.dto.request.RegisterRequest;
import com.clinicavillegas.app.auth.dto.response.AuthResponse;
import com.clinicavillegas.app.auth.exceptions.InvalidTokenException;
import com.clinicavillegas.app.auth.exceptions.TokenExpiredException;
import com.clinicavillegas.app.auth.models.Session;
import com.clinicavillegas.app.auth.repositories.SessionRepository;
import com.clinicavillegas.app.auth.services.AuthService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.mappers.UsuarioMapper;
import com.clinicavillegas.app.user.models.Rol;
import com.clinicavillegas.app.user.models.Sexo;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.TipoDocumentoRepository;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class DefaultAuthService implements AuthService {
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final DentistaRepository dentistaRepository;
    private final SessionRepository sessionRepository;

    public DefaultAuthService(JwtService jwtService, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                              AuthenticationManager authenticationManager, TipoDocumentoRepository tipoDocumentoRepository,
                              DentistaRepository dentistaRepository, SessionRepository sessionRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.dentistaRepository = dentistaRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public AuthResponse authenticateAndGenerateTokens(LoginRequest request, String ip, String userAgent) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getContrasena())
        );

        Usuario usuario = usuarioRepository.findByCorreo(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(Usuario.class, "correo", request.getEmail()));

        return generateAuthResponse(usuario, ip, userAgent);
    }

    @Override
    @Transactional
    public AuthResponse registerAndGenerateTokens(RegisterRequest request, String ip, String userAgent) {
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo electrónico proporcionado.");
        }

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findByAcronimo(request.getTipoDocumento())
                .orElseThrow(() -> new ResourceNotFoundException(TipoDocumento.class, "acrónimo", request.getTipoDocumento()));

        Usuario usuario = Usuario.builder()
                .correo(request.getCorreo())
                .tipoDocumento(tipoDocumento)
                .numeroIdentidad(request.getDocumento())
                .nombres(request.getNombres())
                .apellidoPaterno(request.getApellidoPaterno())
                .apellidoMaterno(request.getApellidoMaterno())
                .fechaNacimiento(request.getFechaNacimiento())
                .telefono(request.getTelefono())
                .estado(true)
                .rol(Rol.PACIENTE)
                .sexo(Sexo.valueOf(request.getSexo()))
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .imagenPerfil("media/logo.jpg")
                .build();

        usuarioRepository.save(usuario);
        return generateAuthResponse(usuario, ip, userAgent);
    }

    @Override
    public UsuarioResponse getUserProfile(Usuario usuario) {
        UsuarioResponse usuarioResponse = UsuarioMapper.toDto(usuario);
        buildResponseByDentistRole(usuario, usuarioResponse);
        return usuarioResponse;
    }

    @Override
    @Transactional
    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("No se encontró el refresh token.");
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("El token proporcionado no es un refresh token válido.");
        }

        Session session = sessionRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token no reconocido o ya ha sido invalidado."));

        if (session.getExpiryDate().isBefore(Instant.now())) {
            sessionRepository.delete(session);
            throw new TokenExpiredException("El refresh token ha expirado. Por favor, inicie sesión nuevamente.");
        }

        Usuario usuario = session.getUsuario();
        return jwtService.getToken(usuario);
    }

    @Override
    @Transactional
    public void invalidateUserSessions(Usuario usuario) {
        sessionRepository.deleteByUsuario(usuario);
    }

    /**
     * Helper method to generate AuthResponse, consolidating token generation and session saving.
     */
    private AuthResponse generateAuthResponse(Usuario usuario, String ip, String userAgent) {
        UsuarioResponse usuarioResponse = UsuarioMapper.toDto(usuario);
        buildResponseByDentistRole(usuario, usuarioResponse);

        String accessToken = jwtService.getToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        saveSession(refreshToken, ip, userAgent, usuario);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expirationTime(jwtService.getEXPIRATION_TIME())
                .expirationTimeRefresh(jwtService.getEXPIRATION_TIME_REFRESH())
                .usuarioResponse(usuarioResponse)
                .build();
    }

    /**
     * Handles saving or updating the user session.
     */
    private void saveSession(String token, String ip, String userAgent, Usuario usuario) {
        sessionRepository.findByToken(token).ifPresent(sessionRepository::delete);

        Session session = Session.builder()
                .token(token)
                .usuario(usuario)
                .ipAddress(ip)
                .userAgent(userAgent)
                .expiryDate(Instant.now().plus(jwtService.getEXPIRATION_TIME_REFRESH(), ChronoUnit.SECONDS))
                .build();
        sessionRepository.save(session);
    }

    /**
     * Enriches the UsuarioResponse with dentist-specific ID if the user is a dentist.
     */
    private void buildResponseByDentistRole(Usuario usuario, UsuarioResponse usuarioResponse) {
        if (usuario.getRol().equals(Rol.DENTISTA)) {
            Specification<Dentista> specs = DentistaSpecification.conUsuarioId(usuario.getId());
            usuarioResponse.setId(dentistaRepository.findOne(specs)
                    .orElseThrow(() -> new ResourceNotFoundException(Dentista.class, "usuarioId", usuario.getId()))
                    .getId());
        }
    }
}