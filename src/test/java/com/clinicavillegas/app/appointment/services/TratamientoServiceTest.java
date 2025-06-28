package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.TratamientoRequest;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.appointment.repositories.TipoTratamientoRepository;
import com.clinicavillegas.app.appointment.repositories.TratamientoRepository;
import com.clinicavillegas.app.appointment.services.impl.DefaultTratamientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TratamientoServiceTest {

    private DefaultTratamientoService tratamientoService;

    @Mock
    private TratamientoRepository tratamientoRepository;

    @Mock
    private TipoTratamientoRepository tipoTratamientoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tratamientoService = new DefaultTratamientoService(tratamientoRepository, tipoTratamientoRepository);
    }

    @Test
    @DisplayName("Debe obtener tratamientos paginados (activos por defecto) cuando no se especifican filtros")
    void testObtenerTratamientosPaginadosSinFiltros() {
        Tratamiento tratamiento1 = Tratamiento.builder()
                .id(1L)
                .nombre("Limpieza")
                .estado(true)
                .build();

        Tratamiento tratamiento2 = Tratamiento.builder()
                .id(2L)
                .nombre("Blanqueamiento")
                .estado(true)
                .build();

        when(tratamientoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tratamiento1, tratamiento2)));

        // Ahora llama al nuevo método obtenerTratamientosPaginados
        Page<Tratamiento> resultadosPage = tratamientoService.obtenerTratamientosPaginados(null, null, null, PageRequest.of(0, 10));

        assertEquals(2, resultadosPage.getContent().size());
        assertTrue(resultadosPage.getContent().contains(tratamiento1));
        assertTrue(resultadosPage.getContent().contains(tratamiento2));
        verify(tratamientoRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }


    @Test
    @DisplayName("Debe obtener tratamientos paginados filtrados por tipo, nombre y estado")
    void testObtenerTratamientosPaginadosFiltrados() {
        Tratamiento tratamiento = Tratamiento.builder()
                .id(1L)
                .nombre("Ortodoncia")
                .estado(true)
                .build();

        when(tratamientoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tratamiento)));

        // Ahora llama al nuevo método obtenerTratamientosPaginados
        Page<Tratamiento> resultadosPage = tratamientoService.obtenerTratamientosPaginados(1L, "Ortodoncia", true, PageRequest.of(0, 10));

        assertEquals(1, resultadosPage.getContent().size());
        assertEquals("Ortodoncia", resultadosPage.getContent().getFirst().getNombre());
        assertTrue(resultadosPage.getContent().getFirst().isEstado());
        verify(tratamientoRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener tratamientos paginados filtrados por estado inactivo")
    void testObtenerTratamientosPaginadosFiltradosPorEstadoInactivo() {
        Tratamiento tratamientoInactivo = Tratamiento.builder()
                .id(2L)
                .nombre("Revisión Anual")
                .estado(false)
                .build();

        when(tratamientoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tratamientoInactivo)));

        // Ahora llama al nuevo método obtenerTratamientosPaginados
        Page<Tratamiento> resultadosPage = tratamientoService.obtenerTratamientosPaginados(null, null, false, PageRequest.of(0, 10));

        assertEquals(1, resultadosPage.getContent().size());
        assertEquals("Revisión Anual", resultadosPage.getContent().getFirst().getNombre());
        assertFalse(resultadosPage.getContent().getFirst().isEstado());
        verify(tratamientoRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener todos los tratamientos (sin paginar) cuando no se especifican filtros")
    void testObtenerTodosTratamientosSinPaginar() {
        Tratamiento tratamiento1 = Tratamiento.builder()
                .id(1L)
                .nombre("Limpieza")
                .estado(true)
                .build();

        Tratamiento tratamiento2 = Tratamiento.builder()
                .id(2L)
                .nombre("Blanqueamiento Inactivo")
                .estado(false)
                .build();

        Tratamiento tratamiento3 = Tratamiento.builder()
                .id(3L)
                .nombre("Consulta")
                .estado(true)
                .build();

        // Mock para el método findAll(Specification) que devuelve una List
        when(tratamientoRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(tratamiento1, tratamiento2, tratamiento3));

        // Llama al nuevo método obtenerTodosTratamientos
        List<Tratamiento> resultados = tratamientoService.obtenerTodosTratamientos(null, null, null);

        assertEquals(3, resultados.size());
        assertTrue(resultados.contains(tratamiento1));
        assertTrue(resultados.contains(tratamiento2)); // Debe incluir inactivos si no se filtra por estado
        assertTrue(resultados.contains(tratamiento3));
        verify(tratamientoRepository, times(1)).findAll(any(Specification.class));
        verify(tratamientoRepository, never()).findAll(any(Specification.class), any(Pageable.class)); // Asegura que no se llamó al paginado
    }

    @Test
    @DisplayName("Debe obtener todos los tratamientos filtrados por tipo y nombre (sin paginar)")
    void testObtenerTodosTratamientosFiltradosSinPaginar() {
        Tratamiento tratamiento = Tratamiento.builder()
                .id(1L)
                .nombre("Blanqueamiento Láser")
                .estado(true)
                .build();

        // Mock para el método findAll(Specification)
        when(tratamientoRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(tratamiento));

        // Llama al nuevo método obtenerTodosTratamientos
        List<Tratamiento> resultados = tratamientoService.obtenerTodosTratamientos(5L, "Blanqueamiento", null);

        assertEquals(1, resultados.size());
        assertEquals("Blanqueamiento Láser", resultados.getFirst().getNombre());
        assertTrue(resultados.getFirst().isEstado());
        verify(tratamientoRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Debe obtener todos los tratamientos filtrados por estado inactivo (sin paginar)")
    void testObtenerTodosTratamientosFiltradosPorEstadoInactivoSinPaginar() {
        Tratamiento tratamientoInactivo = Tratamiento.builder()
                .id(2L)
                .nombre("Revisión Dental Caducada")
                .estado(false)
                .build();

        // Mock para el método findAll(Specification)
        when(tratamientoRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(tratamientoInactivo));

        // Llama al nuevo método obtenerTodosTratamientos
        List<Tratamiento> resultados = tratamientoService.obtenerTodosTratamientos(null, null, false);

        assertEquals(1, resultados.size());
        assertEquals("Revisión Dental Caducada", resultados.getFirst().getNombre());
        assertFalse(resultados.getFirst().isEstado());
        verify(tratamientoRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Debe obtener un tratamiento por ID")
    void testObtenerTratamientoPorId() {
        Tratamiento tratamiento = Tratamiento.builder()
                .id(1L)
                .nombre("Implante")
                .estado(true)
                .build();

        when(tratamientoRepository.findById(1L)).thenReturn(Optional.of(tratamiento));

        Tratamiento resultado = tratamientoService.obtenerTratamiento(1L);

        assertNotNull(resultado);
        assertEquals("Implante", resultado.getNombre());
    }

    @Test
    @DisplayName("Debe guardar un nuevo tratamiento")
    void testGuardarTratamiento() {
        TipoTratamiento tipo = TipoTratamiento.builder()
                .id(1L)
                .nombre("Ortodoncia")
                .estado(true)
                .build();

        TratamientoRequest request = TratamientoRequest.builder()
                .nombre("Frenillos")
                .descripcion("Colocación de frenillos")
                .costo(new BigDecimal("1200.00"))
                .duracion(90)
                .imagenURL("http://img.com/frenillos.png")
                .tipoTratamientoId(1L)
                .build();

        when(tipoTratamientoRepository.findById(1L)).thenReturn(Optional.of(tipo));

        tratamientoService.guardarTratamiento(request);

        verify(tratamientoRepository, times(1)).save(any(Tratamiento.class));
    }

    @Test
    @DisplayName("Debe actualizar un tratamiento existente")
    void testActualizarTratamiento() {
        TipoTratamiento tipo = TipoTratamiento.builder()
                .id(1L)
                .nombre("Endodoncia")
                .build();

        Tratamiento tratamiento = Tratamiento.builder()
                .id(1L)
                .nombre("Tratamiento antiguo")
                .estado(true)
                .build();

        TratamientoRequest request = TratamientoRequest.builder()
                .nombre("Tratamiento actualizado")
                .descripcion("Nueva descripción")
                .costo(new BigDecimal("150.00"))
                .duracion(60)
                .imagenURL("http://img.com/actualizado.png")
                .tipoTratamientoId(1L)
                .build();

        when(tratamientoRepository.findById(1L)).thenReturn(Optional.of(tratamiento));
        when(tipoTratamientoRepository.findById(1L)).thenReturn(Optional.of(tipo));

        tratamientoService.actualizarTratamiento(1L, request);

        assertEquals("Tratamiento actualizado", tratamiento.getNombre());
        assertEquals("Nueva descripción", tratamiento.getDescripcion());
        assertEquals(new BigDecimal("150.00"), tratamiento.getCosto());
        assertEquals(Duration.ofMinutes(60), tratamiento.getDuracion());
        assertEquals("http://img.com/actualizado.png", tratamiento.getImagenURL());
        verify(tratamientoRepository).save(tratamiento);
    }

    @Test
    @DisplayName("Debe eliminar (inhabilitar) un tratamiento")
    void testEliminarTratamiento() {
        Tratamiento tratamiento = Tratamiento.builder()
                .id(1L)
                .nombre("Profilaxis")
                .estado(true)
                .build();

        when(tratamientoRepository.findById(1L)).thenReturn(Optional.of(tratamiento));

        tratamientoService.eliminarTratamiento(1L);

        assertFalse(tratamiento.isEstado());
        verify(tratamientoRepository).save(tratamiento);
    }

}