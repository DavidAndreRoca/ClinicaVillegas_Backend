package com.clinicavillegas.app.appointment.services.impl;

import com.clinicavillegas.app.appointment.dto.request.CitaReprogramarRequest;
import com.clinicavillegas.app.appointment.dto.request.CitaRequest;
import com.clinicavillegas.app.appointment.dto.request.ValidacionCitaRequest;
import com.clinicavillegas.app.appointment.dto.response.CitaResponse;
import com.clinicavillegas.app.appointment.mappers.CitaMapper;
import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.appointment.repositories.CitaRepository;
import com.clinicavillegas.app.appointment.repositories.DentistaRepository;
import com.clinicavillegas.app.appointment.repositories.TratamientoRepository;
import com.clinicavillegas.app.appointment.services.CitaService;
import com.clinicavillegas.app.appointment.specifications.CitaSpecification;
import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import com.clinicavillegas.app.user.models.Sexo;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.TipoDocumentoRepository;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
public class DefaultCitaService implements CitaService {

    private static final String CACHE_CITAS_LISTA_PAGINADA = "citasListaPaginada"; // Nuevo nombre para cache de paginación
    private static final String CACHE_CITAS_LISTA_SIN_PAGINAR = "citasListaSinPaginar"; // Nuevo nombre para cache sin paginar
    private static final String CACHE_CITA_POR_ID = "citaPorId";
    private static final String CACHE_CITAS_POR_USUARIO = "citasPorUsuario"; // Podría ser una lista sin paginar o una paginada por usuario
    private static final String CACHE_CITAS_POR_DENTISTA = "citasPorDentista"; // Podría ser una lista sin paginar o una paginada por dentista


    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DentistaRepository dentistaRepository;
    private final TratamientoRepository tratamientoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    public DefaultCitaService(CitaRepository citaRepository, UsuarioRepository usuarioRepository, DentistaRepository dentistaRepository, TratamientoRepository tratamientoRepository, TipoDocumentoRepository tipoDocumentoRepository) {
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.dentistaRepository = dentistaRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }

    // Método auxiliar para construir la Specification
    private Specification<Cita> buildCitaSpecification(Long usuarioId, Long dentistaId, String estado, LocalDate fechaInicio,
                                                       LocalDate fechaFin, Long tratamientoId, String sexo) {
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la de fin");
        }

        return CitaSpecification.conUsuarioId(usuarioId)
                .and(CitaSpecification.conDentistaId(dentistaId))
                .and(CitaSpecification.conRangoFecha(fechaInicio, fechaFin))
                .and(CitaSpecification.conTratamientoId(tratamientoId))
                .and(CitaSpecification.conSexo(sexo))
                .and(CitaSpecification.conEstado(estado));
    }

    // MÉTODOS DE BÚSQUEDA

    // 1. Método para obtener citas con paginación
    @Cacheable(value = CACHE_CITAS_LISTA_PAGINADA, key = "{#usuarioId, #dentistaId, #estado, #fechaInicio, #fechaFin, #tratamientoId, #sexo, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public Page<CitaResponse> obtenerCitasPaginadas(Long usuarioId, Long dentistaId, String estado, LocalDate fechaInicio,
                                                    LocalDate fechaFin, Long tratamientoId, String sexo, Pageable pageable) {
        log.info("Obteniendo citas PAGINADAS de la base de datos con filtros y paginación: pageable={}", pageable);
        Specification<Cita> specs = buildCitaSpecification(usuarioId, dentistaId, estado, fechaInicio, fechaFin, tratamientoId, sexo);
        Page<Cita> citasPage = citaRepository.findAll(specs, pageable);
        return citasPage.map(CitaMapper::toDto);
    }

    // 2. Método para obtener citas SIN paginación
    @Cacheable(value = CACHE_CITAS_LISTA_SIN_PAGINAR, key = "{#usuarioId, #dentistaId, #estado, #fechaInicio, #fechaFin, #tratamientoId, #sexo}")
    public List<CitaResponse> obtenerCitas(Long usuarioId, Long dentistaId, String estado, LocalDate fechaInicio,
                                                        LocalDate fechaFin, Long tratamientoId, String sexo) {
        log.info("Obteniendo citas SIN PAGINACIÓN de la base de datos con filtros.");
        Specification<Cita> specs = buildCitaSpecification(usuarioId, dentistaId, estado, fechaInicio, fechaFin, tratamientoId, sexo);
        List<Cita> citas = citaRepository.findAll(specs);
        return citas.stream().map(CitaMapper::toDto).toList();
    }

    // Puedes mantener estos si aún son útiles, aunque el método anterior con Specification es más flexible
    public List<Cita> obtenerCitasPorUsuario(Long usuarioId) {
        log.info("Obteniendo citas por usuario de la base de datos para ID: {}", usuarioId);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(
                () -> new ResourceNotFoundException(Usuario.class, usuarioId)
        );
        return citaRepository.findByUsuario(usuario);
    }

    public List<Cita> obtenerCitasPorDentista(Long dentistaId) {
        log.info("Obteniendo citas por dentista de la base de datos para ID: {}", dentistaId);
        Dentista dentista = dentistaRepository.findById(dentistaId).orElseThrow(
                () -> new ResourceNotFoundException(Dentista.class, dentistaId)
        );
        return citaRepository.findByDentista(dentista);
    }

    // MÉTODOS DE MODIFICACIÓN (INVALIDACIÓN DE CACHÉ)

    @Caching(evict = {
            @CacheEvict(value = CACHE_CITA_POR_ID, key = "#id", allEntries = true), // Considera si necesitas invalidar ALL para #id
            @CacheEvict(value = CACHE_CITAS_LISTA_PAGINADA, allEntries = true), // Invalida todas las páginas
            @CacheEvict(value = CACHE_CITAS_LISTA_SIN_PAGINAR, allEntries = true), // Invalida la lista completa sin paginar
            @CacheEvict(value = CACHE_CITAS_POR_USUARIO, key = "#citaRequest.usuarioId"), // Si cacheas listas por usuario
            @CacheEvict(value = CACHE_CITAS_POR_DENTISTA, key = "#citaRequest.dentistaId") // Si cacheas listas por dentista
    })
    public void agregarCita(CitaRequest citaRequest) {
        log.info("Agregando nueva cita: {}", citaRequest);
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findByAcronimo(citaRequest.getTipoDocumento()).orElseThrow(
                () -> new ResourceNotFoundException(TipoDocumento.class, "Acrónimo", citaRequest.getTipoDocumento())
        );
        Dentista dentista = dentistaRepository.findById(citaRequest.getDentistaId()).orElseThrow(
                () -> new ResourceNotFoundException(Dentista.class, citaRequest.getDentistaId())
        );
        Usuario usuario = usuarioRepository.findById(citaRequest.getUsuarioId()).orElseThrow(
                () -> new ResourceNotFoundException(Usuario.class, citaRequest.getUsuarioId())
        );
        Tratamiento tratamiento = tratamientoRepository.findById(citaRequest.getTratamientoId()).orElseThrow(
                () -> new ResourceNotFoundException(Tratamiento.class, citaRequest.getTratamientoId())
        );
        Cita cita = Cita.builder()
                .fecha(citaRequest.getFecha())
                .hora(citaRequest.getHora())
                .monto(citaRequest.getMonto())
                .nombres(citaRequest.getNombres())
                .apellidoPaterno(citaRequest.getApellidoPaterno())
                .apellidoMaterno(citaRequest.getApellidoMaterno())
                .estado("Pendiente")
                .tipoDocumento(tipoDocumento)
                .numeroIdentidad(citaRequest.getNumeroIdentidad())
                .sexo(Sexo.valueOf(citaRequest.getSexo()))
                .fechaNacimiento(citaRequest.getFechaNacimiento())
                .dentista(dentista)
                .usuario(usuario)
                .tratamiento(tratamiento)
                .build();
        citaRepository.save(cita);
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_CITA_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_CITAS_LISTA_PAGINADA, allEntries = true),
            @CacheEvict(value = CACHE_CITAS_LISTA_SIN_PAGINAR, allEntries = true),
            @CacheEvict(value = CACHE_CITAS_POR_USUARIO, key = "#citaRequest.usuarioId"),
            @CacheEvict(value = CACHE_CITAS_POR_DENTISTA, key = "#citaRequest.dentistaId")
    })
    public void actualizarCita(Long id, CitaRequest citaRequest) {
        log.info("Actualizando cita en la base de datos y caché para ID: {}", id);
        Cita cita = citaRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Cita.class, id)
        );
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findByNombre(citaRequest.getTipoDocumento()).orElseThrow(
                () -> new ResourceNotFoundException(Tratamiento.class, "nombre", citaRequest.getTipoDocumento())
        );
        Dentista dentista = dentistaRepository.findById(citaRequest.getDentistaId()).orElseThrow(
                () -> new ResourceNotFoundException(Dentista.class, citaRequest.getDentistaId())
        );
        Usuario usuario = usuarioRepository.findById(citaRequest.getUsuarioId()).orElseThrow(
                () -> new ResourceNotFoundException(Usuario.class, citaRequest.getUsuarioId())
        );
        Tratamiento tratamiento = tratamientoRepository.findById(citaRequest.getTratamientoId()).orElseThrow(
                () -> new ResourceNotFoundException(Tratamiento.class, citaRequest.getTratamientoId())
        );
        cita.setMonto(citaRequest.getMonto());
        cita.setHora(citaRequest.getHora());
        cita.setFecha(citaRequest.getFecha());
        cita.setNombres(citaRequest.getNombres());
        cita.setApellidoPaterno(citaRequest.getApellidoPaterno());
        cita.setApellidoMaterno(citaRequest.getApellidoMaterno());
        cita.setTipoDocumento(tipoDocumento);
        cita.setNumeroIdentidad(citaRequest.getNumeroIdentidad());
        cita.setSexo(Sexo.valueOf(citaRequest.getSexo()));
        cita.setFechaNacimiento(citaRequest.getFechaNacimiento());
        cita.setDentista(dentista);
        cita.setUsuario(usuario);
        cita.setTratamiento(tratamiento);
        citaRepository.save(cita);
    }

    // Para atenderCita, eliminarCita y reprogramarCita, donde el request no contiene los IDs de usuario/dentista,
    // es necesario cargar la cita para obtener esos IDs antes de la ejecución del método y así invalidar cachés.
    // La forma más práctica con las anotaciones en métodos 'void' es re-evaluar la estrategia para estos dos cachés
    // o aceptar que solo se invalidarán las listas generales y la cita por ID.
    // Como acordamos no usar el método auxiliar y no poner anotaciones si no se puede acceder 'externamente',
    // mantendremos solo la invalidación de la cita por ID y las listas ALL_ENTRIES.
    @Caching(evict = {
            @CacheEvict(value = CACHE_CITA_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_CITAS_LISTA_PAGINADA, allEntries = true),
            @CacheEvict(value = CACHE_CITAS_LISTA_SIN_PAGINAR, allEntries = true)
    })
    public void atenderCita(Long id) {
        log.info("Marcando cita como atendida en la base de datos y caché para ID: {}", id);
        Cita cita = citaRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Cita.class, id)
        );
        cita.setEstado("Atendida");
        citaRepository.save(cita);
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_CITA_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_CITAS_LISTA_PAGINADA, allEntries = true),
            @CacheEvict(value = CACHE_CITAS_LISTA_SIN_PAGINAR, allEntries = true)
    })
    public void eliminarCita(Long id, String observaciones) {
        log.info("Marcando cita como cancelada (lógico) en la base de datos y caché para ID: {}", id);
        Cita cita = citaRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Cita.class, id)
        );
        cita.setEstado("Cancelada");
        cita.setObservaciones(observaciones);
        citaRepository.save(cita);
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_CITA_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_CITAS_LISTA_PAGINADA, allEntries = true),
            @CacheEvict(value = CACHE_CITAS_LISTA_SIN_PAGINAR, allEntries = true)
    })
    public void reprogramarCita(Long id, CitaReprogramarRequest request) {
        log.info("Reprogramando cita en la base de datos y caché para ID: {}", id);
        Cita cita = citaRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Cita.class, id)
        );
        cita.setHora(request.getHora());
        cita.setFecha(request.getFecha());
        citaRepository.save(cita);
    }

    public boolean validarDisponibilidad(ValidacionCitaRequest request) {
        log.info("Validando disponibilidad para fecha: {}, hora: {}, dentistaId: {}, tratamientoId: {}",
                request.getFecha(), request.getHora(), request.getDentistaId(), request.getTratamientoId());
        LocalDate fecha = LocalDate.parse(request.getFecha());
        LocalTime hora = LocalTime.parse(request.getHora());

        List<Cita> citasDelDia = citaRepository
                .findAll(CitaSpecification.conFecha(fecha)
                        .and(CitaSpecification.conEstado("Pendiente"))
                        .and(CitaSpecification.conDentistaId(request.getDentistaId())));

        Tratamiento tratamiento = tratamientoRepository.findById(request.getTratamientoId()).orElseThrow(
                () -> new ResourceNotFoundException(Tratamiento.class, request.getTratamientoId())
        );

        Duration duracion = tratamiento.getDuracion();
        LocalTime horaFinPropuesta = hora.plus(duracion);

        for (Cita cita : citasDelDia) {
            LocalTime horaInicioExistente = cita.getHora();
            LocalTime horaFinExistente = horaInicioExistente.plus(cita.getTratamiento().getDuracion());

            boolean seCruza = (hora.isBefore(horaFinExistente) && horaFinPropuesta.isAfter(horaInicioExistente));

            if (seCruza) {
                return false;
            }
        }
        return true;
    }
}