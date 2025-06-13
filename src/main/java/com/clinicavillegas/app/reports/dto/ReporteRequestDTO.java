package com.clinicavillegas.app.reports.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class ReporteRequestDTO {
    private List<String> filas;
    private List<String> columnas;
    private String valor;
    private String agregacion; // "count", "sum"
    private Map<String, Object> filtros;
}
