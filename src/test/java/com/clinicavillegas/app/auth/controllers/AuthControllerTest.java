package com.clinicavillegas.app.auth.controllers;

import com.clinicavillegas.app.auth.dto.request.LoginRequest;
import com.clinicavillegas.app.auth.dto.request.RegisterRequest;
import com.clinicavillegas.app.auth.dto.response.JwtResponse;
import com.clinicavillegas.app.auth.services.AuthService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @TestConfiguration
    static class Config {
        @Bean
        public AuthService authService() {
            return Mockito.mock(AuthService.class);
        }

        @Bean
        public JwtService jwtService() {
            return mock(JwtService.class);
        }
    }

    @Test
    void testLogin() throws Exception {
        // Crear una solicitud válida de login
        LoginRequest request = LoginRequest.builder()
                .email("test@domain.com")
                .contrasena("password123")
                .build();

        // Crear una respuesta simulada de JWT
        JwtResponse response = new JwtResponse("654asdwqe9@$qwe21v987qw21q98H651asVa3f");

        // Configurar Mock para login
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Realizar la solicitud POST para login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("654asdwqe9@$qwe21v987qw21q98H651asVa3f"));

    }

    @Test
    void testRegister() throws Exception {
        // Crear una solicitud válida de registro
        RegisterRequest request = RegisterRequest.builder()
                .correo("newuser@domain.com")
                .tipoDocumento("DNI")
                .documento("12345678")
                .nombres("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("González")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .telefono("123456789")
                .sexo("M")
                .contrasena("securePassword123")
                .build();

        // Crear una respuesta simulada de JWT
        JwtResponse response = new JwtResponse("654asdwqe9@$qwe21v987qw21q98H651asVa3f");

        // Configurar Mock para register
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Realizar la solicitud POST para register
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("654asdwqe9@$qwe21v987qw21q98H651asVa3f"));

    }
}