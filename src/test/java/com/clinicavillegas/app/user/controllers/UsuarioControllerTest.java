package com.clinicavillegas.app.user.controllers;

import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.user.dto.request.UsuarioRequest;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.services.UsuarioService;
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

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(UsuarioControllerTest.Config.class)
@AutoConfigureMockMvc(addFilters = false)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @TestConfiguration
    static class Config {
        @Bean
        public UsuarioService usuarioService() {
            return Mockito.mock(UsuarioService.class);
        }
        @Bean
        public JwtService jwtService() {
            return mock(JwtService.class);
        }
    }

    @Test
    @DisplayName("GET /api/usuarios - debe devolver lista de usuarios")
    void testObtenerClientes() throws Exception {
        // Mock de respuesta
        UsuarioResponse usuario1 = UsuarioResponse.builder()
                .id(1L)
                .nombres("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("Gómez")
                .telefono("123456789")
                .correo("juan.perez@mail.com")
                .estado(true)
                .build();

        UsuarioResponse usuario2 = UsuarioResponse.builder()
                .id(2L)
                .nombres("María")
                .apellidoPaterno("Lopez")
                .apellidoMaterno("Ramirez")
                .telefono("987654321")
                .correo("maria.lopez@mail.com")
                .estado(true)
                .build();

        when(usuarioService.obtenerClientes(null, null)).thenReturn(List.of(usuario1, usuario2));

        // Petición al endpoint y validación
        mockMvc.perform(get("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombres").value("Juan"))
                .andExpect(jsonPath("$[0].apellidoPaterno").value("Pérez"))
                .andExpect(jsonPath("$[0].estado").value(true))
                .andExpect(jsonPath("$[1].nombres").value("María"));
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - debe devolver un usuario por ID")
    void testObtenerClientePorId() throws Exception {
        // Mock de respuesta
        UsuarioResponse response = UsuarioResponse.builder()
                .id(1L)
                .nombres("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("Gómez")
                .telefono("123456789")
                .correo("juan.perez@mail.com")
                .estado(true)
                .build();

        when(usuarioService.obtenerClientePorId(1L)).thenReturn(response);

        // Petición al endpoint y validación
        mockMvc.perform(get("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombres").value("Juan"))
                .andExpect(jsonPath("$.apellidoMaterno").value("Gómez"))
                .andExpect(jsonPath("$.correo").value("juan.perez@mail.com"));
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} - debe actualizar un usuario")
    void testActualizarCliente() throws Exception {
        // Mock de la petición
        UsuarioRequest request = UsuarioRequest.builder()
                .nombres("Juan Actualizado")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("Gómez")
                .telefono("111222333")
                .imagenPerfil("imagen_actualizada.png")
                .build();

        // Validar que el servicio sea llamado
        Mockito.doNothing().when(usuarioService).actualizarClientePorId(Mockito.eq(1L), Mockito.any(UsuarioRequest.class));

        // Petición al endpoint y validación
        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombres\":\"Juan Actualizado\",\"apellidoPaterno\":\"Pérez\",\"apellidoMaterno\":\"Gómez\",\"telefono\":\"111222333\",\"imagenPerfil\":\"imagen_actualizada.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cliente actualizado con exito"));
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} - debe eliminar un usuario")
    void testEliminarCliente() throws Exception {
        // Validar que el servicio sea llamado
        Mockito.doNothing().when(usuarioService).eliminarCliente(1L);

        // Petición al endpoint y validación
        mockMvc.perform(delete("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cliente eliminado con exito"));
    }
}