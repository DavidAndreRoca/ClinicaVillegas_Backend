package com.clinicavillegas.app.appointment.repositories;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.user.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long>, JpaSpecificationExecutor<Cita> {

    List<Cita> findByUsuario(Usuario usuario);

    List<Cita> findByDentista(Dentista dentista);

    List<Cita> findByFecha(LocalDate fecha);

    @Query(value = "SELECT sexo, COUNT(*) AS total " +
            "FROM cita " +
            "WHERE fecha BETWEEN :startDate AND :endDate " +
            "AND estado = 'Pendiente' " +
            "OR estado = 'Atendida' " +
            "GROUP BY sexo " +
            "ORDER BY sexo", nativeQuery = true)
    List<Object[]> countCitasByDateAndSexo(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT tt.nombre AS tipo_tratamiento, COUNT(*) AS total " +
            "FROM cita c " +
            "JOIN tratamiento t ON c.tratamiento_id = t.id " +
            "JOIN tipo_tratamiento tt ON t.tipo_tratamiento_id = tt.id " +
            "WHERE fecha BETWEEN :startDate AND :endDate " +
            "AND c.estado = 'Pendiente' " +
            "OR c.estado = 'Atendida' " +
            "GROUP BY tt.nombre " +
            "ORDER BY tipo_tratamiento", nativeQuery = true)
    List<Object[]> countCitasByDateAndTipoTratamiento(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT fecha AS fecha_cita, COUNT(*) AS total " +
            "FROM cita " +
            "WHERE fecha BETWEEN :startDate AND :endDate " +
            "AND estado = 'Cancelada' " +
            "GROUP BY fecha " +
            "ORDER BY fecha;", nativeQuery = true)
    List<Object[]> countCitasCanceladasByFecha(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT d.id AS dentista_id, " +
            "d.n_colegiatura AS dentista_colegiatura, " +
            "CONCAT(u.apellido_paterno, \" \",u.apellido_materno, \", \",  u.nombres) AS nombres_completos, "
            +
            "COUNT(c.id) AS citas_atendidas " +
            "FROM dentista d " +
            "JOIN cita c ON c.dentista_id = d.id " +
            "JOIN usuario u ON u.id = d.usuario_id " +
            "WHERE c.estado = :estado " +
            "AND d.estado = 1 " +
            "AND c.fecha BETWEEN :startDate AND :endDate " +
            "GROUP BY d.id, d.n_colegiatura", nativeQuery = true)
    List<Object[]> countCitasAtendidasPorDentista(@Param("estado") String estado,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

}
