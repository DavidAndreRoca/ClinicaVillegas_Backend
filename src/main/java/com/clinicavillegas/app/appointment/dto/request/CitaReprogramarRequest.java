package com.clinicavillegas.app.appointment.dto.request;

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
    LocalDate fecha;
    LocalTime hora;
}
