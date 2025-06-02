package com.clinicavillegas.app.user.controllers;

import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.user.dto.request.TipoDocumentoRequest;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.services.TipoDocumentoService;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TipoDocumentoController.class)
@Import(TipoDocumentoControllerTest.Config.class)
@AutoConfigureMockMvc(addFilters = false)
public class TipoDocumentoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TipoDocumentoService tipoDocumentoService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class Config {
        @Bean
        public TipoDocumentoService tipoTratamientoService() {
            return Mockito.mock(TipoDocumentoService.class);
        }
        @Bean
        public JwtService jwtService() {
            return mock(JwtService.class);
        }
    }
    @Test
    void testObtenerTiposDocumento() throws Exception {
        TipoDocumento doc1 = new TipoDocumento(1L, "DNI", "DNI", true);
        TipoDocumento doc2 = new TipoDocumento(2L, "Pasaporte", "PAS", true);

        when(tipoDocumentoService.obtenerTiposDocumento(null, null))
                .thenReturn(List.of(doc1, doc2));

        mockMvc.perform(get("/api/tipo-documento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("DNI"));
    }

    @Test
    void testObtenerTiposDocumentoConFiltros() throws Exception {
        TipoDocumento doc = new TipoDocumento(3L, "Cédula", "CED", true);

        when(tipoDocumentoService.obtenerTiposDocumento("Cédula", "CED"))
                .thenReturn(List.of(doc));

        mockMvc.perform(get("/api/tipo-documento")
                        .param("nombre", "Cédula")
                        .param("acronimo", "CED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].acronimo").value("CED"));
    }

    @Test
    void testObtenerTipoDocumentoPorId() throws Exception {
        TipoDocumento doc = new TipoDocumento(1L, "DNI", "DNI", true);

        when(tipoDocumentoService.obtenerTipoDocumento(1L)).thenReturn(doc);

        mockMvc.perform(get("/api/tipo-documento/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("DNI"));
    }

    @Test
    void testObtenerTipoDocumentoPorIdNotFound() throws Exception {
        when(tipoDocumentoService.obtenerTipoDocumento(99L)).thenReturn(null);

        mockMvc.perform(get("/api/tipo-documento/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAgregarTipoDocumento() throws Exception {
        TipoDocumentoRequest request = new TipoDocumentoRequest("Licencia", "LIC");

        doNothing().when(tipoDocumentoService).agregarTipoDocumento(any());

        mockMvc.perform(post("/api/tipo-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tipo de documento agregado con exito"));
    }

    @Test
    void testActualizarTipoDocumento() throws Exception {
        TipoDocumentoRequest request = new TipoDocumentoRequest("Pasaporte actualizado", "PAS");

        doNothing().when(tipoDocumentoService).actualizarTipoDocumento(eq(1L), any());

        mockMvc.perform(put("/api/tipo-documento/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tipo de documento actualizado con exito"));
    }

    @Test
    void testEliminarTipoDocumento() throws Exception {
        doNothing().when(tipoDocumentoService).eliminarTipoDocumento(2L);

        mockMvc.perform(delete("/api/tipo-documento/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tipo de documento eliminado con exito"));
    }
}
