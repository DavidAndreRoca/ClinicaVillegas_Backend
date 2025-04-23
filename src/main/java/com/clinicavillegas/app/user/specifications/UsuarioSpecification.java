package com.clinicavillegas.app.user.specifications;

import com.clinicavillegas.app.user.models.Rol;
import com.clinicavillegas.app.user.models.Usuario;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class UsuarioSpecification {
    public static Specification<Usuario> conEstado(Boolean estado){
        return (Root<Usuario> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            if (estado == null) {
                return builder.conjunction();
            }
            return builder.equal(root.get("estado"), estado);
        };
    }
    public static Specification<Usuario> conRol(String rol){
        return (Root<Usuario> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            if (rol == null) {
                return builder.conjunction();
            }
            return builder.equal(root.get("rol"), Rol.valueOf(rol.toUpperCase()));
        };
    }
    public static Specification<Usuario> conNombres(String nombres){
        return (Root<Usuario> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            if (nombres == null) {
                return builder.conjunction();
            }
            return builder.like(builder.lower(root.get("nombres")), "%" + nombres.toLowerCase() + "%");
        };
    }
}
