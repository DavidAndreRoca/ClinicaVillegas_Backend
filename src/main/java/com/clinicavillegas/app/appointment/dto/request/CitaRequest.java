package com.clinicavillegas.app.appointment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "La fecha de la cita es un campo obligatorio y no puede estar vacío")
    LocalDate fecha;

    @NotBlank(message = "La hora de la cita es un campo obligatorio y no puede estar vacío")
    LocalTime hora;

    @NotBlank(message = "El costo de la cita es un campo obligatorio y no puede estar vacío")
    BigDecimal monto;

    @NotBlank(message = "Los nombres del paciente son un campo obligatorio y no puede estar vacío")
    @Size(max = 40, message = "Los nombres no pueden superar los 40 caractéres")
    String nombres;

    @NotBlank(message = "El apellido paterno del paciente es un campo obligatorio y no puede estar vacío")
    @Size(max = 35, message = "Los nombres no pueden superar los 35 caractéres")
    String apellidoPaterno;

    @NotBlank(message = "El apellido materno del paciente es un campo obligatorio y no puede estar vacío")
    @Size(max = 35, message = "Los nombres no pueden superar los 35 caractéres")
    String apellidoMaterno;

    @NotBlank(message = "El tipo de documento del paciente es un campo obligatorio y no puede estar vacío")
    String tipoDocumento;

    @NotBlank(message = "El número de identidad del paciente es un campo obligatorio y no puede estar vacío")
    @Size(max = 25, message = "Los nombres no pueden superar los 25 caractéres")
    String numeroIdentidad;

    @NotBlank(message = "El sexo del paciente es un campo obligatorio y no puede estar vacío")
    String sexo;

    @NotBlank(message = "La fecha de nacimiento del paciente es un campo obligatorio y no puede estar vacío")
    LocalDate fechaNacimiento;

    @NotBlank(message = "El ID del dentista asignado a la cita es un campo obligatorio y no puede estar vacío")
    Long dentistaId;

    @NotBlank(message = "El ID del paciente es un campo obligatorio y no puede estar vacío")
    Long usuarioId;

    @NotBlank(message = "El ID del tratamiento asignado a la cita es un campo obligatorio y no puede estar vacío")
    Long tratamientoId;
}
