package com.clinicavillegas.app.reports.controllers;

import com.clinicavillegas.app.reports.dto.ReporteRequestDTO;
import com.clinicavillegas.app.reports.services.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping
    public ResponseEntity<List<Map<String, Object>>> generarReporte(@RequestBody ReporteRequestDTO dto) {
        List<Map<String, Object>> reporte = reporteService.generarPivotReporte(dto);
        return ResponseEntity.ok(reporte);
    }
}

