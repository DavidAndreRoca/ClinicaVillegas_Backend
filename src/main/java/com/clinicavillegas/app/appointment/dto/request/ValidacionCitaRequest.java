package com.clinicavillegas.app.appointment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidacionCitaRequest {

    @NotBlank(message = "La fecha de la cita es un campo obligatorio y no puede estar vacío")
    String fecha;

    @NotBlank(message = "La hora de la cita es un campo obligatorio y no puede estar vacío")
    String hora;

    @NotBlank(message = "El ID del tipo de tratamiento es un campo obligatorio y no puede estar vacío")
    Long tratamientoId;

    @NotBlank(message = "El ID del dentista asignado al horario es un campo obligatorio y no puede estar vacío")
    Long dentistaId;
}
