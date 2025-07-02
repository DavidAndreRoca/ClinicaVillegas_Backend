package com.clinicavillegas.app.reports.services;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.appointment.repositories.CitaRepository;
import com.clinicavillegas.app.reports.dto.ReporteRequestDTO;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.models.Usuario;
import jakarta.persistence.criteria.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
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
        insertarLogo(workbook, resumenSheet);
        int rowIndex = 10;
        rowIndex = agregarEncabezado(resumenSheet, rowIndex, usuarioSolicitante, dto, dateStyle);

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

    private int agregarEncabezado(Sheet sheet, int rowIndex, Usuario usuario, ReporteRequestDTO dto, CellStyle dateStyle) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("REPORTE DE CITAS");

        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Fecha:");
        row.createCell(1).setCellValue(LocalDate.now().toString());

        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(usuario.getRol().name() + ":");
        row.createCell(1).setCellValue(usuario.getNombres() + " " + usuario.getApellidoPaterno());

        if (dto.getFechaDesde() != null){
            row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue("Desde:");
            row.createCell(1).setCellValue(dto.getFechaDesde());
            row.getCell(1).setCellStyle(dateStyle);
        }
        if (dto.getFechaHasta() != null){
            row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue("Hasta:");
            row.createCell(1).setCellValue(dto.getFechaHasta());
            row.getCell(1).setCellStyle(dateStyle);
        }
        return rowIndex + 1;
    }

    private int agregarResumen(Sheet sheet, int rowIndex, List<Map<String, Object>> resumen,
                               CellStyle headerStyle, CellStyle numberStyle, CellStyle textStyle) {
        if (resumen.isEmpty()) return rowIndex;

        List<String> columnas = new ArrayList<>(resumen.getFirst().keySet());
        Row header = sheet.createRow(rowIndex++);
        int colIndex = 0;
        for (String col : columnas) {
            Cell cell = header.createCell(colIndex++);
            cell.setCellValue(col);
            cell.setCellStyle(headerStyle);
        }
        Map<String, Double> totales = new HashMap<>();
        columnas.forEach(col -> totales.put(col, 0.0));
        for (Map<String, Object> rowMap : resumen) {
            Row row = sheet.createRow(rowIndex++);
            colIndex = 0;
            for (String col : columnas) {
                Cell cell = row.createCell(colIndex++);
                Object val = rowMap.get(col);
                if (val instanceof Number number) {
                    cell.setCellValue(number.doubleValue());
                    cell.setCellStyle(numberStyle);
                    totales.put(col, totales.get(col) + number.doubleValue());
                } else {
                    cell.setCellValue(val != null ? val.toString() : "");
                    cell.setCellStyle(textStyle);
                }
            }
        }
        Row totalRow = sheet.createRow(rowIndex++);
        for (int i = 0; i < columnas.size(); i++) {
            String col = columnas.get(i);
            Cell cell = totalRow.createCell(i);
            if (i == 0) {
                cell.setCellValue("TOTAL");
                cell.setCellStyle(headerStyle);
            } else {
                Double total = totales.get(col);
                if (total != null && total != 0.0) {
                    cell.setCellValue(total);
                    cell.setCellStyle(numberStyle);
                } else {
                    cell.setCellValue("");
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
            String[] headers = {"Fecha", "Paciente", "Tratamiento", "Dentista", "Monto", "Observaciones"};
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

                row.createCell(5).setCellValue(c.getObservaciones());
                row.getCell(5).setCellStyle(textStyle);
            }

            rowIndex++;
        }

        for (int i = 0; i < 6; i++) {
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

    public List<Cita> obtenerCitasFiltradas(ReporteRequestDTO dto) {
        Specification<Cita> spec = (root, query, cb) -> {
            // Joins reutilizables
            Join<Cita, Tratamiento> tratamientoJoin = root.join("tratamiento", JoinType.LEFT);
            Join<Tratamiento, TipoTratamiento> tipoTratamientoJoin = tratamientoJoin.join("tipoTratamiento", JoinType.LEFT);
            Join<Cita, Dentista> dentistaJoin = root.join("dentista", JoinType.LEFT);
            Join<Dentista, Usuario> usuarioDentistaJoin = dentistaJoin.join("usuario", JoinType.LEFT);
            Join<Cita, Usuario> usuarioPacienteJoin = root.join("usuario", JoinType.LEFT);
            Join<Usuario, TipoDocumento> tipoDocumentoJoin = usuarioPacienteJoin.join("tipoDocumento", JoinType.LEFT);

            Map<String, Join<?, ?>> joins = Map.of(
                    "tratamiento", tratamientoJoin,
                    "tipoTratamiento", tipoTratamientoJoin,
                    "usuarioDentista", usuarioDentistaJoin,
                    "tipoDocumento", tipoDocumentoJoin
            );
            System.out.println("Joins inicializados: " + joins.keySet());
            List<Predicate> predicates = new ArrayList<>();

            if (dto.getFiltros() != null) {
                for (Map.Entry<String, Object> filtro : dto.getFiltros().entrySet()) {
                    System.out.println("Filtro recibido: " + filtro.getKey() + " = " + filtro.getValue());
                    Path<?> path = mapCampoToPath(filtro.getKey(), root, joins);
                    System.out.println("Path generado para filtro " + filtro.getKey() + ": " + path);
                    predicates.add(cb.equal(path, filtro.getValue()));
                }
            }

            if (dto.getFechaDesde() != null && dto.getFechaHasta() != null) {
                predicates.add(cb.between(root.get("fecha"), dto.getFechaDesde(), dto.getFechaHasta()));
            }
            System.out.println("Agregando filtro: " + predicates);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        System.out.println("Citas encontradas: " + citaRepository.findAll(spec).size());
        return citaRepository.findAll(spec);
    }


    private Path<?> mapCampoToPath(
            String campo,
            Root<Cita> root,
            Map<String, Join<?, ?>> joins
    ) {
        return switch (campo) {
            case "estado" -> root.get("estado");
            case "sexo" -> root.get("sexo");
            case "tratamiento" -> joins.get("tratamiento").get("nombre");
            case "tipoTratamiento" -> joins.get("tipoTratamiento").get("nombre");
            case "dentista" -> joins.get("usuarioDentista").get("nombres");
            case "tipoDocumento" -> joins.get("tipoDocumento").get("nombre");
            default -> root.get(campo);
        };
    }
    private void insertarLogo(XSSFWorkbook workbook, Sheet sheet) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("static/logo.jpg");
        if (inputStream == null) throw new FileNotFoundException("No se encontró el logo en static/logo.jpg");
        byte[] bytes = inputStream.readAllBytes();
        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
        inputStream.close();

        CreationHelper helper = workbook.getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(0);
        anchor.setRow1(0);
        anchor.setCol2(1);
        anchor.setRow2(9);

        drawing.createPicture(anchor, pictureIdx);
    }
}
