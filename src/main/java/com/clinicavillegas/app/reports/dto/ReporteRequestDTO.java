package com.clinicavillegas.app.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReporteRequestDTO {
    private List<String> filtros;          // Filtros aplicados (report filters)
    private List<String> filas;            // Etiquetas de fila (rows)
    private List<String> columnas;         // Etiquetas de columna (columns)
    private AgregacionDTO valor;          // Valor agregado (values)
    private Map<String, Object> condiciones; // Valores específicos de filtros, ej. {"estado": "FINALIZADA"}
}

