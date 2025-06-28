package com.clinicavillegas.app.chat.repositories;

import com.clinicavillegas.app.chat.models.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByComentario(Comentario comentario);

    // Nuevo método para obtener comentarios padre paginados
    Page<Comentario> findByComentarioIsNull(Pageable pageable);
    // findByComentarioIsNull es el equivalente a findByComentario(null) pero para paginación
}