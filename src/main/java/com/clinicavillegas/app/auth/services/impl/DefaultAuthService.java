package com.clinicavillegas.app.auth.services.impl;

import com.clinicavillegas.app.auth.dto.request.LoginRequest;
import com.clinicavillegas.app.auth.dto.request.RegisterRequest;
import com.clinicavillegas.app.auth.dto.response.JwtResponse;
import com.clinicavillegas.app.auth.services.AuthService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.user.models.Rol;
import com.clinicavillegas.app.user.models.Sexo;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.TipoDocumentoRepository;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DefaultAuthService implements AuthService {
    private final JwtService jwtService;

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final TipoDocumentoRepository tipoDocumentoRepository;

    public DefaultAuthService(JwtService jwtService, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TipoDocumentoRepository tipoDocumentoRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }

    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getContrasena())
        );
        UserDetails userDetails =  usuarioRepository.findByCorreo(request.getEmail()).orElseThrow();
        String token = jwtService.getToken(userDetails);
        return JwtResponse.builder()
                .token(token)
                .build();
    }

    public JwtResponse register(RegisterRequest request) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findByAcronimo(request.getTipoDocumento()).orElseThrow();

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

        String token = jwtService.getToken(usuario);
        return JwtResponse.builder()
                .token(token)
                .build();
    }
}