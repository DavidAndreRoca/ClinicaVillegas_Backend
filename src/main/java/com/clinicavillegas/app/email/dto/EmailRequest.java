package com.clinicavillegas.app.email.dto;

import jakarta.validation.constraints.Email;
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
public class EmailRequest {

    @NotBlank(message = "El correo del receptor es obligatorio.")
    @Email(message = "El correo no es válido.")
    private String receptor;

    @NotBlank(message = "El asunto del correo es obligatorio.")
    private String asunto;

    @NotBlank(message = "El contenido del correo no puede estar vacío")
    private String contenido;
}
