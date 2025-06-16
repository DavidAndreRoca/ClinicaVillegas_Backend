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
import org.springframework.data.domain.Page; // Importar Page
import org.springframework.data.domain.PageImpl; // Importar PageImpl
import org.springframework.data.domain.PageRequest; // Importar PageRequest
import org.springframework.data.domain.Pageable; // Importar Pageable
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq; // Necesario para eq(null) o eq("someString")
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
    @DisplayName("Debe obtener tipos de documento por nombre y acrónimo (sin paginación)")
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

    // --- Nuevos Tests para la Paginación ---

    @Test
    @DisplayName("Debe obtener tipos de documento paginados por defecto")
    void testObtenerTiposDocumentoPaginadosPorDefecto() {
        // Arrange
        // Creamos una lista de documentos que simule la respuesta de la DB
        List<TipoDocumento> docs = Arrays.asList(tipoDocumento1, tipoDocumento2);
        // Creamos un objeto Pageable para simular la solicitud de paginación por defecto
        Pageable pageable = PageRequest.of(0, 10);
        // Creamos un objeto Page que el repositorio debería devolver
        Page<TipoDocumento> expectedPage = new PageImpl<>(docs, pageable, docs.size());

        // Configuramos el mock para que devuelva la página esperada
        // Usamos eq(null) para los parámetros opcionales que no se pasan
        when(tipoDocumentoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expectedPage);

        // Act
        Page<TipoDocumento> result = tipoDocumentoService.obtenerTiposDocumentoPaginados(null, null, pageable);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(tipoDocumento1.getNombre(), result.getContent().get(0).getNombre());
        assertEquals(tipoDocumento2.getNombre(), result.getContent().get(1).getNombre());

        // Verificamos que el método findAll del repositorio con Specification y Pageable fue llamado
        verify(tipoDocumentoRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Debe obtener tipos de documento paginados con filtros")
    void testObtenerTiposDocumentoPaginadosConFiltros() {
        // Arrange
        String nombre = "DNI";
        String acronimo = "DNI";
        List<TipoDocumento> docs = Arrays.asList(tipoDocumento1); // Solo DNI debe coincidir
        Pageable pageable = PageRequest.of(0, 10);
        Page<TipoDocumento> expectedPage = new PageImpl<>(docs, pageable, docs.size());

        // Configuramos el mock para que devuelva la página esperada cuando los filtros coincidan
        when(tipoDocumentoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expectedPage);

        // Act
        Page<TipoDocumento> result = tipoDocumentoService.obtenerTiposDocumentoPaginados(nombre, acronimo, pageable);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(tipoDocumento1.getNombre(), result.getContent().get(0).getNombre());
        assertEquals(tipoDocumento1.getAcronimo(), result.getContent().get(0).getAcronimo());

        // Verificamos que el método findAll del repositorio fue llamado con la especificación y pageable
        verify(tipoDocumentoRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    // --- Tests Originales (sin cambios necesarios) ---

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