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
import org.springframework.data.domain.Page; // Importar Page
import org.springframework.data.domain.Pageable; // Importar Pageable
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors; // Importar Collectors, si aún no lo tienes

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

    // Modificación principal aquí:
    // 1. Quité el @Cacheable por la complejidad de cachear páginas.
    //    Si realmente necesitas cachear, la clave del caché debe incluir los parámetros de pageable.
    // 2. Cambié el tipo de retorno a Page<ComentarioResponse>.
    // 3. Añadí Pageable pageable como parámetro.
    @Override // Asegúrate de que tu interfaz ComentarioService también fue actualizada para coincidir con esta firma.
    public Page<ComentarioResponse> obtenerComentarios(Pageable pageable) {
        log.info("Obteniendo comentarios padre paginados de la base de datos con Pageable: {}", pageable);

        // Aquí usamos el nuevo método del repositorio que devuelve un Page
        // Si no creaste findByComentarioIsNull, puedes usar comentarioRepository.findAll(pageable)
        // si solo quieres los comentarios raíz (los que no tienen padre).
        // Sin embargo, findByComentarioIsNull(pageable) es más específico.
        Page<Comentario> comentariosPage = comentarioRepository.findByComentarioIsNull(pageable);

        // Mapeamos el Page<Comentario> a Page<ComentarioResponse>
        return comentariosPage.map(comentario -> ComentarioMapper.toDto(comentario, obtenerRespuestas(comentario.getId())));
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_COMENTARIOS_PADRE, allEntries = true), // Considera cómo esto impacta la paginación.
            @CacheEvict(value = CACHE_RESPUESTAS_COMENTARIO, key = "#request.comentarioId", condition = "#request.comentarioId != null")
    })
    @Override // Agregué el @Override para asegurar que coincida con la interfaz.
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
    @Override // Agregué el @Override para asegurar que coincida con la interfaz.
    public List<ComentarioResponse> obtenerRespuestas(Long comentarioId) {
        log.info("Obteniendo respuestas para el comentario ID: {} de la base de datos.", comentarioId);
        Comentario comentario = comentarioRepository.findById(comentarioId).orElseThrow(
                () -> new ResourceNotFoundException(Comentario.class, comentarioId)
        );
        List<Comentario> comentarios = comentarioRepository.findByComentario(comentario);
        return comentarios.stream().map(ComentarioMapper::toDto).collect(Collectors.toList());
    }
}