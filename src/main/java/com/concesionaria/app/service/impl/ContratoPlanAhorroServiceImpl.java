package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.ComprobantePlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.ComprobantePlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.PlanAhorroRepository;
import com.concesionaria.app.repository.UserRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.ContratoPlanAhorroService;
import com.concesionaria.app.service.ComprobantePlanAhorroService;
import com.concesionaria.app.service.MovimientoCajaService;
import com.concesionaria.app.service.dto.ContratoPlanAhorroDTO;
import com.concesionaria.app.service.dto.CuotaPlanAhorroDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ContratoPlanAhorroMapper;
import com.concesionaria.app.service.mapper.CuotaPlanAhorroMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContratoPlanAhorroServiceImpl implements ContratoPlanAhorroService {

    private static final Logger LOG = LoggerFactory.getLogger(ContratoPlanAhorroServiceImpl.class);
    private static final String CODIGO_CONTADO = "CONTADO";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final ContratoPlanAhorroRepository contratoRepository;
    private final CuotaPlanAhorroRepository cuotaRepository;
    private final PlanAhorroRepository planRepository;
    private final ClienteRepository clienteRepository;
    private final UserRepository userRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PagoRepository pagoRepository;
    private final MovimientoCajaService movimientoCajaService;
    private final ComprobantePlanAhorroService comprobantePlanAhorroService;
    private final ComprobantePlanAhorroRepository comprobantePlanAhorroRepository;
    private final ContratoPlanAhorroMapper contratoMapper;
    private final CuotaPlanAhorroMapper cuotaMapper;

    public ContratoPlanAhorroServiceImpl(
        ContratoPlanAhorroRepository contratoRepository,
        CuotaPlanAhorroRepository cuotaRepository,
        PlanAhorroRepository planRepository,
        ClienteRepository clienteRepository,
        UserRepository userRepository,
        MetodoPagoRepository metodoPagoRepository,
        PagoRepository pagoRepository,
        MovimientoCajaService movimientoCajaService,
        ComprobantePlanAhorroService comprobantePlanAhorroService,
        ComprobantePlanAhorroRepository comprobantePlanAhorroRepository,
        ContratoPlanAhorroMapper contratoMapper,
        CuotaPlanAhorroMapper cuotaMapper
    ) {
        this.contratoRepository = contratoRepository;
        this.cuotaRepository = cuotaRepository;
        this.planRepository = planRepository;
        this.clienteRepository = clienteRepository;
        this.userRepository = userRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.pagoRepository = pagoRepository;
        this.movimientoCajaService = movimientoCajaService;
        this.comprobantePlanAhorroService = comprobantePlanAhorroService;
        this.comprobantePlanAhorroRepository = comprobantePlanAhorroRepository;
        this.contratoMapper = contratoMapper;
        this.cuotaMapper = cuotaMapper;
    }

    @Override
    public ContratoPlanAhorroDTO crearContrato(ContratoPlanAhorroDTO dto) {
        if (dto.getPlan() == null || dto.getPlan().getId() == null) {
            throw new BadRequestException("Debe seleccionar un plan");
        }
        if (dto.getCliente() == null || dto.getCliente().getId() == null) {
            throw new BadRequestException("Debe seleccionar un cliente");
        }

        PlanAhorro plan = planRepository.findById(dto.getPlan().getId()).orElseThrow(() -> new BadRequestException("El plan no existe"));
        Cliente cliente = clienteRepository.findById(dto.getCliente().getId()).orElseThrow(() -> new BadRequestException("El cliente no existe"));
        Instant ahora = Instant.now();
        String login = currentUserLogin();

        ContratoPlanAhorro contrato = new ContratoPlanAhorro();
        contrato.setPlan(plan);
        contrato.setCliente(cliente);
        contrato.setFechaInicio(dto.getFechaInicio() != null ? dto.getFechaInicio() : ahora);
        contrato.setEstado(EstadoContratoPlanAhorro.ACTIVO);
        contrato.setCuotasTotales(plan.getCantidadCuotas());
        contrato.setCuotasPagadas(0);
        contrato.setSaldoPendiente(plan.getValorMovil().setScale(2, RoundingMode.HALF_UP));
        contrato.setObservaciones(dto.getObservaciones());
        contrato.setUsuarioRegistro(login);
        contrato.setUser(userRepository.findOneByLogin(login).orElse(null));
        contrato.setNumeroContrato(generarNumeroContrato());

        ContratoPlanAhorro saved = contratoRepository.save(contrato);
        generarCuotas(saved, plan, contrato.getFechaInicio());
        return contratoMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContratoPlanAhorroDTO> findAll(Pageable pageable) {
        if (isAdmin()) {
            return contratoRepository.findAll(pageable).map(contratoMapper::toDto);
        }
        return contratoRepository.findAllByUserLogin(currentUserLogin(), pageable).map(contratoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContratoPlanAhorroDTO> findOne(Long id) {
        if (isAdmin()) {
            return contratoRepository.findById(id).map(contratoMapper::toDto);
        }
        return contratoRepository.findOneByIdAndUserLogin(id, currentUserLogin()).map(contratoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuotaPlanAhorroDTO> findCuotas(Long contratoId) {
        validarAccesoContrato(contratoId);
        return cuotaRepository
            .findAllByContratoIdOrderByNumeroCuotaAsc(contratoId)
            .stream()
            .map(cuota -> {
                CuotaPlanAhorroDTO dto = cuotaMapper.toDto(cuota);
                List<ComprobantePlanAhorro> comprobantes = comprobantePlanAhorroRepository.findAllByCuotaPlanAhorroIdOrderByIdDesc(cuota.getId());
                if (!comprobantes.isEmpty()) {
                    ComprobantePlanAhorro ultimo = comprobantes.get(0);
                    dto.setComprobantePlanAhorroId(ultimo.getId());
                    dto.setNumeroComprobantePlanAhorro(ultimo.getNumeroComprobante());
                    dto.setEstadoComprobantePlanAhorro(ultimo.getEstado());
                }
                return dto;
            })
            .toList();
    }

    @Override
    public CuotaPlanAhorroDTO pagarCuota(Long cuotaId, BigDecimal monto, String observaciones) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto de pago debe ser mayor a cero");
        }
        CuotaPlanAhorro cuota = obtenerCuotaConPermisos(cuotaId);
        if (cuota.getEstado() == EstadoCuotaPlanAhorro.PAGADA) {
            throw new BadRequestException("La cuota ya fue pagada");
        }
        if (cuota.getEstado() == EstadoCuotaPlanAhorro.ANULADA) {
            throw new BadRequestException("La cuota fue anulada");
        }
        if (monto.compareTo(cuota.getImporte()) != 0) {
            throw new BadRequestException("El monto debe coincidir con el importe de la cuota para esta fase");
        }

        ContratoPlanAhorro contrato = cuota.getContrato();
        Instant ahora = Instant.now();
        MetodoPago contado = metodoPagoRepository
            .findByCodigoIgnoreCase(CODIGO_CONTADO)
            .orElseThrow(() -> new BadRequestException("No existe el metodo de pago CONTADO configurado"));

        Pago pago = new Pago();
        pago.setFecha(ahora);
        pago.setMonto(monto.setScale(2, RoundingMode.HALF_UP));
        pago.setMoneda(contrato.getPlan().getMoneda());
        pago.setMetodoPago(contado);
        pago.setTipoMovimiento(TipoMovimientoPago.PAGO_RECIBIDO);
        pago.setEstado(EstadoPago.REGISTRADO);
        pago.setCotizacionUsada(BigDecimal.ONE);
        pago.setMontoAplicadoVenta(monto.setScale(2, RoundingMode.HALF_UP));
        pago.setFechaCotizacionUsada(ahora);
        pago.setVenta(null);
        pago.setReserva(null);
        pago.setTasacionUsado(null);
        pago.setAdjudicacionPlanAhorro(null);
        pago.setContratoPlanAhorro(contrato);
        pago.setUsuarioRegistro(currentUserLogin());
        pago.setObservaciones(observaciones);
        pago.setCreatedDate(ahora);
        pago.setLastModifiedDate(ahora);
        pago = pagoRepository.save(pago);

        cuota.setPago(pago);
        cuota.setEstado(EstadoCuotaPlanAhorro.PAGADA);
        cuota.setFechaPago(ahora);
        cuotaRepository.save(cuota);

        recalcularContrato(contrato);
        movimientoCajaService.registrarDesdePago(pago, TipoMovimientoCaja.INGRESO, EstadoPago.REGISTRADO, true);
        comprobantePlanAhorroService.emitirParaCuota(cuota, pago);

        return cuotaMapper.toDto(cuota);
    }

    @Override
    public List<CuotaPlanAhorroDTO> pagarCuotas(
        List<Long> cuotaIds,
        BigDecimal montoTotal,
        String observaciones,
        Long metodoPagoId,
        Long monedaId
    ) {
        if (cuotaIds == null || cuotaIds.isEmpty()) {
            throw new BadRequestException("Debe seleccionar al menos una cuota");
        }

        List<CuotaPlanAhorro> cuotas = cuotaIds.stream().distinct().map(this::obtenerCuotaConPermisos).sorted(Comparator.comparing(CuotaPlanAhorro::getNumeroCuota)).toList();
        ContratoPlanAhorro contrato = cuotas.getFirst().getContrato();
        boolean distintoContrato = cuotas.stream().anyMatch(cuota -> !cuota.getContrato().getId().equals(contrato.getId()));
        if (distintoContrato) {
            throw new BadRequestException("Todas las cuotas deben pertenecer al mismo contrato");
        }

        cuotas.forEach(cuota -> {
            if (cuota.getEstado() != EstadoCuotaPlanAhorro.PENDIENTE && cuota.getEstado() != EstadoCuotaPlanAhorro.VENCIDA) {
                throw new BadRequestException("Solo se pueden pagar cuotas pendientes o vencidas");
            }
            if (cuota.getPago() != null) {
                throw new BadRequestException("Una o más cuotas ya tienen un pago asociado");
            }
            if (comprobantePlanAhorroRepository.existsByCuotaPlanAhorroIdAndEstado(cuota.getId(), EstadoComprobante.EMITIDO)) {
                throw new BadRequestException("Una o más cuotas ya tienen un comprobante activo");
            }
        });

        BigDecimal totalEsperado = cuotas
            .stream()
            .map(CuotaPlanAhorro::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        if (montoTotal != null && montoTotal.setScale(2, RoundingMode.HALF_UP).compareTo(totalEsperado) != 0) {
            throw new BadRequestException("El monto total debe coincidir con la suma de cuotas seleccionadas");
        }

        MetodoPago metodoPago = resolverMetodoPago(metodoPagoId);
        if (monedaId != null && (contrato.getPlan().getMoneda() == null || !monedaId.equals(contrato.getPlan().getMoneda().getId()))) {
            throw new BadRequestException("La moneda del pago debe coincidir con la moneda del plan");
        }

        Instant ahora = Instant.now();
        Pago pago = new Pago();
        pago.setFecha(ahora);
        pago.setMonto(totalEsperado);
        pago.setMoneda(contrato.getPlan().getMoneda());
        pago.setMetodoPago(metodoPago);
        pago.setTipoMovimiento(TipoMovimientoPago.PAGO_RECIBIDO);
        pago.setEstado(EstadoPago.REGISTRADO);
        pago.setCotizacionUsada(BigDecimal.ONE);
        pago.setMontoAplicadoVenta(totalEsperado);
        pago.setFechaCotizacionUsada(ahora);
        pago.setVenta(null);
        pago.setReserva(null);
        pago.setTasacionUsado(null);
        pago.setAdjudicacionPlanAhorro(null);
        pago.setContratoPlanAhorro(contrato);
        pago.setUsuarioRegistro(currentUserLogin());
        pago.setObservaciones(observaciones);
        pago.setCreatedDate(ahora);
        pago.setLastModifiedDate(ahora);
        pago = pagoRepository.save(pago);

        for (CuotaPlanAhorro cuota : cuotas) {
            cuota.setPago(pago);
            cuota.setEstado(EstadoCuotaPlanAhorro.PAGADA);
            cuota.setFechaPago(ahora);
            cuotaRepository.save(cuota);
            comprobantePlanAhorroService.emitirParaCuota(cuota, pago);
        }

        recalcularContrato(contrato);
        movimientoCajaService.registrarDesdePago(pago, TipoMovimientoCaja.INGRESO, EstadoPago.REGISTRADO, true);
        return cuotas.stream().map(cuotaMapper::toDto).toList();
    }

    private void generarCuotas(ContratoPlanAhorro contrato, PlanAhorro plan, Instant fechaInicio) {
        int cantidadCuotas = plan.getCantidadCuotas();
        BigDecimal valorTotal = plan.getValorMovil().setScale(2, RoundingMode.HALF_UP);
        BigDecimal importeBaseCuota = valorTotal.divide(BigDecimal.valueOf(cantidadCuotas), 2, RoundingMode.HALF_UP);
        BigDecimal acumulado = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (int numero = 1; numero <= cantidadCuotas; numero++) {
            CuotaPlanAhorro cuota = new CuotaPlanAhorro();
            cuota.setContrato(contrato);
            cuota.setNumeroCuota(numero);
            BigDecimal importeCuota = numero == cantidadCuotas ? valorTotal.subtract(acumulado).setScale(2, RoundingMode.HALF_UP) : importeBaseCuota;
            cuota.setImporte(importeCuota);
            cuota.setEstado(EstadoCuotaPlanAhorro.PENDIENTE);
            cuota.setFechaVencimiento(fechaInicio.plus(30L * numero, ChronoUnit.DAYS));
            cuotaRepository.save(cuota);
            acumulado = acumulado.add(importeCuota).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private void recalcularContrato(ContratoPlanAhorro contrato) {
        List<CuotaPlanAhorro> cuotas = cuotaRepository.findAllByContratoIdOrderByNumeroCuotaAsc(contrato.getId());
        long pagadas = cuotas.stream().filter(c -> c.getEstado() == EstadoCuotaPlanAhorro.PAGADA).count();
        BigDecimal saldo = cuotas
            .stream()
            .filter(c -> c.getEstado() == EstadoCuotaPlanAhorro.PENDIENTE || c.getEstado() == EstadoCuotaPlanAhorro.VENCIDA)
            .map(CuotaPlanAhorro::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        contrato.setCuotasPagadas((int) pagadas);
        contrato.setSaldoPendiente(saldo);
        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            contrato.setEstado(EstadoContratoPlanAhorro.FINALIZADO);
        }
        contratoRepository.save(contrato);
    }

    private CuotaPlanAhorro obtenerCuotaConPermisos(Long cuotaId) {
        if (isAdmin()) {
            return cuotaRepository.findById(cuotaId).orElseThrow(() -> new BadRequestException("La cuota no existe"));
        }
        return cuotaRepository
            .findOneByIdForUser(cuotaId, currentUserLogin())
            .orElseThrow(() -> new AccessDeniedException("No tienes permisos para acceder a esta cuota"));
    }

    private void validarAccesoContrato(Long contratoId) {
        if (isAdmin()) {
            return;
        }
        boolean allowed = contratoRepository.findOneByIdAndUserLogin(contratoId, currentUserLogin()).isPresent();
        if (!allowed) {
            LOG.warn("Acceso denegado a contrato de plan {} para usuario {}", contratoId, currentUserLogin());
            throw new AccessDeniedException("No tienes permisos para acceder a este contrato");
        }
    }

    private MetodoPago resolverMetodoPago(Long metodoPagoId) {
        if (metodoPagoId != null) {
            return metodoPagoRepository.findById(metodoPagoId).orElseThrow(() -> new BadRequestException("El método de pago no existe"));
        }
        return metodoPagoRepository
            .findByCodigoIgnoreCase(CODIGO_CONTADO)
            .orElseThrow(() -> new BadRequestException("No existe el metodo de pago CONTADO configurado"));
    }

    private String generarNumeroContrato() {
        long correlativo = contratoRepository.count() + 1L;
        String numero = "PLAN-" + String.format("%06d", correlativo);
        while (contratoRepository.existsByNumeroContrato(numero)) {
            correlativo++;
            numero = "PLAN-" + String.format("%06d", correlativo);
        }
        return numero;
    }

    private boolean isAdmin() {
        return SecurityUtils.hasCurrentUserAnyOfAuthorities(ROLE_ADMIN);
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }
}

