package com.clinicavillegas.app.email.controllers;

import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.email.dto.CodeRequest;
import com.clinicavillegas.app.email.dto.EmailRequest;
import com.clinicavillegas.app.email.services.EmailService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailService emailService;

    @TestConfiguration
    static class Config {
        @Bean
        public EmailService emailService() {
            return Mockito.mock(EmailService.class);
        }
        @Bean
        public JwtService jwtService() {
            return mock(JwtService.class);
        }
    }

    @Test
    void testSendEmail() throws Exception {
        // Crear una solicitud válida
        EmailRequest request = EmailRequest.builder()
                .receptor("test@domain.com")
                .asunto("Asunto de prueba")
                .contenido("Contenido de prueba")
                .build();

        // Configurar el Mock para el servicio
        when(emailService.enviarCorreo(any(EmailRequest.class)))
                .thenReturn("Email sent successfully!");

        // Realizar la solicitud POST al endpoint /sendemail
        mockMvc.perform(post("/api/email/sendemail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email sent successfully!"));
    }

    @Test
    void testSendCode() throws Exception {
        // Crear una solicitud válida
        CodeRequest request = CodeRequest.builder()
                .email("test@domain.com")
                .build();

        // Configurar el Mock para el servicio
        when(emailService.enviarCodigoVerificacion(anyString()))
                .thenReturn("123456");

        // Realizar la solicitud POST al endpoint /sendcode
        mockMvc.perform(post("/api/email/sendcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("123456"));
    }
}