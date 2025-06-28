package com.clinicavillegas.app.appointment.specifications;

import com.clinicavillegas.app.appointment.models.Dia;
import com.clinicavillegas.app.appointment.models.Horario;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class HorarioSpecification {
    public static Specification<Horario> conDia(String dia) {
        return (Root<Horario> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (dia == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("dia"), Dia.valueOf(dia));
        };
    }
    public static Specification<Horario> conDentistaId(Long dentistaId) {
        return (Root<Horario> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (dentistaId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("dentista").get("id"), dentistaId);
        };
    }
}
