package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.service.PdfComprobanteService;
import com.concesionaria.app.service.dto.ComprobantePdfResult;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.awt.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PdfComprobanteServiceImpl implements PdfComprobanteService {

    private static final Logger LOG = LoggerFactory.getLogger(PdfComprobanteServiceImpl.class);

    private static final Locale LOCALE_AR = Locale.forLanguageTag("es-AR");
    private static final ZoneId ZONE_AR = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font FONT_SUBTITLE = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font FONT_LABEL = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font FONT_VALUE = new Font(Font.HELVETICA, 9);
    private static final Font FONT_FOOTER = new Font(Font.HELVETICA, 9, Font.ITALIC);
    private static final Font FONT_TOTAL = new Font(Font.HELVETICA, 10, Font.BOLD);

    private final ComprobanteRepository comprobanteRepository;
    private final String companyName;
    private final String companyAddress;
    private final String companyPhone;
    private final String companyEmail;
    private final String companyCuit;

    public PdfComprobanteServiceImpl(
        ComprobanteRepository comprobanteRepository,
        @Value("${app.comprobante.pdf.company.name:Concesionaria MB}") String companyName,
        @Value("${app.comprobante.pdf.company.address:Av. Siempre Viva 1234 - CABA}") String companyAddress,
        @Value("${app.comprobante.pdf.company.phone:+54 11 4000-1234}") String companyPhone,
        @Value("${app.comprobante.pdf.company.email:ventas@concesionariamb.com}") String companyEmail,
        @Value("${app.comprobante.pdf.company.cuit:30-12345678-9}") String companyCuit
    ) {
        this.comprobanteRepository = comprobanteRepository;
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.companyPhone = companyPhone;
        this.companyEmail = companyEmail;
        this.companyCuit = companyCuit;
    }

    @Override
    public Optional<ComprobantePdfResult> generarPdfComprobante(Long comprobanteId) {
        LOG.info("Generando PDF de comprobante id={}", comprobanteId);
        Optional<ComprobantePdfResult> result = comprobanteRepository.findOneForPdf(comprobanteId).map(this::buildPdf);
        if (result.isPresent()) {
            LOG.info("PDF generado correctamente para comprobante id={}", comprobanteId);
        } else {
            LOG.warn("No se pudo generar PDF: comprobante id={} inexistente", comprobanteId);
        }
        return result;
    }

    private ComprobantePdfResult buildPdf(Comprobante comprobante) {
        validateComprobanteForPdf(comprobante);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 42, 36);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document);
            document.add(Chunk.NEWLINE);
            addComprobanteSection(document, comprobante);
            document.add(Chunk.NEWLINE);
            addClienteSection(document, comprobante.getVenta() != null ? comprobante.getVenta().getCliente() : null);
            document.add(Chunk.NEWLINE);
            addVehiculoSection(document, comprobante.getVenta() != null ? comprobante.getVenta().getVehiculo() : null);
            document.add(Chunk.NEWLINE);
            addFinancieroSection(document, comprobante);
            document.add(Chunk.NEWLINE);
            addFooter(document);

            if (comprobante.getEstado() == EstadoComprobante.ANULADO) {
                addWatermark(writer, "ANULADO");
            }
        } catch (DocumentException e) {
            LOG.error("Error generando PDF para comprobante id={}", comprobante.getId(), e);
            throw new IllegalStateException("No se pudo generar el PDF del comprobante", e);
        } finally {
            document.close();
        }

        String numeroComprobante = comprobante.getNumeroComprobante() != null ? comprobante.getNumeroComprobante() : "comprobante";
        String fileName = numeroComprobante.replaceAll("[^A-Za-z0-9\\-_]", "_") + ".pdf";
        return new ComprobantePdfResult(baos.toByteArray(), fileName);
    }

    private void addHeader(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 1.2f, 3.8f });
        table.setWidthPercentage(100f);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setFixedHeight(56f);
        logoCell.setBorder(Rectangle.BOX);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.addElement(new Paragraph("LOGO", FONT_LABEL));
        table.addCell(logoCell);

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.addElement(new Paragraph(companyName, FONT_TITLE));
        infoCell.addElement(new Paragraph(companyAddress, FONT_VALUE));
        infoCell.addElement(new Paragraph("Tel: " + companyPhone + "  |  " + companyEmail, FONT_VALUE));
        infoCell.addElement(new Paragraph("CUIT: " + companyCuit, FONT_VALUE));
        table.addCell(infoCell);

        document.add(table);
    }

    private void addComprobanteSection(Document document, Comprobante comprobante) throws DocumentException {
        document.add(new Paragraph("Datos del Comprobante", FONT_SUBTITLE));
        PdfPTable table = new PdfPTable(new float[] { 1.2f, 2f, 1.2f, 2f });
        table.setWidthPercentage(100f);

        addField(table, "Tipo", comprobante.getTipoComprobante() != null ? comprobante.getTipoComprobante().getCodigo() : "-");
        addField(table, "Numero", comprobante.getNumeroComprobante());
        addField(table, "Fecha emision", formatDateTime(comprobante.getFechaEmision()));
        addField(table, "Estado", comprobante.getEstado() != null ? comprobante.getEstado().name() : "-");
        addField(table, "Usuario emisor", nullSafe(comprobante.getUsuarioEmision(), comprobante.getCreatedBy(), "-"));
        addField(table, "ID venta", comprobante.getVenta() != null && comprobante.getVenta().getId() != null ? "#" + comprobante.getVenta().getId() : "-");

        document.add(table);
    }

    private void addClienteSection(Document document, Cliente cliente) throws DocumentException {
        document.add(new Paragraph("Cliente", FONT_SUBTITLE));
        PdfPTable table = new PdfPTable(new float[] { 1.2f, 3.2f, 1.2f, 3.2f });
        table.setWidthPercentage(100f);

        addField(table, "Nombre", cliente == null ? "-" : (nullSafe(cliente.getNombre(), "") + " " + nullSafe(cliente.getApellido(), "")).trim());
        addField(table, "Documento", cliente != null ? cliente.getNroDocumento() : "-");
        addField(table, "Telefono", cliente != null ? cliente.getTelefono() : "-");
        addField(table, "Email", cliente != null ? cliente.getEmail() : "-");
        addField(table, "Direccion", cliente != null ? cliente.getDireccion() : "-");
        addField(
            table,
            "Condicion IVA",
            cliente != null && cliente.getCondicionIva() != null ? nullSafe(cliente.getCondicionIva().getDescripcion(), cliente.getCondicionIva().getCodigo(), "-") : "-"
        );

        document.add(table);
    }

    private void addVehiculoSection(Document document, Vehiculo vehiculo) throws DocumentException {
        document.add(new Paragraph("Vehiculo", FONT_SUBTITLE));
        PdfPTable table = new PdfPTable(new float[] { 1.2f, 3.2f, 1.2f, 3.2f });
        table.setWidthPercentage(100f);

        String marca = vehiculo != null && vehiculo.getVersion() != null && vehiculo.getVersion().getModelo() != null && vehiculo.getVersion().getModelo().getMarca() != null
            ? vehiculo.getVersion().getModelo().getMarca().getNombre()
            : "-";
        String modelo = vehiculo != null && vehiculo.getVersion() != null && vehiculo.getVersion().getModelo() != null
            ? vehiculo.getVersion().getModelo().getNombre()
            : "-";
        String version = vehiculo != null && vehiculo.getVersion() != null ? vehiculo.getVersion().getNombre() : "-";
        String anio = vehiculo != null && vehiculo.getFechaFabricacion() != null ? String.valueOf(vehiculo.getFechaFabricacion().getYear()) : "-";

        addField(table, "Marca", marca);
        addField(table, "Modelo", modelo);
        addField(table, "Version", version);
        addField(table, "Patente", vehiculo != null ? vehiculo.getPatente() : "-");
        addField(table, "VIN", vehiculo != null ? vehiculo.getVinChasis() : "-");
        addField(table, "Anio", anio);
        addField(table, "Color", vehiculo != null ? vehiculo.getColor() : "-");
        addField(table, "Estado unidad", vehiculo != null && vehiculo.getEstado() != null ? vehiculo.getEstado().name() : "-");

        document.add(table);
    }

    private void addFinancieroSection(Document document, Comprobante comprobante) throws DocumentException {
        document.add(new Paragraph("Detalle Financiero", FONT_SUBTITLE));
        PdfPTable table = new PdfPTable(new float[] { 2f, 2f });
        table.setWidthPercentage(100f);

        Venta venta = comprobante.getVenta();
        String moneda = comprobante.getMoneda() != null ? nullSafe(comprobante.getMoneda().getSimbolo(), "") + " " + nullSafe(comprobante.getMoneda().getCodigo(), "") : "";
        BigDecimal pagado = venta != null ? venta.getTotalPagado() : null;
        BigDecimal saldo = venta != null ? venta.getSaldo() : null;

        addMoneyField(table, "Importe neto", comprobante.getImporteNeto(), moneda, false);
        addMoneyField(table, "IVA", comprobante.getImpuesto(), moneda, false);
        addMoneyField(table, "TOTAL", comprobante.getTotal(), moneda, true);
        addMoneyField(table, "Pagado", pagado, moneda, false);
        addMoneyField(table, "Saldo pendiente", saldo, moneda, false);

        document.add(table);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer1 = new Paragraph("Documento generado automaticamente", FONT_FOOTER);
        footer1.setAlignment(Element.ALIGN_CENTER);
        document.add(footer1);

        Paragraph footer2 = new Paragraph("Gracias por elegirnos", FONT_FOOTER);
        footer2.setAlignment(Element.ALIGN_CENTER);
        document.add(footer2);
    }

    private void addWatermark(PdfWriter writer, String text) {
        try {
            PdfContentByte canvas = writer.getDirectContentUnder();
            PdfGState state = new PdfGState();
            state.setFillOpacity(0.18f);
            canvas.saveState();
            canvas.setGState(state);
            canvas.beginText();
            canvas.setColorFill(new Color(180, 0, 0));
            canvas.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED), 86);
            canvas.showTextAligned(
                Element.ALIGN_CENTER,
                text,
                PageSize.A4.getWidth() / 2f,
                PageSize.A4.getHeight() / 2f,
                45f
            );
            canvas.endText();
            canvas.restoreState();
        } catch (DocumentException | IOException e) {
            LOG.warn("No se pudo agregar watermark '{}' al comprobante PDF", text, e);
        }
    }

    private void addField(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_LABEL));
        labelCell.setPadding(6f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(nullSafe(value, "-"), FONT_VALUE));
        valueCell.setPadding(6f);
        table.addCell(valueCell);
    }

    private void addMoneyField(PdfPTable table, String label, BigDecimal amount, String currency, boolean highlight) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, highlight ? FONT_TOTAL : FONT_LABEL));
        labelCell.setPadding(6f);
        table.addCell(labelCell);

        Font valueFont = highlight ? FONT_TOTAL : FONT_VALUE;
        PdfPCell valueCell = new PdfPCell(new Phrase((currency.isBlank() ? "" : currency + " ") + formatAmount(amount), valueFont));
        valueCell.setPadding(6f);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String formatAmount(BigDecimal amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(LOCALE_AR);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(amount == null ? BigDecimal.ZERO : amount);
    }

    private String formatDateTime(java.time.Instant instant) {
        if (instant == null) {
            return "-";
        }
        return DATE_TIME_FORMATTER.format(instant.atZone(ZONE_AR));
    }

    private String nullSafe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String nullSafe(String primary, String fallback, String defaultValue) {
        String primaryValue = nullSafe(primary, "");
        if (!primaryValue.isBlank()) {
            return primaryValue;
        }
        String fallbackValue = nullSafe(fallback, "");
        if (!fallbackValue.isBlank()) {
            return fallbackValue;
        }
        return defaultValue;
    }

    private void validateComprobanteForPdf(Comprobante comprobante) {
        if (comprobante.getVenta() == null) {
            throw new IllegalStateException("El comprobante no tiene venta asociada");
        }
        if (comprobante.getNumeroComprobante() == null || comprobante.getNumeroComprobante().isBlank()) {
            throw new IllegalStateException("El comprobante no tiene numero de comprobante");
        }
        if (comprobante.getFechaEmision() == null) {
            throw new IllegalStateException("El comprobante no tiene fecha de emision");
        }
        if (comprobante.getTotal() == null) {
            throw new IllegalStateException("El comprobante no tiene total");
        }
        if (comprobante.getMoneda() == null) {
            throw new IllegalStateException("El comprobante no tiene moneda asociada");
        }
    }
}

