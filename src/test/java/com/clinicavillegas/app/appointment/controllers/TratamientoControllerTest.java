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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    void testObtenerTratamientos() throws Exception {
        // Datos de prueba
        Tratamiento tratamiento1 = Tratamiento.builder()
                .id(1L)
                .nombre("Blanqueamiento Dental")
                .descripcion("Mejora el color de los dientes")
                .costo(BigDecimal.valueOf(150.00))
                .duracion(Duration.ofMinutes(60))
                .estado(true)
                .imagenURL("url1.jpg")
                .build();

        Tratamiento tratamiento2 = Tratamiento.builder()
                .id(2L)
                .nombre("Ortodoncia")
                .descripcion("Alineación de los dientes")
                .costo(BigDecimal.valueOf(1200.00))
                .duracion(Duration.ofHours(2))
                .estado(true)
                .imagenURL("url2.jpg")
                .build();

        List<Tratamiento> tratamientos = Arrays.asList(tratamiento1, tratamiento2);

        // Simulación del servicio
        when(tratamientoService.obtenerTratamientos(null, null)).thenReturn(tratamientos);

        // Llamada al endpoint y validación
        mockMvc.perform(get("/api/tratamientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Blanqueamiento Dental"))
                .andExpect(jsonPath("$[0].descripcion").value("Mejora el color de los dientes"))
                .andExpect(jsonPath("$[0].costo").value(150.00))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].nombre").value("Ortodoncia"))
                .andExpect(jsonPath("$[1].descripcion").value("Alineación de los dientes"));
    }

    @Test
    void testGuardarTratamiento() throws Exception {
        // Datos de prueba
        TratamientoRequest request = TratamientoRequest.builder()
                .nombre("Limpieza Dental")
                .descripcion("Eliminación de placa y sarro")
                .costo(BigDecimal.valueOf(90.00))
                .duracion(45)
                .imagenURL("limpieza.jpg")
                .tipoTratamientoId(1L)
                .build();

        // Simulación del servicio
        doNothing().when(tratamientoService).guardarTratamiento(any());

        // Llamada al endpoint y validación
        mockMvc.perform(post("/api/tratamientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tratamiento guardado con exito"));
    }

    @Test
    void testActualizarTratamiento() throws Exception {
        // Datos de prueba
        TratamientoRequest request = TratamientoRequest.builder()
                .nombre("Implante Dental")
                .descripcion("Reemplazo de un diente perdido")
                .costo(BigDecimal.valueOf(2500.00))
                .duracion(120)
                .imagenURL("implante.jpg")
                .tipoTratamientoId(2L)
                .build();

        // Simulación del servicio
        doNothing().when(tratamientoService).actualizarTratamiento(eq(1L), any());

        // Llamada al endpoint y validación
        mockMvc.perform(put("/api/tratamientos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tratamiento actualizado con exito"));
    }

    @Test
    void testEliminarTratamiento() throws Exception {
        // Simulación del servicio
        doNothing().when(tratamientoService).eliminarTratamiento(1L);

        // Llamada al endpoint y validación
        mockMvc.perform(delete("/api/tratamientos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tratamiento eliminado con exito"));
    }
}