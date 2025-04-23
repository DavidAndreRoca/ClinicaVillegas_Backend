package com.clinicavillegas.app.user.repositories;

import com.clinicavillegas.app.user.models.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long>, JpaSpecificationExecutor<TipoDocumento> {
    Optional<TipoDocumento> findByNombre(String nombre);
    Optional<TipoDocumento> findByAcronimo(String acronimo);
}
