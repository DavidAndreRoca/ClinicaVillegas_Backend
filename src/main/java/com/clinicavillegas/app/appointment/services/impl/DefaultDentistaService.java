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
import com.clinicavillegas.app.user.models.Rol;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultDentistaService implements DentistaService {
    private final DentistaRepository dentistaRepository;

    private final UsuarioRepository usuarioRepository;

    private final HorarioRepository horarioRepository;

    public DefaultDentistaService(DentistaRepository dentistaRepository, UsuarioRepository usuarioRepository, HorarioRepository horarioRepository) {
        this.dentistaRepository = dentistaRepository;
        this.usuarioRepository = usuarioRepository;
        this.horarioRepository = horarioRepository;
    }

    public List<Dentista> obtenerDentistas() {
        return dentistaRepository.findAll();
    }

    public Dentista obtenerDentista(Long id) {
        return dentistaRepository.findById(id).orElse(null);
    }
    public List<DentistaResponse> obtenerDentistas(String nombre, String especializacion, Long usuarioId) {
        Specification<Dentista> specs = DentistaSpecification.conNombre(nombre)
                .and(DentistaSpecification.conEspecializacion(especializacion))
                .and(DentistaSpecification.conEstado(true));
        List<Dentista> dentistas = dentistaRepository.findAll(specs);
        return dentistas.stream()
                .map(DentistaMapper::toDto)
                .toList();
    }
    public void agregarDentista(DentistaRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId()).orElseThrow();
        usuario.setRol(Rol.DENTISTA);
        usuarioRepository.save(usuario);
        Dentista dentista = Dentista.builder()
                .nColegiatura(request.getNColegiatura())
                .especializacion(request.getEspecializacion())
                .estado(true)
                .usuario(usuarioRepository.findById(request.getUsuarioId()).orElseThrow())
                .build();
        dentistaRepository.save(dentista);
    }

    public void actualizarDentista(Long id, DentistaRequest request) {
        Dentista dentista = dentistaRepository.findById(id).orElseThrow();
        Usuario usuarioAnterior = dentista.getUsuario();
        usuarioAnterior.setRol(Rol.PACIENTE);
        usuarioRepository.save(usuarioAnterior);
        Usuario usuarioActual = usuarioRepository.findById(request.getUsuarioId()).orElseThrow();
        dentista.setUsuario(usuarioActual);
        dentista.setNColegiatura(request.getNColegiatura());
        dentista.setEspecializacion(request.getEspecializacion());
        dentistaRepository.save(dentista);
    }

    public void eliminarDentista(Long id) {
        Dentista dentista = dentistaRepository.findById(id).orElseThrow();
        //obtener horarios del dentista
        List<Horario> horarios = horarioRepository.findByDentista(dentista);
        for (Horario horario : horarios) {
            horarioRepository.delete(horario);
        }
        Usuario usuario = dentista.getUsuario();
        usuario.setRol(Rol.PACIENTE);
        usuarioRepository.save(usuario);
        dentistaRepository.delete(dentista);
    }
    public List<String> obtenerEspecialidades() {
        return dentistaRepository.findEspecializaciones();
    }
}
