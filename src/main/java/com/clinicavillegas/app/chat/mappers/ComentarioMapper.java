package com.clinicavillegas.app.chat.mappers;

import com.clinicavillegas.app.chat.dto.response.ComentarioResponse;
import com.clinicavillegas.app.chat.models.Comentario;

public class ComentarioMapper {

    public static ComentarioResponse toDto(Comentario comentario){
        return ComentarioResponse.builder()
                .id(comentario.getId())
                .contenido(comentario.getContenido())
                .fecha(comentario.getFechaCreacion())
                .nombresUsuario(comentario.getUsuario().getNombres())
                .emailUsuario(comentario.getUsuario().getCorreo())
                .imagenUsuario(comentario.getUsuario().getImagenPerfil())
                .build();
    }
}
