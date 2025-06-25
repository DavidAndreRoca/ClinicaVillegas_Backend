package com.clinicavillegas.app.reports.services;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.appointment.repositories.CitaRepository;
import com.clinicavillegas.app.appointment.specifications.CitaSpecification;
import com.clinicavillegas.app.reports.dto.ReporteRequestDTO;
import com.clinicavillegas.app.user.models.Usuario;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReporteExcelService {
    private final ReporteService reporteService;
    private final CitaRepository citaRepository;

    public ReporteExcelService(ReporteService reporteService, CitaRepository citaRepository) {
        this.reporteService = reporteService;
        this.citaRepository = citaRepository;
    }

    public byte[] generarReporteExcel(ReporteRequestDTO dto, Usuario usuarioSolicitante) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();

        XSSFCellStyle headerStyle = crearEstiloCabecera(workbook);
        XSSFCellStyle dateStyle = crearEstiloFecha(workbook, createHelper);
        XSSFCellStyle numberStyle = crearEstiloNumerico(workbook);
        XSSFCellStyle normalStyle = workbook.createCellStyle();

        Sheet resumenSheet = workbook.createSheet("Resumen Reporte");
        int rowIndex = 0;
        rowIndex = agregarEncabezado(resumenSheet, rowIndex, usuarioSolicitante, dto, normalStyle);

        List<Map<String, Object>> resumen = reporteService.generarPivotReporte(dto);
        rowIndex = agregarResumen(resumenSheet, rowIndex, resumen, headerStyle, numberStyle, normalStyle);

        agregarDetalles(workbook, dto, headerStyle, dateStyle, numberStyle, normalStyle);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    private XSSFCellStyle crearEstiloCabecera(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle crearEstiloFecha(XSSFWorkbook workbook, CreationHelper createHelper) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
        return style;
    }

    private XSSFCellStyle crearEstiloNumerico(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private int agregarEncabezado(Sheet sheet, int rowIndex, Usuario usuario, ReporteRequestDTO dto, CellStyle style) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("REPORTE DE CITAS");

        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Fecha:");
        row.createCell(1).setCellValue(LocalDate.now().toString());

        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Usuario:");
        row.createCell(1).setCellValue(usuario.getNombres() + " " + usuario.getApellidoPaterno());

        return rowIndex + 1;
    }

    private int agregarResumen(Sheet sheet, int rowIndex, List<Map<String, Object>> resumen,
                               CellStyle headerStyle, CellStyle numberStyle, CellStyle textStyle) {
        if (resumen.isEmpty()) return rowIndex;

        Set<String> columnas = resumen.getFirst().keySet();

        Row header = sheet.createRow(rowIndex++);
        int colIndex = 0;
        for (String col : columnas) {
            Cell cell = header.createCell(colIndex++);
            cell.setCellValue(col);
            cell.setCellStyle(headerStyle);
        }

        for (Map<String, Object> rowMap : resumen) {
            Row row = sheet.createRow(rowIndex++);
            colIndex = 0;
            for (String col : columnas) {
                Cell cell = row.createCell(colIndex++);
                Object val = rowMap.get(col);
                if (val instanceof Number) {
                    cell.setCellValue(((Number) val).doubleValue());
                    cell.setCellStyle(numberStyle);
                } else {
                    cell.setCellValue(val != null ? val.toString() : "");
                    cell.setCellStyle(textStyle);
                }
            }
        }

        for (int i = 0; i < columnas.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        return rowIndex + 1;
    }

    private void agregarDetalles(XSSFWorkbook workbook, ReporteRequestDTO dto,
                                 CellStyle headerStyle, CellStyle dateStyle, CellStyle numberStyle, CellStyle textStyle) {
        Sheet sheet = workbook.createSheet("Detalles Citas");
        int rowIndex = 0;

        List<Cita> citas = obtenerCitasFiltradas(dto);
        Map<List<String>, List<Cita>> agrupado = citas.stream().collect(Collectors.groupingBy(c ->
                dto.getFilas().stream().map(f -> obtenerValorCampo(c, f)).toList()
        ));

        for (Map.Entry<List<String>, List<Cita>> entry : agrupado.entrySet()) {
            Row groupRow = sheet.createRow(rowIndex++);
            Cell groupCell = groupRow.createCell(0);
            groupCell.setCellValue("Grupo: " + String.join(" - ", entry.getKey()));
            groupCell.setCellStyle(headerStyle);

            Row header = sheet.createRow(rowIndex++);
            String[] headers = {"Fecha", "Paciente", "Tratamiento", "Dentista", "Monto"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (Cita c : entry.getValue()) {
                Row row = sheet.createRow(rowIndex++);
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(java.sql.Date.valueOf(c.getFecha()));
                cell0.setCellStyle(dateStyle);

                row.createCell(1).setCellValue(c.getNombres() + " " + c.getApellidoPaterno());
                row.getCell(1).setCellStyle(textStyle);

                row.createCell(2).setCellValue(c.getTratamiento().getNombre());
                row.getCell(2).setCellStyle(textStyle);

                row.createCell(3).setCellValue(c.getDentista().getUsuario().getNombres());
                row.getCell(3).setCellStyle(textStyle);

                Cell montoCell = row.createCell(4);
                montoCell.setCellValue(c.getMonto().doubleValue());
                montoCell.setCellStyle(numberStyle);
            }

            rowIndex++;
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String obtenerValorCampo(Cita cita, String campo) {
        return switch (campo) {
            case "dentista" -> cita.getDentista().getUsuario().getNombres();
            case "tratamiento" -> cita.getTratamiento().getNombre();
            case "estado" -> cita.getEstado();
            case "sexo" -> cita.getSexo().name();
            default -> "—";
        };
    }

    private List<Cita> obtenerCitasFiltradas(ReporteRequestDTO dto) {
        Specification<Cita> spec = Specification.where(null);

        if (dto.getFiltros() != null) {
            for (Map.Entry<String, Object> filtro : dto.getFiltros().entrySet()) {
                switch (filtro.getKey()) {
                    case "usuarioId" -> spec = spec.and(CitaSpecification.conUsuarioId(asLong(filtro.getValue())));
                    case "dentistaId" -> spec = spec.and(CitaSpecification.conDentistaId(asLong(filtro.getValue())));
                    case "estado" -> spec = spec.and(CitaSpecification.conEstado(filtro.getValue().toString()));
                    case "fecha" -> spec = spec.and(CitaSpecification.conFecha(asLocalDate(filtro.getValue())));
                    case "tratamientoId" -> spec = spec.and(CitaSpecification.conTratamientoId(asLong(filtro.getValue())));
                    case "sexo" -> spec = spec.and(CitaSpecification.conSexo(filtro.getValue().toString()));
                }
            }
        }

        if (dto.getFechaDesde() != null && dto.getFechaHasta() != null) {
            spec = spec.and(CitaSpecification.conRangoFecha(dto.getFechaDesde(), dto.getFechaHasta()));
        }

        return citaRepository.findAll(spec);
    }

    private Long asLong(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }

    private LocalDate asLocalDate(Object value) {
        return value == null ? null : LocalDate.parse(value.toString());
    }
}
