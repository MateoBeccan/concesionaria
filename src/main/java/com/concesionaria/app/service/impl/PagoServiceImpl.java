package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.PagoService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.PagoMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PagoServiceImpl implements PagoService {

    private static final Logger LOG = LoggerFactory.getLogger(PagoServiceImpl.class);

    private final PagoRepository pagoRepository;
    private final PagoMapper pagoMapper;
    private final VentaRepository ventaRepository;
    private final VentaService ventaService;

    @Value("${app.negocio.reserva.porcentaje-minimo:0.10}")
    private BigDecimal porcentajeMinimoReserva = new BigDecimal("0.10");

    public PagoServiceImpl(PagoRepository pagoRepository, PagoMapper pagoMapper, VentaRepository ventaRepository, VentaService ventaService) {
        this.pagoRepository = pagoRepository;
        this.pagoMapper = pagoMapper;
        this.ventaRepository = ventaRepository;
        this.ventaService = ventaService;
    }

    @Override
    public PagoDTO save(PagoDTO pagoDTO) {
        Long ventaId = pagoDTO.getVenta() != null ? pagoDTO.getVenta().getId() : null;
        if (ventaId == null) {
            throw new BadRequestException("Debe informar la venta para registrar el pago");
        }
        return registrarPago(ventaId, pagoDTO);
    }

    @Override
    public PagoDTO update(PagoDTO pagoDTO) {
        Pago pago = pagoMapper.toEntity(pagoDTO);
        pago.setLastModifiedDate(Instant.now());
        return pagoMapper.toDto(pagoRepository.save(pago));
    }

    @Override
    public Optional<PagoDTO> partialUpdate(PagoDTO pagoDTO) {
        return pagoRepository
            .findById(pagoDTO.getId())
            .map(existing -> {
                pagoMapper.partialUpdate(existing, pagoDTO);
                existing.setLastModifiedDate(Instant.now());
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
    @Transactional(readOnly = true)
    public List<PagoDTO> findByVentaId(Long ventaId) {
        return pagoRepository.findAllByVentaIdWithRelaciones(ventaId).stream().map(pagoMapper::toDto).toList();
    }

    @Override
    public void delete(Long id) {
        pagoRepository.deleteById(id);
    }

    @Override
    public PagoDTO registrarPago(Long ventaId, PagoDTO pagoDTO) {
        LOG.info("Registrando pago para venta {}", ventaId);

        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta no existe"));
        if (venta.getEstado() == EstadoVenta.PAGADA) {
            throw new BadRequestException("La venta ya esta completamente pagada");
        }
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new BadRequestException("No se puede registrar pagos sobre una venta cancelada");
        }
        if (venta.getSaldo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La venta ya esta saldada");
        }
        if (pagoDTO.getMonto() == null || pagoDTO.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto del pago es invalido");
        }
        if (venta.getSaldo().compareTo(pagoDTO.getMonto()) < 0) {
            throw new BadRequestException("El monto excede el saldo pendiente");
        }
        if (venta.getMoneda() != null && pagoDTO.getMoneda() != null && pagoDTO.getMoneda().getId() != null) {
            Long ventaMonedaId = venta.getMoneda().getId();
            if (!ventaMonedaId.equals(pagoDTO.getMoneda().getId())) {
                throw new BadRequestException("La moneda del pago debe coincidir con la moneda de la venta");
            }
        }

        BigDecimal totalPagadoActual = venta.getTotalPagado() == null ? BigDecimal.ZERO : venta.getTotalPagado();
        BigDecimal totalPagadoProyectado = totalPagadoActual.add(pagoDTO.getMonto()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoMinimoReserva = calcularMontoMinimoReserva(venta);
        if (totalPagadoProyectado.compareTo(BigDecimal.ZERO) > 0 && totalPagadoProyectado.compareTo(montoMinimoReserva) < 0) {
            throw new BadRequestException(
                "La venta requiere una sena minima del " +
                porcentajeMinimoReserva.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() +
                "% para registrar pagos"
            );
        }

        Pago pago = pagoMapper.toEntity(pagoDTO);
        pago.setVenta(venta);
        pago.setFecha(Instant.now());
        pago.setCreatedDate(Instant.now());
        pago.setLastModifiedDate(Instant.now());
        pago = pagoRepository.save(pago);

        BigDecimal nuevoTotalPagado = (venta.getTotalPagado() == null ? BigDecimal.ZERO : venta.getTotalPagado()).add(pago.getMonto());
        venta.setTotalPagado(nuevoTotalPagado);

        BigDecimal nuevoSaldo = venta.getTotal().subtract(nuevoTotalPagado);
        venta.setSaldo(nuevoSaldo);
        venta.setLastModifiedDate(Instant.now());

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            ventaService.confirmarVenta(ventaId);
        } else {
            venta.setEstado(EstadoVenta.PENDIENTE);
            ventaRepository.save(venta);
            ventaService.sincronizarInventarioConVenta(ventaId);
        }
        return pagoMapper.toDto(pago);
    }

    private BigDecimal calcularMontoMinimoReserva(Venta venta) {
        BigDecimal base = venta.getImporteNeto() == null ? BigDecimal.ZERO : venta.getImporteNeto();
        return base.multiply(porcentajeMinimoReserva).setScale(2, RoundingMode.HALF_UP);
    }
}
