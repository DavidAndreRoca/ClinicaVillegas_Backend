package com.clinicavillegas.app.auth.repositories;

import com.clinicavillegas.app.auth.models.Session;
import com.clinicavillegas.app.user.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByToken(String token);
    void deleteByUsuario(Usuario usuario);
}

