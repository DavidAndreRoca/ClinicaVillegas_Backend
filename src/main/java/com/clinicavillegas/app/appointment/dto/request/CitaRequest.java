package com.clinicavillegas.app.appointment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CitaRequest {
    LocalDate fecha;
    LocalTime hora;
    BigDecimal monto;

    String nombres;
    String apellidoPaterno;
    String apellidoMaterno;
    String tipoDocumento;
    String numeroIdentidad;
    String sexo;
    LocalDate fechaNacimiento;

    Long dentistaId;
    Long usuarioId;
    Long tratamientoId;
}
