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

        comentarioMock = new Comentario();
        comentarioMock.setId(1L);
        comentarioMock.setContenido("Comentario de prueba");
        comentarioMock.setUsuario(usuarioMock);
    }

    @Test
    @DisplayName("Debe obtener todos los comentarios principales")
    void testObtenerComentarios() {
        // Configurar mock con usuario
        when(comentarioRepository.findByComentario(null))
                .thenReturn(Collections.singletonList(comentarioMock));

        List<ComentarioResponse> resultados = comentarioService.obtenerComentarios();

        assertEquals(1, resultados.size());
        assertEquals("Comentario de prueba", resultados.getFirst().getContenido());
        assertNotNull(resultados.getFirst().getId()); // Verificar que el usuario no es null
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
        respuesta.setUsuario(usuarioMock); // Asignar usuario

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
