package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.CitaReprogramarRequest;
import com.clinicavillegas.app.appointment.dto.request.CitaRequest;
import com.clinicavillegas.app.appointment.dto.request.ValidacionCitaRequest;
import com.clinicavillegas.app.appointment.dto.response.CitaResponse;
import com.clinicavillegas.app.appointment.services.CitaService;
import com.clinicavillegas.app.auth.services.CookieService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitaController.class)
@Import(CitaControllerTest.Config.class)
@AutoConfigureMockMvc(addFilters = false)
public class CitaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CitaService citaService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class Config {
        @Bean
        public CitaService citaService() {
            return Mockito.mock(CitaService.class);
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
    @DisplayName("GET /api/citas - debe devolver lista de citas")
    void testObtenerCitas() throws Exception {
        CitaResponse response = CitaResponse.builder().nombres("Juan").apellidoPaterno("Pérez").build();
        Mockito.when(citaService.obtenerCitas(null, null, null, null, null, null, null))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/citas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombres").value("Juan"));
    }

    @Test
    @DisplayName("POST /api/citas/validar - debe validar disponibilidad")
    void testValidarCita() throws Exception {
        ValidacionCitaRequest request = ValidacionCitaRequest.builder()
                .fecha("2025-04-20")
                .hora("10:00")
                .tratamientoId(1L)
                .dentistaId(2L)
                .build();

        Mockito.when(citaService.validarDisponibilidad(Mockito.any())).thenReturn(true);

        mockMvc.perform(post("/api/citas/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("PUT /api/citas/atender/{id} - debe atender la cita")
    void testAtenderCita() throws Exception {
        mockMvc.perform(put("/api/citas/atender/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cita atendida con exito"));
    }

    @Test
    @DisplayName("PUT /api/citas/reprogramar/{id} - debe reprogramar cita")
    void testReprogramarCita() throws Exception {
        CitaReprogramarRequest request = CitaReprogramarRequest.builder()
                .fecha(LocalDate.of(2025, 5, 1))
                .hora(LocalTime.of(15, 0))
                .build();

        mockMvc.perform(put("/api/citas/reprogramar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cita reprogramada con exito"));
    }

    @Test
    @DisplayName("POST /api/citas - debe agregar cita")
    void testAgregarCita() throws Exception {
        CitaRequest request = CitaRequest.builder()
                .fecha(LocalDate.of(2025, 5, 10))
                .hora(LocalTime.of(9, 30))
                .monto(BigDecimal.valueOf(100))
                .nombres("Maria")
                .apellidoPaterno("Gomez")
                .apellidoMaterno("Lopez")
                .tipoDocumento("DNI")
                .numeroIdentidad("12345678")
                .sexo("F")
                .fechaNacimiento(LocalDate.of(1995, 1, 1))
                .dentistaId(2L)
                .usuarioId(3L)
                .tratamientoId(4L)
                .build();

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cita agregada con exito"));
    }

    @Test
    @DisplayName("PUT /api/citas/{id} - debe actualizar cita")
    void testActualizarCita() throws Exception {
        CitaRequest request = CitaRequest.builder()
                .fecha(LocalDate.of(2025, 5, 15))
                .hora(LocalTime.of(10, 0))
                .monto(BigDecimal.valueOf(150))
                .nombres("Luis")
                .apellidoPaterno("Ramirez")
                .apellidoMaterno("Torres")
                .tipoDocumento("DNI")
                .numeroIdentidad("87654321")
                .sexo("M")
                .fechaNacimiento(LocalDate.of(1988, 6, 15))
                .dentistaId(1L)
                .usuarioId(2L)
                .tratamientoId(3L)
                .build();

        mockMvc.perform(put("/api/citas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cita actualizada con exito"));
    }

    @Test
    @DisplayName("DELETE /api/citas/{id} - debe eliminar cita")
    void testEliminarCita() throws Exception {
        mockMvc.perform(delete("/api/citas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cita cancelada con exito"));
    }
}
