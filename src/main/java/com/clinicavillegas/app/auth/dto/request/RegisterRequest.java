package com.clinicavillegas.app.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El correo es un campo obligatorio y no puede estar vacío.")
    @Email(message = "El correo no es válido.")
    String correo;

    @NotBlank(message = "El tipo de documento del usuario es un campo obligatorio y no puede estar vacío.")
    @Size(max = 100, message = "El tipo de documento no puede superar los 100 caractéres.")
    String tipoDocumento;

    @NotBlank(message = "El número de identidad del usuario es un campo obligatorio y no puede estar vacío.")
    @Size(max = 25, message = "El número de identidad del usuario no puede superar los 100 dígitos.")
    String documento;

    @NotBlank(message = "Los nombres de usuario no pueden estar vacíos")
    @Size(max = 40, message = "Los nombres no pueden superar los 40 caractéres")
    String nombres;

    @NotBlank(message = "El apellido paterno del usuario no puede estar vacío")
    @Size(max = 35, message = "El apellido paterno del usuario no puede superar los 35 caractéres")
    String apellidoPaterno;

    @NotBlank(message = "El apellido materno del usuario no puede estar vacío")
    @Size(max = 35, message = "El apellido materno del usuario no puede superar los 35 caractéres")
    String apellidoMaterno;

    @NotBlank(message = "La fecha de nacimiento del usuario es un campo obligatorio y no puede estar vacío.")
    LocalDate fechaNacimiento;

    @NotBlank(message = "El telefono del usuario no puede ser un campo vacío")
    @Size(min = 9, max = 9, message = "El número de usuario debe tener 9 dígitos")
    String telefono;

    @NotBlank(message = "El sexo del usuario es un campo obligatorio y no puede estar vacío.")
    String sexo;

    @NotBlank(message = "La contraseña del usuario es un campo obligatorio y no puede estar vacío.")
    String contrasena;
}