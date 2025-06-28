// src/main/java/com/clinicavillegas/app/appointment/dto/request/CancelacionCitaRequest.java

package com.clinicavillegas.app.appointment.dto.request;

import jakarta.validation.constraints.NotBlank; // Para validación
import jakarta.validation.constraints.Size;    // Para validación
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancelacionCitaRequest {

    @NotBlank(message = "Las observaciones para la cancelación son obligatorias y no pueden estar vacías.")
    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.")
    private String observaciones;

}