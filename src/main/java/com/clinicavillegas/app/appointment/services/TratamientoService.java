package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.TratamientoRequest;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TratamientoService {

    // Método para obtener tratamientos CON PAGINACIÓN (con filtros)
    // Este método es llamado por el controlador cuando 'all' es false o no se especifica.
    Page<Tratamiento> obtenerTratamientosPaginados(Long tipoId, String nombre, Boolean estado, Pageable pageable);

    // Método para obtener todos los tratamientos SIN PAGINACIÓN (con filtros)
    // Este método es llamado por el controlador cuando 'all' es true.
    List<Tratamiento> obtenerTodosTratamientos(Long tipoId, String nombre, Boolean estado);

    // Otros métodos de CRUD existentes
    Tratamiento obtenerTratamiento(Long id);
    void guardarTratamiento(TratamientoRequest tratamientoRequest);
    void actualizarTratamiento(Long id, TratamientoRequest tratamientoRequest);
    void eliminarTratamiento(Long id);
}