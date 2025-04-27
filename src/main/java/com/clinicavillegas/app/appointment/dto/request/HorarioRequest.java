package com.clinicavillegas.app.appointment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HorarioRequest {
    String dia;
    LocalTime horaComienzo;
    LocalTime horaFin;
    Long dentistaId;
}