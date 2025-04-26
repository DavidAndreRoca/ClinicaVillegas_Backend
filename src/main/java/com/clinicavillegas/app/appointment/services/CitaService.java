package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.CitaReprogramarRequest;
import com.clinicavillegas.app.appointment.dto.request.CitaRequest;
import com.clinicavillegas.app.appointment.dto.request.ValidacionCitaRequest;
import com.clinicavillegas.app.appointment.dto.response.CitaResponse;
import com.clinicavillegas.app.appointment.models.Cita;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {
    List<CitaResponse> obtenerCitas(Long usuarioId, Long dentistaId, String estado, LocalDate fechaInicio,
                                           LocalDate fechaFin, Long tratamientoId, String sexo);

    List<Cita> obtenerCitasPorUsuario(Long usuarioId);

    List<Cita> obtenerCitasPorDentista(Long dentistaId);
    void agregarCita(CitaRequest citaRequest);
    void actualizarCita(Long id, CitaRequest citaRequest);
    void atenderCita(Long id);
    void eliminarCita(Long id);
    boolean validarDisponibilidad(ValidacionCitaRequest request);
    void reprogramarCita(Long id, CitaReprogramarRequest request);
}
