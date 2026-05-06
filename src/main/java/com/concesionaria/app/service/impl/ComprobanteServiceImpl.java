package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.repository.TipoComprobanteRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.ComprobanteService;
import com.concesionaria.app.service.dto.ComprobanteDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ComprobanteMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Comprobante}.
 */
@Service
@Transactional
public class ComprobanteServiceImpl implements ComprobanteService {

    private static final Logger LOG = LoggerFactory.getLogger(ComprobanteServiceImpl.class);

    private final ComprobanteRepository comprobanteRepository;

    private final ComprobanteMapper comprobanteMapper;

    private final VentaRepository ventaRepository;

    private final TipoComprobanteRepository tipoComprobanteRepository;

    public ComprobanteServiceImpl(
        ComprobanteRepository comprobanteRepository,
        ComprobanteMapper comprobanteMapper,
        VentaRepository ventaRepository,
        TipoComprobanteRepository tipoComprobanteRepository
    ) {
        this.comprobanteRepository = comprobanteRepository;
        this.comprobanteMapper = comprobanteMapper;
        this.ventaRepository = ventaRepository;
        this.tipoComprobanteRepository = tipoComprobanteRepository;
    }

    @Override
    public ComprobanteDTO save(ComprobanteDTO comprobanteDTO) {
        Long ventaId = comprobanteDTO.getVenta() != null ? comprobanteDTO.getVenta().getId() : null;
        Long tipoComprobanteId = comprobanteDTO.getTipoComprobante() != null ? comprobanteDTO.getTipoComprobante().getId() : null;
        if (ventaId == null || tipoComprobanteId == null) {
            throw new BadRequestException("Debe informar venta y tipo de comprobante para emitir");
        }
        return emitirComprobante(ventaId, tipoComprobanteId);
    }

    @Override
    public ComprobanteDTO update(ComprobanteDTO comprobanteDTO) {
        throw new BadRequestException("No se permite editar comprobantes emitidos. Solo se permite emitir o anular.");
    }

    @Override
    public Optional<ComprobanteDTO> partialUpdate(ComprobanteDTO comprobanteDTO) {
        throw new BadRequestException("No se permite editar comprobantes emitidos. Solo se permite emitir o anular.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComprobanteDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Comprobantes");
        return comprobanteRepository.findAll(pageable).map(comprobanteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ComprobanteDTO> findOne(Long id) {
        LOG.debug("Request to get Comprobante : {}", id);
        return comprobanteRepository.findById(id).map(comprobanteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComprobanteDTO> findByVentaId(Long ventaId) {
        LOG.debug("Request to get Comprobantes by Venta : {}", ventaId);
        return comprobanteRepository.findAllByVentaIdWithRelaciones(ventaId).stream().map(comprobanteMapper::toDto).toList();
    }

    @Override
    public ComprobanteDTO emitirComprobante(Long ventaId, Long tipoComprobanteId) {
        LOG.debug("Request to emitir comprobante para venta {} y tipo {}", ventaId, tipoComprobanteId);

        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta informada no existe"));
        validarVentaCompletada(venta);
        validarTotalesVentaParaComprobante(venta);
        validarPoliticaUnicoComprobanteActivo(ventaId, tipoComprobanteId);

        TipoComprobante tipoComprobante = tipoComprobanteRepository
            .findById(tipoComprobanteId)
            .orElseThrow(() -> new BadRequestException("El tipo de comprobante no existe"));

        if (venta.getMoneda() == null) {
            throw new BadRequestException("La venta no tiene moneda asociada. No se puede emitir comprobante");
        }

        Long siguienteCorrelativo = (comprobanteRepository.findMaxNumeroCorrelativoByTipoComprobanteId(tipoComprobanteId) + 1);
        String numeroComprobante = generarNumeroComprobante(tipoComprobante, siguienteCorrelativo);

        Instant now = Instant.now();
        String currentUser = currentUserLogin();

        Comprobante comprobante = new Comprobante();
        comprobante.setNumeroComprobante(numeroComprobante);
        comprobante.setFechaEmision(now);
        comprobante.setImporteNeto(venta.getImporteNeto());
        comprobante.setImpuesto(venta.getImpuesto());
        comprobante.setTotal(venta.getTotal());
        comprobante.setMoneda(venta.getMoneda());
        comprobante.setVenta(venta);
        comprobante.setTipoComprobante(tipoComprobante);
        comprobante.setEstado(EstadoComprobante.EMITIDO);
        comprobante.setCreatedDate(now);
        comprobante.setCreatedBy(currentUser);
        comprobante.setUsuarioEmision(currentUser);
        comprobante.setLastModifiedDate(now);
        comprobante.setLastModifiedBy(currentUser);

        Comprobante saved = comprobanteRepository.save(comprobante);
        return comprobanteMapper.toDto(saved);
    }

    @Override
    public ComprobanteDTO anularComprobante(Long comprobanteId, String motivo) {
        LOG.debug("Request to anular comprobante : {}", comprobanteId);
        Comprobante comprobante = comprobanteRepository
            .findById(comprobanteId)
            .orElseThrow(() -> new BadRequestException("El comprobante no existe"));
        if (comprobante.getEstado() == EstadoComprobante.ANULADO) {
            throw new BadRequestException("El comprobante ya se encuentra anulado");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Debe informar un motivo para anular el comprobante");
        }

        comprobante.setEstado(EstadoComprobante.ANULADO);
        Instant fechaAnulacion = Instant.now();
        String usuarioAnulacion = currentUserLogin();
        comprobante.setMotivoAnulacion(motivo.trim());
        comprobante.setFechaAnulacion(fechaAnulacion);
        comprobante.setUsuarioAnulacion(usuarioAnulacion);
        comprobante.setLastModifiedDate(fechaAnulacion);
        comprobante.setLastModifiedBy(usuarioAnulacion);
        Comprobante saved = comprobanteRepository.save(comprobante);
        return comprobanteMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        throw new BadRequestException("No se permite borrar comprobantes. Debe anularse para mantener trazabilidad.");
    }

    private void validarVentaCompletada(Venta venta) {
        if (venta.getEstado() == EstadoVenta.CANCELADA || venta.getEstado() == EstadoVenta.RESERVADA || venta.getEstado() == EstadoVenta.PENDIENTE) {
            throw new BadRequestException("Solo se puede emitir comprobante para ventas completadas");
        }
        if (venta.getEstado() != EstadoVenta.PAGADA && venta.getEstado() != EstadoVenta.FINALIZADA) {
            throw new BadRequestException("Solo se puede emitir comprobante para ventas completadas");
        }
    }

    private void validarPoliticaUnicoComprobanteActivo(Long ventaId, Long tipoComprobanteId) {
        if (comprobanteRepository.existsByVentaIdAndTipoComprobanteIdAndEstado(ventaId, tipoComprobanteId, EstadoComprobante.EMITIDO)) {
            throw new BadRequestException("La venta ya posee un comprobante activo de ese tipo");
        }
    }

    private String generarNumeroComprobante(TipoComprobante tipoComprobante, Long correlativo) {
        String codigo = tipoComprobante.getCodigo();
        if (codigo == null || codigo.isBlank()) {
            throw new BadRequestException("El tipo de comprobante debe tener un codigo para numerar");
        }
        return codigo.trim().toUpperCase() + "-" + String.format("%06d", correlativo);
    }

    private void validarTotalesVentaParaComprobante(Venta venta) {
        if (venta.getTotal() == null || venta.getTotal().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La venta no tiene un total valido para emitir comprobante");
        }
        if (venta.getImporteNeto() == null || venta.getImporteNeto().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("La venta no tiene importe neto valido para emitir comprobante");
        }
        if (venta.getImpuesto() == null || venta.getImpuesto().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("La venta no tiene impuesto valido para emitir comprobante");
        }
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }
}
