package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.DentistaRequest;
import com.clinicavillegas.app.appointment.dto.response.DentistaResponse;
import com.clinicavillegas.app.appointment.models.Dentista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DentistaService {
    List<Dentista> obtenerDentistas();
    Dentista obtenerDentista(Long id);
    List<DentistaResponse> obtenerDentistas(String nombre, String especializacion, Long usuarioId);
    Page<DentistaResponse> obtenerDentistasPaginados(String nombre, String especializacion, Long usuarioId, Pageable pageable);
    void agregarDentista(DentistaRequest request);
    void actualizarDentista(Long id, DentistaRequest request);
    void eliminarDentista(Long id);
    List<String> obtenerEspecialidades();
}
