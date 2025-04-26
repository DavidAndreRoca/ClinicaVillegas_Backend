package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.TipoTratamientoRequest;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;

import java.util.List;

public interface TipoTratamientoService {
    List<TipoTratamiento> obtenerTiposTratamiento();
    void agregarTipoTratamiento(TipoTratamientoRequest request);
    void actualizarTipoTratamiento(Long id, TipoTratamientoRequest request);
    void eliminarTipoTratamiento(Long id);
}

