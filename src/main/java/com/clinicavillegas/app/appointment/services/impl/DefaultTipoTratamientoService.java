package com.clinicavillegas.app.appointment.services.impl;

import com.clinicavillegas.app.appointment.dto.request.TipoTratamientoRequest;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.repositories.TipoTratamientoRepository;
import com.clinicavillegas.app.appointment.services.TipoTratamientoService;
import com.clinicavillegas.app.appointment.specifications.TipoTratamientoSpecification;
import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DefaultTipoTratamientoService implements TipoTratamientoService {
    private final TipoTratamientoRepository tipoTratamientoRepository;

    public DefaultTipoTratamientoService(TipoTratamientoRepository tipoTratamientoRepository){
        this.tipoTratamientoRepository = tipoTratamientoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    // Este método mantiene el nombre 'obtenerTiposTratamiento' y ahora acepta filtros.
    public List<TipoTratamiento> obtenerTiposTratamiento(String nombre, Boolean estado) {
        Specification<TipoTratamiento> spec = Specification
                .where(TipoTratamientoSpecification.conNombre(nombre))
                .and(TipoTratamientoSpecification.conEstado(estado));
        return tipoTratamientoRepository.findAll(spec);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TipoTratamiento> obtenerTiposTratamientoPaginados(String nombre, Boolean estado, Pageable pageable) {
        Specification<TipoTratamiento> spec = Specification
                .where(TipoTratamientoSpecification.conNombre(nombre))
                .and(TipoTratamientoSpecification.conEstado(estado));
        return tipoTratamientoRepository.findAll(spec, pageable);
    }

    public void agregarTipoTratamiento(TipoTratamientoRequest request) {
        TipoTratamiento tipoTratamiento = TipoTratamiento.builder()
                .nombre(request.getNombre())
                .estado(true)
                .build();
        tipoTratamientoRepository.save(tipoTratamiento);
    }

    public void actualizarTipoTratamiento(Long id, TipoTratamientoRequest request) {
        TipoTratamiento tipoTratamiento = tipoTratamientoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(TipoTratamiento.class, id)
        );
        tipoTratamiento.setNombre(request.getNombre());
        tipoTratamientoRepository.save(tipoTratamiento);
    }

    public void eliminarTipoTratamiento(Long id) {
        TipoTratamiento tipoTratamiento = tipoTratamientoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(TipoTratamiento.class, id)
        );
        tipoTratamiento.setEstado(false);
        tipoTratamientoRepository.save(tipoTratamiento);
    }
}