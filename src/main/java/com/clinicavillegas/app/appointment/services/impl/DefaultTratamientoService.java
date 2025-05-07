package com.clinicavillegas.app.appointment.services.impl;

import com.clinicavillegas.app.appointment.dto.request.TratamientoRequest;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.appointment.repositories.TipoTratamientoRepository;
import com.clinicavillegas.app.appointment.repositories.TratamientoRepository;
import com.clinicavillegas.app.appointment.services.TratamientoService;
import com.clinicavillegas.app.appointment.specifications.TratamientoSpecification;
import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class DefaultTratamientoService implements TratamientoService {
    private final TratamientoRepository tratamientoRepository;

    private final TipoTratamientoRepository tipoTratamientoRepository;

    public DefaultTratamientoService(
            TratamientoRepository tratamientoRepository,
            TipoTratamientoRepository tipoTratamientoRepository
    ){
        this.tipoTratamientoRepository = tipoTratamientoRepository;
        this.tratamientoRepository = tratamientoRepository;
    }
    public List<Tratamiento> obtenerTratamientos() {
        return tratamientoRepository.findAll();
    }

    public List<Tratamiento> obtenerTratamientos(Long tipoId, String nombre) {
        Specification<Tratamiento> specs = TratamientoSpecification.tipoTratamientoEquals(tipoId)
                .and(TratamientoSpecification.nombreEquals(nombre))
                .and(TratamientoSpecification.estadoEquals(true));
        return tratamientoRepository.findAll(specs);

    }

    public Tratamiento obtenerTratamiento(Long id) {
        return tratamientoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Tratamiento.class, id)
        );
    }

    public void guardarTratamiento(TratamientoRequest request) {
        TipoTratamiento tipoTratamiento = tipoTratamientoRepository.findById(request.getTipoTratamientoId()).orElseThrow(
                () -> new ResourceNotFoundException(TipoTratamiento.class, request.getTipoTratamientoId())
        );
        Tratamiento tratamiento = Tratamiento.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .costo(request.getCosto())
                .duracion(Duration.ofMinutes(request.getDuracion()))
                .imagenURL(request.getImagenURL())
                .estado(true)
                .tipoTratamiento(tipoTratamiento)
                .build();
        tratamientoRepository.save(tratamiento);
    }

    public void actualizarTratamiento(Long id, TratamientoRequest request) {
        Tratamiento tratamiento = tratamientoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Tratamiento.class, id)
        );
        TipoTratamiento tipoTratamiento = tipoTratamientoRepository.findById(request.getTipoTratamientoId()).orElseThrow(
                () -> new ResourceNotFoundException(TipoTratamiento.class, request.getTipoTratamientoId())
        );
        tratamiento.setNombre(request.getNombre());
        tratamiento.setDescripcion(request.getDescripcion());
        tratamiento.setCosto(request.getCosto());
        tratamiento.setDuracion(Duration.ofMinutes(request.getDuracion()));
        tratamiento.setImagenURL(request.getImagenURL());
        tratamiento.setEstado(true);
        tratamiento.setTipoTratamiento(tipoTratamiento);
        tratamientoRepository.save(tratamiento);
    }

    public void eliminarTratamiento(Long id) {
        Tratamiento tratamiento = tratamientoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Tratamiento.class, id)
        );
        tratamiento.setEstado(false);
        tratamientoRepository.save(tratamiento);
    }

}
