package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.ComprobantePlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.repository.ComprobantePlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.ComprobantePlanAhorroService;
import com.concesionaria.app.service.dto.ComprobantePdfResult;
import com.concesionaria.app.service.dto.ComprobantePlanAhorroDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComprobantePlanAhorroServiceImpl implements ComprobantePlanAhorroService {

    private static final Logger LOG = LoggerFactory.getLogger(ComprobantePlanAhorroServiceImpl.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final Locale LOCALE_AR = Locale.forLanguageTag("es-AR");
    private static final ZoneId ZONE_AR = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZONE_AR);

    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font FONT_SUBTITLE = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font FONT_LABEL = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font FONT_VALUE = new Font(Font.HELVETICA, 9);
    private static final Font FONT_FOOTER = new Font(Font.HELVETICA, 9, Font.ITALIC);
    private static final Font FONT_TOTAL = new Font(Font.HELVETICA, 10, Font.BOLD);

    private final ComprobantePlanAhorroRepository repository;
    private final CuotaPlanAhorroRepository cuotaRepository;
    private final String companyName;
    private final String companyAddress;
    private final String companyPhone;
    private final String companyEmail;
    private final String companyCuit;
    private final String companyLogoPath;

    public ComprobantePlanAhorroServiceImpl(
        ComprobantePlanAhorroRepository repository,
        CuotaPlanAhorroRepository cuotaRepository,
        @Value("${app.comprobante.pdf.company.name:Concesionaria MB}") String companyName,
        @Value("${app.comprobante.pdf.company.address:Av. Siempre Viva 1234 - CABA}") String companyAddress,
        @Value("${app.comprobante.pdf.company.phone:+54 11 4000-1234}") String companyPhone,
        @Value("${app.comprobante.pdf.company.email:ventas@concesionariamb.com}") String companyEmail,
        @Value("${app.comprobante.pdf.company.cuit:30-12345678-9}") String companyCuit,
        @Value("${app.comprobante.pdf.company.logo-path:static/content/images/branding/logo.png}") String companyLogoPath
    ) {
        this.repository = repository;
        this.cuotaRepository = cuotaRepository;
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.companyPhone = companyPhone;
        this.companyEmail = companyEmail;
        this.companyCuit = companyCuit;
        this.companyLogoPath = companyLogoPath;
    }

    @Override
    public ComprobantePlanAhorroDTO emitirParaCuota(CuotaPlanAhorro cuota, Pago pago) {
        if (cuota == null || cuota.getId() == null) {
            throw new BadRequestException("Cuota invalida para emitir comprobante");
        }
        if (pago == null || pago.getId() == null) {
            throw new BadRequestException("Pago invalido para emitir comprobante");
        }
        if (repository.existsByCuotaPlanAhorroIdAndEstado(cuota.getId(), EstadoComprobante.EMITIDO)) {
            throw new BadRequestException("La cuota ya tiene un comprobante activo");
        }
        Instant ahora = Instant.now();
        String usuario = currentUserLogin();
        long correlativo = repository.nextCorrelativoBase() + 1;
        String numero = "CPA-" + String.format("%06d", correlativo);

        ComprobantePlanAhorro entity = new ComprobantePlanAhorro();
        entity.setContratoPlanAhorro(cuota.getContrato());
        entity.setCuotaPlanAhorro(cuota);
        entity.setPago(pago);
        entity.setNumeroComprobante(numero);
        entity.setFechaEmision(ahora);
        entity.setImporte(cuota.getImporte());
        entity.setMoneda(cuota.getContrato().getPlan().getMoneda());
        entity.setEstado(EstadoComprobante.EMITIDO);
        entity.setUsuarioEmision(usuario);
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ComprobantePlanAhorroDTO> findOne(Long id) {
        Optional<ComprobantePlanAhorro> entity = isAdmin() ? repository.findById(id) : repository.findForUser(id, currentUserLogin());
        return entity.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComprobantePlanAhorroDTO> findByCuota(Long cuotaId) {
        validarAccesoCuota(cuotaId);
        return repository.findAllByCuotaPlanAhorroIdOrderByIdDesc(cuotaId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ComprobantePdfResult> generarPdf(Long id) {
        Optional<ComprobantePlanAhorro> opt = isAdmin() ? repository.findById(id) : repository.findForUser(id, currentUserLogin());
        return opt.map(this::buildPdf);
    }

    @Override
    public ComprobantePlanAhorroDTO anular(Long id, String motivo) {
        ComprobantePlanAhorro entity = isAdmin()
            ? repository.findById(id).orElseThrow(() -> new BadRequestException("Comprobante inexistente"))
            : repository.findForUser(id, currentUserLogin()).orElseThrow(() -> new AccessDeniedException("Sin permisos"));
        if (entity.getEstado() == EstadoComprobante.ANULADO) {
            throw new BadRequestException("El comprobante ya esta anulado");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Debe informar motivo de anulacion");
        }
        entity.setEstado(EstadoComprobante.ANULADO);
        entity.setMotivoAnulacion(motivo.trim());
        entity.setFechaAnulacion(Instant.now());
        entity.setUsuarioAnulacion(currentUserLogin());
        return toDto(repository.save(entity));
    }

    @Override
    public void anularPorPago(Long pagoId, String motivo, String usuario, Instant fecha) {
        if (pagoId == null) {
            return;
        }
        repository
            .findFirstByPagoIdAndEstadoOrderByIdDesc(pagoId, EstadoComprobante.EMITIDO)
            .ifPresent(entity -> {
                entity.setEstado(EstadoComprobante.ANULADO);
                entity.setMotivoAnulacion("Anulado por anulacion de pago: " + motivo);
                entity.setFechaAnulacion(fecha);
                entity.setUsuarioAnulacion(usuario);
                repository.save(entity);
            });
    }

    @Override
    public void delete(Long id) {
        throw new BadRequestException("No se permite borrar comprobantes de plan. Debe anularse.");
    }

    private ComprobantePdfResult buildPdf(ComprobantePlanAhorro cpa) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 42, 36);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document);
            document.add(Chunk.NEWLINE);
            addComprobanteSection(document, cpa);
            document.add(Chunk.NEWLINE);
            addContratoSection(document, cpa);
            document.add(Chunk.NEWLINE);
            addFinancieroSection(document, cpa);
            document.add(Chunk.NEWLINE);
            addFooter(document);
            if (cpa.getEstado() == EstadoComprobante.ANULADO) {
                addWatermark(writer, "ANULADO");
            }
        } catch (DocumentException e) {
            LOG.error("Error generando PDF de comprobante plan id={}", cpa.getId(), e);
            throw new IllegalStateException("No se pudo generar PDF de comprobante de plan", e);
        } finally {
            document.close();
        }
        return new ComprobantePdfResult(baos.toByteArray(), cpa.getNumeroComprobante() + ".pdf");
    }

    private void addHeader(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 1.2f, 3.8f });
        table.setWidthPercentage(100f);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setFixedHeight(56f);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        Image logo = loadLogoImage();
        if (logo != null) {
            logo.scaleToFit(120f, 46f);
            logo.setAlignment(Element.ALIGN_CENTER);
            logoCell.addElement(logo);
        } else {
            logoCell.addElement(new Paragraph(companyName, FONT_LABEL));
        }
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

    private Image loadLogoImage() {
        Image fromFs = loadLogoFromFileSystem();
        if (fromFs != null) {
            return fromFs;
        }
        try {
            ClassPathResource resource = new ClassPathResource(companyLogoPath);
            if (!resource.exists()) {
                return null;
            }
            try (InputStream inputStream = resource.getInputStream()) {
                return Image.getInstance(inputStream.readAllBytes());
            }
        } catch (Exception e) {
            LOG.warn("No se pudo cargar logo para PDF de plan desde '{}'", companyLogoPath, e);
            return null;
        }
    }

    private Image loadLogoFromFileSystem() {
        Path configuredPath = Paths.get(companyLogoPath);
        Path[] candidates = new Path[] {
            configuredPath,
            Paths.get("src/main/webapp/content/images/branding/logo.png"),
            Paths.get("src/main/webapp/content/images/branding/Logo.png"),
            Paths.get("content/images/branding/logo.png"),
        };
        for (Path candidate : candidates) {
            try {
                if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                    try (InputStream inputStream = new FileInputStream(candidate.toFile())) {
                        return Image.getInstance(inputStream.readAllBytes());
                    }
                }
            } catch (Exception e) {
                LOG.warn("No se pudo cargar logo PDF desde archivo '{}'", candidate.toAbsolutePath(), e);
            }
        }
        return null;
    }

    private void addComprobanteSection(Document document, ComprobantePlanAhorro cpa) throws DocumentException {
        document.add(new Paragraph("Datos del Comprobante", FONT_SUBTITLE));
        PdfPTable table = new PdfPTable(new float[] { 1.2f, 2f, 1.2f, 2f });
        table.setWidthPercentage(100f);
        addField(table, "Tipo", "RECIBO PLAN");
        addField(table, "Numero", cpa.getNumeroComprobante());
        addField(table, "Fecha emision", DATE_FORMATTER.format(cpa.getFechaEmision()));
        addField(table, "Estado", cpa.getEstado() != null ? cpa.getEstado().name() : "-");
        addField(table, "Usuario emisor", cpa.getUsuarioEmision());
        addField(table, "ID pago", cpa.getPago() != null && cpa.getPago().getId() != null ? "#" + cpa.getPago().getId() : "-");
        document.add(table);
    }

    private void addContratoSection(Document document, ComprobantePlanAhorro cpa) throws DocumentException {
        document.add(new Paragraph("Contrato y Cliente", FONT_SUBTITLE));
        PdfPTable table = new PdfPTable(new float[] { 1.2f, 3.2f, 1.2f, 3.2f });
        table.setWidthPercentage(100f);

        String clienteNombre = "-";
        if (cpa.getContratoPlanAhorro() != null && cpa.getContratoPlanAhorro().getCliente() != null) {
            clienteNombre =
                nullSafe(cpa.getContratoPlanAhorro().getCliente().getApellido(), "") +
                " " +
                nullSafe(cpa.getContratoPlanAhorro().getCliente().getNombre(), "");
            clienteNombre = clienteNombre.trim();
        }
        addField(table, "Cliente", clienteNombre);
        addField(table, "Contrato", cpa.getContratoPlanAhorro() != null ? cpa.getContratoPlanAhorro().getNumeroContrato() : "-");
        addField(
            table,
            "Plan",
            cpa.getContratoPlanAhorro() != null && cpa.getContratoPlanAhorro().getPlan() != null ? cpa.getContratoPlanAhorro().getPlan().getNombre() : "-"
        );
        addField(
            table,
            "Cuota",
            cpa.getCuotaPlanAhorro() != null && cpa.getCuotaPlanAhorro().getNumeroCuota() != null ? "#" + cpa.getCuotaPlanAhorro().getNumeroCuota() : "-"
        );
        addField(
            table,
            "Vencimiento cuota",
            cpa.getCuotaPlanAhorro() != null && cpa.getCuotaPlanAhorro().getFechaVencimiento() != null
                ? DATE_FORMATTER.format(cpa.getCuotaPlanAhorro().getFechaVencimiento())
                : "-"
        );
        addField(
            table,
            "Fecha pago",
            cpa.getCuotaPlanAhorro() != null && cpa.getCuotaPlanAhorro().getFechaPago() != null
                ? DATE_FORMATTER.format(cpa.getCuotaPlanAhorro().getFechaPago())
                : "-"
        );
        document.add(table);
    }

    private void addFinancieroSection(Document document, ComprobantePlanAhorro cpa) throws DocumentException {
        document.add(new Paragraph("Resumen Financiero", FONT_SUBTITLE));
        PdfPTable table = new PdfPTable(new float[] { 2f, 2f });
        table.setWidthPercentage(100f);
        String moneda = cpa.getMoneda() != null ? nullSafe(cpa.getMoneda().getSimbolo(), "") + " " + nullSafe(cpa.getMoneda().getCodigo(), "") : "";
        addMoneyField(table, "Importe cuota", cpa.getImporte(), moneda, true);
        document.add(table);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph(
            "Comprobante generado automaticamente por AutoGestion Concesionaria. Documento valido para control interno.",
            FONT_FOOTER
        );
        footer.setSpacingBefore(8f);
        document.add(footer);
    }

    private void addWatermark(PdfWriter writer, String text) {
        try {
            PdfContentByte canvas = writer.getDirectContentUnder();
            PdfGState state = new PdfGState();
            state.setFillOpacity(0.12f);
            canvas.saveState();
            canvas.setGState(state);
            canvas.beginText();
            canvas.setColorFill(new Color(200, 0, 0));
            canvas.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, false), 72f);
            canvas.showTextAligned(Element.ALIGN_CENTER, text, 297f, 421f, 45f);
            canvas.endText();
            canvas.restoreState();
        } catch (Exception e) {
            LOG.warn("No se pudo agregar watermark '{}' al PDF de plan", text, e);
        }
    }

    private void addField(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_LABEL));
        labelCell.setBackgroundColor(new Color(245, 245, 245));
        labelCell.setPadding(6f);
        table.addCell(labelCell);
        PdfPCell valueCell = new PdfPCell(new Phrase(nullSafe(value, "-"), FONT_VALUE));
        valueCell.setPadding(6f);
        table.addCell(valueCell);
    }

    private void addMoneyField(PdfPTable table, String label, BigDecimal amount, String currency, boolean highlight) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, highlight ? FONT_TOTAL : FONT_LABEL));
        labelCell.setBackgroundColor(new Color(245, 245, 245));
        labelCell.setPadding(6f);
        table.addCell(labelCell);
        Font valueFont = highlight ? FONT_TOTAL : FONT_VALUE;
        String value = (currency == null || currency.isBlank() ? "" : currency + " ") + formatAmount(amount);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(6f);
        table.addCell(valueCell);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0,00";
        }
        return NumberFormat.getNumberInstance(LOCALE_AR).format(amount);
    }

    private String nullSafe(String... values) {
        if (values == null) {
            return "-";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "-";
    }

    private ComprobantePlanAhorroDTO toDto(ComprobantePlanAhorro entity) {
        ComprobantePlanAhorroDTO dto = new ComprobantePlanAhorroDTO();
        dto.setId(entity.getId());
        dto.setContratoPlanAhorroId(entity.getContratoPlanAhorro() != null ? entity.getContratoPlanAhorro().getId() : null);
        dto.setCuotaPlanAhorroId(entity.getCuotaPlanAhorro() != null ? entity.getCuotaPlanAhorro().getId() : null);
        dto.setPagoId(entity.getPago() != null ? entity.getPago().getId() : null);
        dto.setNumeroComprobante(entity.getNumeroComprobante());
        dto.setFechaEmision(entity.getFechaEmision());
        dto.setImporte(entity.getImporte());
        if (entity.getMoneda() != null) {
            MonedaDTO monedaDTO = new MonedaDTO();
            monedaDTO.setId(entity.getMoneda().getId());
            monedaDTO.setCodigo(entity.getMoneda().getCodigo());
            monedaDTO.setDescripcion(entity.getMoneda().getDescripcion());
            monedaDTO.setSimbolo(entity.getMoneda().getSimbolo());
            dto.setMoneda(monedaDTO);
        }
        dto.setEstado(entity.getEstado());
        dto.setUsuarioEmision(entity.getUsuarioEmision());
        dto.setMotivoAnulacion(entity.getMotivoAnulacion());
        dto.setFechaAnulacion(entity.getFechaAnulacion());
        dto.setUsuarioAnulacion(entity.getUsuarioAnulacion());
        return dto;
    }

    private boolean isAdmin() {
        return SecurityUtils.hasCurrentUserAnyOfAuthorities(ROLE_ADMIN);
    }

    private void validarAccesoCuota(Long cuotaId) {
        if (isAdmin()) {
            return;
        }
        boolean allowed = cuotaRepository.findOneByIdForUser(cuotaId, currentUserLogin()).isPresent();
        if (!allowed) {
            throw new AccessDeniedException("No tienes permisos para esta cuota");
        }
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }
}
