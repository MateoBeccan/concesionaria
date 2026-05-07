package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.InventarioHistorial;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoOperativoDocumental;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.EstadoVehiculo;
import com.concesionaria.app.domain.enumeration.OrigenVehiculo;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.domain.enumeration.TipoTenenciaInventario;
import com.concesionaria.app.repository.InventarioHistorialRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.TasacionUsadoRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.TomaUsadoInventarioService;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TomaUsadoInventarioServiceImpl implements TomaUsadoInventarioService {

    private final PagoRepository pagoRepository;
    private final TasacionUsadoRepository tasacionUsadoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;
    private final InventarioHistorialRepository inventarioHistorialRepository;
    private final VentaRepository ventaRepository;
    private final MonedaRepository monedaRepository;

    @Value("${app.negocio.moneda-base-codigo:ARS}")
    private String monedaBaseCodigo;

    public TomaUsadoInventarioServiceImpl(
        PagoRepository pagoRepository,
        TasacionUsadoRepository tasacionUsadoRepository,
        VehiculoRepository vehiculoRepository,
        InventarioRepository inventarioRepository,
        InventarioHistorialRepository inventarioHistorialRepository,
        VentaRepository ventaRepository,
        MonedaRepository monedaRepository
    ) {
        this.pagoRepository = pagoRepository;
        this.tasacionUsadoRepository = tasacionUsadoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.inventarioHistorialRepository = inventarioHistorialRepository;
        this.ventaRepository = ventaRepository;
        this.monedaRepository = monedaRepository;
    }

    @Override
    public void generarSiCorresponde(Venta venta, String usuarioLogin) {
        if (venta == null || venta.getId() == null) {
            return;
        }
        TasacionUsado tasacion = resolverTasacionUsadoParaIngreso(venta, usuarioLogin);
        if (tasacion == null) {
            return;
        }
        if (tasacion.getInventarioGenerado() != null && tasacion.getInventarioGenerado().getId() != null) {
            return;
        }
        validarTasacionParaIngresoInventario(venta, tasacion);

        Instant ahora = Instant.now();
        Vehiculo vehiculoUsado = new Vehiculo();
        vehiculoUsado.setEstado(EstadoVehiculo.USADO);
        vehiculoUsado.setFechaFabricacion(java.time.LocalDate.of(tasacion.getAnioUsado(), 1, 1));
        vehiculoUsado.setKm(tasacion.getKmUsado());
        vehiculoUsado.setPatente(tasacion.getPatenteUsado());
        vehiculoUsado.setVinChasis(tasacion.getVinChasisUsado());
        vehiculoUsado.setColor(tasacion.getColorUsado());
        vehiculoUsado.setObservaciones("Ingresado por toma de usado en venta #" + venta.getId());
        vehiculoUsado.setPrecio(tasacion.getMontoTasacion().setScale(2, RoundingMode.HALF_UP));
        vehiculoUsado.setMoneda(tasacion.getMoneda());
        vehiculoUsado.setVersion(tasacion.getVersion());
        vehiculoUsado.setMotor(tasacion.getMotor());
        vehiculoUsado.setTipoVehiculo(tasacion.getTipoVehiculo());
        vehiculoUsado.setCreatedDate(ahora);
        vehiculoUsado.setCreatedBy(usuarioLogin);
        vehiculoUsado.setLastModifiedDate(ahora);
        vehiculoUsado.setLastModifiedBy(usuarioLogin);
        vehiculoUsado = vehiculoRepository.save(vehiculoUsado);

        Inventario inventarioUsado = new Inventario();
        inventarioUsado.setVehiculo(vehiculoUsado);
        inventarioUsado.setFechaIngreso(ahora);
        inventarioUsado.setCodigoInternoStock(generarCodigoInternoStock(vehiculoUsado.getId()));
        inventarioUsado.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventarioUsado.setOrigenVehiculo(OrigenVehiculo.TOMA_USADO);
        inventarioUsado.setTipoTenencia(TipoTenenciaInventario.PROPIO);
        inventarioUsado.setEstadoOperativoDocumental(EstadoOperativoDocumental.EN_GESTION);
        inventarioUsado.setCostoAdquisicion(tasacion.getMontoTasacion().setScale(2, RoundingMode.HALF_UP));
        inventarioUsado.setObservaciones("Vehiculo ingresado por toma de usado en venta #" + venta.getId());
        inventarioUsado.setCreatedDate(ahora);
        inventarioUsado.setCreatedBy(usuarioLogin);
        inventarioUsado.setLastModifiedDate(ahora);
        inventarioUsado.setLastModifiedBy(usuarioLogin);
        inventarioUsado = inventarioRepository.save(inventarioUsado);

        tasacion.setInventarioGenerado(inventarioUsado);
        tasacion.setLastModifiedDate(ahora);
        tasacionUsadoRepository.save(tasacion);

        InventarioHistorial historial = new InventarioHistorial();
        historial.setInventario(inventarioUsado);
        historial.setEstadoAnterior(null);
        historial.setEstadoNuevo(EstadoInventario.DISPONIBLE);
        historial.setAccion("INGRESO_TOMA_USADO");
        historial.setDetalle("Vehiculo ingresado por toma de usado en venta #" + venta.getId());
        historial.setMotivo("Ingreso automatico por pago con entrega de usado");
        historial.setVenta(venta);
        historial.setCliente(venta.getCliente());
        historial.setFecha(ahora);
        historial.setUsuario(usuarioLogin);
        inventarioHistorialRepository.save(historial);
    }

    private TasacionUsado resolverTasacionUsadoParaIngreso(Venta venta, String usuarioLogin) {
        if (venta.getTasacionUsado() != null && venta.getTasacionUsado().getId() != null) {
            return tasacionUsadoRepository.findById(venta.getTasacionUsado().getId()).orElse(null);
        }

        return pagoRepository
            .findFirstByVentaIdAndEstadoAndTipoMovimientoAndTasacionUsadoIsNotNullOrderByFechaDescIdDesc(
                venta.getId(),
                EstadoPago.REGISTRADO,
                TipoMovimientoPago.ENTREGA_USADO
            )
            .map(pago -> pago.getTasacionUsado())
            .flatMap(tasacion -> tasacion == null || tasacion.getId() == null ? Optional.empty() : tasacionUsadoRepository.findById(tasacion.getId()))
            .map(tasacionRecuperada -> {
                venta.setTasacionUsado(tasacionRecuperada);
                venta.setLastModifiedDate(Instant.now());
                venta.setLastModifiedBy(usuarioLogin);
                ventaRepository.save(venta);
                return tasacionRecuperada;
            })
            .orElse(null);
    }

    private void validarTasacionParaIngresoInventario(Venta venta, TasacionUsado tasacion) {
        if (tasacion.getMontoTasacion() == null || tasacion.getMontoTasacion().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("No se puede generar inventario del usado: la tasacion no tiene precio valido");
        }
        if (tasacion.getKmUsado() == null || tasacion.getKmUsado() < 0) {
            throw new BadRequestException("No se puede generar inventario del usado: la tasacion debe informar kilometraje valido");
        }
        if (tasacion.getAnioUsado() == null || tasacion.getAnioUsado() < 1900 || tasacion.getAnioUsado() > 2100) {
            throw new BadRequestException("No se puede generar inventario del usado: la tasacion debe informar fecha de fabricacion valida");
        }
        if (tasacion.getVersion() == null || tasacion.getVersion().getId() == null) {
            throw new BadRequestException("No se puede generar inventario del usado: la tasacion debe informar una version valida");
        }
        if (tasacion.getTipoVehiculo() == null || tasacion.getTipoVehiculo().getId() == null) {
            throw new BadRequestException("No se puede generar inventario del usado: la tasacion debe informar un tipo de vehiculo valido");
        }
        if (tasacion.getMoneda() == null || tasacion.getMoneda().getId() == null) {
            throw new BadRequestException("No se puede generar inventario del usado: la tasacion debe informar una moneda valida");
        }
        if (venta.getMoneda() == null || venta.getMoneda().getId() == null) {
            throw new BadRequestException("No se puede generar inventario del usado: la venta no tiene moneda configurada");
        }
        Moneda monedaBase = resolverMonedaBaseVenta();
        if (!monedaBase.getId().equals(venta.getMoneda().getId())) {
            throw new BadRequestException("No se puede generar inventario del usado: la venta debe estar en moneda base configurada: " + monedaBase.getCodigo());
        }
        if (!tasacion.getMoneda().getId().equals(venta.getMoneda().getId())) {
            throw new BadRequestException("No se puede generar inventario del usado: la tasacion debe estar en la misma moneda de la venta");
        }
    }

    private Moneda resolverMonedaBaseVenta() {
        return monedaRepository
            .findByCodigoIgnoreCase(monedaBaseCodigo)
            .orElseThrow(() -> new BadRequestException("No existe moneda base configurada para ventas: " + monedaBaseCodigo));
    }

    private String generarCodigoInternoStock(Long vehiculoId) {
        long suffix = vehiculoId == null ? System.currentTimeMillis() : vehiculoId;
        return "TU-" + suffix + "-" + Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond();
    }
}

