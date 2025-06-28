package com.clinicavillegas.app.appointment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TratamientoRequest {

    @NotBlank(message = "El nombre del tratamiento es un campo obligatorio y no puede estar vacío")
    String nombre;

    String descripcion;

    @NotBlank(message = "El costo del tratamiento es un campo obligatorio y no puede estar vacío")
    BigDecimal costo;

    String imagenURL;

    @NotBlank(message = "La duración del tratamiento es un campo obligatorio y no puede estar vacío")
    int duracion;

    @NotBlank(message = "El ID del tipo de tratamiento es un campo obligatorio y no puede estar vacío")
    Long tipoTratamientoId;
}
