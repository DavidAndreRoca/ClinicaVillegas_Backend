package com.clinicavillegas.app.user.controllers;

import com.clinicavillegas.app.auth.services.CookieService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.clinicavillegas.app.user.dto.request.UsuarioRequest;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.models.Rol;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime; // Importar LocalDateTime
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(UsuarioControllerTest.Config.class)
@AutoConfigureMockMvc(addFilters = false) // Deshabilita la seguridad de Spring para este test
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
        @Bean
        public CookieService cookieService(){
            return mock(CookieService.class);
        }
    }

    // --- Tests para GET /api/usuarios (Paginación y Filtros) ---

    @Test
    @DisplayName("GET /api/usuarios - debe devolver una página de usuarios activos por defecto")
    void testObtenerUsuariosPaginadosDefecto() throws Exception {
        // Mock de respuestas de usuarios
        UsuarioResponse usuario1 = UsuarioResponse.builder()
                .id(1L)
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .estado(true)
                .fechaCreacion(LocalDateTime.now()) // Añadir fechas para simular datos reales
                .build();
        UsuarioResponse usuario2 = UsuarioResponse.builder()
                .id(2L)
                .nombres("Maria")
                .apellidoPaterno("Gomez")
                .estado(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        List<UsuarioResponse> usuarios = List.of(usuario1, usuario2);
        Page<UsuarioResponse> page = new PageImpl<>(usuarios, PageRequest.of(0, 10), 2); // Simula una página

        // Configurar el mock del servicio para devolver la página esperada
        when(usuarioService.obtenerUsuariosPaginados(
                isNull(), // nombres es null por defecto
                isNull(), // rol es null por defecto
                eq(true), // estado es true por defecto
                any(PageRequest.class)) // cualquier objeto PageRequest
        ).thenReturn(page);

        // Realizar la petición y verificar
        mockMvc.perform(get("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].nombres").value("Juan"))
                .andExpect(jsonPath("$.content[1].nombres").value("Maria"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("GET /api/usuarios?rol=DENTISTA - debe devolver una página de usuarios filtrados por rol")
    void testObtenerUsuariosPaginadosPorRol() throws Exception {
        UsuarioResponse dentista1 = UsuarioResponse.builder()
                .id(3L)
                .nombres("Dr. Carlos")
                .rol(Rol.DENTISTA)
                .estado(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
        List<UsuarioResponse> dentistas = List.of(dentista1);
        Page<UsuarioResponse> page = new PageImpl<>(dentistas, PageRequest.of(0, 10), 1);

        when(usuarioService.obtenerUsuariosPaginados(
                isNull(),
                eq("DENTISTA"), // Filtro por rol
                eq(true),
                any(PageRequest.class))
        ).thenReturn(page);

        mockMvc.perform(get("/api/usuarios")
                        .param("rol", "DENTISTA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombres").value("Dr. Carlos"))
                .andExpect(jsonPath("$.content[0].rol").value("DENTISTA"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/usuarios?nombres=ang - debe devolver una página de usuarios filtrados por nombre")
    void testObtenerUsuariosPaginadosPorNombres() throws Exception {
        UsuarioResponse angel = UsuarioResponse.builder()
                .id(4L)
                .nombres("ANGEL MARTIN")
                .estado(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
        List<UsuarioResponse> angela = List.of(angel);
        Page<UsuarioResponse> page = new PageImpl<>(angela, PageRequest.of(0, 10), 1);

        when(usuarioService.obtenerUsuariosPaginados(
                eq("ang"), // Filtro por nombres
                isNull(),
                eq(true),
                any(PageRequest.class))
        ).thenReturn(page);

        mockMvc.perform(get("/api/usuarios")
                        .param("nombres", "ang")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombres").value("ANGEL MARTIN"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/usuarios?all=true - debe devolver una lista completa de usuarios (sin paginación)")
    void testObtenerUsuariosSinPaginacion() throws Exception {
        UsuarioResponse usuario1 = UsuarioResponse.builder()
                .id(1L)
                .nombres("Juan")
                .estado(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
        UsuarioResponse usuario2 = UsuarioResponse.builder()
                .id(2L)
                .nombres("Maria")
                .estado(false) // Incluir uno inactivo para probar que el filtro estado=true por defecto funciona
                .fechaCreacion(LocalDateTime.now())
                .build();

        // Cuando all=true, se llama a obtenerUsuarios sin Pageable.
        // El filtro de estado por defecto sigue siendo true.
        when(usuarioService.obtenerUsuarios(
                isNull(), // nombres es null
                isNull(), // rol es null
                eq(true)) // estado es true
        ).thenReturn(List.of(usuario1)); // Solo Juan si Maria está inactiva y estado=true por defecto

        mockMvc.perform(get("/api/usuarios")
                        .param("all", "true") // Activar la bandera all
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // Solo se espera 1 usuario (Juan)
                .andExpect(jsonPath("$[0].nombres").value("Juan"))
                .andExpect(jsonPath("$[0].estado").value(true)); // Asegurarse que es activo
    }


    // --- Tests para GET /api/usuarios/{id} ---

    @Test
    @DisplayName("GET /api/usuarios/{id} - debe devolver un usuario por ID")
    void testObtenerUsuarioPorId() throws Exception {
        // Mock de respuesta
        UsuarioResponse response = UsuarioResponse.builder()
                .id(1L)
                .nombres("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("Gómez")
                .telefono("123456789")
                .correo("juan.perez@mail.com")
                .estado(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        // Asegúrate de usar el nombre de método correcto: obtenerUsuarioPorId
        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(response);

        // Petición al endpoint y validación
        mockMvc.perform(get("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombres").value("Juan"))
                .andExpect(jsonPath("$.apellidoMaterno").value("Gómez"))
                .andExpect(jsonPath("$.correo").value("juan.perez@mail.com"));
    }

    // --- Tests para PUT /api/usuarios/{id} ---

    @Test
    @DisplayName("PUT /api/usuarios/{id} - debe actualizar un usuario")
    void testActualizarUsuario() throws Exception {
        // Mock de la petición
        UsuarioRequest request = UsuarioRequest.builder()
                .nombres("Juan Actualizado")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("Gómez")
                .telefono("111222333")
                .imagenPerfil("imagen_actualizada.png")
                .build();

        // Validar que el servicio sea llamado
        // Asegúrate de usar el nombre de método correcto: actualizarUsuarioPorId
        Mockito.doNothing().when(usuarioService).actualizarUsuarioPorId(Mockito.eq(1L), Mockito.any(UsuarioRequest.class));

        // Petición al endpoint y validación
        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombres\":\"Juan Actualizado\",\"apellidoPaterno\":\"Pérez\",\"apellidoMaterno\":\"Gómez\",\"telefono\":\"111222333\",\"imagenPerfil\":\"imagen_actualizada.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario actualizado con éxito")); // Mensaje actualizado
    }

    // --- Tests para DELETE /api/usuarios/{id} ---

    @Test
    @DisplayName("DELETE /api/usuarios/{id} - debe eliminar lógicamente un usuario")
    void testEliminarUsuario() throws Exception {
        // Validar que el servicio sea llamado
        // Asegúrate de usar el nombre de método correcto: eliminarUsuario
        Mockito.doNothing().when(usuarioService).eliminarUsuario(1L);

        // Petición al endpoint y validación
        mockMvc.perform(delete("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario eliminado (lógicamente) con éxito")); // Mensaje actualizado
    }
}