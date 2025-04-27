package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.HorarioRequest;
import com.clinicavillegas.app.appointment.dto.response.HorarioResponse;
import com.clinicavillegas.app.appointment.models.Horario;

import java.util.List;

public interface HorarioService {
    List<Horario> obteneHorarios();
    List<HorarioResponse> obtenerHorarios(Long dentistaId, String dia);
    void agregarHorario(HorarioRequest request);
    void eliminarHorario(Long id);
}

