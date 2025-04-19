package com.clinicavillegas.app.appointment.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class TipoTratamientoServiceTest {

    @Mock
    private TipoTratamientoRepository tipoTratamientoRepository;

    @InjectMocks
    private DefaultTipoTratamientoService tipoTratamientoService;

    private TipoTratamiento tipoActivo;
    private TipoTratamientoRequest request;

    @BeforeEach
    void setUp() {
        tipoActivo = TipoTratamiento.builder()
                .id(1L)
                .nombre("Fisioterapia")
                .estado(true)
                .build();

        TipoTratamiento tipoInactivo = TipoTratamiento.builder()
                .id(2L)
                .nombre("Acupuntura")
                .estado(false)
                .build();

        request = new TipoTratamientoRequest();
        request.setNombre("Nuevo Tratamiento");
    }

    @Test
    void testObtenerTiposTratamiento() {
        when(tipoTratamientoRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(tipoActivo));

        List<TipoTratamiento> resultado = tipoTratamientoService.obtenerTiposTratamiento();

        assertEquals(1, resultado.size());
        assertEquals("Fisioterapia", resultado.getFirst().getNombre());
        assertTrue(resultado.getFirst().isEstado());
        verify(tipoTratamientoRepository).findAll(any(Specification.class));
    }

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

