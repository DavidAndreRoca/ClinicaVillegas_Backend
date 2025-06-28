package com.clinicavillegas.app.chat.services;

import com.clinicavillegas.app.chat.dto.request.ComentarioRequest;
import com.clinicavillegas.app.chat.dto.response.ComentarioResponse;
import com.clinicavillegas.app.chat.models.Comentario;
import com.clinicavillegas.app.chat.repositories.ComentarioRepository;
import com.clinicavillegas.app.chat.services.impl.DefaultComentarioService;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page; // Importar Page
import org.springframework.data.domain.PageImpl; // Importar PageImpl
import org.springframework.data.domain.PageRequest; // Importar PageRequest (útil para el Pageable)
import org.springframework.data.domain.Pageable; // Importar Pageable


import java.time.LocalDateTime; // Asegúrate de tener este import si usas LocalDateTime.now() en tus mocks
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DefaultComentarioService comentarioService;

    private Usuario usuarioMock;
    private Comentario comentarioMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configuración común para todos los tests
        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setNombres("Juan");
        usuarioMock.setApellidoPaterno("Pérez");
        usuarioMock.setCorreo("juan@example.com"); // Asegúrate de que el DTO mapea estos campos
        usuarioMock.setImagenPerfil("juan.jpg");

        comentarioMock = new Comentario();
        comentarioMock.setId(1L);
        comentarioMock.setContenido("Comentario de prueba");
        comentarioMock.setUsuario(usuarioMock);// Importante para el DTO
    }

    @Test
    @DisplayName("Debe obtener todos los comentarios principales paginados")
    void testObtenerComentarios() {
        List<Comentario> mockComentariosEntidad = List.of(comentarioMock);

        Page<Comentario> comentariosPageEntidadMock = new PageImpl<>(mockComentariosEntidad, PageRequest.of(0, 10), mockComentariosEntidad.size());

        when(comentarioRepository.findByComentarioIsNull(any(Pageable.class)))
                .thenReturn(comentariosPageEntidadMock);

        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(comentarioMock));
        when(comentarioRepository.findByComentario(comentarioMock)).thenReturn(Collections.emptyList());

        Page<ComentarioResponse> resultados = comentarioService.obtenerComentarios(PageRequest.of(0, 10));

        assertNotNull(resultados);
        assertEquals(1, resultados.getContent().size());
        assertEquals("Comentario de prueba", resultados.getContent().getFirst().getContenido());
        assertNotNull(resultados.getContent().getFirst().getId());
        assertEquals("Juan Pérez", resultados.getContent().getFirst().getNombresUsuario());

        assertEquals(0, resultados.getNumber());
        assertEquals(10, resultados.getSize());
        assertEquals(1, resultados.getTotalElements());
        assertEquals(1, resultados.getTotalPages());

        verify(comentarioRepository).findByComentarioIsNull(any(Pageable.class));
        verify(comentarioRepository, times(1)).findById(1L);
        verify(comentarioRepository, times(1)).findByComentario(comentarioMock);
    }


    @Test
    @DisplayName("Debe agregar un comentario nuevo")
    void testAgregarComentario() {
        ComentarioRequest request = new ComentarioRequest();
        request.setContenido("Nuevo comentario");
        request.setUsuarioId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(comentarioRepository.save(any(Comentario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        comentarioService.agregarComentario(request);

        verify(comentarioRepository).save(argThat(c ->
                c.getContenido().equals("Nuevo comentario") &&
                        c.getUsuario().equals(usuarioMock)
        ));
    }

    @Test
    @DisplayName("Debe obtener respuestas de un comentario")
    void testObtenerRespuestas() {
        Comentario respuesta = new Comentario();
        respuesta.setId(2L);
        respuesta.setContenido("Respuesta");
        respuesta.setComentario(comentarioMock);
        respuesta.setUsuario(usuarioMock); // Asignar usuario// Necesario para el mapeo a DTO

        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(comentarioMock));
        when(comentarioRepository.findByComentario(comentarioMock))
                .thenReturn(Collections.singletonList(respuesta));

        List<ComentarioResponse> resultados = comentarioService.obtenerRespuestas(1L);

        assertEquals(1, resultados.size());
        assertEquals("Respuesta", resultados.getFirst().getContenido());
        assertNotNull(resultados.getFirst().getId());
    }

    @Test
    @DisplayName("Debe lanzar excepción si usuario no existe")
    void testAgregarComentarioUsuarioNoExiste() {
        ComentarioRequest request = new ComentarioRequest();
        request.setUsuarioId(99L);

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                comentarioService.agregarComentario(request)
        );
    }
}