package com.clinicavillegas.app.appointment.services.impl;

import com.clinicavillegas.app.appointment.dto.request.HorarioRequest;
import com.clinicavillegas.app.appointment.dto.response.HorarioResponse;
import com.clinicavillegas.app.appointment.mappers.HorarioMapper;
import com.clinicavillegas.app.appointment.models.Dia;
import com.clinicavillegas.app.appointment.models.Horario;
import com.clinicavillegas.app.appointment.repositories.DentistaRepository;
import com.clinicavillegas.app.appointment.repositories.HorarioRepository;
import com.clinicavillegas.app.appointment.services.HorarioService;
import com.clinicavillegas.app.appointment.specifications.HorarioSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class DefaultHorarioService implements HorarioService {
    private final HorarioRepository horarioRepository;

    public final DentistaRepository dentistaRepository;

    public DefaultHorarioService(HorarioRepository horarioRepository, DentistaRepository dentistaRepository) {
        this.horarioRepository = horarioRepository;
        this.dentistaRepository = dentistaRepository;
    }

    public List<Horario> obteneHorarios() {
        return horarioRepository.findAll();
    }

    public List<HorarioResponse> obtenerHorarios(Long dentistaId, String dia){
        Specification<Horario> specs = HorarioSpecification.conDentistaId(dentistaId).and(HorarioSpecification.conDia(dia));
        List<Horario> horarios = horarioRepository.findAll(specs);
        return horarios.stream().map(HorarioMapper::toDto).toList();
    }

    public void agregarHorario(HorarioRequest request) {
        if (request.getHoraComienzo().isAfter(request.getHoraFin())) {
            throw new IllegalArgumentException("La hora de finalización no puede ser anterior a la hora de inicio");
        }
        //que haya una diferencia minima de 8 horas entre ellos
        if (Duration.between(request.getHoraComienzo(), request.getHoraFin()).abs().toHours() < 8) {
            throw new IllegalArgumentException("El tiempo de horario no puede ser menor a 8 horas");
        }
        Specification<Horario> specs = HorarioSpecification.conDentistaId(request.getDentistaId());
        List<Horario> horarios = horarioRepository.findAll(specs);
        for (Horario horario : horarios) {
            if (horario.getDia() == Dia.valueOf(request.getDia())) {
                throw new IllegalArgumentException("Ya existe un horario para ese dia");
            }
        }
        Horario horario = Horario.builder()
                .dia(Dia.valueOf(request.getDia()))
                .horaComienzo(request.getHoraComienzo())
                .horaFin(request.getHoraFin())
                .dentista(dentistaRepository.findById(request.getDentistaId()).orElseThrow())
                .build();
        horarioRepository.save(horario);
    }

    public void eliminarHorario(Long id) {
        horarioRepository.deleteById(id);
    }
}
