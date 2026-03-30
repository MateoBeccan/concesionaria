package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.PagoService;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.mapper.PagoMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import com.concesionaria.app.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Pago}.
 */
@Service
@Transactional
public class PagoServiceImpl implements PagoService {

    private static final Logger LOG = LoggerFactory.getLogger(PagoServiceImpl.class);

    private final PagoRepository pagoRepository;

    private final PagoMapper pagoMapper;

    private final VentaRepository ventaRepository;

    public PagoServiceImpl(PagoRepository pagoRepository, PagoMapper pagoMapper, VentaRepository ventaRepository) {
        this.pagoRepository = pagoRepository;
        this.pagoMapper = pagoMapper;
        this.ventaRepository = ventaRepository;
    }

    @Override
    public PagoDTO save(PagoDTO pagoDTO) {
        LOG.debug("Request to save Pago : {}", pagoDTO);
        Pago pago = pagoMapper.toEntity(pagoDTO);
        pago = pagoRepository.save(pago);
        return pagoMapper.toDto(pago);
    }

    @Override
    public PagoDTO update(PagoDTO pagoDTO) {
        LOG.debug("Request to update Pago : {}", pagoDTO);
        Pago pago = pagoMapper.toEntity(pagoDTO);
        pago = pagoRepository.save(pago);
        return pagoMapper.toDto(pago);
    }

    @Override
    public Optional<PagoDTO> partialUpdate(PagoDTO pagoDTO) {
        LOG.debug("Request to partially update Pago : {}", pagoDTO);

        return pagoRepository
            .findById(pagoDTO.getId())
            .map(existingPago -> {
                pagoMapper.partialUpdate(existingPago, pagoDTO);

                return existingPago;
            })
            .map(pagoRepository::save)
            .map(pagoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Pagos");
        return pagoRepository.findAll(pageable).map(pagoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoDTO> findOne(Long id) {
        LOG.debug("Request to get Pago : {}", id);
        return pagoRepository.findById(id).map(pagoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Pago : {}", id);
        pagoRepository.deleteById(id);
    }

    @Override
    public PagoDTO registrarPago(Long ventaId, PagoDTO pagoDTO) {

        LOG.info("Registrando pago para venta {}", ventaId);

        Venta venta = ventaRepository.findById(ventaId)
            .orElseThrow(() -> new BadRequestAlertException("Venta no existe", "pago", "ventanotfound"));

        if (pagoDTO.getMonto() == null || pagoDTO.getMonto().doubleValue() <= 0) {
            throw new BadRequestAlertException("Monto inválido", "pago", "montoinvalid");
        }

        // ❗ validar sobrepago
        if (venta.getSaldo().compareTo(pagoDTO.getMonto()) < 0) {
            throw new BadRequestAlertException("El monto excede el saldo pendiente", "pago", "excede");
        }

        // CREAR PAGO
        Pago pago = pagoMapper.toEntity(pagoDTO);
        pago.setVenta(venta);
        pago.setFecha(Instant.now());
        pago.setCreatedDate(Instant.now());

        pago = pagoRepository.save(pago);

        // 🔥 ACTUALIZAR VENTA
        venta.setTotalPagado(
            venta.getTotalPagado().add(pago.getMonto())
        );

        venta.setSaldo(
            venta.getTotal().subtract(venta.getTotalPagado())
        );

        // 🔥 CIERRE AUTOMÁTICO
        if (venta.getSaldo().compareTo(BigDecimal.ZERO) == 0) {
            LOG.info("Venta {} totalmente pagada", ventaId);
            // opcional: estadoVenta = PAGADA
        }

        ventaRepository.save(venta);

        return pagoMapper.toDto(pago);
    }
}
