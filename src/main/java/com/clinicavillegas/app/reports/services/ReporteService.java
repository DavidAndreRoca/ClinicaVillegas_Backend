package com.clinicavillegas.app.reports.services;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.reports.dto.ReporteRequestDTO;
import com.clinicavillegas.app.user.models.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteService {

    @PersistenceContext
    private final EntityManager entityManager;

    public List<Map<String, Object>> generarPivotReporte(ReporteRequestDTO dto) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Cita> root = query.from(Cita.class);

        Join<Cita, Tratamiento> tratamientoJoin = root.join("tratamiento", JoinType.LEFT);
        Join<Cita, Dentista> dentistaJoin = root.join("dentista", JoinType.LEFT);
        Join<Dentista, Usuario> usuarioDentistaJoin = dentistaJoin.join("usuario", JoinType.LEFT);

        List<Selection<?>> selections = new ArrayList<>();
        List<Expression<?>> groupByExpressions = new ArrayList<>();

        // Filas
        for (String fila : dto.getFilas()) {
            Expression<String> expr = mapCampoToExpression(fila, root, usuarioDentistaJoin, tratamientoJoin);
            selections.add(expr.alias(fila));
            groupByExpressions.add(expr);
        }

        // Columnas (solo se soporta una para pivot)
        if (dto.getColumnas() != null && !dto.getColumnas().isEmpty()) {
            String campoColumna = dto.getColumnas().getFirst();
            List<String> valoresColumna = obtenerValoresUnicosParaColumna(campoColumna);

            for (String valor : valoresColumna) {
                Expression<String> columnaExpr = mapCampoToExpression(campoColumna, root, usuarioDentistaJoin, tratamientoJoin);
                Expression<?> caseExpr = buildAggregation(
                        dto.getAgregacion(),
                        cb.selectCase()
                                .when(cb.equal(cb.upper(columnaExpr.as(String.class)), valor.toUpperCase()), root.get(dto.getValor()))
                                .otherwise((Object) null),
                        cb
                );
                selections.add(caseExpr.alias(valor));
            }
        } else {
            // Sin columnas → agregación directa
            Expression<?> aggExpr = buildAggregation(dto.getAgregacion(), root.get(dto.getValor()), cb);
            selections.add(aggExpr.alias(dto.getAgregacion()));
        }

        query.multiselect(selections);
        if (!groupByExpressions.isEmpty()) {
            query.groupBy(groupByExpressions);
        }

        // Filtros dinámicos
        List<Predicate> predicates = new ArrayList<>();
        if (dto.getFiltros() != null) {
            for (Map.Entry<String, Object> filtro : dto.getFiltros().entrySet()) {
                Expression<String> expr = mapCampoToExpression(filtro.getKey(), root, usuarioDentistaJoin, tratamientoJoin);
                predicates.add(cb.equal(expr, filtro.getValue().toString()));
            }
        }

        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        return entityManager.createQuery(query)
                .getResultList()
                .stream()
                .map(tuple -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (TupleElement<?> element : tuple.getElements()) {
                        row.put(element.getAlias(), tuple.get(element));
                    }
                    return row;
                })
                .toList();
    }

    // 🎯 Conversión segura de campo a expresión string-compatible
    private Expression<String> mapCampoToExpression(
            String campo,
            Root<Cita> root,
            Join<Dentista, Usuario> usuarioDentistaJoin,
            Join<Cita, Tratamiento> tratamientoJoin
    ) {
        Path<?> path;
        switch (campo) {
            case "estado" -> path = root.get("estado");
            case "sexo" -> path = root.get("sexo");
            case "tratamiento" -> path = tratamientoJoin.get("nombre");
            case "dentista" -> path = usuarioDentistaJoin.get("nombres");
            case "fecha" -> path = root.get("fecha");
            default -> path = root.get(campo);
        }

        // Si es enum, casteamos a String de forma segura
        path.getJavaType();

        return path.as(String.class);
    }

    // 🎯 Agregación dinámica
    private Expression<?> buildAggregation(String tipo, Expression<?> campo, CriteriaBuilder cb) {
        return switch (tipo.toLowerCase()) {
            case "count" -> cb.count(campo);
            case "sum" -> cb.sum((Expression<Number>) campo);
            case "avg" -> cb.avg((Expression<Number>) campo);
            case "max" -> cb.max((Expression<Number>) campo);
            case "min" -> cb.min((Expression<Number>) campo);
            default -> throw new IllegalArgumentException("Agregación no soportada: " + tipo);
        };
    }

    // 🎯 Obtener valores únicos (distintos) para la columna dinámica
    private List<String> obtenerValoresUnicosParaColumna(String campo) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Cita> root = cq.from(Cita.class);

        Join<Cita, Tratamiento> tratamientoJoin = root.join("tratamiento", JoinType.LEFT);
        Join<Cita, Dentista> dentistaJoin = root.join("dentista", JoinType.LEFT);
        Join<Dentista, Usuario> usuarioDentistaJoin = dentistaJoin.join("usuario", JoinType.LEFT);

        Expression<String> expr = mapCampoToExpression(campo, root, usuarioDentistaJoin, tratamientoJoin);
        cq.select(cb.upper(expr.as(String.class))).distinct(true);
        return entityManager.createQuery(cq).getResultList();
    }
}


