package com.clinicavillegas.app.user.dto.request;

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
public class UsuarioRequest {

    @NotBlank(message = "Los nombres de usuario no pueden estar vacíos")
    @Size(max = 40, message = "Los nombres no pueden superar los 40 caractéres")
    String nombres;

    @NotBlank(message = "El apellido paterno del usuario no puede estar vacío")
    @Size(max = 35, message = "El apellido paterno del usuario no puede superar los 35 caractéres")
    String apellidoPaterno;

    @NotBlank(message = "El apellido materno del usuario no puede estar vacío")
    @Size(max = 35, message = "El apellido materno del usuario no puede superar los 35 caractéres")
    String apellidoMaterno;

    @NotBlank(message = "El telefono del usuario no puede ser un campo vacío")
    @Size(min = 9, max = 9, message = "El número de usuario debe tener 9 dígitos")
    String telefono;

    String imagenPerfil;
}
