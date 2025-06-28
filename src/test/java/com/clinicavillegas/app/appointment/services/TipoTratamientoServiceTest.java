package com.clinicavillegas.app.appointment.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.clinicavillegas.app.appointment.dto.request.TipoTratamientoRequest;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.repositories.TipoTratamientoRepository;
import com.clinicavillegas.app.appointment.services.impl.DefaultTipoTratamientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class TipoTratamientoServiceTest {

    @Mock
    private TipoTratamientoRepository tipoTratamientoRepository;

    @InjectMocks
    private DefaultTipoTratamientoService tipoTratamientoService;

    private TipoTratamiento tipoActivo;
    private TipoTratamiento tipoInactivo;
    private TipoTratamientoRequest request;

    @BeforeEach
    void setUp() {
        tipoActivo = TipoTratamiento.builder()
                .id(1L)
                .nombre("Fisioterapia")
                .estado(true)
                .build();

        tipoInactivo = TipoTratamiento.builder()
                .id(2L)
                .nombre("Acupuntura")
                .estado(false)
                .build();

        request = new TipoTratamientoRequest();
        request.setNombre("Nuevo Tratamiento");
    }

    // --- Adaptación y Nuevos Tests para Métodos GET ---

    @Test
    void testObtenerTiposTratamientoSinPaginacionPorDefectoActivo() {
        // Simula el retorno de elementos activos por defecto
        List<TipoTratamiento> tiposActivos = Collections.singletonList(tipoActivo);
        // Cuando el repositorio reciba CUALQUIER Specification, devuelve la lista de activos
        when(tipoTratamientoRepository.findAll(any(Specification.class)))
                .thenReturn(tiposActivos);

        // Llama al método sin filtros explícitos, por lo que 'estado' será TRUE por defecto
        List<TipoTratamiento> resultado = tipoTratamientoService.obtenerTiposTratamiento(null, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.getFirst().isEstado());
        verify(tipoTratamientoRepository).findAll(any(Specification.class));
    }

    @Test
    void testObtenerTiposTratamientoSinPaginacionConFiltroNombreYEstadoActivo() {
        // Simula el retorno de elementos filtrados por nombre y estado activo (por defecto o explícito)
        when(tipoTratamientoRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(tipoActivo));

        List<TipoTratamiento> resultado = tipoTratamientoService.obtenerTiposTratamiento("Fisioterapia", true);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Fisioterapia", resultado.getFirst().getNombre());
        assertTrue(resultado.getFirst().isEstado());
        verify(tipoTratamientoRepository).findAll(any(Specification.class));
    }

    @Test
    void testObtenerTiposTratamientoSinPaginacionConFiltroEstadoInactivo() {
        // Simula el retorno de elementos filtrados por estado inactivo
        when(tipoTratamientoRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(tipoInactivo));

        List<TipoTratamiento> resultado = tipoTratamientoService.obtenerTiposTratamiento(null, false);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertFalse(resultado.getFirst().isEstado());
        verify(tipoTratamientoRepository).findAll(any(Specification.class));
    }


    @Test
    void testObtenerTiposTratamientoPaginadosPorDefectoActivo() {
        // Datos de prueba: Solo elementos activos
        List<TipoTratamiento> content = Collections.singletonList(tipoActivo);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("nombre"));
        Page<TipoTratamiento> simulatedPage = new PageImpl<>(content, pageable, 1);

        // Configurar el mock del repositorio: Esperamos un Specification y un Pageable
        // El servicio llamará a repository.findAll(Specification, Pageable)
        when(tipoTratamientoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(simulatedPage);

        // El controlador llamaría con 'estado=true' por defecto. Aquí simulamos esa llamada al servicio.
        Page<TipoTratamiento> resultado = tipoTratamientoService.obtenerTiposTratamientoPaginados(null, null, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getNumberOfElements());
        assertEquals(0, resultado.getNumber());
        assertEquals(10, resultado.getSize());
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getTotalPages());
        assertTrue(resultado.getContent().getFirst().isEstado());
        verify(tipoTratamientoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testObtenerTiposTratamientoPaginadosConParametrosYEstadoActivo() {
        // Datos de prueba para una página específica, solo activos
        TipoTratamiento tipoActivo2 = TipoTratamiento.builder().id(3L).nombre("Diagnóstico").estado(true).build();
        List<TipoTratamiento> content = Arrays.asList(tipoActivo, tipoActivo2);
        // Usamos un PageRequest consistente con el orden esperado por el controlador
        Pageable pageable = PageRequest.of(0, 2, Sort.by("nombre").ascending());
        Page<TipoTratamiento> simulatedPage = new PageImpl<>(content, pageable, 2);

        // Configurar el mock del repositorio: Esperamos un Specification y el Pageable específico.
        // El servicio llamará a repository.findAll(Specification, Pageable)
        when(tipoTratamientoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(simulatedPage);

        // Llama al método del servicio con los parámetros que esperas del controlador
        Page<TipoTratamiento> resultado = tipoTratamientoService.obtenerTiposTratamientoPaginados(null, true, pageable);

        assertNotNull(resultado);
        assertEquals(2, resultado.getNumberOfElements());
        assertEquals(0, resultado.getNumber());
        assertEquals(2, resultado.getSize());
        assertTrue(resultado.getContent().getFirst().isEstado());
        verify(tipoTratamientoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testObtenerTiposTratamientoPaginadosConFiltroEstadoInactivo() {
        // Simula la obtención de solo el tipo inactivo
        List<TipoTratamiento> content = Collections.singletonList(tipoInactivo);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("nombre").ascending());
        Page<TipoTratamiento> simulatedPage = new PageImpl<>(content, pageable, 1);

        when(tipoTratamientoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(simulatedPage);

        Page<TipoTratamiento> resultado = tipoTratamientoService.obtenerTiposTratamientoPaginados(null, false, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getNumberOfElements());
        assertEquals("Acupuntura", resultado.getContent().getFirst().getNombre());
        assertFalse(resultado.getContent().getFirst().isEstado());
        verify(tipoTratamientoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testObtenerTiposTratamientoPaginadosConFiltroNombreYEstadoInactivo() {
        // Simula la obtención de un tipo inactivo específico por nombre y estado
        List<TipoTratamiento> content = Collections.singletonList(tipoInactivo);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("nombre").ascending());
        Page<TipoTratamiento> simulatedPage = new PageImpl<>(content, pageable, 1);

        when(tipoTratamientoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(simulatedPage);

        Page<TipoTratamiento> resultado = tipoTratamientoService.obtenerTiposTratamientoPaginados("Acupuntura", false, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getNumberOfElements());
        assertEquals("Acupuntura", resultado.getContent().getFirst().getNombre());
        assertFalse(resultado.getContent().getFirst().isEstado());
        verify(tipoTratamientoRepository).findAll(any(Specification.class), eq(pageable));
    }


    // --- Tests existentes (no modificados en su lógica central, pero asegurando coherencia) ---

    @Test
    void testAgregarTipoTratamiento() {
        when(tipoTratamientoRepository.save(any(TipoTratamiento.class)))
                .thenReturn(tipoActivo);

        tipoTratamientoService.agregarTipoTratamiento(request);

        verify(tipoTratamientoRepository).save(argThat(tipo ->
                tipo.getNombre().equals("Nuevo Tratamiento") &&
                        tipo.isEstado()
        ));
    }

    @Test
    void testActualizarTipoTratamiento() {
        when(tipoTratamientoRepository.findById(1L))
                .thenReturn(Optional.of(tipoActivo));
        when(tipoTratamientoRepository.save(any(TipoTratamiento.class)))
                .thenReturn(tipoActivo);

        tipoTratamientoService.actualizarTipoTratamiento(1L, request);

        verify(tipoTratamientoRepository).findById(1L);
        verify(tipoTratamientoRepository).save(argThat(tipo ->
                tipo.getNombre().equals("Nuevo Tratamiento")
        ));
    }

    @Test
    void testActualizarTipoTratamientoSiNoExisteRetornaExcepcion() {
        when(tipoTratamientoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                tipoTratamientoService.actualizarTipoTratamiento(99L, request)
        );
        verify(tipoTratamientoRepository).findById(99L);
        verify(tipoTratamientoRepository, never()).save(any());
    }

    @Test
    void testEliminarTipoTratamiento() {
        when(tipoTratamientoRepository.findById(1L))
                .thenReturn(Optional.of(tipoActivo));
        when(tipoTratamientoRepository.save(any(TipoTratamiento.class)))
                .thenReturn(tipoActivo);

        tipoTratamientoService.eliminarTipoTratamiento(1L);

        verify(tipoTratamientoRepository).findById(1L);
        verify(tipoTratamientoRepository).save(argThat(tipo ->
                !tipo.isEstado()
        ));
    }

    @Test
    void testEliminarTipoTratamientoSiNoExisteRetornaExcepcion() {
        when(tipoTratamientoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                tipoTratamientoService.eliminarTipoTratamiento(99L)
        );
        verify(tipoTratamientoRepository).findById(99L);
        verify(tipoTratamientoRepository, never()).save(any());
    }
}