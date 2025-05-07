package com.clinicavillegas.app.chat.services;

import com.clinicavillegas.app.chat.dto.ComentarioRequest;
import com.clinicavillegas.app.chat.dto.ComentarioResponse;
import com.clinicavillegas.app.chat.mappers.ComentarioMapper;
import com.clinicavillegas.app.chat.models.Comentario;
import com.clinicavillegas.app.chat.repositories.ComentarioRepository;
import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import com.clinicavillegas.app.user.models.Usuario;
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
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId()).orElseThrow(
                () -> new ResourceNotFoundException(Usuario.class, request.getUsuarioId())
        );
        Comentario comentario = Comentario.builder()
                .contenido(request.getContenido())
                .usuario(usuario)
                .build();
        if (request.getComentarioId() != null) {
            Comentario comentarioPadre = comentarioRepository.findById(request.getComentarioId()).orElseThrow(
                    () -> new ResourceNotFoundException(Comentario.class, request.getComentarioId())
            );
            comentario.setComentario(comentarioPadre);
        }
        comentarioRepository.save(comentario);
    }

    public List<ComentarioResponse> obtenerRespuestas(Long comentarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId).orElseThrow(
                () -> new ResourceNotFoundException(Comentario.class, comentarioId)
        );
        List<Comentario> comentarios = comentarioRepository.findByComentario(comentario);
        return comentarios.stream().map(ComentarioMapper::toDto).toList();
    }
}
