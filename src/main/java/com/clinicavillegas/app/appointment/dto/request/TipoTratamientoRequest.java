package com.clinicavillegas.app.appointment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TipoTratamientoRequest {

    @NotBlank(message = "El nombre del tipo de tratamiento es un campo obligatorio y no puede estar vacío")
    @Size(max = 60, message = "Los nombres no pueden superar los 60 caractéres")
    String nombre;
}
