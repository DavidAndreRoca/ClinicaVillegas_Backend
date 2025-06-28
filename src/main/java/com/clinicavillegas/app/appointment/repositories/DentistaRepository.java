package com.clinicavillegas.app.appointment.repositories;

import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.user.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DentistaRepository extends JpaRepository<Dentista, Long>, JpaSpecificationExecutor<Dentista> {
    Optional<Dentista> findByUsuario(Usuario usuario);

    @Query("SELECT DISTINCT d.especializacion FROM dentistas d")
    List<String> findEspecializaciones();
}
