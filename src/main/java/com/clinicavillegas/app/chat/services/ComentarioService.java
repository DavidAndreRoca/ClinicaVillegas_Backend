package com.clinicavillegas.app.chat.services;

import com.clinicavillegas.app.chat.dto.request.ComentarioRequest;
import com.clinicavillegas.app.chat.dto.response.ComentarioResponse;

import java.util.List;

public interface ComentarioService {
    List<ComentarioResponse> obtenerComentarios();

    void agregarComentario(ComentarioRequest request);

    List<ComentarioResponse> obtenerRespuestas(Long comentarioId);
}
