package com.clinicavillegas.app.chat.services.impl;

import com.clinicavillegas.app.chat.dto.request.ComentarioRequest;
import com.clinicavillegas.app.chat.dto.response.ComentarioResponse;
import com.clinicavillegas.app.chat.mappers.ComentarioMapper;
import com.clinicavillegas.app.chat.models.Comentario;
import com.clinicavillegas.app.chat.repositories.ComentarioRepository;
import com.clinicavillegas.app.chat.services.ComentarioService;
import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DefaultComentarioService implements ComentarioService {

    private static final String CACHE_COMENTARIOS_PADRE = "comentariosPadre";
    private static final String CACHE_RESPUESTAS_COMENTARIO = "respuestasComentario";

    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;

    public DefaultComentarioService(ComentarioRepository comentarioRepository, UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Cacheable(value = CACHE_COMENTARIOS_PADRE, key = "'allRootComments'")
    public List<ComentarioResponse> obtenerComentarios() {
        log.info("Obteniendo comentarios padre de la base de datos.");
        List<Comentario> comentarios = comentarioRepository.findByComentario(null);
        return comentarios.stream().map(ComentarioMapper::toDto).toList();
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_COMENTARIOS_PADRE, allEntries = true),
            @CacheEvict(value = CACHE_RESPUESTAS_COMENTARIO, key = "#request.comentarioId", condition = "#request.comentarioId != null")
    })
    public void agregarComentario(ComentarioRequest request) {
        log.info("Agregando nuevo comentario: {}", request);
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

    @Cacheable(value = CACHE_RESPUESTAS_COMENTARIO, key = "#comentarioId")
    public List<ComentarioResponse> obtenerRespuestas(Long comentarioId) {
        log.info("Obteniendo respuestas para el comentario ID: {} de la base de datos.", comentarioId);
        Comentario comentario = comentarioRepository.findById(comentarioId).orElseThrow(
                () -> new ResourceNotFoundException(Comentario.class, comentarioId)
        );
        List<Comentario> comentarios = comentarioRepository.findByComentario(comentario);
        return comentarios.stream().map(ComentarioMapper::toDto).toList();
    }
}