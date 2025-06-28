package com.clinicavillegas.app.appointment.specifications;


import com.clinicavillegas.app.appointment.models.Tratamiento;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class TratamientoSpecification {
    public static Specification<Tratamiento> tipoTratamientoEquals(Long tipoId) {
        return (Root<Tratamiento> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (tipoId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("tipoTratamiento").get("id"), tipoId);
        };
    }
    public static Specification<Tratamiento> estadoEquals(Boolean estado) {
        return (Root<Tratamiento> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (estado == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("estado"), estado);
        };
    }
    public static Specification<Tratamiento> nombreEquals(String nombre) {
        return (Root<Tratamiento> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (nombre == null) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%");
        };
    }
}
