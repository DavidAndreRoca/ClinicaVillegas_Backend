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
    @DisplayName("Debe obtener todos los tratamientos")
    void testObtenerTodosTratamientos() {
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

        when(tratamientoRepository.findAll()).thenReturn(List.of(tratamiento1, tratamiento2));

        List<Tratamiento> resultados = tratamientoService.obtenerTratamientos();

        assertEquals(2, resultados.size());
        verify(tratamientoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener tratamientos filtrados por tipo y nombre")
    void testObtenerTratamientosFiltrados() {
        Tratamiento tratamiento = Tratamiento.builder()
                .id(1L)
                .nombre("Ortodoncia")
                .estado(true)
                .build();

        when(tratamientoRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(tratamiento));

        List<Tratamiento> resultados = tratamientoService.obtenerTratamientos(1L, "Ortodoncia");

        assertEquals(1, resultados.size());
        assertEquals("Ortodoncia", resultados.getFirst().getNombre());
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
