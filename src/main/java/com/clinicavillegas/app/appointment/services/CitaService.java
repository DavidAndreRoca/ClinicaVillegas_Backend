package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.CitaReprogramarRequest;
import com.clinicavillegas.app.appointment.dto.request.CitaRequest;
import com.clinicavillegas.app.appointment.dto.request.ValidacionCitaRequest;
import com.clinicavillegas.app.appointment.dto.response.CitaResponse;
import com.clinicavillegas.app.appointment.models.Cita;

import org.springframework.data.domain.Page;    // <-- Nueva Importación
import org.springframework.data.domain.Pageable; // <-- Nueva Importación

import java.time.LocalDate;
import java.util.List;

public interface CitaService {

    // Método para obtener citas SIN PAGINACIÓN (con tus filtros existentes)
    List<CitaResponse> obtenerCitas(Long usuarioId, Long dentistaId, String estado, LocalDate fechaInicio,
                                    LocalDate fechaFin, Long tratamientoId, String sexo);

    // NUEVO MÉTODO: Para obtener citas CON PAGINACIÓN y los mismos filtros
    Page<CitaResponse> obtenerCitasPaginadas(Long usuarioId, Long dentistaId, String estado, LocalDate fechaInicio,
                                             LocalDate fechaFin, Long tratamientoId, String sexo, Pageable pageable);

    List<Cita> obtenerCitasPorUsuario(Long usuarioId);
    List<Cita> obtenerCitasPorDentista(Long dentistaId);

    void agregarCita(CitaRequest citaRequest);
    void actualizarCita(Long id, CitaRequest citaRequest);
    void atenderCita(Long id);
    void eliminarCita(Long id, String observaciones);
    boolean validarDisponibilidad(ValidacionCitaRequest request);
    void reprogramarCita(Long id, CitaReprogramarRequest request);
}