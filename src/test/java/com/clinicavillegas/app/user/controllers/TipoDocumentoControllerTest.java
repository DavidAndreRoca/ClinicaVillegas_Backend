package com.clinicavillegas.app.user.controllers;

import com.clinicavillegas.app.auth.services.CookieService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.user.dto.request.TipoDocumentoRequest;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.services.TipoDocumentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach; // Importar BeforeEach
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        @Bean
        public CookieService cookieService(){
            return mock(CookieService.class);
        }
    }

    @BeforeEach
    void setup() {
        // Resetear el mock antes de cada test para asegurar la independencia
        Mockito.reset(tipoDocumentoService);
    }

    @Test
    void testObtenerTiposDocumentoPaginadoPorDefecto() throws Exception {
        TipoDocumento doc1 = new TipoDocumento(1L, "Carnet de extranjería", "CARNET EXT.", true);
        TipoDocumento doc2 = new TipoDocumento(2L, "Documento Nacional de Identidad", "DNI", true);
        TipoDocumento doc3 = new TipoDocumento(3L, "Pasaporte", "PASAPORTE", true);

        List<TipoDocumento> docs = List.of(doc1, doc2, doc3);

        Pageable defaultPageable = PageRequest.of(0, 10, Sort.by("nombre").ascending());

        Page<TipoDocumento> page = new PageImpl<>(docs, defaultPageable, docs.size());

        when(tipoDocumentoService.obtenerTiposDocumentoPaginados(eq(null), eq(null), eq(defaultPageable)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tipo-documento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].nombre").value("Carnet de extranjería"))
                .andExpect(jsonPath("$.content[1].nombre").value("Documento Nacional de Identidad"))
                .andExpect(jsonPath("$.content[2].nombre").value("Pasaporte"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(tipoDocumentoService, times(1)).obtenerTiposDocumentoPaginados(eq(null), eq(null), eq(defaultPageable));
        verify(tipoDocumentoService, never()).obtenerTiposDocumento(any(), any());
    }

    @Test
    void testObtenerTiposDocumentoPaginadoConFiltros() throws Exception {
        TipoDocumento doc = new TipoDocumento(3L, "Cédula de Identidad", "CED", true);

        List<TipoDocumento> docs = List.of(doc);
        Pageable pageableWithFilters = PageRequest.of(0, 10, Sort.by("nombre").ascending());

        Page<TipoDocumento> page = new PageImpl<>(docs, pageableWithFilters, docs.size());

        when(tipoDocumentoService.obtenerTiposDocumentoPaginados(eq("Cédula"), eq("CED"), eq(pageableWithFilters)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tipo-documento")
                        .param("nombre", "Cédula")
                        .param("acronimo", "CED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Cédula de Identidad"))
                .andExpect(jsonPath("$.content[0].acronimo").value("CED"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));


        verify(tipoDocumentoService, times(1)).obtenerTiposDocumentoPaginados(eq("Cédula"), eq("CED"), eq(pageableWithFilters));
        verify(tipoDocumentoService, never()).obtenerTiposDocumento(any(), any());
    }

    @Test
    void testObtenerTiposDocumentoPaginadoVacioPorPagina() throws Exception {
        List<TipoDocumento> allDocs = List.of(
                new TipoDocumento(1L, "DocA", "A", true),
                new TipoDocumento(2L, "DocB", "B", true),
                new TipoDocumento(3L, "DocC", "C", true)
        );
        Pageable requestedPageable = PageRequest.of(1, 2, Sort.by("nombre").ascending());

        Page<TipoDocumento> emptyPage = new PageImpl<>(Collections.emptyList(), requestedPageable, allDocs.size());

        when(tipoDocumentoService.obtenerTiposDocumentoPaginados(eq(null), eq(null), eq(requestedPageable)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/tipo-documento")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(tipoDocumentoService, times(1)).obtenerTiposDocumentoPaginados(eq(null), eq(null), eq(requestedPageable));
    }


    @Test
    void testObtenerTiposDocumentoAllTrue() throws Exception {
        TipoDocumento doc1 = new TipoDocumento(1L, "DNI", "DNI", true);
        TipoDocumento doc2 = new TipoDocumento(2L, "Pasaporte", "PAS", true);

        when(tipoDocumentoService.obtenerTiposDocumento(eq(null), eq(null)))
                .thenReturn(List.of(doc1, doc2));

        mockMvc.perform(get("/api/tipo-documento")
                        .param("all", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("DNI"));

        verify(tipoDocumentoService, times(1)).obtenerTiposDocumento(eq(null), eq(null));
        verify(tipoDocumentoService, never()).obtenerTiposDocumentoPaginados(any(), any(), any());
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