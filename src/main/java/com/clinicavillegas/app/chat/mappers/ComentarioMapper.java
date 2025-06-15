package com.clinicavillegas.app.chat.mappers;

import com.clinicavillegas.app.chat.dto.response.ComentarioResponse;
import com.clinicavillegas.app.chat.models.Comentario;

import java.util.List;

public class ComentarioMapper {

    public static ComentarioResponse toDto(Comentario comentario){

        String nombreCompletos= comentario.getUsuario().getNombres() + " " + comentario.getUsuario().getApellidoPaterno();

        return ComentarioResponse.builder()
                .id(comentario.getId())
                .contenido(comentario.getContenido())
                .fecha(comentario.getFechaCreacion())
                .nombresUsuario(nombreCompletos)
                .emailUsuario(comentario.getUsuario().getCorreo())
                .imagenUsuario(comentario.getUsuario().getImagenPerfil())
                .build();
    }

    public static ComentarioResponse toDto(Comentario comentario, List<ComentarioResponse> respuestas) {

        String nombreCompletos= comentario.getUsuario().getNombres() + " " + comentario.getUsuario().getApellidoPaterno();

        return ComentarioResponse.builder()
                .id(comentario.getId())
                .contenido(comentario.getContenido())
                .fecha(comentario.getFechaCreacion())
                .nombresUsuario(nombreCompletos)
                .emailUsuario(comentario.getUsuario().getCorreo())
                .imagenUsuario(comentario.getUsuario().getImagenPerfil())
                .comentarios(respuestas) // Aquí añades las respuestas
                .build();
    }
}