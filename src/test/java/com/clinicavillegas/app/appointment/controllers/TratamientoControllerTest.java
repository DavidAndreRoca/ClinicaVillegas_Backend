package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.TratamientoRequest;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.appointment.services.TratamientoService;
import com.clinicavillegas.app.auth.services.CookieService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*; // Mantén solo esta línea para Mockito. Los otros matchers (any, isNull, eq) se importan con este comodín.

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(TratamientoController.class)
@Import(TratamientoControllerTest.Config.class)
@AutoConfigureMockMvc(addFilters = false)
public class TratamientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TratamientoService tratamientoService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class Config {
        @Bean
        public TratamientoService tratamientoService() {
            return Mockito.mock(TratamientoService.class);
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
    void testObtenerTratamientosPorEstadoFalse() throws Exception {
        Tratamiento inactivo1 = Tratamiento.builder()
                .id(3L)
                .nombre("Revisión Inactiva")
                .descripcion("Revisión para tratamientos inactivos")
                .costo(BigDecimal.valueOf(50.00))
                .duracion(Duration.ofMinutes(30))
                .estado(false)
                .imagenURL("url_inactivo1.jpg")
                .build();

        List<Tratamiento> tratamientosInactivos = Arrays.asList(inactivo1);

        // Se espera que el controlador llame a obtenerTratamientosPaginados
        when(tratamientoService.obtenerTratamientosPaginados(isNull(), isNull(), eq(false), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(tratamientosInactivos));

        mockMvc.perform(get("/api/tratamientos")
                        .param("estado", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(3L))
                .andExpect(jsonPath("$.content[0].nombre").value("Revisión Inactiva"))
                .andExpect(jsonPath("$.content[0].estado").value(false));
    }

    @Test
    void testObtenerTratamientosPorEstadoTrue() throws Exception {
        Tratamiento activo1 = Tratamiento.builder()
                .id(4L)
                .nombre("Limpieza Activa")
                .descripcion("Limpieza para tratamientos activos")
                .costo(BigDecimal.valueOf(100.00))
                .duracion(Duration.ofMinutes(45))
                .estado(true)
                .imagenURL("url_activo1.jpg")
                .build();

        List<Tratamiento> tratamientosActivos = Arrays.asList(activo1);

        // Se espera que el controlador llame a obtenerTratamientosPaginados
        when(tratamientoService.obtenerTratamientosPaginados(isNull(), isNull(), eq(true), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(tratamientosActivos));

        mockMvc.perform(get("/api/tratamientos")
                        .param("estado", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(4L))
                .andExpect(jsonPath("$.content[0].nombre").value("Limpieza Activa"))
                .andExpect(jsonPath("$.content[0].estado").value(true));
    }

    @Test
    void testObtenerTratamientosPorDefecto() throws Exception {
        Tratamiento activoDefault1 = Tratamiento.builder()
                .id(5L)
                .nombre("Consulta General Activa")
                .descripcion("Consulta para comportamiento por defecto")
                .costo(BigDecimal.valueOf(75.00))
                .duracion(Duration.ofMinutes(60))
                .estado(true)
                .imagenURL("url_default_activo1.jpg")
                .build();

        List<Tratamiento> tratamientosActivosDefault = Arrays.asList(activoDefault1);

        // Se espera que el controlador llame a obtenerTratamientosPaginados
        // Se corrige el matcher para 'estado' a 'eq(true)' ya que el valor por defecto en el controlador es true.
        when(tratamientoService.obtenerTratamientosPaginados(isNull(), isNull(), eq(true), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(tratamientosActivosDefault));

        mockMvc.perform(get("/api/tratamientos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(5L))
                .andExpect(jsonPath("$.content[0].nombre").value("Consulta General Activa"))
                .andExpect(jsonPath("$.content[0].estado").value(true));
    }

    @Test
    void testObtenerTodosTratamientosConAllTrue() throws Exception {
        Tratamiento allTratamiento1 = Tratamiento.builder()
                .id(6L)
                .nombre("Tratamiento Completo 1")
                .descripcion("Descripcion 1")
                .costo(BigDecimal.valueOf(200.00))
                .duracion(Duration.ofMinutes(60))
                .estado(true)
                .imagenURL("url_all1.jpg")
                .build();

        Tratamiento allTratamiento2 = Tratamiento.builder()
                .id(7L)
                .nombre("Tratamiento Completo 2")
                .descripcion("Descripcion 2")
                .costo(BigDecimal.valueOf(300.00))
                .duracion(Duration.ofMinutes(90))
                .estado(false) // Incluimos uno inactivo para mostrar que 'all' los trae
                .imagenURL("url_all2.jpg")
                .build();

        List<Tratamiento> todosLosTratamientos = Arrays.asList(allTratamiento1, allTratamiento2);

        // Se espera que el controlador llame a obtenerTodosTratamientos cuando 'all' es true
        // Se corrige el matcher para 'estado' a 'eq(true)' ya que el valor por defecto en el controlador es true.
        when(tratamientoService.obtenerTodosTratamientos(isNull(), isNull(), eq(true)))
                .thenReturn(todosLosTratamientos);

        mockMvc.perform(get("/api/tratamientos")
                        .param("all", "true") // Aquí indicamos all=true
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Se espera una lista directa, no un objeto Page
                .andExpect(jsonPath("$[0].id").value(6L))
                .andExpect(jsonPath("$[0].nombre").value("Tratamiento Completo 1"))
                .andExpect(jsonPath("$[1].id").value(7L))
                .andExpect(jsonPath("$[1].nombre").value("Tratamiento Completo 2"))
                .andExpect(jsonPath("$[1].estado").value(false)); // Validar que incluye inactivos
    }

    @Test
    void testGuardarTratamiento() throws Exception {
        TratamientoRequest request = TratamientoRequest.builder()
                .nombre("Limpieza Dental")
                .descripcion("Eliminación de placa y sarro")
                .costo(BigDecimal.valueOf(90.00))
                .duracion(45)
                .imagenURL("limpieza.jpg")
                .tipoTratamientoId(1L)
                .build();

        doNothing().when(tratamientoService).guardarTratamiento(Mockito.any(TratamientoRequest.class));

        mockMvc.perform(post("/api/tratamientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Tratamiento guardado con éxito"));
    }

    @Test
    void testActualizarTratamiento() throws Exception {
        TratamientoRequest request = TratamientoRequest.builder()
                .nombre("Implante Dental")
                .descripcion("Reemplazo de un diente perdido")
                .costo(BigDecimal.valueOf(2500.00))
                .duracion(120)
                .imagenURL("implante.jpg")
                .tipoTratamientoId(2L)
                .build();

        doNothing().when(tratamientoService).actualizarTratamiento(eq(1L), Mockito.any(TratamientoRequest.class));

        mockMvc.perform(put("/api/tratamientos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Tratamiento actualizado con éxito"));
    }

    @Test
    void testEliminarTratamiento() throws Exception {
        doNothing().when(tratamientoService).eliminarTratamiento(1L);

        mockMvc.perform(delete("/api/tratamientos/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tratamiento eliminado con éxito"));
    }
}