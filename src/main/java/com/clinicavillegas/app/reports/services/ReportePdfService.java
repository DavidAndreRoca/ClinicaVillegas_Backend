package com.clinicavillegas.app.reports.services;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.appointment.repositories.CitaRepository;
import com.clinicavillegas.app.reports.dto.ReporteRequestDTO;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.models.Usuario;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.persistence.criteria.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReportePdfService {
    private final ReporteService reporteService;
    private final CitaRepository citaRepository;

    public ReportePdfService(ReporteService reporteService, CitaRepository citaRepository) {
        this.reporteService = reporteService;
        this.citaRepository = citaRepository;
    }

    public byte[] generarReportePdf(ReporteRequestDTO dto, Usuario usuarioSolicitante) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        agregarEncabezado(document, usuarioSolicitante, dto);

        List<Map<String, Object>> resumen = reporteService.generarPivotReporte(dto);

        JFreeChart chart = generarGrafico(resumen, dto);
        agregarGrafico(document, chart);

        agregarTablaResumen(document, resumen);

        agregarDetalles(document, dto);

        document.close();
        return byteArrayOutputStream.toByteArray();
    }
    private void agregarEncabezado(Document document, Usuario usuarioSolicitante, ReporteRequestDTO dto) throws Exception {
        Image logo = Image.getInstance("src/main/resources/static/logo.jpg");
        logo.scaleToFit(80, 80);
        logo.setAlignment(Image.ALIGN_LEFT);
        document.add(logo);

        String titulo = "REPORTE DE CITAS";

        boolean tieneFilas = dto.getFilas() != null && !dto.getFilas().isEmpty();
        boolean tieneColumnas = dto.getColumnas() != null && !dto.getColumnas().isEmpty();

        if (tieneFilas && tieneColumnas) {
            titulo += " POR " + dto.getColumnas().getFirst().toUpperCase() + " VS " + dto.getFilas().getFirst().toUpperCase();
        } else if (tieneFilas) {
            titulo += " POR " + dto.getFilas().getFirst().toUpperCase();
        } else if (tieneColumnas) {
            titulo += " POR " + dto.getColumnas().getFirst().toUpperCase();
        }


        Paragraph title = new Paragraph(
                titulo,
                new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD)
        );
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("Fecha: " + LocalDate.now()));
        document.add(new Paragraph(usuarioSolicitante.getRol().name() + ": " + usuarioSolicitante.getNombres() + " " + usuarioSolicitante.getApellidoPaterno()));
        if (dto.getFechaDesde() != null){
            document.add(new Paragraph("Desde: " + dto.getFechaDesde()));
        }
        if (dto.getFechaHasta() != null){
            document.add(new Paragraph("Hasta: " + dto.getFechaHasta()));
        }
        document.add(Chunk.NEWLINE);
    }
    private JFreeChart generarGrafico(List<Map<String, Object>> resumen, ReporteRequestDTO dto) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String fila = dto.getFilas().getFirst();

        for (Map<String, Object> row : resumen) {
            String categoriaX = String.valueOf(row.get(fila));

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String clave = entry.getKey();
                Object valor = entry.getValue();

                if (clave.equals(fila)) continue;

                if (valor instanceof Number number) {
                    dataset.addValue(number, clave, categoriaX);
                }
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Resumen gráfico",
                fila,
                dto.getAgregacion(),
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);

        renderer.setDefaultItemLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER)
        );

        return chart;
    }

    private void agregarGrafico(Document document, JFreeChart chart) throws Exception {
        int width = 500;
        int height = 300;
        BufferedImage bufferedImage = chart.createBufferedImage(width, height);
        ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", chartOut);
        Image chartImage = Image.getInstance(chartOut.toByteArray());
        chartImage.scaleToFit(width, height);
        chartImage.setAlignment(Element.ALIGN_CENTER);
        document.add(chartImage);
        document.add(Chunk.NEWLINE);
    }
    private void agregarTablaResumen(Document document, List<Map<String, Object>> resumen) throws DocumentException {
        if (resumen.isEmpty()) return;

        List<String> columnas = new ArrayList<>(resumen.getFirst().keySet());
        PdfPTable table = new PdfPTable(columnas.size());
        table.setWidthPercentage(100);

        // Headers
        columnas.forEach(col -> table.addCell(new PdfPCell(new Phrase(col))));

        // Inicializar mapa de totales
        Map<String, Integer> totales = new HashMap<>();
        columnas.forEach(col -> totales.put(col, 0));

        // Filas de datos
        for (Map<String, Object> row : resumen) {
            for (String col : columnas) {
                Object valor = row.get(col);
                String valorStr = valor != null ? valor.toString() : "";
                table.addCell(new PdfPCell(new Phrase(valorStr)));

                if (valor instanceof Number number) {
                    totales.put(col, totales.get(col) + number.intValue());
                }
            }
        }

        // Fila de totales
        for (int i = 0; i < columnas.size(); i++) {
            String col = columnas.get(i);
            if (i == 0) {
                // Primera celda dice "TOTAL"
                table.addCell(new PdfPCell(new Phrase("TOTAL")));
            } else {
                Integer total = totales.get(col);
                if (total != null && total != 0.0) {
                    table.addCell(new PdfPCell(new Phrase(String.format("%d", total))));
                } else {
                    table.addCell(new PdfPCell(new Phrase("")));
                }
            }
        }

        Paragraph title = new Paragraph("Resumen del Reporte", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        title.setSpacingBefore(10);
        document.add(title);
        document.add(table);
        document.add(Chunk.NEWLINE);
    }


    private void agregarDetalles(Document document, ReporteRequestDTO dto) throws DocumentException {
        List<Cita> citas = obtenerCitasFiltradas(dto);

        Map<List<String>, List<Cita>> agrupado = citas.stream().collect(Collectors.groupingBy(c ->
                dto.getFilas().stream().map(f -> obtenerValorCampo(c, f)).toList()
        ));

        for (Map.Entry<List<String>, List<Cita>> entry : agrupado.entrySet()) {
            Paragraph subtitulo = new Paragraph("Grupo: " + String.join(" - ", entry.getKey()),
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
            subtitulo.setSpacingBefore(10);
            document.add(subtitulo);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            Stream.of("Fecha", "Paciente", "Tratamiento", "Dentista", "Monto", "Estado", "Observaciones")
                    .forEach(h -> {
                        Phrase phrase = new Phrase(h);
                        phrase.setFont(new Font(Font.FontFamily.HELVETICA, Font.DEFAULTSIZE, Font.BOLD));
                        PdfPCell headerCell = new PdfPCell(phrase);
                        headerCell.setBackgroundColor(new BaseColor(118, 219, 178));
                        table.addCell(headerCell);
                    });

            double totalMontoAtendida = 0;
            double totalMontoCancelada = 0;
            double totalMontoPendiente = 0;

            long totalAtendida = 0;
            long totalCancelada = 0;
            long totalPendiente = 0;

            for (Cita c : entry.getValue()) {
                table.addCell(c.getFecha().toString());
                table.addCell(c.getNombres() + " " + c.getApellidoPaterno());
                table.addCell(c.getTratamiento().getNombre());
                table.addCell(c.getDentista().getUsuario().getNombres());
                table.addCell(c.getMonto().toString());
                table.addCell(c.getEstado());
                table.addCell(c.getObservaciones() != null ? c.getObservaciones() : "");
                switch (c.getEstado()){
                    case "Atendida" -> {
                        totalAtendida++;
                        totalMontoAtendida += c.getMonto().doubleValue();
                    }
                    case "Cancelada" -> {
                        totalCancelada++;
                        totalMontoCancelada += c.getMonto().doubleValue();
                    }
                    case "Pendiente" -> {
                        totalPendiente++;
                        totalMontoPendiente += c.getMonto().doubleValue();
                    }
                }
            }

            // Fila de total
            PdfPCell headerCell1 = new PdfPCell(new Phrase("Total de citas atendidas"));
            headerCell1.setBackgroundColor(new BaseColor(118, 219, 178));
            table.addCell(headerCell1);
            table.addCell(String.valueOf(totalAtendida));
            table.addCell("");
            PdfPCell headerCell2 = new PdfPCell(new Phrase("Monto Total"));
            headerCell2.setBackgroundColor(new BaseColor(118, 219, 178));
            table.addCell(headerCell2);
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalMontoAtendida))));
            table.addCell("");
            table.addCell("");

            PdfPCell headerCell3 = new PdfPCell(new Phrase("Total de citas canceladas"));
            headerCell3.setBackgroundColor(new BaseColor(118, 219, 178));
            table.addCell(headerCell3);
            table.addCell(String.valueOf(totalCancelada));
            table.addCell("");
            PdfPCell headerCell4 = new PdfPCell(new Phrase("Monto Total"));
            headerCell4.setBackgroundColor(new BaseColor(118, 219, 178));
            table.addCell(headerCell4);
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalMontoCancelada))));
            table.addCell("");
            table.addCell("");

            PdfPCell headerCell5 = new PdfPCell(new Phrase("Total de citas pendientes"));
            headerCell5.setBackgroundColor(new BaseColor(118, 219, 178));
            table.addCell(headerCell5);
            table.addCell(String.valueOf(totalPendiente));
            table.addCell("");
            PdfPCell headerCell6 = new PdfPCell(new Phrase("Monto Total"));
            headerCell6.setBackgroundColor(new BaseColor(118, 219, 178));
            table.addCell(headerCell6);
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalMontoPendiente))));
            table.addCell("");
            table.addCell("");

            document.add(table);
        }
    }

    private String obtenerValorCampo(Cita cita, String campo) {
        return switch (campo) {
            case "dentista" -> cita.getDentista().getUsuario().getNombres();
            case "tratamiento" -> cita.getTratamiento().getNombre();
            case "tipoTratamiento" -> cita.getTratamiento().getTipoTratamiento().getNombre();
            case "tipoDocumento" -> cita.getTipoDocumento().getNombre();
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
}
