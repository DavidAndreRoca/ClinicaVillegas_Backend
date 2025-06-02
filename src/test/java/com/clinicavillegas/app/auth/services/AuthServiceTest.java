package com.clinicavillegas.app.auth.services;

import com.clinicavillegas.app.auth.dto.request.LoginRequest;
import com.clinicavillegas.app.auth.dto.request.RegisterRequest;
import com.clinicavillegas.app.auth.dto.response.JwtResponse;
import com.clinicavillegas.app.auth.services.impl.DefaultAuthService;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.TipoDocumentoRepository;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private DefaultAuthService authService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new DefaultAuthService(jwtService, usuarioRepository, passwordEncoder, authenticationManager, tipoDocumentoRepository);
    }

    @Test
    @DisplayName("Debe autenticar usuario y retornar token en login")
    void testLogin() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@correo.com")
                .contrasena("123456")
                .build();

        Usuario usuario = Usuario.builder()
                .correo("test@correo.com")
                .contrasena("encoded123456")
                .build();

        when(usuarioRepository.findByCorreo(loginRequest.getEmail()))
                .thenReturn(Optional.of(usuario));
        when(jwtService.getToken(any(UserDetails.class))).thenReturn("fake-jwt-token");

        JwtResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).getToken(usuario);
    }

    @Test
    @DisplayName("Debe registrar usuario y retornar token")
    void testRegister() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .correo("nuevo@correo.com")
                .contrasena("123456")
                .documento("12345678")
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .apellidoMaterno("Garcia")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .telefono("987654321")
                .tipoDocumento("DNI")
                .sexo("MASCULINO")
                .build();

        TipoDocumento tipoDocumento = TipoDocumento.builder()
                .id(1L)
                .acronimo("DNI")
                .nombre("Documento Nacional de Identidad")
                .build();

        when(tipoDocumentoRepository.findByAcronimo("DNI")).thenReturn(Optional.of(tipoDocumento));
        when(passwordEncoder.encode("123456")).thenReturn("encoded123456");
        when(jwtService.getToken(any(UserDetails.class))).thenReturn("jwt-token-registrado");

        JwtResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token-registrado", response.getToken());
        verify(usuarioRepository).save(any(Usuario.class));
    }
    
}
