package com.clinicavillegas.app.appointment.services;


import com.clinicavillegas.app.appointment.dto.request.CitaReprogramarRequest;
import com.clinicavillegas.app.appointment.dto.request.CitaRequest;
import com.clinicavillegas.app.appointment.dto.request.ValidacionCitaRequest;
import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.appointment.repositories.*;
import com.clinicavillegas.app.appointment.services.impl.DefaultCitaService;
import com.clinicavillegas.app.user.repositories.TipoDocumentoRepository;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CitaServiceTest {

    private DefaultCitaService citaService;

    @Mock
    private CitaRepository citaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private DentistaRepository dentistaRepository;
    @Mock
    private TratamientoRepository tratamientoRepository;
    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        citaService = new DefaultCitaService(
                citaRepository, usuarioRepository, dentistaRepository, tratamientoRepository, tipoDocumentoRepository
        );
    }

    @Test
    @DisplayName("Debe agregar una nueva cita")
    void testAgregarCita() {
        CitaRequest request = CitaRequest.builder()
                .fecha(LocalDate.of(2025, 5, 10))
                .hora(LocalTime.of(10, 0))
                .monto(BigDecimal.valueOf(100))
                .nombres("Ana")
                .apellidoPaterno("Morales")
                .apellidoMaterno("López")
                .tipoDocumento("DNI")
                .numeroIdentidad("12345678")
                .sexo("FEMENINO")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .dentistaId(1L)
                .usuarioId(2L)
                .tratamientoId(3L)
                .build();
        Tratamiento tratamiento = Tratamiento.builder()
                .id(3L)
                .nombre("Ortodoncia")
                .costo(BigDecimal.valueOf(250.0))
                .duracion(Duration.ofMinutes(40))
                .estado(true)
                .build();


        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(mock()));
        when(dentistaRepository.findById(1L)).thenReturn(Optional.of(mock()));
        when(tratamientoRepository.findById(3L)).thenReturn(Optional.of(tratamiento));
        when(tipoDocumentoRepository.findByAcronimo("DNI")).thenReturn(Optional.of(mock()));

        citaService.agregarCita(request);

        verify(citaRepository).save(any(Cita.class));
    }

    @Test
    @DisplayName("Debe validar la disponibilidad para una cita")
    void testValidarDisponibilidad() {
        ValidacionCitaRequest request = ValidacionCitaRequest.builder()
                .fecha("2025-05-10")
                .hora("09:00")
                .tratamientoId(1L)
                .dentistaId(2L)
                .build();

        Tratamiento tratamiento = Tratamiento.builder()
                .id(3L)
                .nombre("Ortodoncia")
                .costo(BigDecimal.valueOf(250.0))
                .duracion(Duration.ofMinutes(40))
                .estado(true)
                .build();

        LocalDate fecha = LocalDate.of(2025, 5, 10);
        LocalTime horaInicio = LocalTime.of(9, 0);

        when(tratamientoRepository.findById(1L)).thenReturn(Optional.of(tratamiento));
        when(citaRepository.findAll(any(Specification.class))).thenReturn(List.of());

        boolean disponible = citaService.validarDisponibilidad(request);

        assertTrue(disponible);
    }

    @Test
    @DisplayName("Debe reprogramar una cita correctamente")
    void testReprogramarCita() {
        Cita cita = new Cita();
        CitaReprogramarRequest request = CitaReprogramarRequest.builder()
                .fecha(LocalDate.of(2025, 5, 15))
                .hora(LocalTime.of(10, 30))
                .build();

        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        citaService.reprogramarCita(1L, request);

        assertEquals(LocalDate.of(2025, 5, 15), cita.getFecha());
        assertEquals(LocalTime.of(10, 30), cita.getHora());
        verify(citaRepository).save(cita);
    }

    @Test
    @DisplayName("Debe eliminar una cita correctamente")
    void testEliminarCita() {
        Cita cita = new Cita();
        cita.setEstado("Pendiente");

        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        citaService.eliminarCita(1L);

        assertEquals("Cancelada", cita.getEstado());
        verify(citaRepository).save(cita);
    }
}
