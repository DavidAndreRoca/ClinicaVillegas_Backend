package com.clinicavillegas.app.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComentarioRequest {
    String contenido;
    Long usuarioId;
    Long comentarioId;
}
