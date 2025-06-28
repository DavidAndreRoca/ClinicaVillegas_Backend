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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        return ChartFactory.createBarChart(
                "Resumen gráfico",
                fila,
                dto.getAgregacion(),
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
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

        Set<String> columnas = resumen.getFirst().keySet();
        PdfPTable table = new PdfPTable(columnas.size());
        table.setWidthPercentage(100);

        // Headers
        columnas.forEach(col -> table.addCell(new PdfPCell(new Phrase(col))));

        for (Map<String, Object> row : resumen) {
            for (String col : columnas) {
                table.addCell(new PdfPCell(new Phrase(row.get(col).toString())));
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
            Paragraph subtitulo = new Paragraph("Grupo: " + String.join(" - ", entry.getKey()), new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
            subtitulo.setSpacingBefore(10);
            document.add(subtitulo);

            PdfPTable table = new PdfPTable(6);
            Stream.of("Fecha", "Paciente", "Tratamiento", "Dentista", "Monto", "Observaciones")
                    .forEach(h -> table.addCell(new PdfPCell(new Phrase(h))));

            for (Cita c : entry.getValue()) {
                table.addCell(c.getFecha().toString());
                table.addCell(c.getNombres() + " " + c.getApellidoPaterno());
                table.addCell(c.getTratamiento().getNombre());
                table.addCell(c.getDentista().getUsuario().getNombres());
                table.addCell(c.getMonto().toString());
                table.addCell(c.getObservaciones() != null ? c.getObservaciones():"");
            }

            document.add(table);
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
}
