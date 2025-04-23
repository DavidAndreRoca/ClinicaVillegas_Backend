package com.clinicavillegas.app.user.specifications;

import com.clinicavillegas.app.user.models.TipoDocumento;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class TipoDocumentoSpecification {
    public static Specification<TipoDocumento> conNombre(String nombre) {
        return (Root<TipoDocumento> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (nombre == null) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%");
        };
    }
    public static Specification<TipoDocumento> conAcronimo(String acronimo) {
        return (Root<TipoDocumento> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (acronimo == null) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("acronimo")), "%" + acronimo.toLowerCase() + "%");
        };
    }
    public static Specification<TipoDocumento> conEstado(Boolean estado) {
        return (Root<TipoDocumento> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (estado == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("estado"), estado);
        };
    }
}
