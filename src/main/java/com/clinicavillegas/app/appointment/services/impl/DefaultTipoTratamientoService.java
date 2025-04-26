package com.clinicavillegas.app.appointment.services.impl;

import com.clinicavillegas.app.appointment.dto.request.TipoTratamientoRequest;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.repositories.TipoTratamientoRepository;
import com.clinicavillegas.app.appointment.services.TipoTratamientoService;
import com.clinicavillegas.app.appointment.specifications.TipoTratamientoSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultTipoTratamientoService implements TipoTratamientoService {
    private final TipoTratamientoRepository tipoTratamientoRepository;

    public DefaultTipoTratamientoService(TipoTratamientoRepository tipoTratamientoRepository){
        this.tipoTratamientoRepository = tipoTratamientoRepository;
    }

    public List<TipoTratamiento> obtenerTiposTratamiento() {
        Specification<TipoTratamiento> specs = TipoTratamientoSpecification.conEstado(true);
        return tipoTratamientoRepository.findAll(specs);
    }

    public void agregarTipoTratamiento(TipoTratamientoRequest request) {
        TipoTratamiento tipoTratamiento = TipoTratamiento.builder()
                .nombre(request.getNombre())
                .estado(true)
                .build();
        tipoTratamientoRepository.save(tipoTratamiento);
    }

    public void actualizarTipoTratamiento(Long id, TipoTratamientoRequest request) {
        TipoTratamiento tipoTratamiento = tipoTratamientoRepository.findById(id).orElseThrow();
        tipoTratamiento.setNombre(request.getNombre());
        tipoTratamientoRepository.save(tipoTratamiento);
    }

    public void eliminarTipoTratamiento(Long id) {
        TipoTratamiento tipoTratamiento = tipoTratamientoRepository.findById(id).orElseThrow();
        tipoTratamiento.setEstado(false);
        tipoTratamientoRepository.save(tipoTratamiento);
    }
}

