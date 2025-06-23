package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.CitaReprogramarRequest;
import com.clinicavillegas.app.appointment.dto.request.CitaRequest;
import com.clinicavillegas.app.appointment.dto.request.ValidacionCitaRequest;
import com.clinicavillegas.app.appointment.dto.response.CitaResponse;
import com.clinicavillegas.app.appointment.dto.response.DentistaResponse;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.appointment.services.CitaService;
import com.clinicavillegas.app.auth.services.CookieService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.user.models.TipoDocumento;
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
import com.clinicavillegas.app.appointment.dto.request.CancelacionCitaRequest; // Add this import
// ... (other imports)

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.Arrays;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.*;
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

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper;
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
                .andExpect(jsonPath("$.content[0].nombres").value("Paciente Paginado"));
    }

    @Test
    @DisplayName("GET /api/citas - debe devolver citas paginadas por defecto")
    void testObtenerCitasPaginadasDefault() throws Exception {
        DentistaResponse dummyDentista = DentistaResponse.builder()
                .id(1L)
                .nombres("Dr. Mock")
                .apellidoPaterno("ApellidoMock")
                .build();
        Tratamiento dummyTratamiento = Tratamiento.builder()
                .id(10L)
                .nombre("Revisión")
                .build();
        TipoDocumento dummyTipoDocumento = TipoDocumento.builder()
                .id(1L)
                .nombre("DNI")
                .acronimo("DNI")
                .build();

        CitaResponse paginatedCitaResponse = CitaResponse.builder()
                .id(1L)
                .fecha(LocalDate.of(2025, 7, 10))
                .hora(LocalTime.of(9, 0))
                .nombres("Paciente Paginado")
                .apellidoPaterno("Paginado")
                .estado("CONFIRMADA")
                .dentista(dummyDentista)
                .tratamiento(dummyTratamiento)
                .tipoDocumento(dummyTipoDocumento)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<CitaResponse> citasPage = new PageImpl<>(Collections.singletonList(paginatedCitaResponse), pageable, 1);

        Mockito.when(citaService.obtenerCitasPaginadas(
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(Pageable.class)
        )).thenReturn(citasPage);

        mockMvc.perform(get("/api/citas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(paginatedCitaResponse.getId()))
                .andExpect(jsonPath("$.content[0].nombres").value(paginatedCitaResponse.getNombres()))
                .andExpect(jsonPath("$.content[0].dentista.nombres").value(dummyDentista.getNombres()))
                .andExpect(jsonPath("$.content[0].tratamiento.nombre").value(dummyTratamiento.getNombre()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(citaService, Mockito.times(1)).obtenerCitasPaginadas(
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(Pageable.class)
        );
        verify(citaService, Mockito.never()).obtenerCitas(
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any()
        );
    }

    @Test
    @DisplayName("GET /api/citas?all=true - debe devolver todas las citas sin paginación")
    void testObtenerCitasSinPaginacionExplicitamente() throws Exception {
        DentistaResponse dummyDentista = DentistaResponse.builder()
                .id(1L)
                .nombres("Dr. Mock All")
                .apellidoPaterno("Apellido All")
                .build();
        Tratamiento dummyTratamiento = Tratamiento.builder()
                .id(11L)
                .nombre("Consulta")
                .build();
        TipoDocumento dummyTipoDocumento = TipoDocumento.builder()
                .id(2L)
                .nombre("CE")
                .acronimo("CE")
                .build();

        CitaResponse allCitaResponse = CitaResponse.builder()
                .id(2L)
                .fecha(LocalDate.of(2025, 7, 11))
                .hora(LocalTime.of(10, 0))
                .nombres("Paciente All")
                .apellidoPaterno("All")
                .estado("PROGRAMADA")
                .dentista(dummyDentista)
                .tratamiento(dummyTratamiento)
                .tipoDocumento(dummyTipoDocumento)
                .build();

        List<CitaResponse> citas = Collections.singletonList(allCitaResponse);

        Mockito.when(citaService.obtenerCitas(
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any()
        )).thenReturn(citas);

        mockMvc.perform(get("/api/citas")
                        .param("all", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(allCitaResponse.getId()))
                .andExpect(jsonPath("$[0].nombres").value(allCitaResponse.getNombres()))
                .andExpect(jsonPath("$[0].dentista.nombres").value(dummyDentista.getNombres()))
                .andExpect(jsonPath("$[0].tratamiento.nombre").value(dummyTratamiento.getNombre()));

        verify(citaService, Mockito.times(1)).obtenerCitas(
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any()
        );
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

        mockMvc.perform(post("/api/citas/validar-disponibilidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("PUT /api/citas/atender/{id} - debe atender la cita")
    void testAtenderCita() throws Exception {
        mockMvc.perform(patch("/api/citas/1/atender"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/citas/reprogramar/{id} - debe reprogramar cita")
    void testReprogramarCita() throws Exception {
        CitaReprogramarRequest request = CitaReprogramarRequest.builder()
                .fecha(LocalDate.of(2025, 5, 1))
                .hora(LocalTime.of(15, 0))
                .build();

        mockMvc.perform(patch("/api/citas/1/reprogramar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
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
                .andExpect(status().isCreated());
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
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe cancelar una cita y retornar 200 OK") // Update display name to reflect cancellation
    void testCancelarCita() throws Exception { // Update method name to reflect cancellation
        Long citaId = 1L;
        String observaciones = "Paciente no pudo asistir.";
        CancelacionCitaRequest requestBody = new CancelacionCitaRequest(observaciones);

        // Mock the service call, as it's a void method
        // (assuming citaService is your MockBean for CitaService)
        doNothing().when(citaService).eliminarCita(citaId, observaciones);

        mockMvc.perform(patch("/api/citas/{id}/cancelar", citaId) // <--- Use PATCH method and /cancelar path
                        .contentType(MediaType.APPLICATION_JSON) // <--- Set content type
                        .content(objectMapper.writeValueAsString(requestBody))) // <--- Include request body
                .andExpect(status().isOk()); // <--- Expect 200 OK for ResponseEntity<Void>

        // Verify that the service method was called with the correct arguments
        verify(citaService, times(1)).eliminarCita(citaId, observaciones);
    }
}