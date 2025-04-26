package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.DentistaRequest;
import com.clinicavillegas.app.appointment.dto.response.DentistaResponse;
import com.clinicavillegas.app.appointment.models.Dentista;

import java.util.List;

public interface DentistaService {
    List<Dentista> obtenerDentistas();
    Dentista obtenerDentista(Long id);
    List<DentistaResponse> obtenerDentistas(String nombre, String especializacion, Long usuarioId);
    void agregarDentista(DentistaRequest request);
    void actualizarDentista(Long id, DentistaRequest request);
    void eliminarDentista(Long id);
    List<String> obtenerEspecialidades();
}
