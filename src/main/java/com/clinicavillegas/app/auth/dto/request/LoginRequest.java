package com.clinicavillegas.app.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El correo del usuario es un campo obligatorio.")
    @Email
    private String email;

    @NotBlank(message = "La contraseña del usuario es un campo obligatorio")
    private String contrasena;
}
