package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.dto.CotizacionConversionDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class VentaValidator {
    private final VentaRepository ventaRepository;
    private final ReservaRepository reservaRepository;
    private final InventarioRepository inventarioRepository;
    private final VehiculoRepository vehiculoRepository;

    public VentaValidator(
        VentaRepository ventaRepository,
        ReservaRepository reservaRepository,
        InventarioRepository inventarioRepository,
        VehiculoRepository vehiculoRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.reservaRepository = reservaRepository;
        this.inventarioRepository = inventarioRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    public void validarVentaDto(
        VentaDTO dto,
        boolean exigirMinimoTradicional,
        Moneda monedaBaseVenta,
        CotizacionConversionDTO conversion,
        BigDecimal porcentajeMinimoReservaEscalaHumana,
        Function<BigDecimal, BigDecimal> calcularMontoMinimoReserva,
        BiFunction<VentaDTO, Vehiculo, Boolean> esUsuarioOperadorResoluble,
        Function<Inventario, Void> normalizarReservaVencidaFn
    ) {
        if (dto.getCliente() == null || dto.getCliente().getId() == null) {
            throw new BadRequestException("El cliente es obligatorio");
        }
        if (dto.getFecha() == null) {
            throw new BadRequestException("La fecha de la venta es obligatoria");
        }
        if (dto.getEstado() == null) {
            throw new BadRequestException("El estado de la venta es obligatorio");
        }
        if (dto.getVehiculo() == null || dto.getVehiculo().getId() == null) {
            throw new BadRequestException("El vehiculo de la venta es obligatorio");
        }
        Vehiculo vehiculo = obtenerVehiculo(dto.getVehiculo().getId());
        validarVehiculoParaVenta(vehiculo);
        validarDisponibilidadInventarioParaVenta(dto, vehiculo, normalizarReservaVencidaFn);

        dto.setMoneda(toMonedaDto(monedaBaseVenta));
        dto.setCotizacion(conversion.getCotizacionAplicada());
        dto.setFechaCotizacionUsada(conversion.getFechaCotizacionUsada());
        dto.setCotizacionId(conversion.getCotizacionOrigenId());
        dto.setPrecioBaseVehiculo(vehiculo.getPrecio().setScale(2, java.math.RoundingMode.HALF_UP));
        dto.setMonedaVehiculo(toMonedaDto(vehiculo.getMoneda()));
        dto.setImporteConvertido(conversion.getMontoConvertido());

        if (dto.getReserva() != null && dto.getReserva().getId() != null) {
            boolean reservaYaUsada = dto.getId() == null
                ? ventaRepository.existsByReservaId(dto.getReserva().getId())
                : ventaRepository.existsByReservaIdAndIdNot(dto.getReserva().getId(), dto.getId());
            if (reservaYaUsada) {
                throw new BadRequestException("La reserva ya se encuentra asociada a otra venta");
            }
        }
        validarReservaActivaParaVenta(dto);

        boolean existeVentaActiva = dto.getId() == null
            ? ventaRepository.existsByVehiculoIdAndEstadoIn(
                dto.getVehiculo().getId(),
                EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA, EstadoVenta.FINALIZADA)
            )
            : ventaRepository.existsByVehiculoIdAndEstadoInAndIdNot(
                dto.getVehiculo().getId(),
                EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA, EstadoVenta.FINALIZADA),
                dto.getId()
            );
        if (existeVentaActiva) {
            throw new BadRequestException("El vehiculo ya se encuentra asociado a otra venta activa");
        }

        if (!Boolean.TRUE.equals(esUsuarioOperadorResoluble.apply(dto, vehiculo))) {
            throw new BadRequestException("No se pudo resolver el usuario operador de la venta");
        }
        if (dto.getImporteNeto().compareTo(BigDecimal.ZERO) <= 0 || dto.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La venta debe tener un total mayor a 0");
        }

        BigDecimal totalPagado = dto.getTotalPagado() == null ? BigDecimal.ZERO : dto.getTotalPagado();
        BigDecimal saldo = dto.getSaldo() == null ? dto.getTotal().subtract(totalPagado) : dto.getSaldo();
        if (totalPagado.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El total pagado no puede ser negativo");
        }
        if (exigirMinimoTradicional && totalPagado.compareTo(BigDecimal.ZERO) == 0) {
            throw new BadRequestException(
                "La venta requiere un pago minimo del " + porcentajeMinimoReservaEscalaHumana.stripTrailingZeros().toPlainString() + "% del valor del vehiculo"
            );
        }
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El saldo no puede ser negativo");
        }
        if (totalPagado.compareTo(dto.getTotal()) > 0) {
            throw new BadRequestException("El total pagado no puede superar al total de la venta");
        }
        if (dto.getEstado() == EstadoVenta.PAGADA && saldo.compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("Una venta PAGADA debe tener saldo cero");
        }
        if (dto.getEstado() == EstadoVenta.CANCELADA && totalPagado.compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("No se puede marcar como CANCELADA una venta con pagos registrados");
        }
        BigDecimal montoMinimoReserva = calcularMontoMinimoReserva.apply(dto.getImporteNeto());
        if (exigirMinimoTradicional && totalPagado.compareTo(BigDecimal.ZERO) > 0 && totalPagado.compareTo(montoMinimoReserva) < 0) {
            throw new BadRequestException(
                "La venta requiere una sena minima del " + porcentajeMinimoReservaEscalaHumana.stripTrailingZeros().toPlainString() + "% del valor del vehiculo"
            );
        }
        if (dto.getEstado() == EstadoVenta.RESERVADA && totalPagado.compareTo(montoMinimoReserva) < 0) {
            throw new BadRequestException(
                "La reserva requiere un pago minimo del " + porcentajeMinimoReservaEscalaHumana.stripTrailingZeros().toPlainString() + "% del valor del vehiculo"
            );
        }

        dto.setTotalPagado(totalPagado);
        dto.setSaldo(saldo);
    }

    private Vehiculo obtenerVehiculo(Long vehiculoId) {
        return vehiculoRepository
            .findById(vehiculoId)
            .orElseThrow(() -> new BadRequestException("El vehiculo de la venta no existe"));
    }

    private void validarVehiculoParaVenta(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new BadRequestException("El vehiculo de la venta no existe");
        }
        if (vehiculo.getMoneda() == null || vehiculo.getMoneda().getId() == null) {
            throw new BadRequestException("El vehiculo seleccionado no tiene moneda configurada");
        }
        if (vehiculo.getPrecio() == null || vehiculo.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El vehiculo seleccionado no tiene un precio de lista valido");
        }
    }

    private void validarReservaActivaParaVenta(VentaDTO dto) {
        if (dto.getReserva() == null || dto.getReserva().getId() == null) {
            return;
        }
        Reserva reserva = reservaRepository.findById(dto.getReserva().getId()).orElseThrow(() -> new BadRequestException("La reserva indicada no existe"));
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new BadRequestException("Solo se puede convertir en venta una reserva ACTIVA");
        }
        if (reserva.getInventario() == null || reserva.getInventario().getId() == null) {
            throw new BadRequestException("La reserva no tiene inventario asociado");
        }
        Inventario inventario = inventarioRepository
            .findById(reserva.getInventario().getId())
            .orElseThrow(() -> new BadRequestException("El inventario de la reserva no existe"));
        if (inventario.getEstadoInventario() != EstadoInventario.RESERVADO) {
            throw new BadRequestException("La reserva solo puede convertirse cuando el inventario esta RESERVADO");
        }
        if (
            reserva.getCliente() == null ||
            reserva.getCliente().getId() == null ||
            dto.getCliente() == null ||
            dto.getCliente().getId() == null ||
            !reserva.getCliente().getId().equals(dto.getCliente().getId())
        ) {
            throw new BadRequestException("La venta debe mantener el mismo cliente de la reserva activa");
        }
        Long vehiculoId = inventario.getVehiculo() != null ? inventario.getVehiculo().getId() : null;
        if (vehiculoId == null) {
            throw new BadRequestException("La unidad reservada no tiene vehiculo asociado");
        }
        if (dto.getVehiculo() == null || dto.getVehiculo().getId() == null) {
            throw new BadRequestException("La venta debe indicar el vehiculo reservado");
        }
        if (!vehiculoId.equals(dto.getVehiculo().getId())) {
            throw new BadRequestException("La venta debe mantener el mismo vehiculo de la reserva activa");
        }
        boolean existeVentaActiva = dto.getId() == null
            ? ventaRepository.existsByVehiculoIdAndEstadoIn(
                vehiculoId,
                EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA, EstadoVenta.FINALIZADA)
            )
            : ventaRepository.existsByVehiculoIdAndEstadoInAndIdNot(
                vehiculoId,
                EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA, EstadoVenta.FINALIZADA),
                dto.getId()
            );
        if (existeVentaActiva) {
            throw new BadRequestException("La unidad reservada ya tiene una venta activa incompatible");
        }
    }

    private void validarDisponibilidadInventarioParaVenta(
        VentaDTO dto,
        Vehiculo vehiculo,
        Function<Inventario, Void> normalizarReservaVencidaFn
    ) {
        Inventario inventario = inventarioRepository
            .findByVehiculoId(vehiculo.getId())
            .orElseThrow(() -> new BadRequestException("Inventario no encontrado para el vehiculo seleccionado"));
        normalizarReservaVencidaFn.apply(inventario);
        if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new BadRequestException("El vehiculo seleccionado ya fue vendido");
        }
        if (inventario.getEstadoInventario() != EstadoInventario.RESERVADO) {
            return;
        }
        Optional<Reserva> reservaActivaOpt = reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(
            inventario.getId(),
            EstadoReserva.ACTIVA
        );
        if (reservaActivaOpt.isEmpty()) {
            throw new BadRequestException("El vehiculo seleccionado se encuentra reservado y no esta disponible");
        }
        Reserva reservaActiva = reservaActivaOpt.get();
        Long reservaDtoId = dto.getReserva() != null ? dto.getReserva().getId() : null;
        if (!esReservaPropiaDeVenta(dto, reservaActiva, reservaDtoId)) {
            throw new BadRequestException("El vehiculo seleccionado se encuentra reservado por otra operacion activa");
        }
        Long clienteVentaId = dto.getCliente() != null ? dto.getCliente().getId() : null;
        Long clienteReservaId = reservaActiva.getCliente() != null ? reservaActiva.getCliente().getId() : null;
        if (clienteVentaId == null || clienteReservaId == null || !clienteVentaId.equals(clienteReservaId)) {
            throw new BadRequestException("La venta debe mantener el mismo cliente de la reserva activa");
        }
    }

    private boolean esReservaPropiaDeVenta(VentaDTO dto, Reserva reservaActiva, Long reservaDtoId) {
        if (reservaDtoId != null && reservaDtoId.equals(reservaActiva.getId())) {
            return true;
        }
        if (dto.getId() == null) {
            return false;
        }
        return ventaRepository
            .findById(dto.getId())
            .map(ventaExistente -> {
                if (ventaExistente.getReserva() != null && reservaActiva.getId().equals(ventaExistente.getReserva().getId())) {
                    return true;
                }
                Long vehiculoVentaExistenteId = ventaExistente.getVehiculo() != null ? ventaExistente.getVehiculo().getId() : null;
                Long vehiculoDtoId = dto.getVehiculo() != null ? dto.getVehiculo().getId() : null;
                Long vehiculoReservaActivaId = reservaActiva.getInventario() != null && reservaActiva.getInventario().getVehiculo() != null
                    ? reservaActiva.getInventario().getVehiculo().getId()
                    : null;
                return (
                    vehiculoVentaExistenteId != null &&
                    vehiculoVentaExistenteId.equals(vehiculoDtoId) &&
                    vehiculoVentaExistenteId.equals(vehiculoReservaActivaId)
                );
            })
            .orElse(false);
    }

    private com.concesionaria.app.service.dto.MonedaDTO toMonedaDto(Moneda moneda) {
        com.concesionaria.app.service.dto.MonedaDTO dto = new com.concesionaria.app.service.dto.MonedaDTO();
        dto.setId(moneda.getId());
        dto.setCodigo(moneda.getCodigo());
        dto.setDescripcion(moneda.getDescripcion());
        dto.setSimbolo(moneda.getSimbolo());
        dto.setActivo(moneda.getActivo());
        return dto;
    }
}

