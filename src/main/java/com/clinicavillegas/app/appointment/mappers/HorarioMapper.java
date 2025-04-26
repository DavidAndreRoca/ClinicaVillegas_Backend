package com.clinicavillegas.app.appointment.mappers;

import com.clinicavillegas.app.appointment.dto.response.HorarioResponse;
import com.clinicavillegas.app.appointment.models.Horario;

public class HorarioMapper {
    public static HorarioResponse toDto(Horario horario){
        return HorarioResponse.builder()
                .id(horario.getId())
                .dia(horario.getDia().toString())
                .horaComienzo(horario.getHoraComienzo())
                .horaFin(horario.getHoraFin())
                .dentistaId(horario.getDentista().getId())
                .build();
    }
}
