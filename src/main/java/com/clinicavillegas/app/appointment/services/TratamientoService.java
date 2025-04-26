package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.TratamientoRequest;
import com.clinicavillegas.app.appointment.models.Tratamiento;

import java.util.List;

public interface TratamientoService {
    List<Tratamiento> obtenerTratamientos();

    List<Tratamiento> obtenerTratamientos(Long tipoId, String nombre);

    Tratamiento obtenerTratamiento(Long id);

    void guardarTratamiento(TratamientoRequest request);

    void actualizarTratamiento(Long id, TratamientoRequest request);

    void eliminarTratamiento(Long id);
}
