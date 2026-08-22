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
import org.apache.poi.xssf.usermodel.XSSFColor;
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
        XSSFCellStyle decimalStyle = crearEstiloDecimal(workbook);
        XSSFCellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setBorderBottom(BorderStyle.THIN);
        normalStyle.setBorderTop(BorderStyle.THIN);
        normalStyle.setBorderLeft(BorderStyle.THIN);
        normalStyle.setBorderRight(BorderStyle.THIN);


        Sheet resumenSheet = workbook.createSheet("Resumen Reporte");
        insertarLogo(workbook, resumenSheet);
        int rowIndex = 10;
        rowIndex = agregarEncabezado(resumenSheet, rowIndex, usuarioSolicitante, dto, dateStyle);

        List<Map<String, Object>> resumen = reporteService.generarPivotReporte(dto);
        rowIndex = agregarResumen(resumenSheet, rowIndex, resumen, headerStyle, numberStyle, normalStyle);

        agregarDetalles(workbook, dto, headerStyle, dateStyle, numberStyle, normalStyle, decimalStyle);


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
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0x59, (byte) 0x59, (byte) 0x59}));
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
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle crearEstiloNumerico(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    private XSSFCellStyle crearEstiloDecimal(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
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
                                 CellStyle headerStyle, CellStyle dateStyle, CellStyle numberStyle, CellStyle textStyle, CellStyle decimalStyle) {
        Sheet sheet = workbook.createSheet("Detalles Citas");
        int rowIndex = 0;

        XSSFCellStyle atendidaTextStyle = workbook.createCellStyle();
        atendidaTextStyle.cloneStyleFrom(textStyle);
        atendidaTextStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        atendidaTextStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle atendidaDateStyle = workbook.createCellStyle();
        atendidaDateStyle.cloneStyleFrom(dateStyle);
        atendidaDateStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        atendidaDateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle atendidaNumberStyle = workbook.createCellStyle();
        atendidaNumberStyle.cloneStyleFrom(decimalStyle);
        atendidaNumberStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        atendidaNumberStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle canceladaTextStyle = workbook.createCellStyle();
        canceladaTextStyle.cloneStyleFrom(textStyle);
        canceladaTextStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        canceladaTextStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle canceladaDateStyle = workbook.createCellStyle();
        canceladaDateStyle.cloneStyleFrom(dateStyle);
        canceladaDateStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        canceladaDateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle canceladaNumberStyle = workbook.createCellStyle();
        canceladaNumberStyle.cloneStyleFrom(decimalStyle);
        canceladaNumberStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        canceladaNumberStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle pendienteTextStyle = workbook.createCellStyle();
        pendienteTextStyle.cloneStyleFrom(textStyle);
        pendienteTextStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        pendienteTextStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle pendienteDateStyle = workbook.createCellStyle();
        pendienteDateStyle.cloneStyleFrom(dateStyle);
        pendienteDateStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        pendienteDateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle pendienteNumberStyle = workbook.createCellStyle();
        pendienteNumberStyle.cloneStyleFrom(decimalStyle);
        pendienteNumberStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        pendienteNumberStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


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
            String[] headers = {"Fecha", "Paciente", "Tratamiento", "Dentista", "Monto", "Estado", "Observaciones"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            double totalMontoAtendida = 0;
            double totalMontoCancelada = 0;
            double totalMontoPendiente = 0;

            long totalAtendida = 0;
            long totalCancelada = 0;
            long totalPendiente = 0;
            for (Cita c : entry.getValue()) {
                Row row = sheet.createRow(rowIndex++);
                String estado = c.getEstado();

                CellStyle textStyleEstado;
                CellStyle dateStyleEstado;
                CellStyle numberStyleEstado;

                switch (estado) {
                    case "Atendida" -> {
                        textStyleEstado = atendidaTextStyle;
                        dateStyleEstado = atendidaDateStyle;
                        numberStyleEstado = atendidaNumberStyle;
                        totalAtendida++;
                        totalMontoAtendida += c.getMonto().doubleValue();
                    }
                    case "Cancelada" -> {
                        textStyleEstado = canceladaTextStyle;
                        dateStyleEstado = canceladaDateStyle;
                        numberStyleEstado = canceladaNumberStyle;
                        totalCancelada++;
                        totalMontoCancelada += c.getMonto().doubleValue();
                    }
                    case "Pendiente" -> {
                        textStyleEstado = pendienteTextStyle;
                        dateStyleEstado = pendienteDateStyle;
                        numberStyleEstado = pendienteNumberStyle;
                        totalPendiente++;
                        totalMontoPendiente += c.getMonto().doubleValue();
                    }
                    default -> {
                        textStyleEstado = textStyle;
                        dateStyleEstado = dateStyle;
                        numberStyleEstado = numberStyle;
                    }
                }

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(java.sql.Date.valueOf(c.getFecha()));
                cell0.setCellStyle(dateStyleEstado);

                row.createCell(1).setCellValue(c.getNombres() + " " + c.getApellidoPaterno());
                row.getCell(1).setCellStyle(textStyleEstado);

                row.createCell(2).setCellValue(c.getTratamiento().getNombre());
                row.getCell(2).setCellStyle(textStyleEstado);

                row.createCell(3).setCellValue(c.getDentista().getUsuario().getNombres());
                row.getCell(3).setCellStyle(textStyleEstado);

                Cell montoCell = row.createCell(4);
                montoCell.setCellValue(c.getMonto().doubleValue());
                montoCell.setCellStyle(numberStyleEstado);

                row.createCell(5).setCellValue(c.getEstado());
                row.getCell(5).setCellStyle(textStyleEstado);

                row.createCell(6).setCellValue(c.getObservaciones());
                row.getCell(6).setCellStyle(textStyleEstado);
            }


            Row totalRowAtendida = sheet.createRow(rowIndex++);
            totalRowAtendida.createCell(0).setCellValue("Total de citas atendidas");
            totalRowAtendida.getCell(0).setCellStyle(headerStyle);
            totalRowAtendida.createCell(1).setCellValue(totalAtendida);
            totalRowAtendida.getCell(1).setCellStyle(numberStyle);
            totalRowAtendida.createCell(3).setCellValue("Monto total");
            totalRowAtendida.getCell(3).setCellStyle(headerStyle);
            totalRowAtendida.createCell(4).setCellValue(totalMontoAtendida);
            totalRowAtendida.getCell(4).setCellStyle(decimalStyle);

            Row totalRowCancelada = sheet.createRow(rowIndex++);
            totalRowCancelada.createCell(0).setCellValue("Total de citas canceladas");
            totalRowCancelada.getCell(0).setCellStyle(headerStyle);
            totalRowCancelada.createCell(1).setCellValue(totalCancelada);
            totalRowCancelada.getCell(1).setCellStyle(numberStyle);
            totalRowCancelada.createCell(3).setCellValue("Monto total");
            totalRowCancelada.getCell(3).setCellStyle(headerStyle);
            totalRowCancelada.createCell(4).setCellValue(totalMontoCancelada);
            totalRowCancelada.getCell(4).setCellStyle(decimalStyle);

            Row totalRowPendiente = sheet.createRow(rowIndex++);
            totalRowPendiente.createCell(0).setCellValue("Total de citas pendientes");
            totalRowPendiente.getCell(0).setCellStyle(headerStyle);
            totalRowPendiente.createCell(1).setCellValue(totalPendiente);
            totalRowPendiente.getCell(1).setCellStyle(numberStyle);
            totalRowPendiente.createCell(3).setCellValue("Monto total");
            totalRowPendiente.getCell(3).setCellStyle(headerStyle);
            totalRowPendiente.createCell(4).setCellValue(totalMontoPendiente);
            totalRowPendiente.getCell(4).setCellStyle(decimalStyle);

            rowIndex++;
        }

        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String obtenerValorCampo(Cita cita, String campo) {
        return switch (campo) {
            case "dentista" -> cita.getDentista().getUsuario().getNombres();
            case "tratamiento" -> cita.getTratamiento().getNombre();
            case "tipoTratamiento" -> cita.getTratamiento().getTipoTratamiento().getNombre();
            case "tipoDocumento" -> cita.getUsuario().getTipoDocumento().getNombre();
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
