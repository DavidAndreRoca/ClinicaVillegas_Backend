package com.clinicavillegas.app.user.services;

import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import com.clinicavillegas.app.user.dto.request.TipoDocumentoRequest;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.repositories.TipoDocumentoRepository;
import com.clinicavillegas.app.user.services.impl.DefaultTipoDocumentoService;
import com.clinicavillegas.app.user.specifications.TipoDocumentoSpecification;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoDocumentoServiceTest {

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @InjectMocks
    private DefaultTipoDocumentoService tipoDocumentoService;

    private TipoDocumento tipoDocumento1;
    private TipoDocumento tipoDocumento2;
    private TipoDocumentoRequest tipoDocumentoRequest;

    @BeforeEach
    void setUp() {
        tipoDocumento1 = TipoDocumento.builder()
                .id(1L)
                .nombre("DNI")
                .acronimo("DNI")
                .estado(true)
                .build();

        tipoDocumento2 = TipoDocumento.builder()
                .id(2L)
                .nombre("Pasaporte")
                .acronimo("PAS")
                .estado(true)
                .build();

        tipoDocumentoRequest = new TipoDocumentoRequest();
        tipoDocumentoRequest.setNombre("Carnet de Extranjería");
        tipoDocumentoRequest.setAcronimo("CE");
    }

    @Test
    @DisplayName("Debe agregar un nuevo tipo de documento exitosamente")
    void testAgregarTipoDocumento() {
        // Arrange
        when(tipoDocumentoRepository.save(any(TipoDocumento.class))).thenReturn(tipoDocumento1);

        // Act
        tipoDocumentoService.agregarTipoDocumento(tipoDocumentoRequest);

        // Assert
        verify(tipoDocumentoRepository, times(1)).save(any(TipoDocumento.class));
    }

    @Test
    @DisplayName("Debe obtener todos los tipos de documento")
    void testObtenerTiposDocumento() {
        // Arrange
        when(tipoDocumentoRepository.findAll()).thenReturn(Arrays.asList(tipoDocumento1, tipoDocumento2));

        // Act
        List<TipoDocumento> result = tipoDocumentoService.obtenerTiposDocumento();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(tipoDocumento1));
        assertTrue(result.contains(tipoDocumento2));
        verify(tipoDocumentoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener tipos de documento por nombre y acrónimo")
    void testObtenerTiposDocumentoPorNombreYAcronimo() {
        // Arrange
        String nombre = "DNI";
        String acronimo = "DNI";
        // Mockear el comportamiento de findAll(Specification)
        when(tipoDocumentoRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(tipoDocumento1));

        // Act
        List<TipoDocumento> result = tipoDocumentoService.obtenerTiposDocumento(nombre, acronimo);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DNI", result.get(0).getNombre());
        verify(tipoDocumentoRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Debe actualizar un tipo de documento existente")
    void testActualizarTipoDocumento() {
        // Arrange
        Long id = 1L;
        TipoDocumentoRequest updateRequest = new TipoDocumentoRequest();
        updateRequest.setNombre("DNI Actualizado");
        updateRequest.setAcronimo("DNA");

        when(tipoDocumentoRepository.findById(id)).thenReturn(Optional.of(tipoDocumento1));
        when(tipoDocumentoRepository.save(any(TipoDocumento.class))).thenReturn(tipoDocumento1);

        // Act
        tipoDocumentoService.actualizarTipoDocumento(id, updateRequest);

        // Assert
        assertEquals("DNI Actualizado", tipoDocumento1.getNombre());
        assertEquals("DNA", tipoDocumento1.getAcronimo());
        verify(tipoDocumentoRepository, times(1)).findById(id);
        verify(tipoDocumentoRepository, times(1)).save(tipoDocumento1);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al actualizar un tipo de documento no existente")
    void testActualizarTipoDocumentoNotFound() {
        // Arrange
        Long id = 99L; // ID no existente
        when(tipoDocumentoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                tipoDocumentoService.actualizarTipoDocumento(id, tipoDocumentoRequest)
        );
        verify(tipoDocumentoRepository, times(1)).findById(id);
        verify(tipoDocumentoRepository, never()).save(any(TipoDocumento.class));
    }

    @Test
    @DisplayName("Debe eliminar lógicamente un tipo de documento existente")
    void testEliminarTipoDocumento() {
        // Arrange
        Long id = 1L;
        when(tipoDocumentoRepository.findById(id)).thenReturn(Optional.of(tipoDocumento1));
        when(tipoDocumentoRepository.save(any(TipoDocumento.class))).thenReturn(tipoDocumento1);

        // Act
        tipoDocumentoService.eliminarTipoDocumento(id);

        // Assert
        assertFalse(tipoDocumento1.isEstado()); // Verificar que el estado cambió a false
        verify(tipoDocumentoRepository, times(1)).findById(id);
        verify(tipoDocumentoRepository, times(1)).save(tipoDocumento1);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al eliminar un tipo de documento no existente")
    void testEliminarTipoDocumentoNotFound() {
        // Arrange
        Long id = 99L; // ID no existente
        when(tipoDocumentoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                tipoDocumentoService.eliminarTipoDocumento(id)
        );
        verify(tipoDocumentoRepository, times(1)).findById(id);
        verify(tipoDocumentoRepository, never()).save(any(TipoDocumento.class));
    }

    @Test
    @DisplayName("Debe obtener un tipo de documento por ID")
    void testObtenerTipoDocumentoPorId() {
        // Arrange
        Long id = 1L;
        when(tipoDocumentoRepository.findById(id)).thenReturn(Optional.of(tipoDocumento1));

        // Act
        TipoDocumento result = tipoDocumentoService.obtenerTipoDocumento(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("DNI", result.getNombre());
        verify(tipoDocumentoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al obtener un tipo de documento no existente por ID")
    void testObtenerTipoDocumentoPorIdNotFound() {
        // Arrange
        Long id = 99L; // ID no existente
        when(tipoDocumentoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                tipoDocumentoService.obtenerTipoDocumento(id)
        );
        verify(tipoDocumentoRepository, times(1)).findById(id);
    }
}