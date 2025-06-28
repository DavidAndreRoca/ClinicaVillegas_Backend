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
public class DentistaRequest {

    @NotBlank(message = "El número de colegiatura del dentista es un campo obligatorio y no debe estar vacío")
    @Size(max = 25, message = "Los nombres no pueden superar los 25 caractéres")
    String nColegiatura;

    @NotBlank(message = "La especialización del dentista es un campo obligatorio y no debe estar vacío")
    @Size(max = 25, message = "Los nombres no pueden superar los 25 caractéres")
    String especializacion;

    @NotBlank(message = "El ID del usuario asignado a los datos del dentista es un campo obligatorio y no debe estar vacío")
    Long usuarioId;
}
