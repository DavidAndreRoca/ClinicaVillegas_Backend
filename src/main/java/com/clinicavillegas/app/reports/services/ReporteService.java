package com.clinicavillegas.app.reports.services;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.reports.dto.ReporteRequestDTO;
import com.clinicavillegas.app.user.models.TipoDocumento;
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
        Join<Tratamiento, TipoTratamiento> tipoTratamientoJoin = tratamientoJoin.join("tipoTratamiento", JoinType.LEFT);

        Join<Cita, Dentista> dentistaJoin = root.join("dentista", JoinType.LEFT);
        Join<Dentista, Usuario> usuarioDentistaJoin = dentistaJoin.join("usuario", JoinType.LEFT);

        Join<Cita, Usuario> usuarioPacienteJoin = root.join("usuario", JoinType.LEFT);
        Join<Usuario, TipoDocumento> tipoDocumentoJoin = usuarioPacienteJoin.join("tipoDocumento", JoinType.LEFT);

        List<Selection<?>> selections = new ArrayList<>();
        List<Expression<?>> groupByExpressions = new ArrayList<>();

        for (String fila : dto.getFilas()) {
            Expression<String> expr = mapCampoToExpression(fila, root, usuarioDentistaJoin, tratamientoJoin, tipoTratamientoJoin, tipoDocumentoJoin);
            selections.add(expr.alias(fila));
            groupByExpressions.add(expr);
        }

        if (dto.getColumnas() != null && !dto.getColumnas().isEmpty()) {
            String campoColumna = dto.getColumnas().getFirst();
            List<String> valoresColumna = obtenerValoresUnicosParaColumna(campoColumna);

            for (String valor : valoresColumna) {
                Expression<String> columnaExpr = mapCampoToExpression(campoColumna, root, usuarioDentistaJoin, tratamientoJoin, tipoTratamientoJoin, tipoDocumentoJoin);
                Expression<?> caseExpr = buildAggregation(
                        dto.getAgregacion(),
                        cb.selectCase()
                                .when(cb.equal(cb.upper(columnaExpr), valor.toUpperCase()), root.get(dto.getValor()))
                                .otherwise((Object) null),
                        cb
                );
                selections.add(caseExpr.alias(valor));
            }
        } else {
            Expression<?> aggExpr = buildAggregation(dto.getAgregacion(), root.get(dto.getValor()), cb);
            selections.add(aggExpr.alias(dto.getAgregacion()));
        }

        query.multiselect(selections);
        if (!groupByExpressions.isEmpty()) {
            query.groupBy(groupByExpressions);
        }

        List<Predicate> predicates = new ArrayList<>();
        if (dto.getFiltros() != null) {
            for (Map.Entry<String, Object> filtro : dto.getFiltros().entrySet()) {
                if (filtro.getKey() != null){
                    Expression<String> expr = mapCampoToExpression(filtro.getKey(), root, usuarioDentistaJoin, tratamientoJoin, tipoTratamientoJoin, tipoDocumentoJoin);
                    predicates.add(cb.equal(expr, filtro.getValue().toString()));
                }
            }
        }
        if (dto.getFechaDesde() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), dto.getFechaDesde()));
        }
        if (dto.getFechaHasta() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), dto.getFechaHasta()));
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

    private Expression<String> mapCampoToExpression(
            String campo,
            Root<Cita> root,
            Join<Dentista, Usuario> usuarioDentistaJoin,
            Join<Cita, Tratamiento> tratamientoJoin,
            Join<Tratamiento, TipoTratamiento> tipoTratamientoJoin,
            Join<Usuario, TipoDocumento> tipoDocumentoJoin
    ) {
        Path<?> path;
        return switch (campo) {
            case "estado" -> root.get("estado").as(String.class);
            case "sexo" -> root.get("sexo").as(String.class);
            case "tratamiento" -> tratamientoJoin.get("nombre").as(String.class);
            case "tipoTratamiento" -> tipoTratamientoJoin.get("nombre").as(String.class);
            case "dentista" -> usuarioDentistaJoin.get("nombres").as(String.class);
            case "tipoDocumento" -> tipoDocumentoJoin.get("nombre").as(String.class);
            default -> root.get(campo).as(String.class);
        };
    }

    private Expression<?> buildAggregation(String tipo, Expression<?> campo, CriteriaBuilder cb) {
        return switch (tipo.toLowerCase()) {
            case "conteo" -> cb.count(campo);
            case "suma" -> cb.sum((Expression<Number>) campo);
            case "promedio" -> cb.avg((Expression<Number>) campo);
            case "max" -> cb.max((Expression<Number>) campo);
            case "min" -> cb.min((Expression<Number>) campo);
            default -> throw new IllegalArgumentException("Agregación no soportada: " + tipo);
        };
    }

    private List<String> obtenerValoresUnicosParaColumna(String campo) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Cita> root = cq.from(Cita.class);

        Join<Cita, Tratamiento> tratamientoJoin = root.join("tratamiento", JoinType.LEFT);
        Join<Tratamiento, TipoTratamiento> tipoTratamientoJoin = tratamientoJoin.join("tipoTratamiento", JoinType.LEFT);

        Join<Cita, Dentista> dentistaJoin = root.join("dentista", JoinType.LEFT);
        Join<Dentista, Usuario> usuarioDentistaJoin = dentistaJoin.join("usuario", JoinType.LEFT);

        Join<Cita, Usuario> usuarioPacienteJoin = root.join("usuario", JoinType.LEFT);
        Join<Usuario, TipoDocumento> tipoDocumentoJoin = usuarioPacienteJoin.join("tipoDocumento", JoinType.LEFT);

        Expression<String> expr = mapCampoToExpression(campo, root, usuarioDentistaJoin, tratamientoJoin, tipoTratamientoJoin, tipoDocumentoJoin);
        cq.select(cb.upper(expr)).distinct(true);
        return entityManager.createQuery(cq).getResultList();
    }
}



