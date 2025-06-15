package com.clinicavillegas.app.user.services;

import com.clinicavillegas.app.user.dto.request.UsuarioRequest;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.mappers.UsuarioMapper;
import com.clinicavillegas.app.user.models.Rol; // Asegúrate de que esta importación esté presente
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import com.clinicavillegas.app.user.services.impl.DefaultUsuarioService;
import com.clinicavillegas.app.user.specifications.UsuarioSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DefaultUsuarioService usuarioService;

    private Usuario usuario1;
    private Usuario usuario2;
    private UsuarioRequest usuarioRequestActualizacion;

    @BeforeEach
    void setUp() {
        usuario1 = Usuario.builder()
                .id(1L)
                .nombres("Ana")
                .apellidoPaterno("García")
                .apellidoMaterno("López")
                .correo("ana@example.com")
                .telefono("987654321")
                .rol(Rol.PACIENTE)
                .estado(true)
                .build();

        usuario2 = Usuario.builder()
                .id(2L)
                .nombres("Pedro")
                .apellidoPaterno("Martínez")
                .apellidoMaterno("Ruiz")
                .correo("pedro@example.com")
                .telefono("123456789")
                .rol(Rol.DENTISTA)
                .estado(true)
                .build();

        usuarioRequestActualizacion = UsuarioRequest.builder()
                .nombres("Ana Updated")
                .apellidoPaterno("García Updated")
                .apellidoMaterno("López Updated")
                .telefono("999888777")
                .imagenPerfil("new_image.jpg")
                .build();
    }

    // --- Tests para obtenerUsuarios (sin paginación) ---

    @Test
    @DisplayName("Debe obtener usuarios (sin paginación) con filtros exitosamente")
    void testObtenerUsuariosConFiltros() {
        // Arrange
        String nombres = "Ana";
        String rolString = "PACIENTE";
        Boolean estado = true;

        when(usuarioRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(usuario1));

        // Act
        List<UsuarioResponse> result = usuarioService.obtenerUsuarios(nombres, rolString, estado);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).getNombres());
        // CORRECCIÓN: Compara el objeto Rol directamente, no su nombre String
        assertEquals(Rol.PACIENTE, result.get(0).getRol()); // AHORA SE ASUME QUE getRol() en UsuarioResponse devuelve Rol
        assertTrue(result.get(0).isEstado());
        verify(usuarioRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Debe obtener usuarios (sin paginación) sin filtros, devolviendo todos los activos")
    void testObtenerUsuariosSinFiltros() {
        // Arrange
        when(usuarioRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(usuario1, usuario2));

        // Act
        List<UsuarioResponse> result = usuarioService.obtenerUsuarios(null, null, true);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(usuarioRepository, times(1)).findAll(any(Specification.class));
    }

    // --- Tests para obtenerUsuariosPaginados ---

    @Test
    @DisplayName("Debe obtener usuarios paginados con filtros exitosamente")
    void testObtenerUsuariosPaginadosConFiltros() {
        // Arrange
        String nombres = "Pedro";
        String rolString = "DENTISTA";
        Boolean estado = true;
        Pageable pageable = PageRequest.of(0, 10);

        Page<Usuario> mockPage = new PageImpl<>(Arrays.asList(usuario2), pageable, 1);
        when(usuarioRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);

        // Act
        Page<UsuarioResponse> result = usuarioService.obtenerUsuariosPaginados(nombres, rolString, estado, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Pedro", result.getContent().get(0).getNombres());
        // CORRECCIÓN: Compara el objeto Rol directamente, no su nombre String
        assertEquals(Rol.DENTISTA, result.getContent().get(0).getRol()); // AHORA SE ASUME QUE getRol() en UsuarioResponse devuelve Rol
        assertTrue(result.getContent().get(0).isEstado());
        verify(usuarioRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Debe obtener usuarios paginados por defecto (todos activos)")
    void testObtenerUsuariosPaginadosDefecto() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> mockPage = new PageImpl<>(Arrays.asList(usuario1, usuario2), pageable, 2);

        when(usuarioRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);

        // Act
        Page<UsuarioResponse> result = usuarioService.obtenerUsuariosPaginados(null, null, true, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(usuarioRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    // --- Tests para obtenerUsuarioPorId ---

    @Test
    @DisplayName("Debe obtener un usuario por ID exitosamente")
    void testObtenerUsuarioPorId() {
        // Arrange
        Long id = 1L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario1));

        // Act
        UsuarioResponse result = usuarioService.obtenerUsuarioPorId(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Ana", result.getNombres());
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException al obtener un usuario por ID no existente")
    void testObtenerUsuarioPorIdNotFound() {
        // Arrange
        Long id = 99L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                usuarioService.obtenerUsuarioPorId(id)
        );
        verify(usuarioRepository, times(1)).findById(id);
    }

    // --- Tests para actualizarUsuarioPorId ---

    @Test
    @DisplayName("Debe actualizar un usuario por ID exitosamente")
    void testActualizarUsuarioPorId() {
        // Arrange
        Long id = 1L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario1);

        // Act
        usuarioService.actualizarUsuarioPorId(id, usuarioRequestActualizacion);

        // Assert
        assertEquals("Ana Updated", usuario1.getNombres());
        assertEquals("García Updated", usuario1.getApellidoPaterno());
        assertEquals("López Updated", usuario1.getApellidoMaterno());
        assertEquals("999888777", usuario1.getTelefono());
        assertEquals("new_image.jpg", usuario1.getImagenPerfil());
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).save(usuario1);
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException al actualizar un usuario por ID no existente")
    void testActualizarUsuarioPorIdNotFound() {
        // Arrange
        Long id = 99L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                usuarioService.actualizarUsuarioPorId(id, usuarioRequestActualizacion)
        );
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // --- Tests para eliminarUsuario (eliminación lógica) ---

    @Test
    @DisplayName("Debe eliminar lógicamente un usuario por ID exitosamente")
    void testEliminarUsuario() {
        // Arrange
        Long id = 1L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario1);

        // Act
        usuarioService.eliminarUsuario(id);

        // Assert
        assertFalse(usuario1.isEstado());
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).save(usuario1);
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException al eliminar un usuario por ID no existente")
    void testEliminarUsuarioNotFound() {
        // Arrange
        Long id = 99L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                usuarioService.eliminarUsuario(id)
        );
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}