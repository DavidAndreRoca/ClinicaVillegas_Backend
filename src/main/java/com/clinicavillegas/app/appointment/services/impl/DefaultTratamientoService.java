package com.clinicavillegas.app.appointment.services.impl;

import com.clinicavillegas.app.appointment.dto.request.TratamientoRequest;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.appointment.repositories.TipoTratamientoRepository;
import com.clinicavillegas.app.appointment.repositories.TratamientoRepository;
import com.clinicavillegas.app.appointment.services.TratamientoService;
import com.clinicavillegas.app.appointment.specifications.TratamientoSpecification; // Importa tu clase de especificación estática
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // ¡Importante! Asegúrate de importar esta
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
public class DefaultTratamientoService implements TratamientoService {

    private final TratamientoRepository tratamientoRepository;
    private final TipoTratamientoRepository tipoTratamientoRepository;

    public DefaultTratamientoService(TratamientoRepository tratamientoRepository, TipoTratamientoRepository tipoTratamientoRepository) {
        this.tratamientoRepository = tratamientoRepository;
        this.tipoTratamientoRepository = tipoTratamientoRepository;
    }

    // Renombramos este método a obtenerTratamientosPaginados para que el controlador lo use
    @Override
    @Transactional(readOnly = true)
    public Page<Tratamiento> obtenerTratamientosPaginados(Long tipoId, String nombre, Boolean estado, Pageable pageable) {
        // Combina los métodos estáticos de TratamientoSpecification
        Specification<Tratamiento> spec = Specification
                .where(TratamientoSpecification.tipoTratamientoEquals(tipoId))
                .and(TratamientoSpecification.nombreEquals(nombre))
                .and(TratamientoSpecification.estadoEquals(estado));

        return tratamientoRepository.findAll(spec, pageable);
    }

    // Este método es para obtener TODOS los tratamientos (sin paginación) con filtros
    @Override
    @Transactional(readOnly = true)
    public List<Tratamiento> obtenerTodosTratamientos(Long tipoId, String nombre, Boolean estado) {
        // Combina los métodos estáticos de TratamientoSpecification
        Specification<Tratamiento> spec = Specification
                .where(TratamientoSpecification.tipoTratamientoEquals(tipoId))
                .and(TratamientoSpecification.nombreEquals(nombre))
                .and(TratamientoSpecification.estadoEquals(estado));

        return tratamientoRepository.findAll(spec);
    }

    @Override
    @Transactional(readOnly = true)
    public Tratamiento obtenerTratamiento(Long id) {
        return tratamientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tratamiento con ID " + id + " no encontrado"));
    }

    @Override
    @Transactional
    public void guardarTratamiento(TratamientoRequest request) {
        TipoTratamiento tipoTratamiento = tipoTratamientoRepository.findById(request.getTipoTratamientoId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de tratamiento con ID " + request.getTipoTratamientoId() + " no encontrado"));

        Tratamiento tratamiento = Tratamiento.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .costo(request.getCosto())
                .duracion(Duration.ofMinutes(request.getDuracion()))
                .estado(true)
                .imagenURL(request.getImagenURL())
                .tipoTratamiento(tipoTratamiento)
                .build();
        tratamientoRepository.save(tratamiento);
    }

    @Override
    @Transactional
    public void actualizarTratamiento(Long id, TratamientoRequest request) {
        Tratamiento tratamientoExistente = tratamientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tratamiento con ID " + id + " no encontrado"));

        TipoTratamiento tipoTratamiento = tipoTratamientoRepository.findById(request.getTipoTratamientoId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de tratamiento con ID " + request.getTipoTratamientoId() + " no encontrado"));

        tratamientoExistente.setNombre(request.getNombre());
        tratamientoExistente.setDescripcion(request.getDescripcion());
        tratamientoExistente.setCosto(request.getCosto());
        tratamientoExistente.setDuracion(Duration.ofMinutes(request.getDuracion()));
        tratamientoExistente.setImagenURL(request.getImagenURL());
        tratamientoExistente.setTipoTratamiento(tipoTratamiento);

        tratamientoRepository.save(tratamientoExistente);
    }

    @Override
    @Transactional
    public void eliminarTratamiento(Long id) {
        Tratamiento tratamientoExistente = tratamientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tratamiento con ID " + id + " no encontrado"));
        tratamientoExistente.setEstado(false);
        tratamientoRepository.save(tratamientoExistente);
    }
}