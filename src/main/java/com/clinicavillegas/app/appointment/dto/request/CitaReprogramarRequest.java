package com.clinicavillegas.app.appointment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CitaReprogramarRequest {

    @NotBlank(message = "La fecha de reprogramación es un campo obligatorio y no puede estar vacío.")
    LocalDate fecha;

    @NotBlank(message = "La hora de reprogramación es un campo obligatorio y no puede estar vacío.")
    LocalTime hora;
}
