package com.clinicavillegas.app.reports.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class ReporteRequestDTO {
    private List<String> filas;
    private List<String> columnas;
    private String valor;
    private String agregacion;
    private Map<String, Object> filtros;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
}
