package com.clinicavillegas.app.email.dto;

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
public class CodeRequest {

    @NotBlank(message = "El correo del receptor del correo no puede estar vacío.")
    @Email(message = "El correo no es válido.")
    String email;

}
