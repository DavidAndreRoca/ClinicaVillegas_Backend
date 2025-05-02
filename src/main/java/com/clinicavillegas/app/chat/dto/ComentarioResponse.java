package com.clinicavillegas.app.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComentarioResponse {
    Long id;
    String contenido;
    LocalDateTime fecha;
    String nombresUsuario;
    String emailUsuario;
    String imagenUsuario;
    List<ComentarioResponse> comentarios;
}
