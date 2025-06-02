package com.clinicavillegas.app.user.services;

import com.clinicavillegas.app.user.dto.request.UsuarioRequest;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.mappers.UsuarioMapper;
import com.clinicavillegas.app.user.models.Rol;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException; // Importar NoSuchElementException

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DefaultUsuarioService usuarioService;

    private Usuario usuario1;
    private Usuario usuario2;
    private UsuarioRequest usuarioRequest;

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
                .rol(Rol.PACIENTE)
                .estado(true)
                .build();

        usuarioRequest = new UsuarioRequest();
        usuarioRequest.setNombres("Ana Updated");
        usuarioRequest.setApellidoPaterno("García Updated");
        usuarioRequest.setApellidoMaterno("López Updated");
        usuarioRequest.setTelefono("999888777");
        usuarioRequest.setImagenPerfil("new_image.jpg");
    }

    @Test
    @DisplayName("Debe obtener clientes por nombre y rol exitosamente")
    void testObtenerClientes() {
        // Arrange
        String nombre = "Ana";
        String rol = "CLIENTE";
        // Mockeamos que findAll con cualquier Specification devuelva la lista de usuarios.
        // Asumimos que UsuarioMapper.toDto funciona correctamente y se prueba por separado o es simple.
        when(usuarioRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(usuario1));

        // Act
        List<UsuarioResponse> result = usuarioService.obtenerClientes(nombre, rol);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).getNombres());
        // Verificamos que se llamó a findAll con una Specification.
        verify(usuarioRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Debe obtener un cliente por ID exitosamente")
    void testObtenerClientePorId() {
        // Arrange
        Long id = 1L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario1));

        // Act
        UsuarioResponse result = usuarioService.obtenerClientePorId(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Ana", result.getNombres());
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException al obtener un cliente por ID no existente")
    void testObtenerClientePorIdNotFound() {
        // Arrange
        Long id = 99L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        // El método .orElseThrow() sin argumento lanza NoSuchElementException por defecto.
        assertThrows(NoSuchElementException.class, () ->
                usuarioService.obtenerClientePorId(id)
        );
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe actualizar un cliente por ID exitosamente")
    void testActualizarClientePorId() {
        // Arrange
        Long id = 1L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario1));
        // No es estrictamente necesario mockear save si no se necesita un valor de retorno específico,
        // pero es una buena práctica si el servicio depende de él.
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario1);

        // Act
        usuarioService.actualizarClientePorId(id, usuarioRequest);

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
    @DisplayName("Debe lanzar NoSuchElementException al actualizar un cliente por ID no existente")
    void testActualizarClientePorIdNotFound() {
        // Arrange
        Long id = 99L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () ->
                usuarioService.actualizarClientePorId(id, usuarioRequest)
        );
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, never()).save(any(Usuario.class)); // Asegurarse de que save no se llama
    }

    @Test
    @DisplayName("Debe eliminar lógicamente un cliente por ID exitosamente")
    void testEliminarCliente() {
        // Arrange
        Long id = 1L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario1);

        // Act
        usuarioService.eliminarCliente(id);

        // Assert
        assertFalse(usuario1.isEstado()); // Verificar que el estado cambió a false
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).save(usuario1);
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException al eliminar un cliente por ID no existente")
    void testEliminarClienteNotFound() {
        // Arrange
        Long id = 99L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () ->
                usuarioService.eliminarCliente(id)
        );
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, never()).save(any(Usuario.class)); // Asegurarse de que save no se llama
    }
}