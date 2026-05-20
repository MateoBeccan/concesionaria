package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.InventarioHistorial;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.VentaHistorial;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoOperativoDocumental;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.domain.enumeration.EstadoVehiculo;
import com.concesionaria.app.domain.enumeration.OrigenVehiculo;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.domain.enumeration.TipoTenenciaInventario;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.repository.InventarioHistorialRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.TasacionUsadoRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.repository.VentaHistorialRepository;
import com.concesionaria.app.repository.UserRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.CurrencyConversionService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.CotizacionConversionDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.dto.VentaHistorialDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.VentaMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VentaServiceImpl implements VentaService {

    private static final Logger LOG = LoggerFactory.getLogger(VentaServiceImpl.class);

    private final VentaRepository ventaRepository;
    private final VentaMapper ventaMapper;
    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;
    private final ClienteRepository clienteRepository;
    private final InventarioHistorialRepository inventarioHistorialRepository;
    private final PagoRepository pagoRepository;
    private final MonedaRepository monedaRepository;
    private final UserRepository userRepository;
    private final ReservaRepository reservaRepository;
    private final TasacionUsadoRepository tasacionUsadoRepository;
    private final VentaHistorialRepository ventaHistorialRepository;
    private final CurrencyConversionService currencyConversionService;
    private final VentaValidator ventaValidator;
    private final VentaCalculator ventaCalculator;
    private final VentaHistorialService ventaHistorialService;
    private final VentaInventarioSyncService ventaInventarioSyncService;

    @Value("${app.negocio.reserva.porcentaje-minimo:0.10}")
    private BigDecimal porcentajeMinimoReserva = new BigDecimal("0.10");

    @Value("${app.negocio.moneda-base-codigo:ARS}")
    private String monedaBaseCodigo;

    public VentaServiceImpl(
        VentaRepository ventaRepository,
        VentaMapper ventaMapper,
        VehiculoRepository vehiculoRepository,
        InventarioRepository inventarioRepository,
        ClienteRepository clienteRepository,
        InventarioHistorialRepository inventarioHistorialRepository,
        PagoRepository pagoRepository,
        MonedaRepository monedaRepository,
        UserRepository userRepository,
        ReservaRepository reservaRepository,
        TasacionUsadoRepository tasacionUsadoRepository,
        VentaHistorialRepository ventaHistorialRepository,
        CurrencyConversionService currencyConversionService,
        VentaValidator ventaValidator,
        VentaCalculator ventaCalculator,
        VentaHistorialService ventaHistorialService,
        VentaInventarioSyncService ventaInventarioSyncService
    ) {
        this.ventaRepository = ventaRepository;
        this.ventaMapper = ventaMapper;
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.clienteRepository = clienteRepository;
        this.inventarioHistorialRepository = inventarioHistorialRepository;
        this.pagoRepository = pagoRepository;
        this.monedaRepository = monedaRepository;
        this.userRepository = userRepository;
        this.reservaRepository = reservaRepository;
        this.tasacionUsadoRepository = tasacionUsadoRepository;
        this.ventaHistorialRepository = ventaHistorialRepository;
        this.currencyConversionService = currencyConversionService;
        this.ventaValidator = ventaValidator;
        this.ventaCalculator = ventaCalculator;
        this.ventaHistorialService = ventaHistorialService;
        this.ventaInventarioSyncService = ventaInventarioSyncService;
    }

    @Override
    public VentaDTO save(VentaDTO dto) {
        validarVentaDto(dto, true);
        Instant now = Instant.now();
        String currentUser = currentUserLogin();
        Venta venta = ventaMapper.toEntity(dto);
        venta.setCotizacion(dto.getCotizacion());
        venta.setFechaCotizacionUsada(dto.getFechaCotizacionUsada());
        venta.setImporteNeto(dto.getImporteNeto());
        venta.setImpuesto(dto.getImpuesto());
        venta.setTotal(dto.getTotal());
        venta.setTotalPagado(dto.getTotalPagado());
        venta.setSaldo(dto.getSaldo());
        venta.setUser(resolveUsuarioOperador(dto));
        if (dto.getCotizacionId() != null) {
            venta.setCotizacionRef(new com.concesionaria.app.domain.Cotizacion().id(dto.getCotizacionId()));
        } else {
            venta.setCotizacionRef(null);
        }
        venta.setCreatedDate(now);
        venta.setCreatedBy(currentUser);
        venta.setLastModifiedDate(now);
        venta.setLastModifiedBy(currentUser);
        Venta saved = ventaRepository.save(venta);
        registrarHistorialVenta(saved, null, saved.getEstado(), "VENTA_CREADA", "Alta de venta");
        sincronizarReservaDesdeVenta(saved);
        sincronizarInventarioConVenta(saved.getId());
        return ventaMapper.toDto(saved);
    }

    @Override
    public VentaDTO saveDesdePlanAhorro(VentaDTO dto) {
        validarVentaDto(dto, false);
        Instant now = Instant.now();
        String currentUser = currentUserLogin();
        Venta venta = ventaMapper.toEntity(dto);
        venta.setCotizacion(dto.getCotizacion());
        venta.setFechaCotizacionUsada(dto.getFechaCotizacionUsada());
        venta.setImporteNeto(dto.getImporteNeto());
        venta.setImpuesto(dto.getImpuesto());
        venta.setTotal(dto.getTotal());
        venta.setTotalPagado(dto.getTotalPagado());
        venta.setSaldo(dto.getSaldo());
        venta.setUser(resolveUsuarioOperador(dto));
        if (dto.getCotizacionId() != null) {
            venta.setCotizacionRef(new com.concesionaria.app.domain.Cotizacion().id(dto.getCotizacionId()));
        } else {
            venta.setCotizacionRef(null);
        }
        venta.setCreatedDate(now);
        venta.setCreatedBy(currentUser);
        venta.setLastModifiedDate(now);
        venta.setLastModifiedBy(currentUser);
        Venta saved = ventaRepository.save(venta);
        registrarHistorialVenta(saved, null, saved.getEstado(), "VENTA_CREADA", "Alta de venta");
        sincronizarReservaDesdeVenta(saved);
        sincronizarInventarioConVenta(saved.getId());
        return ventaMapper.toDto(saved);
    }

    @Override
    public VentaDTO update(VentaDTO dto) {
        Venta existing = ventaRepository.findById(dto.getId()).orElseThrow(() -> new BadRequestException("La venta no existe"));
        validarVentaDto(dto, true);
        Venta venta = ventaMapper.toEntity(dto);
        venta.setCotizacion(dto.getCotizacion());
        venta.setFechaCotizacionUsada(dto.getFechaCotizacionUsada());
        venta.setImporteNeto(dto.getImporteNeto());
        venta.setImpuesto(dto.getImpuesto());
        venta.setTotal(dto.getTotal());
        venta.setTotalPagado(dto.getTotalPagado());
        venta.setSaldo(dto.getSaldo());
        venta.setUser(resolveUsuarioOperador(dto));
        if (dto.getCotizacionId() != null) {
            venta.setCotizacionRef(new com.concesionaria.app.domain.Cotizacion().id(dto.getCotizacionId()));
        } else {
            venta.setCotizacionRef(null);
        }
        venta.setCreatedDate(existing.getCreatedDate());
        venta.setCreatedBy(existing.getCreatedBy());
        venta.setLastModifiedDate(Instant.now());
        venta.setLastModifiedBy(currentUserLogin());
        EstadoVenta estadoAnterior = existing.getEstado();
        Venta saved = ventaRepository.save(venta);
        registrarCambioEstadoVentaSiCorresponde(saved, estadoAnterior, "VENTA_ESTADO_ACTUALIZADO", "Cambio de estado por actualizacion de venta");
        sincronizarReservaDesdeVenta(saved);
        sincronizarInventarioConVenta(saved.getId());
        return ventaMapper.toDto(saved);
    }

    @Override
    public Optional<VentaDTO> partialUpdate(VentaDTO dto) {
        return ventaRepository
            .findById(dto.getId())
            .map(existing -> {
                EstadoVenta estadoAnterior = existing.getEstado();
                ventaMapper.partialUpdate(existing, dto);
                VentaDTO dtoActualizado = ventaMapper.toDto(existing);
                validarVentaDto(dtoActualizado, true);
                existing.setCotizacion(dtoActualizado.getCotizacion());
                existing.setFechaCotizacionUsada(dtoActualizado.getFechaCotizacionUsada());
                existing.setImporteNeto(dtoActualizado.getImporteNeto());
                existing.setImpuesto(dtoActualizado.getImpuesto());
                existing.setTotal(dtoActualizado.getTotal());
                existing.setTotalPagado(dtoActualizado.getTotalPagado());
                existing.setSaldo(dtoActualizado.getSaldo());
                if (existing.getCreatedBy() == null) {
                    existing.setCreatedBy(currentUserLogin());
                }
                existing.setUser(resolveUsuarioOperador(dtoActualizado));
                if (dtoActualizado.getCotizacionId() != null) {
                    existing.setCotizacionRef(new com.concesionaria.app.domain.Cotizacion().id(dtoActualizado.getCotizacionId()));
                } else {
                    existing.setCotizacionRef(null);
                }
                existing.setLastModifiedDate(Instant.now());
                existing.setLastModifiedBy(currentUserLogin());
                Venta saved = ventaRepository.save(existing);
                sincronizarReservaDesdeVenta(saved);
                sincronizarInventarioConVenta(saved.getId());
                registrarCambioEstadoVentaSiCorresponde(
                    saved,
                    estadoAnterior,
                    "VENTA_ESTADO_ACTUALIZADO",
                    "Cambio de estado por actualizacion parcial de venta"
                );
                return ventaMapper.toDto(saved);
            });
    }

    private void validarVentaDto(VentaDTO dto, boolean exigirMinimoTradicional) {
        if (dto.getVehiculo() == null || dto.getVehiculo().getId() == null) {
            throw new BadRequestException("El vehiculo de la venta es obligatorio");
        }
        Vehiculo vehiculo = vehiculoRepository
            .findById(dto.getVehiculo().getId())
            .orElseThrow(() -> new BadRequestException("El vehiculo de la venta no existe"));
        validarDisponibilidadInventarioParaVenta(dto, vehiculo);
        Moneda monedaBaseVenta = resolverMonedaBaseVenta();
        CotizacionConversionDTO conversion = resolverConversionVehiculoAVenta(dto, vehiculo, monedaBaseVenta);
        recalcularMontosDesdeVehiculo(dto, conversion.getMontoConvertido());
        ventaValidator.validarVentaDto(
            dto,
            exigirMinimoTradicional,
            monedaBaseVenta,
            conversion,
            porcentajeMinimoReservaEscalaHumana(),
            this::calcularMontoMinimoReserva,
            (ventaDto, ignoredVehiculo) -> resolveUsuarioOperador(ventaDto) != null,
            inventario -> {
                normalizarReservaVencida(inventario);
                return null;
            }
        );
    }

    @Override
    public Page<VentaDTO> findAll(Pageable pageable) {
        Page<Venta> page = ventaRepository.findAll(pageable);
        reconciliarInventarioVentasActivas(page.getContent());
        return page.map(ventaMapper::toDto);
    }

    @Override
    public Page<VentaDTO> findAllWithEagerRelationships(Pageable pageable) {
        Page<Venta> page = ventaRepository.findAllWithEagerRelationships(pageable);
        reconciliarInventarioVentasActivas(page.getContent());
        return page.map(ventaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaDTO> findAllCurrentUser(Pageable pageable) {
        Page<Venta> page = ventaRepository.findByUserIsCurrentUser(pageable);
        reconciliarInventarioVentasActivas(page.getContent());
        return page.map(ventaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaDTO> findAllCurrentUserWithEagerRelationships(Pageable pageable) {
        Page<Venta> page = ventaRepository.findAllCurrentUserWithToOneRelationships(pageable);
        reconciliarInventarioVentasActivas(page.getContent());
        return page.map(ventaMapper::toDto);
    }

    @Override
    public Optional<VentaDTO> findOne(Long id) {
        sincronizarInventarioConVenta(id);
        return ventaRepository.findOneWithEagerRelationships(id).map(ventaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VentaDTO> findByReservaId(Long reservaId) {
        if (reservaId == null) {
            return Optional.empty();
        }
        return ventaRepository.findFirstByReservaIdOrderByFechaDesc(reservaId).map(ventaMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        ventaRepository.deleteById(id);
    }

    @Override
    public VentaDTO crearVenta(Long vehiculoId, Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new BadRequestException("El cliente no existe"));
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId).orElseThrow(() -> new BadRequestException("El vehiculo no existe"));
        Inventario inv = inventarioRepository.findByVehiculoId(vehiculoId).orElseThrow(() -> new BadRequestException("Inventario no encontrado"));
        normalizarReservaVencida(inv);

        if (inv.getEstadoInventario() == EstadoInventario.VENDIDO) {
            throw new BadRequestException("El vehiculo ya fue vendido");
        }
        if (inv.getEstadoInventario() == EstadoInventario.RESERVADO) {
            var reservaActiva = reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(inv.getId(), EstadoReserva.ACTIVA);
            if (reservaActiva.isPresent() && !reservaActiva.get().getCliente().getId().equals(clienteId)) {
                throw new BadRequestException("El vehiculo esta reservado por otro cliente");
            }
        }
        if (
            ventaRepository.existsByVehiculoIdAndEstadoIn(
                vehiculoId,
                EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA, EstadoVenta.FINALIZADA)
            )
        ) {
            throw new BadRequestException("El vehiculo ya tiene una venta activa asociada");
        }

        BigDecimal precio = vehiculo.getPrecio();
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El vehiculo seleccionado no tiene un precio de lista valido");
        }
        if (vehiculo.getMoneda() == null || vehiculo.getMoneda().getId() == null) {
            throw new BadRequestException("El vehiculo seleccionado no tiene moneda configurada");
        }
        Moneda monedaBaseVenta = resolverMonedaBaseVenta();
        CotizacionConversionDTO conversion = currencyConversionService.convertir(
            precio,
            vehiculo.getMoneda().getId(),
            monedaBaseVenta.getId(),
            Instant.now()
        );
        BigDecimal cotizacionAplicada = conversion.getCotizacionAplicada().setScale(8, RoundingMode.HALF_UP);
        BigDecimal importeNeto = conversion.getMontoConvertido().setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuesto = importeNeto.multiply(new BigDecimal("0.21")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = importeNeto.add(impuesto);

        Venta venta = new Venta();
        String currentUser = currentUserLogin();
        Instant now = Instant.now();
        venta.setCliente(cliente);
        venta.setFecha(now);
        venta.setCreatedDate(now);
        venta.setCreatedBy(currentUser);
        venta.setLastModifiedDate(now);
        venta.setLastModifiedBy(currentUser);
        venta.setImporteNeto(importeNeto);
        venta.setImpuesto(impuesto);
        venta.setTotal(total);
        venta.setTotalPagado(BigDecimal.ZERO);
        venta.setSaldo(total);
        venta.setEstado(EstadoVenta.PENDIENTE);

        venta.setCotizacion(cotizacionAplicada);
        venta.setFechaCotizacionUsada(conversion.getFechaCotizacionUsada());
        if (conversion.getCotizacionOrigenId() != null) {
            venta.setCotizacionRef(new com.concesionaria.app.domain.Cotizacion().id(conversion.getCotizacionOrigenId()));
        }
        venta.setMoneda(monedaBaseVenta);
        venta.setUser(resolveUsuarioOperador(null));
        venta.setVehiculo(vehiculo);

        Venta saved = ventaRepository.save(venta);
        registrarHistorialVenta(saved, null, saved.getEstado(), "VENTA_CREADA", "Alta de venta desde flujo rapido");

        sincronizarInventarioConVenta(saved.getId());

        return ventaMapper.toDto(saved);
    }

    @Override
    public void confirmarVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta no existe"));

        if (venta.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("La venta no esta totalmente paga");
        }
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new BadRequestException("No se puede confirmar una venta cancelada");
        }

        ventaInventarioSyncService.marcarVendidoPorVenta(venta);

        EstadoVenta estadoAnterior = venta.getEstado();
        venta.setEstado(EstadoVenta.PAGADA);
        venta.setLastModifiedDate(Instant.now());
        venta.setLastModifiedBy(currentUserLogin());
        ventaRepository.save(venta);
        generarInventarioTomaUsadoSiCorresponde(venta);
        registrarCambioEstadoVentaSiCorresponde(venta, estadoAnterior, "VENTA_CONFIRMADA", "Venta confirmada por pago completo");
    }

    @Override
    public void sincronizarInventarioConVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta no existe"));
        asegurarIngresoUsadoSiVentaCobrada(venta);
        ventaInventarioSyncService.sincronizarConVenta(ventaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaHistorialDTO> findHistorialByVentaId(Long ventaId) {
        if (ventaId == null) {
            return List.of();
        }
        return ventaHistorialRepository.findAllByVentaIdOrderByFechaDesc(ventaId).stream().map(this::toHistorialDto).collect(Collectors.toList());
    }

    private void reconciliarInventarioVentasActivas(List<Venta> ventas) {
        for (Venta venta : ventas) {
            if (venta == null || venta.getId() == null || venta.getEstado() == null) {
                continue;
            }
            try {
                sincronizarInventarioConVenta(venta.getId());
            } catch (BadRequestException ex) {
                LOG.warn("No se pudo reconciliar inventario para venta {}: {}", venta.getId(), ex.getMessage());
            }
        }
    }

    private void asegurarIngresoUsadoSiVentaCobrada(Venta venta) {
        if (venta == null || venta.getId() == null || venta.getEstado() == null) {
            return;
        }
        if (venta.getEstado() != EstadoVenta.PAGADA && venta.getEstado() != EstadoVenta.FINALIZADA) {
            return;
        }
        if (venta.getSaldo() != null && venta.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
            return;
        }
        generarInventarioTomaUsadoSiCorresponde(venta);
    }

    private void normalizarReservaVencida(Inventario inventario) {
        ventaInventarioSyncService.normalizarReservaVencida(inventario);
    }

    private void sincronizarReservaDesdeVenta(Venta venta) {
        ventaInventarioSyncService.sincronizarReservaDesdeVenta(venta);
    }


    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }

    private User resolveUsuarioOperador(VentaDTO dto) {
        if (dto != null && dto.getUser() != null && dto.getUser().getId() != null) {
            return userRepository.findById(dto.getUser().getId()).orElseThrow(() -> new BadRequestException("El usuario operador no existe"));
        }

        Optional<String> login = SecurityUtils.getCurrentUserLogin();
        if (login.isPresent()) {
            return userRepository.findOneByLogin(login.get()).orElseGet(this::resolveUsuarioFallback);
        }

        return resolveUsuarioFallback();
    }

    private User resolveUsuarioFallback() {
        return userRepository
            .findAllByIdNotNullAndActivatedIsTrue(org.springframework.data.domain.PageRequest.of(0, 1))
            .stream()
            .findFirst()
            .orElseThrow(() -> new BadRequestException("No hay usuario operador disponible para registrar la venta"));
    }

    private void generarInventarioTomaUsadoSiCorresponde(Venta venta) {
        if (venta == null || venta.getId() == null) {
            return;
        }
        TasacionUsado tasacion = resolverTasacionUsadoParaIngreso(venta);
        if (tasacion == null) {
            return;
        }
        if (tasacion.getInventarioGenerado() != null && tasacion.getInventarioGenerado().getId() != null) {
            return;
        }
        validarTasacionParaIngresoInventario(venta, tasacion);

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
        vehiculoUsado.setCreatedDate(Instant.now());
        vehiculoUsado.setCreatedBy(currentUserLogin());
        vehiculoUsado.setLastModifiedDate(Instant.now());
        vehiculoUsado.setLastModifiedBy(currentUserLogin());
        vehiculoUsado = vehiculoRepository.save(vehiculoUsado);

        Inventario inventarioUsado = new Inventario();
        inventarioUsado.setVehiculo(vehiculoUsado);
        inventarioUsado.setFechaIngreso(Instant.now());
        inventarioUsado.setCodigoInternoStock(generarCodigoInternoStock(vehiculoUsado.getId()));
        inventarioUsado.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventarioUsado.setOrigenVehiculo(OrigenVehiculo.TOMA_USADO);
        inventarioUsado.setTipoTenencia(TipoTenenciaInventario.PROPIO);
        inventarioUsado.setEstadoOperativoDocumental(EstadoOperativoDocumental.EN_GESTION);
        inventarioUsado.setCostoAdquisicion(tasacion.getMontoTasacion().setScale(2, RoundingMode.HALF_UP));
        inventarioUsado.setObservaciones("Vehiculo ingresado por toma de usado en venta #" + venta.getId());
        inventarioUsado.setCreatedDate(Instant.now());
        inventarioUsado.setCreatedBy(currentUserLogin());
        inventarioUsado.setLastModifiedDate(Instant.now());
        inventarioUsado.setLastModifiedBy(currentUserLogin());
        inventarioUsado = inventarioRepository.save(inventarioUsado);

        tasacion.setInventarioGenerado(inventarioUsado);
        tasacion.setLastModifiedDate(Instant.now());
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
        historial.setFecha(Instant.now());
        historial.setUsuario(currentUserLogin());
        inventarioHistorialRepository.save(historial);
    }

    private TasacionUsado resolverTasacionUsadoParaIngreso(Venta venta) {
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
                venta.setLastModifiedBy(currentUserLogin());
                ventaRepository.save(venta);
                return tasacionRecuperada;
            })
            .orElse(null);
    }

    private String generarCodigoInternoStock(Long vehiculoId) {
        long suffix = vehiculoId == null ? System.currentTimeMillis() : vehiculoId;
        return "TU-" + suffix + "-" + Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond();
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
            throw new BadRequestException("No se puede generar inventario del usado: la venta debe estar en moneda base ARS");
        }
        if (!tasacion.getMoneda().getId().equals(venta.getMoneda().getId())) {
            throw new BadRequestException("No se puede generar inventario del usado: la tasacion debe estar en la misma moneda de la venta");
        }
    }

    private BigDecimal calcularMontoMinimoReserva(BigDecimal base) {
        return ventaCalculator.calcularMontoMinimoReserva(base, porcentajeMinimoReserva);
    }

    private BigDecimal porcentajeMinimoReservaEscalaHumana() {
        return ventaCalculator.porcentajeMinimoReservaEscalaHumana(porcentajeMinimoReserva);
    }

    private CotizacionConversionDTO resolverConversionVehiculoAVenta(VentaDTO dto, Vehiculo vehiculo, Moneda monedaBaseVenta) {
        return ventaCalculator.resolverConversionVehiculoAVenta(dto, vehiculo, monedaBaseVenta);
    }

    private void recalcularMontosDesdeVehiculo(VentaDTO dto, BigDecimal importeConvertido) {
        ventaCalculator.recalcularMontosDesdeVehiculo(dto, importeConvertido);
    }

    private Moneda resolverMonedaBaseVenta() {
        return ventaCalculator.resolverMonedaBaseVenta(monedaBaseCodigo);
    }

    private BigDecimal calcularImporteBaseReserva(Venta venta) {
        return ventaCalculator.calcularImporteBaseReserva(venta);
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

    private void validarDisponibilidadInventarioParaVenta(VentaDTO dto, Vehiculo vehiculo) {
        Inventario inventario = inventarioRepository
            .findByVehiculoId(vehiculo.getId())
            .orElseThrow(() -> new BadRequestException("Inventario no encontrado para el vehiculo seleccionado"));
        normalizarReservaVencida(inventario);

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

    private void registrarCambioEstadoVentaSiCorresponde(Venta venta, EstadoVenta estadoAnterior, String accion, String detalle) {
        ventaHistorialService.registrarCambioEstadoVentaSiCorresponde(venta, estadoAnterior, accion, detalle, this::currentUserLogin);
    }

    private void registrarHistorialVenta(Venta venta, EstadoVenta estadoAnterior, EstadoVenta estadoNuevo, String accion, String detalle) {
        ventaHistorialService.registrarHistorialVenta(venta, estadoAnterior, estadoNuevo, accion, detalle, this::currentUserLogin);
    }

    private VentaHistorialDTO toHistorialDto(VentaHistorial historial) {
        VentaHistorialDTO dto = new VentaHistorialDTO();
        dto.setId(historial.getId());
        dto.setVentaId(historial.getVenta() != null ? historial.getVenta().getId() : null);
        dto.setFecha(historial.getFecha());
        dto.setEstadoAnterior(historial.getEstadoAnterior());
        dto.setEstadoNuevo(historial.getEstadoNuevo());
        dto.setAccion(historial.getAccion());
        dto.setDetalle(historial.getDetalle());
        dto.setUsuario(historial.getUsuario());
        return dto;
    }
}
