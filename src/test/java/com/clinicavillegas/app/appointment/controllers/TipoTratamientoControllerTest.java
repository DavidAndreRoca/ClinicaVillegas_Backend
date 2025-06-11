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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

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

    @Test
    void testListarTiposDeTratamiento() throws Exception{
        TipoTratamiento tipoTratamiento1 = new TipoTratamiento(1L, "Ortodoncias", true);
        TipoTratamiento tipoTratamiento2 = new TipoTratamiento(2L, "Estética dental", true);

        when(tipoTratamientoService.obtenerTiposTratamiento()).thenReturn(Arrays.asList(
                tipoTratamiento1,
                tipoTratamiento2
        ));
        mockMvc.perform(get(EndpointPaths.TIPO_TRATAMIENTO_BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
    @Test
    void testAgregarTipoTratamiento() throws Exception {
        TipoTratamientoRequest request = new TipoTratamientoRequest();
        request.setNombre("Implante");

        doNothing().when(tipoTratamientoService).agregarTipoTratamiento(any());

        mockMvc.perform(post(EndpointPaths.TIPO_TRATAMIENTO_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tipo de tratamiento agregado con exito"));
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
                .andExpect(jsonPath("$.mensaje").value("Tipo de tratamiento actualizado con exito"));
    }

    @Test
    void testEliminarTipoTratamiento() throws Exception {
        doNothing().when(tipoTratamientoService).eliminarTipoTratamiento(1L);

        mockMvc.perform(delete(EndpointPaths.TIPO_TRATAMIENTO_BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tipo de tratamiento eliminado con exito"));
    }
}
