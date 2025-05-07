package com.clinicavillegas.app.appointment.dto.request;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "El día es un campo obligatorio y no puede estar vacío")
    String dia;

    @NotBlank(message = "La hora de comienzo es un campo obligatorio y no puede estar vacío")
    LocalTime horaComienzo;

    @NotBlank(message = "La hora de fin es un campo obligatorio y no puede estar vacío")
    LocalTime horaFin;

    @NotBlank(message = "El ID del dentista asignado al horario es un campo obligatorio y no puede estar vacío")
    Long dentistaId;
}