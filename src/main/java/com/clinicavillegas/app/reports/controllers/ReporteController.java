package com.clinicavillegas.app.reports.controllers;

import com.clinicavillegas.app.common.EndpointPaths;
import com.clinicavillegas.app.reports.dto.ReporteRequestDTO;
import com.clinicavillegas.app.reports.services.ReportePdfService;
import com.clinicavillegas.app.reports.services.ReporteService;
import com.clinicavillegas.app.user.models.Usuario;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.REPORTE_BASE)
public class ReporteController {

    private final ReporteService reporteService;
    private final ReportePdfService reportePdfService;

    public ReporteController(ReporteService reporteService, ReportePdfService reportePdfService) {
        this.reporteService = reporteService;
        this.reportePdfService = reportePdfService;
    }

    @PostMapping
    public ResponseEntity<List<Map<String, Object>>> generarReporte(@RequestBody ReporteRequestDTO dto) {
        List<Map<String, Object>> reporte = reporteService.generarPivotReporte(dto);
        return ResponseEntity.ok(reporte);
    }
    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> generarReportePdf(
            @RequestBody ReporteRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario // Asumiendo autenticación Spring Security
    ) {
        try {
            byte[] pdf = reportePdfService.generarReportePdf(dto, usuario);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition
                    .builder("attachment")
                    .filename("reporte_" + LocalDate.now() + ".pdf")
                    .build()
            );

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}

