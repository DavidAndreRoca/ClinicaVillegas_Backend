package com.clinicavillegas.app.appointment.specifications;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.user.models.Sexo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CitaSpecification {
    public static Specification<Cita> conUsuarioId(Long usuarioId) {
        return (Root<Cita> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (usuarioId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("usuario").get("id"), usuarioId);
        };
    }
    public static Specification<Cita> conDentistaId(Long dentistaId) {
        return (Root<Cita> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (dentistaId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("dentista").get("id"), dentistaId);
        };
    }
    public static Specification<Cita> conEstado(String estado) {
        return (Root<Cita> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (estado == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("estado"), estado);
        };
    }
    public static Specification<Cita> conFecha(LocalDate fecha) {
        return (Root<Cita> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (fecha == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("fecha"), fecha);
        };
    }
    public static Specification<Cita> conRangoFecha(LocalDate startDate, LocalDate endDate) {
        return (Root<Cita> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (startDate == null || endDate == null) {
                return cb.conjunction();
            }
            return cb.between(root.get("fecha"), startDate, endDate);
        };
    }

    public static Specification<Cita> conTratamientoId(Long tratamientoId) {
        return (Root<Cita> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (tratamientoId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("tratamiento").get("id"), tratamientoId);
        };
    }
    public static Specification<Cita> conSexo(String sexo) {
        return (Root<Cita> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (sexo == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("sexo"), Sexo.valueOf(sexo.toUpperCase()));
        };
    }
}
