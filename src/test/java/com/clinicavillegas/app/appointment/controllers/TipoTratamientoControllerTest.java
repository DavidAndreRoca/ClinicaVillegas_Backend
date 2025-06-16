package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.TipoTratamientoRequest;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.services.TipoTratamientoService;
import com.clinicavillegas.app.auth.services.CookieService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.common.EndpointPaths;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TipoTratamientoController.class)
@Import(TipoTratamientoControllerTest.Config.class)
@AutoConfigureMockMvc(addFilters = false)
public class TipoTratamientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TipoTratamientoService tipoTratamientoService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class Config {
        @Bean
        public TipoTratamientoService tipoTratamientoService() {
            return Mockito.mock(TipoTratamientoService.class);
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

    // --- TEST PARA LISTAR TIPOS DE TRATAMIENTO CON PAGINACIÓN ---

    @Test
    void testListarTiposDeTratamientoPaginadoPorDefecto() throws Exception {
        // Datos de prueba: Asumimos que estos son tipos de tratamiento activos
        TipoTratamiento tipoTratamiento1 = new TipoTratamiento(1L, "Ortodoncia", true);
        TipoTratamiento tipoTratamiento2 = new TipoTratamiento(2L, "Estética Dental", true);
        TipoTratamiento tipoTratamiento3 = new TipoTratamiento(3L, "Cirugías", true);

        // Crear una lista de tipo de tratamientos para la página
        List<TipoTratamiento> content = Arrays.asList(tipoTratamiento3, tipoTratamiento2, tipoTratamiento1);

        // Crear un objeto Page simulado
        Pageable defaultPageable = PageRequest.of(0, 10, Sort.by("nombre").ascending());
        Page<TipoTratamiento> simulatedPage = new PageImpl<>(content, defaultPageable, 3);

        // Configurar el mock del servicio: Ahora esperamos 'true' para 'estado' por defecto
        when(tipoTratamientoService.obtenerTiposTratamientoPaginados(
                eq(null), // nombre
                eq(true), // estado, ahora por defecto es TRUE
                any(Pageable.class)
        )).thenReturn(simulatedPage);

        // Realizar la petición sin especificar estado, esperando el valor por defecto
        mockMvc.perform(get(EndpointPaths.TIPO_TRATAMIENTO_BASE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Cirugías"));
    }

    @Test
    void testListarTiposDeTratamientoPaginadoConParametros() throws Exception {
        // Datos de prueba (solo los que estarían en la página solicitada)
        // Asumimos que estos son tipos de tratamiento activos
        TipoTratamiento tipoTratamiento1 = new TipoTratamiento(1L, "Ortodoncia", true);
        TipoTratamiento tipoTratamiento2 = new TipoTratamiento(2L, "Estética Dental", true);

        // Crear una lista de tipo de tratamientos para la página
        List<TipoTratamiento> content = Arrays.asList(tipoTratamiento1, tipoTratamiento2);

        // Crear un objeto Page simulado con parámetros específicos
        Pageable customPageable = PageRequest.of(0, 2, Sort.by("nombre").descending());
        Page<TipoTratamiento> simulatedPage = new PageImpl<>(content, customPageable, 5); // Total 5 elementos, asumiendo 5 activos

        // Configurar el mock del servicio: Ahora esperamos 'true' para 'estado' por defecto
        when(tipoTratamientoService.obtenerTiposTratamientoPaginados(
                eq(null), // nombre
                eq(true), // estado, ahora por defecto es TRUE
                any(Pageable.class)
        )).thenReturn(simulatedPage);

        // Realizar la petición con parámetros y verificar
        mockMvc.perform(get(EndpointPaths.TIPO_TRATAMIENTO_BASE)
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "nombre,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content[0].nombre").value("Ortodoncia"))
                .andExpect(jsonPath("$.content[1].nombre").value("Estética Dental"));
    }

    @Test
    void testListarTiposDeTratamientoPaginadoFiltrandoPorEstadoInactivo() throws Exception {
        // Datos de prueba: Un tipo de tratamiento inactivo
        TipoTratamiento tipoTratamientoInactivo = new TipoTratamiento(4L, "Revisión Inactiva", false);

        List<TipoTratamiento> content = Collections.singletonList(tipoTratamientoInactivo);
        Pageable customPageable = PageRequest.of(0, 10, Sort.by("nombre").ascending());
        Page<TipoTratamiento> simulatedPage = new PageImpl<>(content, customPageable, 1);

        // Configurar el mock del servicio: Esperamos 'false' para 'estado'
        when(tipoTratamientoService.obtenerTiposTratamientoPaginados(
                eq(null), // nombre
                eq(false), // estado, ahora explícitamente false
                any(Pageable.class)
        )).thenReturn(simulatedPage);

        // Realizar la petición con parámetro estado=false
        mockMvc.perform(get(EndpointPaths.TIPO_TRATAMIENTO_BASE)
                        .param("estado", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Revisión Inactiva"))
                .andExpect(jsonPath("$.content[0].estado").value(false));
    }


    @Test
    void testListarTodosTiposDeTratamientoConParametroAll() throws Exception {
        // Datos de prueba (todos los elementos): Asumimos que estos son activos
        TipoTratamiento tipoTratamiento1 = new TipoTratamiento(1L, "Ortodoncia", true);
        TipoTratamiento tipoTratamiento2 = new TipoTratamiento(2L, "Estética Dental", true);
        TipoTratamiento tipoTratamiento3 = new TipoTratamiento(3L, "Cirugías", true);

        List<TipoTratamiento> allContent = Arrays.asList(tipoTratamiento1, tipoTratamiento2, tipoTratamiento3);

        // Configurar el mock del servicio para cuando 'all' es true: Ahora esperamos 'true' para 'estado' por defecto
        when(tipoTratamientoService.obtenerTiposTratamiento(
                eq(null),
                eq(true) // estado, ahora por defecto es TRUE
        )).thenReturn(allContent);

        // Realizar la petición con all=true sin especificar estado
        mockMvc.perform(get(EndpointPaths.TIPO_TRATAMIENTO_BASE)
                        .param("all", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].nombre").value("Ortodoncia"));
    }

    @Test
    void testListarTodosTiposDeTratamientoConParametroAllYEstadoInactivo() throws Exception {
        // Datos de prueba para cuando all=true y estado=false
        TipoTratamiento tipoTratamientoInactivo1 = new TipoTratamiento(4L, "Revision Inactiva 1", false);
        TipoTratamiento tipoTratamientoInactivo2 = new TipoTratamiento(5L, "Revision Inactiva 2", false);

        List<TipoTratamiento> allInactiveContent = Arrays.asList(tipoTratamientoInactivo1, tipoTratamientoInactivo2);

        // Configurar el mock del servicio para cuando 'all' es true y 'estado' es false
        when(tipoTratamientoService.obtenerTiposTratamiento(
                eq(null),
                eq(false) // estado, ahora explícitamente false
        )).thenReturn(allInactiveContent);

        // Realizar la petición con all=true y estado=false
        mockMvc.perform(get(EndpointPaths.TIPO_TRATAMIENTO_BASE)
                        .param("all", "true")
                        .param("estado", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].estado").value(false))
                .andExpect(jsonPath("$[1].estado").value(false));
    }


    // --- TESTS EXISTENTES (NO MODIFICADOS) ---

    @Test
    void testAgregarTipoTratamiento() throws Exception {
        TipoTratamientoRequest request = new TipoTratamientoRequest();
        request.setNombre("Implante");

        doNothing().when(tipoTratamientoService).agregarTipoTratamiento(any());

        mockMvc.perform(post(EndpointPaths.TIPO_TRATAMIENTO_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tipo de tratamiento agregado con éxito"));
    }

    @Test
    void testActualizarTipoTratamiento() throws Exception {
        TipoTratamientoRequest request = new TipoTratamientoRequest();
        request.setNombre("Limpieza");

        doNothing().when(tipoTratamientoService).actualizarTipoTratamiento(eq(1L), any());

        mockMvc.perform(put(EndpointPaths.TIPO_TRATAMIENTO_BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tipo de tratamiento actualizado con éxito"));
    }

    @Test
    void testEliminarTipoTratamiento() throws Exception {
        doNothing().when(tipoTratamientoService).eliminarTipoTratamiento(1L);

        mockMvc.perform(delete(EndpointPaths.TIPO_TRATAMIENTO_BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tipo de tratamiento eliminado con éxito"));
    }
}