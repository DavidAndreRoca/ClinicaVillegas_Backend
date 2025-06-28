package com.clinicavillegas.app.chat.services;

import com.clinicavillegas.app.chat.dto.request.ComentarioRequest;
import com.clinicavillegas.app.chat.dto.response.ComentarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ComentarioService {
    //Antes
    //List<ComentarioResponse> obtenerComentarios();
    //Despues
    Page<ComentarioResponse> obtenerComentarios(Pageable pageable);
    void agregarComentario(ComentarioRequest request);

    List<ComentarioResponse> obtenerRespuestas(Long comentarioId);
}