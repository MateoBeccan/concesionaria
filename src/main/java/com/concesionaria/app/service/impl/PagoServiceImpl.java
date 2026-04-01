package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.PagoService;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.PagoMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PagoServiceImpl implements PagoService {

    private static final Logger LOG = LoggerFactory.getLogger(PagoServiceImpl.class);

    private final PagoRepository pagoRepository;
    private final PagoMapper pagoMapper;
    private final VentaRepository ventaRepository;

    public PagoServiceImpl(
        PagoRepository pagoRepository,
        PagoMapper pagoMapper,
        VentaRepository ventaRepository
    ) {
        this.pagoRepository = pagoRepository;
        this.pagoMapper = pagoMapper;
        this.ventaRepository = ventaRepository;
    }

    // =========================
    // CRUD BASE
    // =========================

    @Override
    public PagoDTO save(PagoDTO pagoDTO) {
        Pago pago = pagoMapper.toEntity(pagoDTO);
        return pagoMapper.toDto(pagoRepository.save(pago));
    }

    @Override
    public PagoDTO update(PagoDTO pagoDTO) {
        Pago pago = pagoMapper.toEntity(pagoDTO);
        return pagoMapper.toDto(pagoRepository.save(pago));
    }

    @Override
    public Optional<PagoDTO> partialUpdate(PagoDTO pagoDTO) {
        return pagoRepository
            .findById(pagoDTO.getId())
            .map(existing -> {
                pagoMapper.partialUpdate(existing, pagoDTO);
                return existing;
            })
            .map(pagoRepository::save)
            .map(pagoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDTO> findAll(Pageable pageable) {
        return pagoRepository.findAll(pageable).map(pagoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoDTO> findOne(Long id) {
        return pagoRepository.findById(id).map(pagoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        pagoRepository.deleteById(id);
    }

    // =========================
    // LÓGICA DE NEGOCIO
    // =========================

    @Override
    public PagoDTO registrarPago(Long ventaId, PagoDTO pagoDTO) {

        LOG.info("Registrando pago para venta {}", ventaId);

        // =========================
        // VALIDAR VENTA
        // =========================
        Venta venta = ventaRepository.findById(ventaId)
            .orElseThrow(() -> new BadRequestException("La venta no existe"));

        if (venta.getEstado() == EstadoVenta.PAGADA) {
            throw new BadRequestException("La venta ya está completamente pagada");
        }

        // =========================
        // VALIDAR MONTO
        // =========================
        if (pagoDTO.getMonto() == null || pagoDTO.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto del pago es inválido");
        }

        // =========================
        // VALIDAR SOBREPAGO
        // =========================
        if (venta.getSaldo().compareTo(pagoDTO.getMonto()) < 0) {
            throw new BadRequestException("El monto excede el saldo pendiente");
        }

        // =========================
        // CREAR PAGO
        // =========================
        Pago pago = pagoMapper.toEntity(pagoDTO);
        pago.setVenta(venta);
        pago.setFecha(Instant.now());
        pago.setCreatedDate(Instant.now());

        pago = pagoRepository.save(pago);

        // =========================
        // ACTUALIZAR VENTA
        // =========================
        BigDecimal nuevoTotalPagado = venta.getTotalPagado().add(pago.getMonto());
        venta.setTotalPagado(nuevoTotalPagado);

        BigDecimal nuevoSaldo = venta.getTotal().subtract(nuevoTotalPagado);
        venta.setSaldo(nuevoSaldo);

        // =========================
        // CIERRE AUTOMÁTICO
        // =========================
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            venta.setEstado(EstadoVenta.PAGADA);
            LOG.info("Venta {} completamente pagada", ventaId);
        }

        ventaRepository.save(venta);

        return pagoMapper.toDto(pago);
    }
}
