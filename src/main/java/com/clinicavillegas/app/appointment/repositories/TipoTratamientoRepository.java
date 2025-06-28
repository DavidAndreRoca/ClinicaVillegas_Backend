package com.clinicavillegas.app.appointment.repositories;

import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoTratamientoRepository extends JpaRepository<TipoTratamiento, Long>, JpaSpecificationExecutor<TipoTratamiento> {
    List<TipoTratamiento> findByEstado(boolean estado);
}