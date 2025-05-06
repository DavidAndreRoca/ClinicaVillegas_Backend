package com.clinicavillegas.app.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComentarioRequest {

    @NotBlank(message = "El contenido del mensaje no puede estar vacío.")
    String contenido;

    @NotBlank(message = "El ID del usuario es un campo obligatorio y no puede estar vacío")
    Long usuarioId;

    Long comentarioId;
}
