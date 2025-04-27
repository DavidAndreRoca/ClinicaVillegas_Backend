package com.clinicavillegas.app.appointment.specifications;

import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class TipoTratamientoSpecification {
    public static Specification<TipoTratamiento> conEstado(Boolean estado){
        return (Root<TipoTratamiento> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (estado == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("estado"), estado);
        };
    }
}
