package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.HorarioRequest;
import com.clinicavillegas.app.appointment.dto.response.HorarioResponse;
import com.clinicavillegas.app.appointment.services.HorarioService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalTime;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HorarioController.class)
@Import(HorarioControllerTest.Config.class)
@AutoConfigureMockMvc(addFilters = false)
public class HorarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class Config {
        @Bean
        public HorarioService horarioService() {
            return Mockito.mock(HorarioService.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }
    }

    @Test
    void testObtenerHorarios() throws Exception {
        HorarioResponse horario1 = HorarioResponse.builder()
                .id(1L)
                .dia("Lunes")
                .horaComienzo(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(17, 0))
                .dentistaId(1L)
                .build();

        HorarioResponse horario2 = HorarioResponse.builder()
                .id(2L)
                .dia("Martes")
                .horaComienzo(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(16, 0))
                .dentistaId(2L)
                .build();

        when(horarioService.obtenerHorarios(null, null)).thenReturn(Arrays.asList(horario1, horario2));

        mockMvc.perform(get("/api/horarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].dia").value("Lunes"))
                .andExpect(jsonPath("$[0].horaComienzo").value("09:00:00"))
                .andExpect(jsonPath("$[0].horaFin").value("17:00:00"))
                .andExpect(jsonPath("$[0].dentistaId").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].dia").value("Martes"))
                .andExpect(jsonPath("$[1].horaComienzo").value("08:00:00"))
                .andExpect(jsonPath("$[1].horaFin").value("16:00:00"))
                .andExpect(jsonPath("$[1].dentistaId").value(2L));
    }

    @Test
    void testAgregarHorario() throws Exception {
        HorarioRequest request = HorarioRequest.builder()
                .dia("Lunes")
                .horaComienzo(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(17, 0))
                .dentistaId(1L)
                .build();

        doNothing().when(horarioService).agregarHorario(any());

        mockMvc.perform(post("/api/horarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Horario agregado con exito"));
    }

    @Test
    void testEliminarHorario() throws Exception {
        doNothing().when(horarioService).eliminarHorario(1L);

        mockMvc.perform(delete("/api/horarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Horario eliminado con exito"));
    }
}