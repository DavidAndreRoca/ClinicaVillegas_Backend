package com.clinicavillegas.app.chat.services;

import com.clinicavillegas.app.chat.dto.ComentarioRequest;
import com.clinicavillegas.app.chat.dto.ComentarioResponse;
import com.clinicavillegas.app.chat.mappers.ComentarioMapper;
import com.clinicavillegas.app.chat.models.Comentario;
import com.clinicavillegas.app.chat.repositories.ComentarioRepository;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultComentarioService implements ComentarioService {
    private final ComentarioRepository comentarioRepository;

    private final UsuarioRepository usuarioRepository;

    public DefaultComentarioService(ComentarioRepository comentarioRepository, UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ComentarioResponse> obtenerComentarios() {
        List<Comentario> comentarios = comentarioRepository.findByComentario(null);
        return comentarios.stream().map(ComentarioMapper::toDto).toList();

    }

    public void agregarComentario(ComentarioRequest request) {
        Comentario comentario = Comentario.builder()
                .contenido(request.getContenido())
                .usuario(usuarioRepository.findById(request.getUsuarioId()).orElseThrow())
                .build();
        if (request.getComentarioId() != null) {
            comentario.setComentario(comentarioRepository.findById(request.getComentarioId()).orElseThrow());
        }
        comentarioRepository.save(comentario);
    }

    public List<ComentarioResponse> obtenerRespuestas(Long comentarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId).orElseThrow();
        List<Comentario> comentarios = comentarioRepository.findByComentario(comentario);
        return comentarios.stream().map(ComentarioMapper::toDto).toList();
    }
}
