package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.Cotizacion;
import com.concesionaria.app.domain.EntidadFinanciera;
import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.enumeration.EstadoTasacionUsado;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.repository.EntidadFinancieraRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.TasacionUsadoRepository;
import com.concesionaria.app.repository.TipoComprobanteRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import com.concesionaria.app.service.CurrencyConversionService;
import com.concesionaria.app.service.PagoService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.ComprobanteService;
import com.concesionaria.app.service.MovimientoCajaService;
import com.concesionaria.app.service.ComprobantePlanAhorroService;
import com.concesionaria.app.service.dto.CotizacionConversionDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.PagoMapper;
import com.concesionaria.app.security.SecurityUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PagoServiceImpl implements PagoService {

    private static final Logger LOG = LoggerFactory.getLogger(PagoServiceImpl.class);
    private static final int ESCALA_MONETARIA = 2;
    private static final int ESCALA_COTIZACION = 8;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
    private static final String CODIGO_CONTADO = "CONTADO";
    private static final String CODIGO_TRANSFERENCIA = "TRANSFERENCIA";
    private static final String CODIGO_DEPOSITO = "DEPOSITO";
    private static final String CODIGO_CHEQUE = "CHEQUE";
    private static final String CODIGO_DEBITO = "DEBITO";
    private static final String CODIGO_CREDITO = "CREDITO";
    private static final String CODIGO_AJUSTE = "AJUSTE";
    private static final String CODIGO_BONIFICACION = "BONIFICACION";
    private static final String CODIGO_ENTREGA_USADO = "ENTREGA_USADO";
    private static final String CODIGO_TARJETA = "TARJETA";
    private static final String CODIGO_SENIA = "SENIA";
    private static final String TIPO_COMPROBANTE_REC = "REC";
    private static final String TIPO_COMPROBANTE_SEN = "SEN";

    private final PagoRepository pagoRepository;
    private final PagoMapper pagoMapper;
    private final VentaRepository ventaRepository;
    private final ReservaRepository reservaRepository;
    private final VentaService ventaService;
    private final MetodoPagoRepository metodoPagoRepository;
    private final MonedaRepository monedaRepository;
    private final EntidadFinancieraRepository entidadFinancieraRepository;
    private final TasacionUsadoRepository tasacionUsadoRepository;
    private final CurrencyConversionService currencyConversionService;
    private final ComprobanteService comprobanteService;
    private final TipoComprobanteRepository tipoComprobanteRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final MovimientoCajaService movimientoCajaService;
    private final ComprobantePlanAhorroService comprobantePlanAhorroService;
    private final CuotaPlanAhorroRepository cuotaPlanAhorroRepository;
    private final ContratoPlanAhorroRepository contratoPlanAhorroRepository;

    @Value("${app.negocio.reserva.porcentaje-minimo:0.10}")
    private BigDecimal porcentajeMinimoReserva = new BigDecimal("0.10");

    @Value("${app.negocio.moneda-base-codigo:ARS}")
    private String monedaBaseCodigo;

    @Autowired
    public PagoServiceImpl(
        PagoRepository pagoRepository,
        PagoMapper pagoMapper,
        VentaRepository ventaRepository,
        ReservaRepository reservaRepository,
        VentaService ventaService,
        MetodoPagoRepository metodoPagoRepository,
        MonedaRepository monedaRepository,
        EntidadFinancieraRepository entidadFinancieraRepository,
        TasacionUsadoRepository tasacionUsadoRepository,
        CurrencyConversionService currencyConversionService,
        ComprobanteService comprobanteService,
        TipoComprobanteRepository tipoComprobanteRepository,
        ComprobanteRepository comprobanteRepository,
        MovimientoCajaService movimientoCajaService,
        ComprobantePlanAhorroService comprobantePlanAhorroService,
        CuotaPlanAhorroRepository cuotaPlanAhorroRepository,
        ContratoPlanAhorroRepository contratoPlanAhorroRepository
    ) {
        this.pagoRepository = pagoRepository;
        this.pagoMapper = pagoMapper;
        this.ventaRepository = ventaRepository;
        this.reservaRepository = reservaRepository;
        this.ventaService = ventaService;
        this.metodoPagoRepository = metodoPagoRepository;
        this.monedaRepository = monedaRepository;
        this.entidadFinancieraRepository = entidadFinancieraRepository;
        this.tasacionUsadoRepository = tasacionUsadoRepository;
        this.currencyConversionService = currencyConversionService;
        this.comprobanteService = comprobanteService;
        this.tipoComprobanteRepository = tipoComprobanteRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.movimientoCajaService = movimientoCajaService;
        this.comprobantePlanAhorroService = comprobantePlanAhorroService;
        this.cuotaPlanAhorroRepository = cuotaPlanAhorroRepository;
        this.contratoPlanAhorroRepository = contratoPlanAhorroRepository;
    }

    // Constructor legacy para compatibilidad con tests que instancian manualmente el servicio.
    public PagoServiceImpl(
        PagoRepository pagoRepository,
        PagoMapper pagoMapper,
        VentaRepository ventaRepository,
        ReservaRepository reservaRepository,
        VentaService ventaService,
        MetodoPagoRepository metodoPagoRepository,
        MonedaRepository monedaRepository,
        EntidadFinancieraRepository entidadFinancieraRepository,
        TasacionUsadoRepository tasacionUsadoRepository,
        CurrencyConversionService currencyConversionService,
        ComprobanteService comprobanteService,
        TipoComprobanteRepository tipoComprobanteRepository,
        ComprobanteRepository comprobanteRepository,
        MovimientoCajaService movimientoCajaService
    ) {
        this(
            pagoRepository,
            pagoMapper,
            ventaRepository,
            reservaRepository,
            ventaService,
            metodoPagoRepository,
            monedaRepository,
            entidadFinancieraRepository,
            tasacionUsadoRepository,
            currencyConversionService,
            comprobanteService,
            tipoComprobanteRepository,
            comprobanteRepository,
            movimientoCajaService,
            new ComprobantePlanAhorroService() {
                @Override
                public com.concesionaria.app.service.dto.ComprobantePlanAhorroDTO emitirParaCuota(
                    com.concesionaria.app.domain.CuotaPlanAhorro cuota,
                    Pago pago
                ) {
                    return null;
                }

                @Override
                public Optional<com.concesionaria.app.service.dto.ComprobantePlanAhorroDTO> findOne(Long id) {
                    return Optional.empty();
                }

                @Override
                public List<com.concesionaria.app.service.dto.ComprobantePlanAhorroDTO> findByCuota(Long cuotaId) {
                    return List.of();
                }

                @Override
                public Optional<com.concesionaria.app.service.dto.ComprobantePdfResult> generarPdf(Long id) {
                    return Optional.empty();
                }

                @Override
                public com.concesionaria.app.service.dto.ComprobantePlanAhorroDTO anular(Long id, String motivo) {
                    return null;
                }

                @Override
                public void anularPorPago(Long pagoId, String motivo, String usuario, Instant fecha) {}

                @Override
                public void delete(Long id) {}
            },
            null,
            null
        );
    }

    @Override
    public PagoDTO save(PagoDTO pagoDTO) {
        Long ventaId = pagoDTO.getVenta() != null ? pagoDTO.getVenta().getId() : null;
        Long reservaId = pagoDTO.getReserva() != null ? pagoDTO.getReserva().getId() : null;
        if (ventaId != null && reservaId != null) {
            throw new BadRequestException("El pago debe asociarse a una venta o a una reserva, no a ambas");
        }
        if (ventaId != null) {
            return registrarPago(ventaId, pagoDTO);
        }
        if (reservaId != null) {
            return registrarPagoReserva(reservaId, pagoDTO);
        }
        throw new BadRequestException("Debe informar venta o reserva para registrar el pago");
    }

    @Override
    public PagoDTO update(PagoDTO pagoDTO) {
        throw new BadRequestException("Los pagos registrados no admiten edicion directa. Solo se permite registrar o anular.");
    }

    @Override
    public Optional<PagoDTO> partialUpdate(PagoDTO pagoDTO) {
        throw new BadRequestException("Los pagos registrados no admiten edicion parcial. Solo se permite registrar o anular.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDTO> findAll(Pageable pageable) {
        if (isAdmin()) {
            LOG.debug("Acceso administrativo a listado global de pagos");
            return pagoRepository.findAll(pageable).map(pagoMapper::toDto);
        }
        String login = currentUserLogin();
        return pagoRepository.findAllCurrentUser(login, pageable).map(pagoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoDTO> findOne(Long id) {
        validarAccesoPago(id);
        Optional<PagoDTO> result = pagoRepository.findById(id).map(pagoMapper::toDto);
        if (result.isEmpty()) {
            LOG.warn("Pago inexistente para id {}", id);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDTO> findByVentaId(Long ventaId) {
        if (isAdmin()) {
            return pagoRepository.findAllByVentaIdWithRelaciones(ventaId).stream().map(pagoMapper::toDto).toList();
        }
        return pagoRepository.findAllByVentaIdWithRelacionesForUser(ventaId, currentUserLogin()).stream().map(pagoMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDTO> findByReservaId(Long reservaId) {
        if (isAdmin()) {
            return pagoRepository.findAllByReservaIdWithRelaciones(reservaId).stream().map(pagoMapper::toDto).toList();
        }
        return pagoRepository.findAllByReservaIdWithRelacionesForUser(reservaId, currentUserLogin()).stream().map(pagoMapper::toDto).toList();
    }

    @Override
    public void delete(Long id) {
        throw new BadRequestException("No se permite borrar pagos. Debe anularse para mantener trazabilidad.");
    }

    @Override
    public PagoDTO registrarPago(Long ventaId, PagoDTO pagoDTO) {
        Instant ahora = Instant.now();
        validarContextoEntradaPago(pagoDTO);
        Venta venta = ventaRepository.findByIdForUpdate(ventaId).orElseThrow(() -> new BadRequestException("La venta no existe"));
        validarVentaEnMonedaBase(venta);
        if (venta.getEstado() == EstadoVenta.PAGADA) {
            throw new BadRequestException("La venta ya esta completamente pagada");
        }
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new BadRequestException("No se puede registrar pagos sobre una venta cancelada");
        }
        BigDecimal saldoVenta = venta.getSaldo() == null ? BigDecimal.ZERO : venta.getSaldo();
        if (saldoVenta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La venta ya esta saldada");
        }
        if (pagoDTO.getMonto() == null || pagoDTO.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto del pago es invalido");
        }

        MetodoPago metodoPago = pagoDTO.getMetodoPago() != null && pagoDTO.getMetodoPago().getId() != null
            ? metodoPagoRepository.findById(pagoDTO.getMetodoPago().getId()).orElseThrow(() -> new BadRequestException("El metodo de pago no existe"))
            : null;
        if (metodoPago == null) {
            throw new BadRequestException("Debe informar el metodo de pago");
        }
        validarMetodoPagoEspecial(metodoPago);
        validarDatosOperacionParaMetodo(pagoDTO, metodoPago);
        validarDatosAdministrativosPorMetodo(pagoDTO, metodoPago);
        EntidadFinanciera entidadFinanciera = resolverEntidadFinanciera(pagoDTO, metodoPago);
        if (esMetodoEntregaUsado(metodoPago)) {
            return registrarPagoEntregaUsado(venta, pagoDTO, metodoPago);
        }
        if (pagoDTO.getTasacionUsadoId() != null) {
            throw new BadRequestException("La tasacion_usado_id solo aplica para metodo ENTREGA_USADO");
        }
        Moneda monedaVenta = venta.getMoneda();
        if (monedaVenta == null || monedaVenta.getId() == null) {
            throw new BadRequestException("La venta no tiene moneda configurada");
        }
        Instant fechaPago = pagoDTO.getFecha() != null ? pagoDTO.getFecha() : ahora;
        Moneda monedaPago = resolverMonedaPago(pagoDTO, monedaVenta);
        CotizacionConversionDTO conversion = currencyConversionService.convertir(
            pagoDTO.getMonto(),
            monedaPago.getId(),
            monedaVenta.getId(),
            fechaPago
        );
        validarConversion(conversion);
        BigDecimal cotizacionUsada = normalizarCotizacion(conversion.getCotizacionAplicada());
        BigDecimal montoAplicadoVenta = normalizarMoneda(conversion.getMontoConvertido());
        if (montoAplicadoVenta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto aplicado en moneda de venta debe ser mayor a 0");
        }
        if (saldoVenta.compareTo(montoAplicadoVenta) < 0) {
            throw new BadRequestException("El monto convertido del pago excede el saldo pendiente de la venta");
        }

        BigDecimal totalPagadoActual = totalPagadoRegistrado(ventaId);
        BigDecimal totalPagadoProyectado = normalizarMoneda(totalPagadoActual.add(montoAplicadoVenta));
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
        pago.setReserva(null);
        if (metodoPago != null) {
            pago.setMetodoPago(metodoPago);
        }
        if (pago.getTipoMovimiento() == null) {
            pago.setTipoMovimiento(TipoMovimientoPago.PAGO_RECIBIDO);
        }
        pago.setMoneda(monedaPago);
        pago.setEntidadFinanciera(entidadFinanciera);
        pago.setCotizacionUsada(cotizacionUsada);
        pago.setMontoAplicadoVenta(montoAplicadoVenta);
        pago.setFechaCotizacionUsada(conversion.getFechaCotizacionUsada());
        pago.setCotizacionRef(referenciaCotizacion(conversion.getCotizacionOrigenId()));
        if (pago.getUsuarioRegistro() == null || pago.getUsuarioRegistro().isBlank()) {
            pago.setUsuarioRegistro(currentUserLogin());
        }
        pago.setFecha(fechaPago);
        completarDatosOperacion(pago, metodoPago, null);
        completarBancoEntidadLegacy(pago, entidadFinanciera);
        pago.setCreatedDate(ahora);
        pago.setLastModifiedDate(ahora);
        pago.setEstado(EstadoPago.REGISTRADO);
        pago.setTasacionUsado(null);
        normalizarCamposTextoPago(pago);
        LOG.info(
            "Registrando pago ventaId={} montoOriginal={} monedaPagoId={} cotizacionAplicada={} montoAplicadoVenta={}",
            ventaId,
            pagoDTO.getMonto(),
            monedaPago.getId(),
            cotizacionUsada,
            montoAplicadoVenta
        );
        pago = pagoRepository.save(pago);
        movimientoCajaService.registrarDesdePago(pago, TipoMovimientoCaja.INGRESO, EstadoPago.REGISTRADO, true);
        emitirComprobanteDePagoSiCorresponde(pago, metodoPago);

        recalcularVentaEInventario(venta);
        return pagoMapper.toDto(pago);
    }

    @Override
    public PagoDTO registrarPagoReserva(Long reservaId, PagoDTO pagoDTO) {
        Instant ahora = Instant.now();
        validarContextoEntradaPago(pagoDTO);
        Reserva reserva = reservaRepository.findById(reservaId).orElseThrow(() -> new BadRequestException("La reserva no existe"));
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new BadRequestException("Solo se pueden registrar pagos en reservas ACTIVAS");
        }
        if (pagoDTO.getMonto() == null || pagoDTO.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto del pago es invalido");
        }

        MetodoPago metodoPago = pagoDTO.getMetodoPago() != null && pagoDTO.getMetodoPago().getId() != null
            ? metodoPagoRepository.findById(pagoDTO.getMetodoPago().getId()).orElseThrow(() -> new BadRequestException("El metodo de pago no existe"))
            : null;
        if (metodoPago == null) {
            throw new BadRequestException("Debe informar el metodo de pago");
        }
        validarMetodoPagoEspecial(metodoPago);
        validarDatosOperacionParaMetodo(pagoDTO, metodoPago);
        validarDatosAdministrativosPorMetodo(pagoDTO, metodoPago);
        EntidadFinanciera entidadFinanciera = resolverEntidadFinanciera(pagoDTO, metodoPago);
        if (esMetodoEntregaUsado(metodoPago)) {
            throw new BadRequestException("ENTREGA_USADO solo puede registrarse sobre una venta");
        }
        if (pagoDTO.getTasacionUsadoId() != null) {
            throw new BadRequestException("La tasacion_usado_id solo aplica para pagos de venta");
        }

        Pago pago = pagoMapper.toEntity(pagoDTO);
        pago.setVenta(null);
        pago.setReserva(reserva);
        if (metodoPago != null) {
            pago.setMetodoPago(metodoPago);
        }
        if (pago.getTipoMovimiento() == null) {
            pago.setTipoMovimiento(TipoMovimientoPago.ANTICIPO);
        }
        Instant fechaPago = pagoDTO.getFecha() != null ? pagoDTO.getFecha() : ahora;
        Moneda monedaPago = resolverMonedaPago(pagoDTO, reserva.getMoneda());
        Moneda monedaReserva = reserva.getMoneda() != null ? reserva.getMoneda() : monedaPago;
        if (monedaReserva == null || monedaReserva.getId() == null) {
            throw new BadRequestException("No se pudo resolver la moneda de la reserva para aplicar el pago");
        }
        pago.setMoneda(monedaPago);
        pago.setEntidadFinanciera(entidadFinanciera);
        CotizacionConversionDTO conversion = currencyConversionService.convertir(
            pagoDTO.getMonto(),
            monedaPago.getId(),
            monedaReserva.getId(),
            fechaPago
        );
        validarConversion(conversion);
        pago.setCotizacionUsada(normalizarCotizacion(conversion.getCotizacionAplicada()));
        pago.setMontoAplicadoVenta(normalizarMoneda(conversion.getMontoConvertido()));
        pago.setFechaCotizacionUsada(conversion.getFechaCotizacionUsada());
        pago.setCotizacionRef(referenciaCotizacion(conversion.getCotizacionOrigenId()));
        if (pago.getUsuarioRegistro() == null || pago.getUsuarioRegistro().isBlank()) {
            pago.setUsuarioRegistro(currentUserLogin());
        }
        pago.setFecha(fechaPago);
        completarDatosOperacion(pago, metodoPago, null);
        completarBancoEntidadLegacy(pago, entidadFinanciera);
        pago.setCreatedDate(ahora);
        pago.setLastModifiedDate(ahora);
        pago.setEstado(EstadoPago.REGISTRADO);
        pago.setTasacionUsado(null);
        normalizarCamposTextoPago(pago);
        LOG.info(
            "Registrando pago reservaId={} montoOriginal={} monedaPagoId={} cotizacionAplicada={} montoAplicadoReserva={}",
            reservaId,
            pagoDTO.getMonto(),
            monedaPago.getId(),
            pago.getCotizacionUsada(),
            pago.getMontoAplicadoVenta()
        );
        Pago pagoGuardado = pagoRepository.save(pago);
        movimientoCajaService.registrarDesdePago(pagoGuardado, TipoMovimientoCaja.INGRESO, EstadoPago.REGISTRADO, true);
        emitirComprobanteDePagoSiCorresponde(pagoGuardado, metodoPago);

        recalcularMontoSeniaReserva(reserva);
        return pagoMapper.toDto(pagoGuardado);
    }

    @Override
    public PagoDTO anularPago(Long pagoId, String motivo) {
        Instant ahora = Instant.now();
        Pago pago = pagoRepository.findById(pagoId).orElseThrow(() -> new BadRequestException("El pago no existe"));
        if (pago.getEstado() == EstadoPago.ANULADO) {
            throw new BadRequestException("El pago ya se encuentra anulado");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Debe informar un motivo para anular el pago");
        }
        if (pago.getTipoMovimiento() == TipoMovimientoPago.ENTREGA_USADO && pago.getTasacionUsado() != null && pago.getTasacionUsado().getInventarioGenerado() != null) {
            throw new BadRequestException("No se puede anular la entrega de usado porque ya genero inventario");
        }
        Venta venta = pago.getVenta();
        Reserva reservaAsociada = pago.getReserva();
        CuotaPlanAhorro cuotaPlan = cuotaPlanAhorroRepository == null ? null : cuotaPlanAhorroRepository.findFirstByPagoId(pagoId).orElse(null);
        if ((venta == null || venta.getId() == null) && (reservaAsociada == null || reservaAsociada.getId() == null)) {
            if (cuotaPlan == null) {
                throw new BadRequestException("El pago no tiene una operacion asociada");
            }
        }
        pago.setEstado(EstadoPago.ANULADO);
        if (pago.getTipoMovimiento() == TipoMovimientoPago.PAGO_RECIBIDO || pago.getTipoMovimiento() == TipoMovimientoPago.ANTICIPO) {
            pago.setTipoMovimiento(TipoMovimientoPago.ANULACION);
        }
        if (pago.getTipoMovimiento() == TipoMovimientoPago.ENTREGA_USADO && venta != null && venta.getTasacionUsado() != null) {
            venta.setTasacionUsado(null);
            venta.setLastModifiedDate(ahora);
            ventaRepository.save(venta);
        }
        String usuarioAnulacion = currentUserLogin();
        Instant fechaAnulacion = ahora;
        pago.setMotivoAnulacion(motivo.trim());
        pago.setUsuarioAnulacion(usuarioAnulacion);
        pago.setFechaAnulacion(fechaAnulacion);
        pago.setLastModifiedDate(ahora);
        normalizarCamposTextoPago(pago);
        LOG.info("Anulando pago pagoId={} ventaId={} reservaId={}", pagoId, venta != null ? venta.getId() : null, reservaAsociada != null ? reservaAsociada.getId() : null);
        Pago pagoActualizado = pagoRepository.save(pago);
        boolean monetario = pagoActualizado.getTipoMovimiento() != TipoMovimientoPago.ENTREGA_USADO;
        movimientoCajaService.registrarDesdePago(
            pagoActualizado,
            monetario ? TipoMovimientoCaja.REVERSO : TipoMovimientoCaja.INFORMATIVO,
            EstadoPago.ANULADO,
            monetario
        );
        anularComprobantesAsociadosAPago(pagoActualizado, motivo, usuarioAnulacion, fechaAnulacion);
        comprobantePlanAhorroService.anularPorPago(pagoActualizado.getId(), motivo, usuarioAnulacion, fechaAnulacion);

        if (cuotaPlan != null && cuotaPlanAhorroRepository != null) {
            cuotaPlan.setEstado(EstadoCuotaPlanAhorro.PENDIENTE);
            cuotaPlan.setFechaPago(null);
            cuotaPlan.setPago(null);
            cuotaPlanAhorroRepository.save(cuotaPlan);
            recalcContratoPlan(cuotaPlan.getContrato());
        }

        if (venta != null && venta.getId() != null) {
            recalcularVentaEInventario(venta);
        } else if (reservaAsociada != null && reservaAsociada.getId() != null) {
            Reserva reserva = reservaRepository.findById(reservaAsociada.getId()).orElse(null);
            if (reserva != null) {
                recalcularMontoSeniaReserva(reserva);
            }
        }
        return pagoMapper.toDto(pagoActualizado);
    }

    private void recalcContratoPlan(ContratoPlanAhorro contrato) {
        if (contrato == null || contrato.getId() == null || cuotaPlanAhorroRepository == null || contratoPlanAhorroRepository == null) {
            return;
        }
        List<CuotaPlanAhorro> cuotas = cuotaPlanAhorroRepository.findAllByContratoIdOrderByNumeroCuotaAsc(contrato.getId());
        long pagadas = cuotas.stream().filter(c -> c.getEstado() == EstadoCuotaPlanAhorro.PAGADA).count();
        BigDecimal saldo = cuotas
            .stream()
            .filter(c -> c.getEstado() == EstadoCuotaPlanAhorro.PENDIENTE || c.getEstado() == EstadoCuotaPlanAhorro.VENCIDA)
            .map(CuotaPlanAhorro::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, REDONDEO);
        contrato.setCuotasPagadas((int) pagadas);
        contrato.setSaldoPendiente(saldo);
        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            contrato.setEstado(EstadoContratoPlanAhorro.FINALIZADO);
        } else if (contrato.getEstado() == EstadoContratoPlanAhorro.FINALIZADO) {
            contrato.setEstado(EstadoContratoPlanAhorro.ACTIVO);
        }
        contratoPlanAhorroRepository.save(contrato);
    }

    private void recalcularVentaEInventario(Venta venta) {
        Instant ahora = Instant.now();
        BigDecimal totalPagado = totalPagadoRegistrado(venta.getId());
        BigDecimal total = venta.getTotal() == null ? BigDecimal.ZERO : venta.getTotal();
        BigDecimal saldo = normalizarMoneda(total.subtract(totalPagado));
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Los pagos registrados superan el total de la venta");
        }

        venta.setTotalPagado(totalPagado);
        venta.setSaldo(saldo);
        venta.setLastModifiedDate(ahora);
        ventaRepository.save(venta);

        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            ventaService.confirmarVenta(venta.getId());
        } else {
            BigDecimal minimoReserva = calcularMontoMinimoReserva(venta);
            if (totalPagado.compareTo(minimoReserva) >= 0 && minimoReserva.compareTo(BigDecimal.ZERO) > 0) {
                venta.setEstado(EstadoVenta.RESERVADA);
            } else {
                venta.setEstado(EstadoVenta.PENDIENTE);
            }
            venta.setLastModifiedDate(ahora);
            ventaRepository.save(venta);
            ventaService.sincronizarInventarioConVenta(venta.getId());
        }
    }

    private BigDecimal totalPagadoRegistrado(Long ventaId) {
        BigDecimal total = pagoRepository.sumMontoByVentaId(ventaId);
        if (total == null) {
            return normalizarMoneda(BigDecimal.ZERO);
        }
        return normalizarMoneda(total);
    }

    private void recalcularMontoSeniaReserva(Reserva reserva) {
        if (reserva.getId() == null) {
            return;
        }
        Instant ahora = Instant.now();
        BigDecimal total = pagoRepository.sumMontoByReservaId(reserva.getId());
        reserva.setMontoSenia(total == null ? normalizarMoneda(BigDecimal.ZERO) : normalizarMoneda(total));
        reserva.setLastModifiedDate(ahora);
        reservaRepository.save(reserva);
    }

    private BigDecimal calcularMontoMinimoReserva(Venta venta) {
        BigDecimal base = venta.getImporteNeto() == null ? BigDecimal.ZERO : venta.getImporteNeto();
        return normalizarMoneda(base.multiply(porcentajeMinimoReserva));
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }

    private boolean isAdmin() {
        return SecurityUtils.hasCurrentUserAnyOfAuthorities("ROLE_ADMIN");
    }

    private void validarAccesoPago(Long pagoId) {
        if (pagoId == null || isAdmin()) {
            return;
        }
        String login = currentUserLogin();
        boolean allowed = pagoRepository.existsAccessibleByIdForUser(pagoId, login);
        if (!allowed) {
            LOG.warn("Acceso denegado a pago {} para usuario {}", pagoId, login);
            throw new AccessDeniedException("No tienes permisos para acceder a este pago");
        }
    }

    private Moneda resolverMonedaPago(PagoDTO pagoDTO, Moneda monedaVenta) {
        if (pagoDTO.getMoneda() == null || pagoDTO.getMoneda().getId() == null) {
            if (monedaVenta == null || monedaVenta.getId() == null) {
                throw new BadRequestException("Debe informar la moneda del pago");
            }
            return monedaVenta;
        }
        return monedaRepository.findById(pagoDTO.getMoneda().getId()).orElseThrow(() -> new BadRequestException("La moneda del pago no existe"));
    }

    private void validarMetodoPagoEspecial(MetodoPago metodoPago) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        if (codigoMetodo == null) {
            return;
        }
        if (
            (CODIGO_TRANSFERENCIA.equals(codigoMetodo) ||
                CODIGO_TARJETA.equals(codigoMetodo) ||
                CODIGO_DEBITO.equals(codigoMetodo) ||
                CODIGO_CREDITO.equals(codigoMetodo)) &&
            (metodoPago.getRequiereReferencia() == null || !metodoPago.getRequiereReferencia())
        ) {
            LOG.warn("Metodo de pago {} deberia requerir referencia para trazabilidad", codigoMetodo);
        }
    }

    private void validarDatosOperacionParaMetodo(PagoDTO pagoDTO, MetodoPago metodoPago) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        if (codigoMetodo == null) {
            return;
        }
        if (CODIGO_CONTADO.equals(codigoMetodo)) {
            return;
        }
        // Referencia y numero de operacion se autogeneran en backend cuando no son informados.
    }

    private void validarDatosAdministrativosPorMetodo(PagoDTO pagoDTO, MetodoPago metodoPago) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        if (codigoMetodo == null) {
            return;
        }
        boolean bancoVacio = pagoDTO.getBancoEntidad() == null || pagoDTO.getBancoEntidad().isBlank();
        boolean entidadVacia = pagoDTO.getEntidadFinanciera() == null || pagoDTO.getEntidadFinanciera().getId() == null;
        boolean sinIdentificacionEntidad = bancoVacio && entidadVacia;
        boolean comprobanteVacio = pagoDTO.getComprobanteExterno() == null || pagoDTO.getComprobanteExterno().isBlank();
        boolean observacionesVacio = pagoDTO.getObservaciones() == null || pagoDTO.getObservaciones().isBlank();

        switch (codigoMetodo) {
            case CODIGO_TRANSFERENCIA:
            case CODIGO_DEPOSITO:
                if (sinIdentificacionEntidad) {
                    throw new BadRequestException("Para " + codigoMetodo + " debe informar entidad financiera");
                }
                break;
            case CODIGO_CHEQUE:
                if (sinIdentificacionEntidad) {
                    throw new BadRequestException("Para CHEQUE debe informar entidad financiera");
                }
                if (comprobanteVacio) {
                    throw new BadRequestException("Para CHEQUE debe informar numero de cheque o comprobante externo");
                }
                break;
            case CODIGO_AJUSTE:
            case CODIGO_BONIFICACION:
                if (observacionesVacio) {
                    throw new BadRequestException("Para " + codigoMetodo + " debe informar observaciones");
                }
                break;
            default:
                break;
        }
    }

    private PagoDTO registrarPagoEntregaUsado(Venta venta, PagoDTO pagoDTO, MetodoPago metodoPago) {
        Instant ahora = Instant.now();
        if (pagoDTO.getTasacionUsadoId() == null) {
            throw new BadRequestException("Debe informar tasacion_usado_id para registrar ENTREGA_USADO");
        }
        TasacionUsado tasacion = tasacionUsadoRepository
            .findById(pagoDTO.getTasacionUsadoId())
            .orElseThrow(() -> new BadRequestException("La tasacion indicada no existe"));
        validarTasacionParaEntrega(venta, tasacion);

        BigDecimal saldoVenta = venta.getSaldo() == null ? BigDecimal.ZERO : venta.getSaldo();
        BigDecimal montoTasado = normalizarMoneda(tasacion.getMontoTasacion());
        if (saldoVenta.compareTo(montoTasado) < 0) {
            throw new BadRequestException("El monto tasado excede el saldo pendiente de la venta");
        }
        BigDecimal totalPagadoActual = totalPagadoRegistrado(venta.getId());
        BigDecimal totalPagadoProyectado = normalizarMoneda(totalPagadoActual.add(montoTasado));
        BigDecimal montoMinimoReserva = calcularMontoMinimoReserva(venta);
        if (totalPagadoProyectado.compareTo(BigDecimal.ZERO) > 0 && totalPagadoProyectado.compareTo(montoMinimoReserva) < 0) {
            throw new BadRequestException(
                "La venta requiere una sena minima del " +
                porcentajeMinimoReserva.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() +
                "% para registrar pagos"
            );
        }

        Moneda monedaVenta = venta.getMoneda();
        Pago pago = pagoMapper.toEntity(pagoDTO);
        pago.setVenta(venta);
        pago.setReserva(null);
        pago.setMetodoPago(metodoPago);
        pago.setMoneda(monedaVenta);
        pago.setEntidadFinanciera(null);
        pago.setMonto(montoTasado);
        pago.setTipoMovimiento(TipoMovimientoPago.ENTREGA_USADO);
        pago.setCotizacionUsada(normalizarCotizacion(BigDecimal.ONE));
        pago.setMontoAplicadoVenta(montoTasado);
        pago.setFechaCotizacionUsada(pagoDTO.getFecha() != null ? pagoDTO.getFecha() : tasacion.getFechaTasacion());
        pago.setCotizacionRef(null);
        pago.setTasacionUsado(tasacion);
        if (pago.getUsuarioRegistro() == null || pago.getUsuarioRegistro().isBlank()) {
            pago.setUsuarioRegistro(currentUserLogin());
        }
        if (pago.getFecha() == null) {
            pago.setFecha(ahora);
        }
        completarDatosOperacion(pago, metodoPago, tasacion);
        pago.setCreatedDate(ahora);
        pago.setLastModifiedDate(ahora);
        pago.setEstado(EstadoPago.REGISTRADO);
        normalizarCamposTextoPago(pago);

        venta.setTasacionUsado(tasacion);
        venta.setLastModifiedDate(ahora);
        ventaRepository.save(venta);

        LOG.info(
            "Registrando ENTREGA_USADO ventaId={} tasacionId={} montoAplicadoVenta={}",
            venta.getId(),
            tasacion.getId(),
            montoTasado
        );
        Pago pagoGuardado = pagoRepository.save(pago);
        movimientoCajaService.registrarDesdePago(pagoGuardado, TipoMovimientoCaja.INFORMATIVO, EstadoPago.REGISTRADO, false);
        emitirComprobanteDePagoSiCorresponde(pagoGuardado, metodoPago);
        recalcularVentaEInventario(venta);
        return pagoMapper.toDto(pagoGuardado);
    }

    private void emitirComprobanteDePagoSiCorresponde(Pago pago, MetodoPago metodoPago) {
        if (pago == null || pago.getId() == null || pago.getVenta() == null || pago.getVenta().getId() == null) {
            return;
        }
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        if (codigoMetodo == null) {
            return;
        }
        boolean emitible = CODIGO_CONTADO.equals(codigoMetodo) ||
            CODIGO_TRANSFERENCIA.equals(codigoMetodo) ||
            CODIGO_DEPOSITO.equals(codigoMetodo) ||
            CODIGO_CHEQUE.equals(codigoMetodo) ||
            CODIGO_TARJETA.equals(codigoMetodo) ||
            CODIGO_DEBITO.equals(codigoMetodo) ||
            CODIGO_CREDITO.equals(codigoMetodo) ||
            CODIGO_ENTREGA_USADO.equals(codigoMetodo) ||
            CODIGO_SENIA.equals(codigoMetodo);
        if (!emitible) {
            return;
        }

        String codigoTipo = CODIGO_SENIA.equals(codigoMetodo) ? TIPO_COMPROBANTE_SEN : TIPO_COMPROBANTE_REC;
        TipoComprobante tipo = tipoComprobanteRepository.findByCodigoIgnoreCase(codigoTipo).orElse(null);
        if (tipo == null && !TIPO_COMPROBANTE_REC.equals(codigoTipo)) {
            tipo = tipoComprobanteRepository.findByCodigoIgnoreCase(TIPO_COMPROBANTE_REC).orElse(null);
        }
        if (tipo == null || tipo.getId() == null) {
            LOG.warn("No se emitio comprobante de pago para pagoId={} por falta de tipo comprobante REC/SEN", pago.getId());
            return;
        }
        if (comprobanteRepository.existsByPagoIdAndTipoComprobanteIdAndEstado(pago.getId(), tipo.getId(), EstadoComprobante.EMITIDO)) {
            return;
        }
        comprobanteService.emitirComprobantePago(pago.getId(), tipo.getId());
    }

    private void anularComprobantesAsociadosAPago(Pago pago, String motivo, String usuario, Instant fecha) {
        if (pago == null || pago.getId() == null) {
            return;
        }
        List<Comprobante> comprobantes = comprobanteRepository.findAllByPagoIdOrderByFechaEmisionDescIdDesc(pago.getId());
        for (Comprobante comprobante : comprobantes) {
            if (comprobante.getEstado() == EstadoComprobante.ANULADO) {
                continue;
            }
            comprobante.setEstado(EstadoComprobante.ANULADO);
            comprobante.setMotivoAnulacion("Anulado por anulacion de pago: " + motivo);
            comprobante.setUsuarioAnulacion(usuario);
            comprobante.setFechaAnulacion(fecha);
            comprobante.setLastModifiedBy(usuario);
            comprobante.setLastModifiedDate(fecha);
            comprobanteRepository.save(comprobante);
        }
    }

    private void validarTasacionParaEntrega(Venta venta, TasacionUsado tasacion) {
        if (tasacion.getCliente() == null || tasacion.getCliente().getId() == null || venta.getCliente() == null || venta.getCliente().getId() == null) {
            throw new BadRequestException("No se pudo validar el cliente de la tasacion");
        }
        if (!venta.getCliente().getId().equals(tasacion.getCliente().getId())) {
            throw new BadRequestException("La tasacion no pertenece al mismo cliente de la venta");
        }
        if (tasacion.getEstado() != EstadoTasacionUsado.ACEPTADA) {
            throw new BadRequestException("La tasacion debe estar aceptada para aplicarse como pago");
        }
        if (tasacion.getMontoTasacion() == null || tasacion.getMontoTasacion().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La tasacion tiene un monto invalido");
        }
        if (tasacion.getMoneda() == null || tasacion.getMoneda().getId() == null || tasacion.getMoneda().getCodigo() == null) {
            throw new BadRequestException("La tasacion no tiene moneda configurada");
        }
        boolean tasacionEnMonedaBase = tasacion.getMoneda().getCodigo().equalsIgnoreCase(monedaBaseCodigo);
        boolean tasacionEnMonedaVenta = venta.getMoneda() != null && venta.getMoneda().getId() != null && venta.getMoneda().getId().equals(tasacion.getMoneda().getId());
        if (!tasacionEnMonedaBase || !tasacionEnMonedaVenta) {
            throw new BadRequestException("La tasacion debe estar en moneda base configurada: " + monedaBaseCodigo + " para aplicarse a la venta");
        }
        if (tasacion.getInventarioGenerado() != null) {
            throw new BadRequestException("La tasacion ya genero inventario y no puede reutilizarse");
        }
        boolean disponibleParaCliente = tasacionUsadoRepository.existsAceptadaDisponibleByIdAndClienteId(tasacion.getId(), venta.getCliente().getId());
        boolean esTasacionYaAsociadaAMismaVenta = venta.getTasacionUsado() != null && venta.getTasacionUsado().getId() != null && venta.getTasacionUsado().getId().equals(tasacion.getId());
        if (!disponibleParaCliente && !esTasacionYaAsociadaAMismaVenta) {
            throw new BadRequestException("La tasacion indicada no esta disponible para el cliente de la venta");
        }
        if (ventaRepository.existsByTasacionUsadoIdAndIdNot(tasacion.getId(), venta.getId())) {
            throw new BadRequestException("La tasacion ya fue aplicada a otra venta");
        }
        if (venta.getTasacionUsado() != null && !venta.getTasacionUsado().getId().equals(tasacion.getId())) {
            throw new BadRequestException("La venta ya tiene una tasacion de usado asociada");
        }
    }

    private boolean esMetodoEntregaUsado(MetodoPago metodoPago) {
        return metodoPago != null && CODIGO_ENTREGA_USADO.equals(normalizarCodigoMetodo(metodoPago));
    }

    private void validarContextoEntradaPago(PagoDTO pagoDTO) {
        if (pagoDTO == null) {
            throw new BadRequestException("Debe informar los datos del pago");
        }
        if (pagoDTO.getMonto() == null || pagoDTO.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto del pago es invalido");
        }
        if (pagoDTO.getFecha() != null && pagoDTO.getFecha().isAfter(Instant.now().plusSeconds(300))) {
            throw new BadRequestException("La fecha del pago no puede estar en el futuro");
        }
    }

    private void normalizarCamposTextoPago(Pago pago) {
        pago.setReferencia(normalizarTexto(pago.getReferencia(), 100));
        pago.setNumeroOperacion(normalizarTexto(pago.getNumeroOperacion(), 100));
        pago.setComprobanteExterno(normalizarTexto(pago.getComprobanteExterno(), 100));
        pago.setBancoEntidad(normalizarTexto(pago.getBancoEntidad(), 100));
        pago.setObservaciones(normalizarTexto(pago.getObservaciones(), 500));
        pago.setUsuarioRegistro(normalizarTexto(pago.getUsuarioRegistro(), 50));
        pago.setMotivoAnulacion(normalizarTexto(pago.getMotivoAnulacion(), 500));
        pago.setUsuarioAnulacion(normalizarTexto(pago.getUsuarioAnulacion(), 50));
    }

    private void completarDatosOperacion(Pago pago, MetodoPago metodoPago, TasacionUsado tasacionUsado) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        if (codigoMetodo == null) {
            codigoMetodo = "PAGO";
        }
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);

        if (CODIGO_CONTADO.equals(codigoMetodo)) {
            pago.setReferencia(null);
            pago.setNumeroOperacion(null);
            return;
        }

        if (CODIGO_ENTREGA_USADO.equals(codigoMetodo)) {
            String tasacionRef = tasacionUsado != null && tasacionUsado.getId() != null ? String.valueOf(tasacionUsado.getId()) : token;
            if (pago.getReferencia() == null || pago.getReferencia().isBlank()) {
                pago.setReferencia("TAS-" + tasacionRef);
            }
            if (pago.getNumeroOperacion() == null || pago.getNumeroOperacion().isBlank()) {
                pago.setNumeroOperacion("ENT-" + timestamp.substring(Math.max(0, timestamp.length() - 8)));
            }
            return;
        }

        if (pago.getReferencia() == null || pago.getReferencia().isBlank()) {
            pago.setReferencia(codigoMetodo + "-REF-" + token);
        }
        if (pago.getNumeroOperacion() == null || pago.getNumeroOperacion().isBlank()) {
            pago.setNumeroOperacion(codigoMetodo + "-OP-" + timestamp.substring(Math.max(0, timestamp.length() - 8)));
        }
    }

    private EntidadFinanciera resolverEntidadFinanciera(PagoDTO pagoDTO, MetodoPago metodoPago) {
        String codigoMetodo = normalizarCodigoMetodo(metodoPago);
        boolean requiereEntidad = CODIGO_TRANSFERENCIA.equals(codigoMetodo) ||
            CODIGO_DEPOSITO.equals(codigoMetodo) ||
            CODIGO_CHEQUE.equals(codigoMetodo) ||
            CODIGO_TARJETA.equals(codigoMetodo) ||
            CODIGO_DEBITO.equals(codigoMetodo) ||
            CODIGO_CREDITO.equals(codigoMetodo);

        Long entidadId = pagoDTO.getEntidadFinanciera() != null ? pagoDTO.getEntidadFinanciera().getId() : null;
        if (!requiereEntidad) {
            return entidadId == null ? null : entidadFinancieraRepository.findById(entidadId).orElse(null);
        }
        if (entidadId != null) {
            return entidadFinancieraRepository
                .findById(entidadId)
                .filter(EntidadFinanciera::getActiva)
                .orElseThrow(() -> new BadRequestException("La entidad financiera no existe o no esta activa"));
        }
        if (pagoDTO.getBancoEntidad() != null && !pagoDTO.getBancoEntidad().isBlank()) {
            return null;
        }
        throw new BadRequestException("Para " + codigoMetodo + " debe informar entidad financiera");
    }

    private void completarBancoEntidadLegacy(Pago pago, EntidadFinanciera entidadFinanciera) {
        if (entidadFinanciera == null) {
            return;
        }
        if (pago.getBancoEntidad() == null || pago.getBancoEntidad().isBlank()) {
            pago.setBancoEntidad(entidadFinanciera.getNombre());
        }
    }

    private String normalizarTexto(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > max) {
            throw new BadRequestException("Un campo de texto excede el maximo permitido de " + max + " caracteres");
        }
        return trimmed;
    }

    private BigDecimal normalizarMoneda(BigDecimal valor) {
        return valor.setScale(ESCALA_MONETARIA, REDONDEO);
    }

    private BigDecimal normalizarCotizacion(BigDecimal valor) {
        return valor.setScale(ESCALA_COTIZACION, REDONDEO);
    }

    private Cotizacion referenciaCotizacion(Long cotizacionId) {
        return cotizacionId == null ? null : new Cotizacion().id(cotizacionId);
    }

    private void validarConversion(CotizacionConversionDTO conversion) {
        if (conversion.getCotizacionAplicada() == null || conversion.getCotizacionAplicada().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Cotización inválida para aplicar el pago");
        }
        if (conversion.getMontoConvertido() == null || conversion.getMontoConvertido().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Monto convertido inválido");
        }
    }

    private void validarVentaEnMonedaBase(Venta venta) {
        Moneda monedaVenta = venta.getMoneda();
        if (monedaVenta == null) {
            throw new BadRequestException(mensajeVentaMonedaBaseConfigurada());
        }
        Moneda monedaBase = monedaRepository.findByCodigoIgnoreCase(monedaBaseCodigo).orElse(null);
        boolean esMonedaBase = false;
        if (monedaBase != null && monedaBase.getId() != null && monedaVenta.getId() != null) {
            esMonedaBase = monedaBase.getId().equals(monedaVenta.getId());
        }
        if (!esMonedaBase && monedaVenta.getCodigo() != null) {
            esMonedaBase = monedaVenta.getCodigo().equalsIgnoreCase(monedaBaseCodigo);
        }
        if (!esMonedaBase) {
            throw new BadRequestException(mensajeVentaMonedaBaseConfigurada());
        }
    }

    private String normalizarCodigoMetodo(MetodoPago metodoPago) {
        if (metodoPago == null || metodoPago.getCodigo() == null) {
            return null;
        }
        return metodoPago.getCodigo().trim().toUpperCase(Locale.ROOT);
    }

    private String mensajeVentaMonedaBaseConfigurada() {
        return "La venta debe estar registrada en moneda base configurada: " + monedaBaseCodigo;
    }
}
