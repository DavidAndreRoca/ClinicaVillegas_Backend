package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.HorarioRequest;
import com.clinicavillegas.app.appointment.dto.response.HorarioResponse;
import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.models.Dia;
import com.clinicavillegas.app.appointment.models.Horario;
import com.clinicavillegas.app.appointment.repositories.DentistaRepository;
import com.clinicavillegas.app.appointment.repositories.HorarioRepository;
import com.clinicavillegas.app.appointment.services.impl.DefaultHorarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HorarioServiceTest {
    private DefaultHorarioService horarioService;

    @Mock
    private HorarioRepository horarioRepository;
    @Mock
    private DentistaRepository dentistaRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        horarioService = new DefaultHorarioService(horarioRepository, dentistaRepository);
    }

    @Test
    @DisplayName("Debe agregar un horario correctamente cuando cumple todas las validaciones")
    void testAgregarHorarioExitoso() {
        HorarioRequest request = HorarioRequest.builder()
                .dia("LUNES")
                .horaComienzo(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(17, 0))
                .dentistaId(1L)
                .build();

        Dentista dentista = new Dentista();
        when(dentistaRepository.findById(1L)).thenReturn(Optional.of(dentista));
        when(horarioRepository.findAll(any(Specification.class))).thenReturn(List.of());

        horarioService.agregarHorario(request);

        verify(horarioRepository).save(any(Horario.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si la hora de fin es anterior a la de inicio")
    void testAgregarHorarioConHoraFinAnterior() {
        HorarioRequest request = HorarioRequest.builder()
                .dia("LUNES")
                .horaComienzo(LocalTime.of(15, 0))
                .horaFin(LocalTime.of(8, 0))
                .dentistaId(1L)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> horarioService.agregarHorario(request));

        assertEquals("La hora de finalización no puede ser anterior a la hora de inicio", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el horario dura menos de 8 horas")
    void testAgregarHorarioDuracionMenorA8Horas() {
        HorarioRequest request = HorarioRequest.builder()
                .dia("MARTES")
                .horaComienzo(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(12, 0)) // solo 4 horas
                .dentistaId(1L)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> horarioService.agregarHorario(request));

        assertEquals("El tiempo de horario no puede ser menor a 8 horas", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si ya existe un horario para ese día")
    void testAgregarHorarioDiaDuplicado() {
        HorarioRequest request = HorarioRequest.builder()
                .dia("MIERCOLES")
                .horaComienzo(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(17, 0))
                .dentistaId(1L)
                .build();

        Horario existente = Horario.builder()
                .dia(Dia.MIERCOLES)
                .build();

        when(horarioRepository.findAll(any(Specification.class))).thenReturn(List.of(existente));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> horarioService.agregarHorario(request));

        assertEquals("Ya existe un horario para ese dia", exception.getMessage());
    }

    @Test
    @DisplayName("Debe obtener todos los horarios")
    void testObtenerTodosLosHorarios() {
        when(horarioRepository.findAll()).thenReturn(List.of(
                Horario.builder().dia(Dia.LUNES).build(),
                Horario.builder().dia(Dia.MARTES).build()
        ));

        List<Horario> result = horarioService.obteneHorarios();

        assertEquals(2, result.size());
        verify(horarioRepository).findAll();
    }

    @Test
    @DisplayName("Debe eliminar un horario por ID")
    void testEliminarHorario() {
        horarioService.eliminarHorario(5L);
        verify(horarioRepository).deleteById(5L);
    }

    @Test
    @DisplayName("Debe obtener horarios por dentista y día")
    void testObtenerHorariosPorDentistaYDia() {
        Dentista dentista = new Dentista();
        dentista.setId(1L); // necesario para que getId() no lance NullPointerException

        Horario horario = Horario.builder()
                .dia(Dia.LUNES)
                .horaComienzo(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(17, 0))
                .dentista(dentista) // ¡este era el que faltaba!
                .build();

        when(horarioRepository.findAll(any(Specification.class))).thenReturn(List.of(horario));

        List<HorarioResponse> response = horarioService.obtenerHorarios(1L, "LUNES");

        assertEquals(1, response.size());
        assertEquals("LUNES", response.getFirst().getDia());
    }

}
