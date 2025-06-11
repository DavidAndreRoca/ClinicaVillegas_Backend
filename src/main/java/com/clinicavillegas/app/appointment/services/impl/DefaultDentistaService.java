package com.clinicavillegas.app.appointment.services.impl;

import com.clinicavillegas.app.appointment.dto.request.DentistaRequest;
import com.clinicavillegas.app.appointment.dto.response.DentistaResponse;
import com.clinicavillegas.app.appointment.mappers.DentistaMapper;
import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.models.Horario;
import com.clinicavillegas.app.appointment.repositories.DentistaRepository;
import com.clinicavillegas.app.appointment.repositories.HorarioRepository;
import com.clinicavillegas.app.appointment.services.DentistaService;
import com.clinicavillegas.app.appointment.specifications.DentistaSpecification;
import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import com.clinicavillegas.app.user.models.Rol;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DefaultDentistaService implements DentistaService {

    private static final String CACHE_DENTISTAS_LISTA = "dentistasLista";
    private static final String CACHE_DENTISTA_POR_ID = "dentistaPorId";
    private static final String CACHE_ESPECIALIZACIONES_LISTA = "especializacionesLista";


    private final DentistaRepository dentistaRepository;

    private final UsuarioRepository usuarioRepository;

    private final HorarioRepository horarioRepository;

    public DefaultDentistaService(DentistaRepository dentistaRepository, UsuarioRepository usuarioRepository, HorarioRepository horarioRepository) {
        this.dentistaRepository = dentistaRepository;
        this.usuarioRepository = usuarioRepository;
        this.horarioRepository = horarioRepository;
    }

    public List<Dentista> obtenerDentistas() {
        log.info("Obteniendo todos los dentistas sin filtros.");
        return dentistaRepository.findAll();
    }

    @Cacheable(value = CACHE_DENTISTA_POR_ID, key = "#id")
    public Dentista obtenerDentista(Long id) {
        log.info("Obteniendo dentista de la base de datos por ID: {}", id);
        return dentistaRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Dentista.class, id)
        );
    }

    @Cacheable(value = CACHE_DENTISTAS_LISTA, key = "{#nombre, #especializacion, #usuarioId}")
    public List<DentistaResponse> obtenerDentistas(String nombre, String especializacion, Long usuarioId) {
        log.info("Obteniendo dentistas de la base de datos con nombre: {}, especialización: {}, usuarioId: {}", nombre, especializacion, usuarioId);

        Specification<Dentista> specs = DentistaSpecification.conNombre(nombre)
                .and(DentistaSpecification.conEspecializacion(especializacion))
                .and(DentistaSpecification.conEstado(true));

        List<Dentista> dentistas = dentistaRepository.findAll(specs);

        return dentistas.stream()
                .map(DentistaMapper::toDto)
                .toList();
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_DENTISTAS_LISTA, allEntries = true),
            @CacheEvict(value = CACHE_ESPECIALIZACIONES_LISTA, allEntries = true)
    })
    public void agregarDentista(DentistaRequest request) {
        log.info("Agregando nuevo dentista para usuario ID: {}", request.getUsuarioId());

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId()).orElseThrow(
                () -> new ResourceNotFoundException(Usuario.class, request.getUsuarioId())
        );

        usuario.setRol(Rol.DENTISTA);
        usuarioRepository.save(usuario);

        Dentista dentista = Dentista.builder()
                .nColegiatura(request.getNColegiatura())
                .especializacion(request.getEspecializacion())
                .estado(true)
                .usuario(usuario)
                .build();
        dentistaRepository.save(dentista);
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_DENTISTA_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_DENTISTAS_LISTA, allEntries = true),
            @CacheEvict(value = CACHE_ESPECIALIZACIONES_LISTA, allEntries = true)
    })
    public void actualizarDentista(Long id, DentistaRequest request) {
        log.info("Actualizando dentista en la base de datos y caché para ID: {}", id);
        Dentista dentista = dentistaRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Dentista.class, id)
        );
        Usuario usuarioAnterior = dentista.getUsuario();
        usuarioAnterior.setRol(Rol.PACIENTE);
        usuarioRepository.save(usuarioAnterior);

        Usuario usuarioActual = usuarioRepository.findById(request.getUsuarioId()).orElseThrow(
                () -> new ResourceNotFoundException(Usuario.class, request.getUsuarioId())
        );
        usuarioActual.setRol(Rol.DENTISTA);
        usuarioRepository.save(usuarioActual);


        dentista.setUsuario(usuarioActual);
        dentista.setNColegiatura(request.getNColegiatura());
        dentista.setEspecializacion(request.getEspecializacion());
        dentistaRepository.save(dentista);
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_DENTISTA_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_DENTISTAS_LISTA, allEntries = true),
            @CacheEvict(value = CACHE_ESPECIALIZACIONES_LISTA, allEntries = true)
    })
    public void eliminarDentista(Long id) {
        log.info("Eliminando dentista (y sus horarios) de la base de datos y caché para ID: {}", id);
        Dentista dentista = dentistaRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Dentista.class, id)
        );
        List<Horario> horarios = horarioRepository.findByDentista(dentista);
        for (Horario horario : horarios) {
            horarioRepository.delete(horario);
        }
        Usuario usuario = dentista.getUsuario();
        usuario.setRol(Rol.PACIENTE);
        usuarioRepository.save(usuario);
        dentistaRepository.delete(dentista);
    }

    @Cacheable(value = CACHE_ESPECIALIZACIONES_LISTA, key = "'allEspecializaciones'")
    public List<String> obtenerEspecialidades() {
        log.info("Obteniendo lista de especializaciones de la base de datos.");
        return dentistaRepository.findEspecializaciones();
    }
}