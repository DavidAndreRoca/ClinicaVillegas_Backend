package com.clinicavillegas.app.chat.repositories;

import com.clinicavillegas.app.chat.models.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByComentario(Comentario comentario);
}
