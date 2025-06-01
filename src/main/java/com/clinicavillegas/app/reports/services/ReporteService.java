package com.clinicavillegas.app.reports.services;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.reports.dto.ReporteRequestDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Map<String, Object>> generarPivotReporte(ReporteRequestDTO dto) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Cita> root = query.from(Cita.class);

        List<Predicate> predicates = new ArrayList<>();

        // Filtros con valores específicos
        if (dto.getCondiciones() != null) {
            for (Map.Entry<String, Object> entry : dto.getCondiciones().entrySet()) {
                String campo = entry.getKey();
                Object valor = entry.getValue();

                Path<?> path = switch (campo) {
                    case "estado" -> root.get("estado");
                    case "dentistaNombres" -> root.get("dentista").get("usuario").get("nombres");
                    case "fecha" -> root.get("fecha");
                    default -> throw new IllegalArgumentException("Campo no soportado: " + campo);
                };

                predicates.add(cb.equal(path, valor));
            }
        }

        List<Selection<?>> selections = new ArrayList<>();
        List<Expression<?>> groupBy = new ArrayList<>();

        if (dto.getFilas() != null) {
            for (String campo : dto.getFilas()) {
                Path<?> path = obtenerPath(root, campo);
                selections.add(path.alias(campo));
                groupBy.add(path);
            }
        }

        if (dto.getColumnas() != null) {
            for (String campo : dto.getColumnas()) {
                Path<?> path = obtenerPath(root, campo);
                selections.add(path.alias(campo));
                groupBy.add(path);
            }
        }

        // Agregado
        Expression<?> valorExpr = switch (dto.getValor().getFuncion()) {
            case "count" -> cb.count(root.get(dto.getValor().getCampo()));
            case "sum" -> cb.sum(root.get(dto.getValor().getCampo()));
            case "avg" -> cb.avg(root.get(dto.getValor().getCampo()));
            default -> throw new IllegalArgumentException("Función no soportada");
        };
        selections.add(valorExpr.alias("valor"));

        query.multiselect(selections).where(predicates.toArray(new Predicate[0]));
        if (!groupBy.isEmpty()) query.groupBy(groupBy);

        return entityManager.createQuery(query).getResultList().stream().map(tuple -> {
            Map<String, Object> row = new LinkedHashMap<>();
            for (TupleElement<?> el : tuple.getElements()) {
                row.put(el.getAlias(), tuple.get(el));
            }
            return row;
        }).toList();
    }

    private Path<?> obtenerPath(Root<Cita> root, String campo) {
        return switch (campo) {
            case "estado" -> root.get("estado");
            case "fecha" -> root.get("fecha");
            case "dentistaNombres" -> root.get("dentista").get("usuario").get("nombres");
            case "id" -> root.get("id");
            case "monto" -> root.get("monto");
            default -> throw new IllegalArgumentException("Campo no reconocido: " + campo);
        };
    }

}
