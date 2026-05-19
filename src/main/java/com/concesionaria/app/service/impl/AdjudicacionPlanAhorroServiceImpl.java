package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.AdjudicacionPlanAhorro;
import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.ReglaAdjudicacionPlan;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoAdjudicacionPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.domain.enumeration.TipoReglaAdjudicacionPlan;
import com.concesionaria.app.repository.AdjudicacionPlanAhorroRepository;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.AdjudicacionPlanAhorroService;
import com.concesionaria.app.service.PagoService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.AdjudicacionPlanAhorroDTO;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.ElegibilidadAdjudicacionDTO;
import com.concesionaria.app.service.dto.InventarioDTO;
import com.concesionaria.app.service.dto.MetodoPagoDTO;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.VehiculoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ClienteMapper;
import com.concesionaria.app.service.mapper.InventarioMapper;
import com.concesionaria.app.service.mapper.VentaMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
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
public class AdjudicacionPlanAhorroServiceImpl implements AdjudicacionPlanAhorroService {

    private static final Logger LOG = LoggerFactory.getLogger(AdjudicacionPlanAhorroServiceImpl.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final BigDecimal PORCENTAJE_IMPUESTO = new BigDecimal("21.00");
    private static final String CODIGO_METODO_PLAN_AHORRO = "PLAN_AHORRO";
    private static final String OBS_PAGO_PLAN_AHORRO = "Aplicacion automatica de cuotas de plan de ahorro";

    private final AdjudicacionPlanAhorroRepository adjudicacionRepository;
    private final ContratoPlanAhorroRepository contratoRepository;
    private final CuotaPlanAhorroRepository cuotaRepository;
    private final InventarioRepository inventarioRepository;
    private final InventarioMapper inventarioMapper;
    private final ClienteMapper clienteMapper;
    private final VentaMapper ventaMapper;
    private final VentaService ventaService;
    private final PagoService pagoService;
    private final PagoRepository pagoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final VentaRepository ventaRepository;

    public AdjudicacionPlanAhorroServiceImpl(
        AdjudicacionPlanAhorroRepository adjudicacionRepository,
        ContratoPlanAhorroRepository contratoRepository,
        CuotaPlanAhorroRepository cuotaRepository,
        InventarioRepository inventarioRepository,
        InventarioMapper inventarioMapper,
        ClienteMapper clienteMapper,
        VentaMapper ventaMapper,
        VentaService ventaService,
        PagoService pagoService,
        PagoRepository pagoRepository,
        MetodoPagoRepository metodoPagoRepository,
        VentaRepository ventaRepository
    ) {
        this.adjudicacionRepository = adjudicacionRepository;
        this.contratoRepository = contratoRepository;
        this.cuotaRepository = cuotaRepository;
        this.inventarioRepository = inventarioRepository;
        this.inventarioMapper = inventarioMapper;
        this.clienteMapper = clienteMapper;
        this.ventaMapper = ventaMapper;
        this.ventaService = ventaService;
        this.pagoService = pagoService;
        this.pagoRepository = pagoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    public AdjudicacionPlanAhorroDTO adjudicarContrato(Long contratoId, String observaciones) {
        ContratoPlanAhorro contrato = obtenerContratoConPermisos(contratoId);
        validarContratoAdjudicable(contrato);
        ElegibilidadAdjudicacionDTO elegibilidad = evaluarElegibilidadInterna(contrato);
        if (!elegibilidad.isApto()) {
            throw new BadRequestException(elegibilidad.getMensaje());
        }
        if (
            adjudicacionRepository.existsByContratoIdAndEstadoIn(
                contratoId,
                List.of(EstadoAdjudicacionPlanAhorro.PENDIENTE, EstadoAdjudicacionPlanAhorro.ADJUDICADA, EstadoAdjudicacionPlanAhorro.VENTA_GENERADA)
            )
        ) {
            throw new BadRequestException("El contrato ya tiene una adjudicacion activa");
        }

        BigDecimal montoReconocido = calcularMontoReconocidoCuotas(contrato.getId());
        AdjudicacionPlanAhorro adjudicacion = new AdjudicacionPlanAhorro();
        adjudicacion.setContratoPlanAhorro(contrato);
        adjudicacion.setCliente(contrato.getCliente());
        adjudicacion.setFechaAdjudicacion(Instant.now());
        adjudicacion.setEstado(EstadoAdjudicacionPlanAhorro.ADJUDICADA);
        adjudicacion.setMontoReconocidoCuotas(montoReconocido);
        adjudicacion.setDiferenciaAPagar(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        adjudicacion.setObservaciones(observaciones);
        adjudicacion.setUsuarioAdjudicacion(currentUserLogin());
        adjudicacion = adjudicacionRepository.save(adjudicacion);

        contrato.setEstado(EstadoContratoPlanAhorro.ADJUDICADO);
        contratoRepository.save(contrato);

        return toDto(adjudicacion);
    }

    @Override
    public AdjudicacionPlanAhorroDTO asignarInventario(Long adjudicacionId, Long inventarioId) {
        AdjudicacionPlanAhorro adjudicacion = obtenerAdjudicacionConPermisos(adjudicacionId);
        if (adjudicacion.getEstado() == EstadoAdjudicacionPlanAhorro.CANCELADA || adjudicacion.getEstado() == EstadoAdjudicacionPlanAhorro.ENTREGADA) {
            throw new BadRequestException("La adjudicacion no permite asignar inventario");
        }
        Inventario inventario = inventarioRepository.findById(inventarioId).orElseThrow(() -> new BadRequestException("Inventario inexistente"));
        if (inventario.getEstadoInventario() != EstadoInventario.DISPONIBLE) {
            throw new BadRequestException("Solo se puede asignar inventario disponible");
        }
        if (!esInventarioCompatibleConPlan(adjudicacion.getContratoPlanAhorro(), inventario)) {
            throw new BadRequestException("El inventario seleccionado no es compatible con el plan");
        }

        adjudicacion.setInventario(inventario);
        adjudicacion.setVehiculo(inventario.getVehiculo());
        BigDecimal precioVehiculo = inventario.getVehiculo() != null && inventario.getVehiculo().getPrecio() != null
            ? inventario.getVehiculo().getPrecio().setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal diferencia = precioVehiculo.subtract(adjudicacion.getMontoReconocidoCuotas()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        adjudicacion.setDiferenciaAPagar(diferencia);
        return toDto(adjudicacionRepository.save(adjudicacion));
    }

    @Override
    public AdjudicacionPlanAhorroDTO generarVenta(Long adjudicacionId) {
        AdjudicacionPlanAhorro adjudicacion = obtenerAdjudicacionConPermisos(adjudicacionId);
        if (adjudicacion.getVenta() != null) {
            return toDto(adjudicacion);
        }
        if (adjudicacion.getInventario() == null || adjudicacion.getInventario().getId() == null) {
            throw new BadRequestException("Debe asignar inventario antes de generar la venta");
        }

        Inventario inventario = inventarioRepository
            .findById(adjudicacion.getInventario().getId())
            .orElseThrow(() -> new BadRequestException("El inventario asignado no existe"));
        if (inventario.getEstadoInventario() != EstadoInventario.DISPONIBLE) {
            throw new BadRequestException("El inventario asignado ya no esta disponible");
        }

        VentaDTO nuevaVenta = new VentaDTO();
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setId(adjudicacion.getCliente().getId());
        nuevaVenta.setCliente(clienteDTO);
        VehiculoDTO vehiculoDTO = new VehiculoDTO();
        vehiculoDTO.setId(inventario.getVehiculo().getId());
        nuevaVenta.setVehiculo(vehiculoDTO);
        nuevaVenta.setFecha(Instant.now());
        nuevaVenta.setEstado(EstadoVenta.PENDIENTE);
        nuevaVenta.setPorcentajeImpuesto(PORCENTAJE_IMPUESTO);
        nuevaVenta.setTotalPagado(BigDecimal.ZERO);
        nuevaVenta.setObservaciones(
            "Venta generada desde plan de ahorro contrato N° " + adjudicacion.getContratoPlanAhorro().getNumeroContrato()
        );

        VentaDTO ventaCreada = ventaService.saveDesdePlanAhorro(nuevaVenta);
        aplicarCreditoPlanAhorroSiCorresponde(adjudicacion, ventaCreada);
        ajustarEstadoFinalVentaDesdePlan(ventaCreada.getId());
        Venta venta = ventaMapper.toEntity(ventaCreada);
        adjudicacion.setVenta(venta);
        adjudicacion.setEstado(EstadoAdjudicacionPlanAhorro.VENTA_GENERADA);
        return toDto(adjudicacionRepository.save(adjudicacion));
    }

    private void ajustarEstadoFinalVentaDesdePlan(Long ventaId) {
        if (ventaId == null) {
            return;
        }
        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta generada no existe"));
        BigDecimal total = venta.getTotal() == null ? BigDecimal.ZERO : venta.getTotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPagado = pagoRepository.sumMontoByVentaId(ventaId);
        totalPagado = totalPagado == null ? BigDecimal.ZERO : totalPagado.setScale(2, RoundingMode.HALF_UP);
        if (totalPagado.compareTo(total) > 0) {
            totalPagado = total;
        }
        BigDecimal saldo = total.subtract(totalPagado).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        venta.setTotalPagado(totalPagado);
        venta.setSaldo(saldo);
        venta.setEstado(saldo.compareTo(BigDecimal.ZERO) == 0 ? EstadoVenta.PAGADA : EstadoVenta.PENDIENTE);
        venta.setLastModifiedDate(Instant.now());
        venta.setLastModifiedBy(currentUserLogin());
        ventaRepository.save(venta);
        if (venta.getEstado() == EstadoVenta.PAGADA) {
            ventaService.sincronizarInventarioConVenta(venta.getId());
        }
    }

    private void aplicarCreditoPlanAhorroSiCorresponde(AdjudicacionPlanAhorro adjudicacion, VentaDTO ventaCreada) {
        if (ventaCreada == null || ventaCreada.getId() == null || adjudicacion == null || adjudicacion.getId() == null) {
            return;
        }
        BigDecimal montoReconocido = adjudicacion.getMontoReconocidoCuotas() == null
            ? BigDecimal.ZERO
            : adjudicacion.getMontoReconocidoCuotas().setScale(2, RoundingMode.HALF_UP);
        if (montoReconocido.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal totalVenta = ventaCreada.getTotal() == null ? BigDecimal.ZERO : ventaCreada.getTotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoAplicar = montoReconocido.min(totalVenta);
        if (montoAplicar.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (pagoRepository.existsByVentaIdAndMetodoPagoCodigoAndEstado(ventaCreada.getId(), CODIGO_METODO_PLAN_AHORRO, com.concesionaria.app.domain.enumeration.EstadoPago.REGISTRADO)) {
            return;
        }

        MetodoPagoDTO metodoPagoDTO = new MetodoPagoDTO();
        metodoPagoDTO.setId(
            metodoPagoRepository
                .findByCodigoIgnoreCase(CODIGO_METODO_PLAN_AHORRO)
                .orElseThrow(() -> new BadRequestException("No existe metodo de pago PLAN_AHORRO configurado"))
                .getId()
        );

        VentaDTO ventaRef = new VentaDTO();
        ventaRef.setId(ventaCreada.getId());

        MonedaDTO monedaDTO = new MonedaDTO();
        monedaDTO.setId(ventaCreada.getMoneda().getId());

        com.concesionaria.app.service.dto.PagoDTO pagoDTO = new com.concesionaria.app.service.dto.PagoDTO();
        pagoDTO.setVenta(ventaRef);
        pagoDTO.setMetodoPago(metodoPagoDTO);
        pagoDTO.setMoneda(monedaDTO);
        pagoDTO.setMonto(montoAplicar);
        pagoDTO.setFecha(Instant.now());
        pagoDTO.setObservaciones(OBS_PAGO_PLAN_AHORRO);
        pagoDTO.setAdjudicacionPlanAhorroId(adjudicacion.getId());
        pagoDTO.setContratoPlanAhorroId(adjudicacion.getContratoPlanAhorro().getId());

        pagoService.registrarPago(ventaCreada.getId(), pagoDTO);
    }

    @Override
    public AdjudicacionPlanAhorroDTO cancelarAdjudicacion(Long adjudicacionId, String motivo) {
        AdjudicacionPlanAhorro adjudicacion = obtenerAdjudicacionConPermisos(adjudicacionId);
        if (adjudicacion.getVenta() != null) {
            throw new BadRequestException("No se puede cancelar una adjudicacion que ya genero venta");
        }
        adjudicacion.setEstado(EstadoAdjudicacionPlanAhorro.CANCELADA);
        adjudicacion.setObservaciones(motivo);
        return toDto(adjudicacionRepository.save(adjudicacion));
    }

    @Override
    public AdjudicacionPlanAhorroDTO marcarEntregada(Long adjudicacionId) {
        AdjudicacionPlanAhorro adjudicacion = obtenerAdjudicacionConPermisos(adjudicacionId);
        if (adjudicacion.getEstado() != EstadoAdjudicacionPlanAhorro.VENTA_GENERADA) {
            throw new BadRequestException("Solo se puede marcar entregada una adjudicacion con venta generada");
        }
        adjudicacion.setEstado(EstadoAdjudicacionPlanAhorro.ENTREGADA);
        return toDto(adjudicacionRepository.save(adjudicacion));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdjudicacionPlanAhorroDTO> findLatestByContrato(Long contratoId) {
        List<AdjudicacionPlanAhorro> adjudicaciones = isAdmin()
            ? adjudicacionRepository.findAllByContratoIdOrderByIdDesc(contratoId)
            : adjudicacionRepository.findAllByContratoIdAndUserLogin(contratoId, currentUserLogin());
        return adjudicaciones.stream().findFirst().map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioDTO> findInventarioCompatibleDisponible(Long contratoId) {
        ContratoPlanAhorro contrato = obtenerContratoConPermisos(contratoId);
        Version versionObjetivo = contrato.getPlan() != null ? contrato.getPlan().getVersionObjetivo() : null;
        List<Inventario> compatibles = buscarInventarioCompatible(versionObjetivo);
        return compatibles
            .stream()
            .map(inventarioMapper::toDto)
            .toList();
    }

    private List<Inventario> buscarInventarioCompatible(Version versionObjetivo) {
        if (versionObjetivo == null || versionObjetivo.getId() == null) {
            return inventarioRepository.findDisponiblesByVersionObjetivo(EstadoInventario.DISPONIBLE, null);
        }
        List<Inventario> exactos = inventarioRepository.findDisponiblesByVersionObjetivo(EstadoInventario.DISPONIBLE, versionObjetivo.getId());
        if (!exactos.isEmpty()) {
            return exactos;
        }
        Long modeloId = versionObjetivo.getModelo() != null ? versionObjetivo.getModelo().getId() : null;
        if (modeloId == null) {
            return List.of();
        }
        return inventarioRepository.findDisponiblesByModeloObjetivo(EstadoInventario.DISPONIBLE, modeloId);
    }

    private boolean esInventarioCompatibleConPlan(ContratoPlanAhorro contrato, Inventario inventario) {
        Version versionObjetivo = contrato.getPlan() != null ? contrato.getPlan().getVersionObjetivo() : null;
        if (versionObjetivo == null || versionObjetivo.getId() == null) {
            return true;
        }
        Version versionInventario = inventario.getVehiculo() != null ? inventario.getVehiculo().getVersion() : null;
        if (versionInventario == null) {
            return false;
        }
        if (versionObjetivo.getId().equals(versionInventario.getId())) {
            return true;
        }
        Long modeloObjetivoId = versionObjetivo.getModelo() != null ? versionObjetivo.getModelo().getId() : null;
        Long modeloInventarioId = versionInventario.getModelo() != null ? versionInventario.getModelo().getId() : null;
        return modeloObjetivoId != null && modeloObjetivoId.equals(modeloInventarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdjudicacionPlanAhorroDTO> findAll(Pageable pageable) {
        return adjudicacionRepository.findAllWithEagerRelationships(pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdjudicacionPlanAhorroDTO> findAllCurrentUser(Pageable pageable) {
        return adjudicacionRepository.findAllByUserLoginWithEagerRelationships(currentUserLogin(), pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ElegibilidadAdjudicacionDTO evaluarElegibilidad(Long contratoId) {
        ContratoPlanAhorro contrato = obtenerContratoConPermisos(contratoId);
        return evaluarElegibilidadInterna(contrato);
    }

    private void validarContratoAdjudicable(ContratoPlanAhorro contrato) {
        if (contrato.getEstado() == EstadoContratoPlanAhorro.CANCELADO) {
            throw new BadRequestException("No se puede adjudicar un contrato cancelado");
        }
    }

    private ElegibilidadAdjudicacionDTO evaluarElegibilidadInterna(ContratoPlanAhorro contrato) {
        ElegibilidadAdjudicacionDTO dto = new ElegibilidadAdjudicacionDTO();
        ReglaAdjudicacionPlan regla = contrato.getPlan() != null ? contrato.getPlan().getReglaAdjudicacion() : null;
        if (regla == null) {
            regla = reglaSinReglaDefault();
        }
        TipoReglaAdjudicacionPlan tipo = regla.getTipoRegla() == null ? TipoReglaAdjudicacionPlan.SIN_REGLA : regla.getTipoRegla();

        int cuotasPagadas = contrato.getCuotasPagadas() == null ? 0 : contrato.getCuotasPagadas();
        BigDecimal valorMovil = contrato.getPlan() == null || contrato.getPlan().getValorMovil() == null
            ? BigDecimal.ZERO
            : contrato.getPlan().getValorMovil().setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldoPendiente = contrato.getSaldoPendiente() == null ? BigDecimal.ZERO : contrato.getSaldoPendiente().setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoPagado = valorMovil.subtract(saldoPendiente).max(BigDecimal.ZERO);
        BigDecimal porcentajeIntegrado = valorMovil.compareTo(BigDecimal.ZERO) > 0
            ? montoPagado.multiply(BigDecimal.valueOf(100)).divide(valorMovil, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        dto.setNombreRegla(regla.getNombre());
        dto.setTipoRegla(tipo);
        dto.setCuotasPagadas(cuotasPagadas);
        dto.setMinimoCuotas(regla.getMinimoCuotas());
        dto.setPorcentajeIntegrado(porcentajeIntegrado);
        dto.setMinimoPorcentaje(regla.getMinimoPorcentaje());
        dto.setPermiteMora(Boolean.TRUE.equals(regla.getPermiteMora()));

        if (tipo == TipoReglaAdjudicacionPlan.MANUAL) {
            dto.setApto(false);
            dto.setMensaje("Este plan requiere adjudicación manual por administrador.");
            return dto;
        }

        if (contrato.getEstado() == EstadoContratoPlanAhorro.EN_MORA && !Boolean.TRUE.equals(regla.getPermiteMora())) {
            dto.setApto(false);
            dto.setMensaje("El contrato está en mora y la regla no permite adjudicar en este estado.");
            return dto;
        }

        if (Boolean.TRUE.equals(regla.getRequiereContratoActivo())) {
            boolean estadoValido =
                contrato.getEstado() == EstadoContratoPlanAhorro.ACTIVO ||
                contrato.getEstado() == EstadoContratoPlanAhorro.EN_MORA ||
                contrato.getEstado() == EstadoContratoPlanAhorro.FINALIZADO ||
                contrato.getEstado() == EstadoContratoPlanAhorro.ADJUDICADO;
            if (!estadoValido) {
                dto.setApto(false);
                dto.setMensaje("El estado del contrato no habilita adjudicación según la regla.");
                return dto;
            }
        }

        boolean cumpleCuotas = regla.getMinimoCuotas() == null || cuotasPagadas >= regla.getMinimoCuotas();
        boolean cumplePorcentaje = regla.getMinimoPorcentaje() == null || porcentajeIntegrado.compareTo(regla.getMinimoPorcentaje()) >= 0;

        boolean apto = switch (tipo) {
            case SIN_REGLA -> true;
            case POR_CUOTAS -> cumpleCuotas;
            case POR_PORCENTAJE -> cumplePorcentaje;
            case CUOTAS_O_PORCENTAJE -> cumpleCuotas || cumplePorcentaje;
            case CUOTAS_Y_PORCENTAJE -> cumpleCuotas && cumplePorcentaje;
            case MANUAL -> false;
        };

        dto.setApto(apto);
        dto.setMensaje(
            apto
                ? "Contrato apto para adjudicación."
                : "El contrato no cumple las condiciones mínimas de adjudicación para la regla configurada."
        );
        return dto;
    }

    private ReglaAdjudicacionPlan reglaSinReglaDefault() {
        ReglaAdjudicacionPlan regla = new ReglaAdjudicacionPlan();
        regla.setNombre("SIN_REGLA_DEFAULT");
        regla.setTipoRegla(TipoReglaAdjudicacionPlan.SIN_REGLA);
        regla.setPermiteMora(true);
        regla.setRequiereContratoActivo(false);
        return regla;
    }

    private BigDecimal calcularMontoReconocidoCuotas(Long contratoId) {
        return cuotaRepository
            .findAllByContratoIdOrderByNumeroCuotaAsc(contratoId)
            .stream()
            .filter(c -> c.getEstado() == EstadoCuotaPlanAhorro.PAGADA)
            .map(CuotaPlanAhorro::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private ContratoPlanAhorro obtenerContratoConPermisos(Long contratoId) {
        if (isAdmin()) {
            return contratoRepository.findById(contratoId).orElseThrow(() -> new BadRequestException("Contrato inexistente"));
        }
        return contratoRepository
            .findOneByIdAndUserLogin(contratoId, currentUserLogin())
            .orElseThrow(() -> new AccessDeniedException("No tienes permisos sobre este contrato"));
    }

    private AdjudicacionPlanAhorro obtenerAdjudicacionConPermisos(Long adjudicacionId) {
        if (isAdmin()) {
            return adjudicacionRepository.findById(adjudicacionId).orElseThrow(() -> new BadRequestException("Adjudicacion inexistente"));
        }
        LOG.warn("Validando acceso de usuario {} a adjudicacion {}", currentUserLogin(), adjudicacionId);
        return adjudicacionRepository
            .findOneByIdAndUserLogin(adjudicacionId, currentUserLogin())
            .orElseThrow(() -> new AccessDeniedException("No tienes permisos sobre esta adjudicacion"));
    }

    private AdjudicacionPlanAhorroDTO toDto(AdjudicacionPlanAhorro adjudicacion) {
        AdjudicacionPlanAhorroDTO dto = new AdjudicacionPlanAhorroDTO();
        dto.setId(adjudicacion.getId());
        dto.setContratoPlanAhorroId(adjudicacion.getContratoPlanAhorro().getId());
        dto.setNumeroContrato(adjudicacion.getContratoPlanAhorro().getNumeroContrato());
        if (adjudicacion.getContratoPlanAhorro().getPlan() != null) {
            dto.setPlanNombre(adjudicacion.getContratoPlanAhorro().getPlan().getNombre());
        }
        dto.setCliente(clienteMapper.toDto(adjudicacion.getCliente()));
        dto.setFechaAdjudicacion(adjudicacion.getFechaAdjudicacion());
        dto.setEstado(adjudicacion.getEstado());
        dto.setMontoReconocidoCuotas(adjudicacion.getMontoReconocidoCuotas());
        dto.setDiferenciaAPagar(adjudicacion.getDiferenciaAPagar());
        dto.setObservaciones(adjudicacion.getObservaciones());
        dto.setUsuarioAdjudicacion(adjudicacion.getUsuarioAdjudicacion());
        if (adjudicacion.getInventario() != null) {
            dto.setInventario(inventarioMapper.toDto(adjudicacion.getInventario()));
        }
        if (adjudicacion.getVehiculo() != null) {
            VehiculoDTO vehiculoDTO = new VehiculoDTO();
            vehiculoDTO.setId(adjudicacion.getVehiculo().getId());
            vehiculoDTO.setPatente(adjudicacion.getVehiculo().getPatente());
            vehiculoDTO.setPrecio(adjudicacion.getVehiculo().getPrecio());
            dto.setVehiculo(vehiculoDTO);
        }
        if (adjudicacion.getVenta() != null) {
            VentaDTO ventaDTO = new VentaDTO();
            ventaDTO.setId(adjudicacion.getVenta().getId());
            ventaDTO.setEstado(adjudicacion.getVenta().getEstado());
            dto.setVenta(ventaDTO);
        }
        return dto;
    }

    private boolean isAdmin() {
        return SecurityUtils.hasCurrentUserAnyOfAuthorities(ROLE_ADMIN);
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }
}
