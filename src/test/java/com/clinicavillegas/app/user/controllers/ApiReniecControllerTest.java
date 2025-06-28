package com.clinicavillegas.app.user.controllers;

import com.clinicavillegas.app.auth.services.CookieService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.user.services.ApiReniecService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiReniecController.class)
@Import(ApiReniecControllerTest.Config.class)
@AutoConfigureMockMvc(addFilters = false)
public class ApiReniecControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiReniecService apiReniecService;

    @TestConfiguration
    static class Config {
        @Bean
        public ApiReniecService apiReniecService() {
            return Mockito.mock(ApiReniecService.class);
        }
        @Bean
        public JwtService jwtService() {
            return mock(JwtService.class);
        }
        @Bean
        public CookieService cookieService(){
            return mock(CookieService.class);
        }
    }

    @Test
    @DisplayName("GET /api/reniec - debe retornar la información correspondiente a un DNI")
    void testConsultarDni() throws Exception {
        // Mock de la respuesta del servicio
        String dni = "12345678";
        Map<String, Object> mockResponse = Map.of(
                "dni", dni,
                "nombres", "Juan Carlos",
                "apellidoPaterno", "Pérez",
                "apellidoMaterno", "Gómez"
        );
        when(apiReniecService.consultarDni(dni)).thenReturn(mockResponse);

        // Petición al endpoint y validación
        mockMvc.perform(get("/api/reniec")
                        .param("dni", dni)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.nombres").value("Juan Carlos"))
                .andExpect(jsonPath("$.apellidoPaterno").value("Pérez"))
                .andExpect(jsonPath("$.apellidoMaterno").value("Gómez"));
    }
}