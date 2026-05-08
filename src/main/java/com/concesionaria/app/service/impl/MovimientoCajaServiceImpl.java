package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.MovimientoCaja;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.repository.MovimientoCajaRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.MovimientoCajaService;
import com.concesionaria.app.service.dto.MovimientoCajaDTO;
import com.concesionaria.app.service.dto.ResumenDiarioCajaDTO;
import com.concesionaria.app.service.dto.ResumenMetodoCajaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.MovimientoCajaMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MovimientoCajaServiceImpl implements MovimientoCajaService {

    private static final Logger LOG = LoggerFactory.getLogger(MovimientoCajaServiceImpl.class);
    private static final String ADMIN = "ROLE_ADMIN";
    private static final ZoneId ZONE_ID = ZoneId.of("America/Argentina/Buenos_Aires");

    private final MovimientoCajaRepository movimientoCajaRepository;
    private final MovimientoCajaMapper movimientoCajaMapper;

    public MovimientoCajaServiceImpl(MovimientoCajaRepository movimientoCajaRepository, MovimientoCajaMapper movimientoCajaMapper) {
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.movimientoCajaMapper = movimientoCajaMapper;
    }

    @Override
    public void registrarDesdePago(Pago pago, TipoMovimientoCaja tipoMovimiento, EstadoPago estadoMovimiento, boolean monetario) {
        if (pago == null || pago.getId() == null) {
            return;
        }
        if (movimientoCajaRepository.existsByPagoIdAndTipoMovimiento(pago.getId(), tipoMovimiento)) {
            LOG.debug("Movimiento de caja ya existente para pagoId={} tipo={}. Se evita duplicado.", pago.getId(), tipoMovimiento);
            return;
        }
        Instant ahora = Instant.now();
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setPago(pago);
        movimiento.setVenta(pago.getVenta());
        movimiento.setReserva(pago.getReserva());
        movimiento.setMetodoPago(pago.getMetodoPago());
        movimiento.setMoneda(pago.getMoneda());
        movimiento.setEntidadFinanciera(pago.getEntidadFinanciera());
        movimiento.setFecha(pago.getFecha() != null ? pago.getFecha() : ahora);
        movimiento.setUsuario(pago.getUsuarioRegistro() != null && !pago.getUsuarioRegistro().isBlank() ? pago.getUsuarioRegistro() : currentUserLogin());
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setEstado(estadoMovimiento);
        movimiento.setMontoOriginal(valorSeguro(pago.getMonto()));
        movimiento.setCotizacionUsada(pago.getCotizacionUsada());
        movimiento.setMontoAplicadoArs(monetario ? valorSeguro(pago.getMontoAplicadoVenta()) : BigDecimal.ZERO);
        movimiento.setReferencia(pago.getReferencia());
        movimiento.setNumeroOperacion(pago.getNumeroOperacion());
        movimiento.setCreatedDate(ahora);
        movimiento.setLastModifiedDate(ahora);

        movimientoCajaRepository.save(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoCajaDTO> findAll(
        Instant fechaDesde,
        Instant fechaHasta,
        String usuario,
        Long metodoPagoId,
        Long entidadFinancieraId,
        TipoMovimientoCaja tipo,
        EstadoPago estado,
        Pageable pageable
    ) {
        Page<MovimientoCaja> page;
        if (isAdmin()) {
            page = movimientoCajaRepository.findAllByFiltrosAdmin(
                fechaDesde,
                fechaHasta,
                normalizarTexto(usuario),
                metodoPagoId,
                entidadFinancieraId,
                tipo,
                estado,
                pageable
            );
        } else {
            page = movimientoCajaRepository.findAllByFiltrosUsuario(
                currentUserLogin(),
                fechaDesde,
                fechaHasta,
                metodoPagoId,
                entidadFinancieraId,
                tipo,
                estado,
                pageable
            );
        }
        return page.map(movimientoCajaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenDiarioCajaDTO obtenerResumenDiario(LocalDate fecha) {
        LocalDate fechaObjetivo = fecha != null ? fecha : LocalDate.now(ZONE_ID);
        Instant desde = fechaObjetivo.atStartOfDay(ZONE_ID).toInstant();
        Instant hasta = fechaObjetivo.plusDays(1).atStartOfDay(ZONE_ID).minusNanos(1).toInstant();

        List<MovimientoCaja> movimientos = findMovimientosParaResumen(desde, hasta);

        BigDecimal ingresos = movimientos
            .stream()
            .filter(m -> m.getTipoMovimiento() == TipoMovimientoCaja.INGRESO)
            .map(MovimientoCaja::getMontoAplicadoArs)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal reversos = movimientos
            .stream()
            .filter(m -> m.getTipoMovimiento() == TipoMovimientoCaja.REVERSO)
            .map(MovimientoCaja::getMontoAplicadoArs)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        ResumenDiarioCajaDTO resumen = new ResumenDiarioCajaDTO();
        resumen.setFecha(fechaObjetivo);
        resumen.setTotalIngresos(ingresos);
        resumen.setTotalReversos(reversos);
        resumen.setNeto(ingresos.subtract(reversos));
        resumen.setPorMetodo(resumenPorMetodo(movimientos));
        return resumen;
    }

    @Override
    public void delete(Long id) {
        throw new BadRequestException("No se permite borrar movimientos de caja");
    }

    private List<MovimientoCaja> findMovimientosParaResumen(Instant desde, Instant hasta) {
        Page<MovimientoCaja> page;
        if (isAdmin()) {
            page = movimientoCajaRepository.findAllByFiltrosAdmin(desde, hasta, null, null, null, null, null, Pageable.unpaged());
        } else {
            page = movimientoCajaRepository.findAllByFiltrosUsuario(currentUserLogin(), desde, hasta, null, null, null, null, Pageable.unpaged());
        }
        return page.getContent();
    }

    private List<ResumenMetodoCajaDTO> resumenPorMetodo(List<MovimientoCaja> movimientos) {
        return movimientos
            .stream()
            .filter(m -> m.getMetodoPago() != null)
            .collect(
                java.util.stream.Collectors.groupingBy(
                    m -> m.getMetodoPago().getId(),
                    java.util.stream.Collectors.toList()
                )
            )
            .entrySet()
            .stream()
            .map(entry -> {
                MetodoPago metodoPago = entry.getValue().get(0).getMetodoPago();
                BigDecimal ingresos = entry
                    .getValue()
                    .stream()
                    .filter(m -> m.getTipoMovimiento() == TipoMovimientoCaja.INGRESO)
                    .map(MovimientoCaja::getMontoAplicadoArs)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal reversos = entry
                    .getValue()
                    .stream()
                    .filter(m -> m.getTipoMovimiento() == TipoMovimientoCaja.REVERSO)
                    .map(MovimientoCaja::getMontoAplicadoArs)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                ResumenMetodoCajaDTO dto = new ResumenMetodoCajaDTO();
                dto.setMetodoPagoId(entry.getKey());
                dto.setMetodoPagoCodigo(metodoPago.getCodigo());
                dto.setMetodoPagoDescripcion(metodoPago.getDescripcion());
                dto.setTotalIngresos(ingresos);
                dto.setTotalReversos(reversos);
                dto.setNeto(ingresos.subtract(reversos));
                return dto;
            })
            .toList();
    }

    private boolean isAdmin() {
        return SecurityUtils.hasCurrentUserThisAuthority(ADMIN);
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String normalizarTexto(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
