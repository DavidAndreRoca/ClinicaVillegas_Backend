package com.clinicavillegas.app.appointment.specifications;

import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class TipoTratamientoSpecification {

    // Método para filtrar por nombre (LIKE %nombre%)
    public static Specification<TipoTratamiento> conNombre(String nombre) {
        return (Root<TipoTratamiento> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (!StringUtils.hasText(nombre)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%");
        };
    }

    public static Specification<TipoTratamiento> conEstado(Boolean estado){
        return (Root<TipoTratamiento> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (estado == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("estado"), estado);
        };
    }
}
