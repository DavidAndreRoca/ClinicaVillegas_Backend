package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.CancelacionDentistaRequest;
import com.clinicavillegas.app.appointment.dto.request.DentistaRequest;
import com.clinicavillegas.app.appointment.dto.response.DentistaResponse;
import com.clinicavillegas.app.appointment.services.DentistaService;
import com.clinicavillegas.app.auth.services.CookieService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.common.EndpointPaths;
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
import org.springframework.data.domain.Page; // Importar Page
import org.springframework.data.domain.PageImpl; // Importar PageImpl
import org.springframework.data.domain.PageRequest; // Importar PageRequest
import org.springframework.data.domain.Pageable; // Importar Pageable
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DentistaController.class)
@Import(DentistaControllerTest.Config.class)
@AutoConfigureMockMvc(addFilters = false) // Deshabilita los filtros de seguridad, si los tienes
public class DentistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DentistaService dentistaService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class Config {
        @Bean
        public DentistaService dentistaService() {
            return Mockito.mock(DentistaService.class);
        }

        // Si tu controlador usa JwtService o CookieService, mockéalos aquí.
        // Si no los usa directamente, puedes omitirlos.
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
    @DisplayName("GET /api/dentistas - debe devolver una página de dentistas (sin filtros, paginación por defecto)")
    void testObtenerDentistas() throws Exception {
        DentistaResponse response = DentistaResponse.builder()
                .id(1L)
                .nColegiatura("NC12345")
                .nombres("Carlos")
                .apellidoPaterno("Gómez")
                .apellidoMaterno("Fernández")
                .especializacion("Ortodoncia")
                .estado(true)
                .usuarioId(10L)
                .correo("carlos.gomez@mail.com")
                .telefono("999888777")
                .sexo("M")
                .build();

        Page<DentistaResponse> mockPage = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        Mockito.when(dentistaService.obtenerDentistasPaginados(
                        Mockito.eq(null),
                        Mockito.eq(null),
                        Mockito.eq(null),
                        Mockito.any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get(EndpointPaths.DENTISTA_BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].ncolegiatura").value("NC12345"))
                .andExpect(jsonPath("$.content[0].nombres").value("Carlos"))
                .andExpect(jsonPath("$.content[0].apellidoPaterno").value("Gómez"))
                .andExpect(jsonPath("$.content[0].apellidoMaterno").value("Fernández"))
                .andExpect(jsonPath("$.content[0].especializacion").value("Ortodoncia"))
                .andExpect(jsonPath("$.content[0].estado").value(true))
                .andExpect(jsonPath("$.content[0].usuarioId").value(10))
                .andExpect(jsonPath("$.content[0].correo").value("carlos.gomez@mail.com"))
                .andExpect(jsonPath("$.content[0].telefono").value("999888777"))
                .andExpect(jsonPath("$.content[0].sexo").value("M"))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /api/dentistas?all=true - debe devolver una lista de todos los dentistas (sin paginación)")
    void testObtenerTodosLosDentistas() throws Exception {
        DentistaResponse response1 = DentistaResponse.builder().id(1L).nombres("Dentista 1").build();
        DentistaResponse response2 = DentistaResponse.builder().id(2L).nombres("Dentista 2").build();
        List<DentistaResponse> mockList = List.of(response1, response2);

        Mockito.when(dentistaService.obtenerDentistas(
                        Mockito.eq(null),
                        Mockito.eq(null),
                        Mockito.eq(null)))
                .thenReturn(mockList);

        mockMvc.perform(get(EndpointPaths.DENTISTA_BASE).param("all", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("POST /api/dentistas - debe agregar un dentista")
    void testAgregarDentista() throws Exception {
        DentistaRequest request = DentistaRequest.builder()
                .nColegiatura("NC12345")
                .especializacion("Endodoncia")
                .usuarioId(10L)
                .build();

        mockMvc.perform(post(EndpointPaths.DENTISTA_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Dentista agregado con exito"));

        // Verify that the service method was called
        Mockito.verify(dentistaService).agregarDentista(request);
    }

    @Test
    @DisplayName("PUT /api/dentistas/{id} - debe actualizar un dentista")
    void testActualizarDentista() throws Exception {
        DentistaRequest request = DentistaRequest.builder()
                .nColegiatura("NC54321")
                .especializacion("Cirugía Maxilofacial")
                .usuarioId(20L)
                .build();

        Long dentistaId = 1L;

        mockMvc.perform(put(EndpointPaths.DENTISTA_BASE + "/{id}", dentistaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Dentista actualizado con exito"));

        // Verify that the service method was called
        Mockito.verify(dentistaService).actualizarDentista(dentistaId, request);
    }

    @Test
    @DisplayName("PATCH /api/dentistas/{id}/eliminar - debe eliminar lógicamente un dentista")
    void testEliminarDentistaLogicamente() throws Exception {
        Long dentistaId = 1L;
        CancelacionDentistaRequest cancelacionRequest = new CancelacionDentistaRequest("Renuncia voluntaria");

        mockMvc.perform(patch(EndpointPaths.DENTISTA_BASE + "/{id}/eliminar", dentistaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelacionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Dentista eliminado con exito"));

        // Verify that the service method was called with the correct arguments
        Mockito.verify(dentistaService).eliminarDentista(dentistaId, cancelacionRequest);
    }

    @Test
    @DisplayName("GET /api/dentistas/especialidades - debe devolver lista de especialidades")
    void testObtenerEspecialidades() throws Exception {
        List<String> especialidades = List.of("Ortodoncia", "Endodoncia", "Cirugía Maxilofacial");

        Mockito.when(dentistaService.obtenerEspecialidades())
                .thenReturn(especialidades);

        mockMvc.perform(get(EndpointPaths.DENTISTA_BASE + "/especialidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Ortodoncia"))
                .andExpect(jsonPath("$[1]").value("Endodoncia"))
                .andExpect(jsonPath("$[2]").value("Cirugía Maxilofacial"));
    }
}