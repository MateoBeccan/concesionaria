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
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComprobantePlanAhorroServiceImpl implements ComprobantePlanAhorroService {

    private static final Logger LOG = LoggerFactory.getLogger(ComprobantePlanAhorroServiceImpl.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final Locale LOCALE_AR = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(
        ZoneId.of("America/Argentina/Buenos_Aires")
    );

    private final ComprobantePlanAhorroRepository repository;
    private final CuotaPlanAhorroRepository cuotaRepository;

    public ComprobantePlanAhorroServiceImpl(ComprobantePlanAhorroRepository repository, CuotaPlanAhorroRepository cuotaRepository) {
        this.repository = repository;
        this.cuotaRepository = cuotaRepository;
    }

    @Override
    public ComprobantePlanAhorroDTO emitirParaCuota(CuotaPlanAhorro cuota, Pago pago) {
        if (cuota == null || cuota.getId() == null) {
            throw new BadRequestException("Cuota inválida para emitir comprobante");
        }
        if (pago == null || pago.getId() == null) {
            throw new BadRequestException("Pago inválido para emitir comprobante");
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
            throw new BadRequestException("El comprobante ya está anulado");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Debe informar motivo de anulación");
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
                entity.setMotivoAnulacion("Anulado por anulación de pago: " + motivo);
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
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph("Concesionaria MB - Recibo de Plan de Ahorro", new Font(Font.HELVETICA, 15, Font.BOLD)));
            document.add(new Paragraph("Número: " + cpa.getNumeroComprobante(), new Font(Font.HELVETICA, 11, Font.BOLD)));
            document.add(new Paragraph("Estado: " + cpa.getEstado().name()));
            document.add(new Paragraph("Fecha emisión: " + DATE_FORMATTER.format(cpa.getFechaEmision())));
            document.add(new Paragraph(" "));
            String cliente = cpa.getContratoPlanAhorro().getCliente().getApellido() + " " + cpa.getContratoPlanAhorro().getCliente().getNombre();
            document.add(new Paragraph("Cliente: " + cliente));
            document.add(new Paragraph("Contrato: " + cpa.getContratoPlanAhorro().getNumeroContrato()));
            document.add(new Paragraph("Plan: " + cpa.getContratoPlanAhorro().getPlan().getNombre()));
            document.add(new Paragraph("Cuota número: " + cpa.getCuotaPlanAhorro().getNumeroCuota()));
            document.add(new Paragraph("Importe: " + cpa.getImporte() + " " + cpa.getMoneda().getCodigo()));
            document.add(new Paragraph("Usuario emisor: " + cpa.getUsuarioEmision()));
        } catch (DocumentException e) {
            LOG.error("Error generando PDF de comprobante plan id={}", cpa.getId(), e);
            throw new IllegalStateException("No se pudo generar PDF de comprobante de plan", e);
        } finally {
            document.close();
        }
        return new ComprobantePdfResult(baos.toByteArray(), cpa.getNumeroComprobante() + ".pdf");
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
