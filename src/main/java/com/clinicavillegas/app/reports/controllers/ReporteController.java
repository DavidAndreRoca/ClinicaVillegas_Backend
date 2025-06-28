package com.clinicavillegas.app.reports.controllers;

import com.clinicavillegas.app.common.EndpointPaths;
import com.clinicavillegas.app.reports.dto.ReporteRequestDTO;
import com.clinicavillegas.app.reports.services.ReporteExcelService;
import com.clinicavillegas.app.reports.services.ReportePdfService;
import com.clinicavillegas.app.reports.services.ReporteService;
import com.clinicavillegas.app.user.models.Rol;
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
    private final ReporteExcelService reporteExcelService;

    public ReporteController(ReporteService reporteService, ReportePdfService reportePdfService, ReporteExcelService reporteExcelService) {
        this.reporteService = reporteService;
        this.reportePdfService = reportePdfService;
        this.reporteExcelService = reporteExcelService;
    }

    @PostMapping
    public ResponseEntity<List<Map<String, Object>>> generarReporte(
            @RequestBody ReporteRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        if (usuario.getRol().equals(Rol.DENTISTA)){
            dto.getFiltros().put("dentista", usuario.getNombres());
        }
        List<Map<String, Object>> reporte = reporteService.generarPivotReporte(dto);
        return ResponseEntity.ok(reporte);
    }


    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> generarReportePdf(
            @RequestBody ReporteRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        try {
            if (usuario.getRol().equals(Rol.DENTISTA)){
                dto.getFiltros().put("dentista", usuario.getNombres());
            }
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
    @PostMapping("/excel")
    public ResponseEntity<byte[]> generarReporteExcel(@RequestBody ReporteRequestDTO dto,
                                                      @AuthenticationPrincipal Usuario usuario) throws Exception {
        if (usuario.getRol().equals(Rol.DENTISTA)){
            dto.getFiltros().put("dentista", usuario.getNombres());
        }
        byte[] excelBytes = reporteExcelService.generarReporteExcel(dto, usuario);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("reporte_" + LocalDate.now() + ".xlsx")
                .build());

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}

