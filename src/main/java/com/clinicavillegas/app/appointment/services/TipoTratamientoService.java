package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.TipoTratamientoRequest;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TipoTratamientoService {

    // Método para obtener TODOS los tipos de tratamiento (sin paginación) con filtros.
    List<TipoTratamiento> obtenerTiposTratamiento(String nombre, Boolean estado);

    // NUEVO MÉTODO: Para obtener tipos de tratamiento CON PAGINACIÓN y filtros
    Page<TipoTratamiento> obtenerTiposTratamientoPaginados(String nombre, Boolean estado, Pageable pageable);

    void agregarTipoTratamiento(TipoTratamientoRequest request);
    void actualizarTipoTratamiento(Long id, TipoTratamientoRequest request);
    void eliminarTipoTratamiento(Long id);
}