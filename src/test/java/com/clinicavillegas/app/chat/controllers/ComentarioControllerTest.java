package com.clinicavillegas.app.chat.controllers;

import com.clinicavillegas.app.auth.services.CookieService;
import com.clinicavillegas.app.chat.dto.request.ComentarioRequest;
import com.clinicavillegas.app.chat.dto.response.ComentarioResponse;
import com.clinicavillegas.app.chat.services.ComentarioService;
import com.clinicavillegas.app.auth.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest; // Necesitas este import
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComentarioController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ComentarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComentarioService comentarioService;

    @TestConfiguration
    static class Config {
        @Bean
        public ComentarioService comentarioService() {
            return Mockito.mock(ComentarioService.class);
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
    void testObtenerComentarios() throws Exception {
        // Crear una lista simulada de comentarios
        List<ComentarioResponse> comentariosList = List.of(
                ComentarioResponse.builder()
                        .id(1L)
                        .contenido("Primer comentario")
                        .fecha(LocalDateTime.now())
                        .nombresUsuario("Usuario 1")
                        .emailUsuario("usuario1@domain.com")
                        .imagenUsuario("imagen1.png")
                        .build(),
                ComentarioResponse.builder()
                        .id(2L)
                        .contenido("Segundo comentario")
                        .fecha(LocalDateTime.now())
                        .nombresUsuario("Usuario 2")
                        .emailUsuario("usuario2@domain.com")
                        .imagenUsuario("imagen2.png")
                        .build()
        );

        // Crear un objeto Page a partir de la lista, especificando pageable y totalElements
        // El tamaño de página por defecto de Spring Data es 10, así que lo replicamos aquí.
        // PageRequest.of(número_de_página, tamaño_de_página)
        Pageable defaultPageable = PageRequest.of(0, 10);
        Page<ComentarioResponse> comentariosPage = new PageImpl<>(comentariosList, defaultPageable, comentariosList.size());


        // Configurar Mock para el servicio
        when(comentarioService.obtenerComentarios(any(Pageable.class))).thenReturn(comentariosPage);

        // Realizar la solicitud GET al endpoint /comentarios
        mockMvc.perform(get("/api/comentarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].contenido").value("Primer comentario"))
                .andExpect(jsonPath("$.content[1].contenido").value("Segundo comentario"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                // Aquí, el assertion para 'size' ahora coincidirá con el 'size' del Pageable mockeado
                .andExpect(jsonPath("$.size").value(10)); // <-- Este valor debe coincidir con el size del PageRequest.of() arriba
    }

    @Test
    void testAgregarComentario() throws Exception {
        // Crear una solicitud válida para agregar un comentario
        ComentarioRequest request = ComentarioRequest.builder()
                .contenido("Nuevo comentario")
                .usuarioId(1L)
                .build();

        // Configurar Mock para el servicio
        doNothing().when(comentarioService).agregarComentario(any(ComentarioRequest.class));

        // Realizar la solicitud POST al endpoint /comentarios
        mockMvc.perform(post("/api/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Comentario agregado con exito"));
    }
}