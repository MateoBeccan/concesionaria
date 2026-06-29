package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.AdjudicacionPlanAhorro;
import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoAdjudicacionPlanAhorro;
import com.concesionaria.app.repository.AdjudicacionPlanAhorroRepository;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.AdjudicacionPlanAhorroService;
import com.concesionaria.app.service.dto.AdjudicacionPlanAhorroDTO;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.ElegibilidadAdjudicacionDTO;
import com.concesionaria.app.service.dto.InventarioDTO;
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

    private final AdjudicacionPlanAhorroRepository adjudicacionRepository;
    private final ContratoPlanAhorroRepository contratoRepository;
    private final InventarioRepository inventarioRepository;
    private final InventarioMapper inventarioMapper;
    private final ClienteMapper clienteMapper;
    private final VentaMapper ventaMapper;
    private final AdjudicacionPlanAhorroValidator validator;
    private final ElegibilidadAdjudicacionEvaluator elegibilidadEvaluator;
    private final GeneradorVentaPlanAhorro generadorVentaPlanAhorro;
    private final AplicadorCreditoPlanAhorro aplicadorCreditoPlanAhorro;

    public AdjudicacionPlanAhorroServiceImpl(
        AdjudicacionPlanAhorroRepository adjudicacionRepository,
        ContratoPlanAhorroRepository contratoRepository,
        InventarioRepository inventarioRepository,
        InventarioMapper inventarioMapper,
        ClienteMapper clienteMapper,
        VentaMapper ventaMapper,
        AdjudicacionPlanAhorroValidator validator,
        ElegibilidadAdjudicacionEvaluator elegibilidadEvaluator,
        GeneradorVentaPlanAhorro generadorVentaPlanAhorro,
        AplicadorCreditoPlanAhorro aplicadorCreditoPlanAhorro
    ) {
        this.adjudicacionRepository = adjudicacionRepository;
        this.contratoRepository = contratoRepository;
        this.inventarioRepository = inventarioRepository;
        this.inventarioMapper = inventarioMapper;
        this.clienteMapper = clienteMapper;
        this.ventaMapper = ventaMapper;
        this.validator = validator;
        this.elegibilidadEvaluator = elegibilidadEvaluator;
        this.generadorVentaPlanAhorro = generadorVentaPlanAhorro;
        this.aplicadorCreditoPlanAhorro = aplicadorCreditoPlanAhorro;
    }

    @Override
    public AdjudicacionPlanAhorroDTO adjudicarContrato(Long contratoId, String observaciones) {
        ContratoPlanAhorro contrato = obtenerContratoConPermisosForUpdate(contratoId);
        validator.validarContratoAdjudicable(contrato);
        ElegibilidadAdjudicacionDTO elegibilidad = elegibilidadEvaluator.evaluar(contrato);
        if (!elegibilidad.isApto()) {
            throw new BadRequestException(elegibilidad.getMensaje());
        }
        validator.validarNoDuplicarAdjudicacionActiva(contratoId);

        BigDecimal montoReconocido = elegibilidadEvaluator.calcularMontoReconocidoCuotas(contrato.getId());
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

        contrato.setEstado(com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro.ADJUDICADO);
        contratoRepository.save(contrato);
        return toDto(adjudicacion);
    }

    @Override
    public AdjudicacionPlanAhorroDTO asignarInventario(Long adjudicacionId, Long inventarioId) {
        AdjudicacionPlanAhorro adjudicacion = obtenerAdjudicacionConPermisosForUpdate(adjudicacionId);
        validator.validarEstadoPermiteAsignarInventario(adjudicacion);
        Inventario inventario = inventarioRepository.findByIdForUpdate(inventarioId).or(() -> inventarioRepository.findById(inventarioId)).orElseThrow(() -> new BadRequestException("Inventario inexistente"));
        validator.validarInventarioDisponible(inventario);
        validator.validarInventarioCompatible(adjudicacion.getContratoPlanAhorro(), inventario);

        adjudicacion.setInventario(inventario);
        adjudicacion.setVehiculo(inventario.getVehiculo());
        generadorVentaPlanAhorro.actualizarDiferenciaAPagar(adjudicacion, inventario);
        return toDto(adjudicacionRepository.save(adjudicacion));
    }

    @Override
    public AdjudicacionPlanAhorroDTO generarVenta(Long adjudicacionId) {
        AdjudicacionPlanAhorro adjudicacion = obtenerAdjudicacionConPermisosForUpdate(adjudicacionId);
        if (adjudicacion.getVenta() != null) {
            return toDto(adjudicacion);
        }
        validator.validarGeneracionVenta(adjudicacion);

        VentaDTO ventaCreada = generadorVentaPlanAhorro.generarVentaDesdeAdjudicacion(adjudicacion);
        aplicadorCreditoPlanAhorro.aplicarCreditoSiCorresponde(adjudicacion, ventaCreada);
        aplicadorCreditoPlanAhorro.ajustarEstadoFinalVentaDesdePlan(ventaCreada.getId(), currentUserLogin());

        Venta venta = ventaMapper.toEntity(ventaCreada);
        adjudicacion.setVenta(venta);
        adjudicacion.setEstado(EstadoAdjudicacionPlanAhorro.VENTA_GENERADA);
        return toDto(adjudicacionRepository.save(adjudicacion));
    }

    @Override
    public AdjudicacionPlanAhorroDTO cancelarAdjudicacion(Long adjudicacionId, String motivo) {
        AdjudicacionPlanAhorro adjudicacion = obtenerAdjudicacionConPermisosForUpdate(adjudicacionId);
        validator.validarEstadoPermiteCancelar(adjudicacion);
        adjudicacion.setEstado(EstadoAdjudicacionPlanAhorro.CANCELADA);
        adjudicacion.setObservaciones(motivo);
        return toDto(adjudicacionRepository.save(adjudicacion));
    }

    @Override
    public AdjudicacionPlanAhorroDTO marcarEntregada(Long adjudicacionId) {
        AdjudicacionPlanAhorro adjudicacion = obtenerAdjudicacionConPermisosForUpdate(adjudicacionId);
        validator.validarEstadoPermiteMarcarEntregada(adjudicacion);
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
        com.concesionaria.app.domain.Version versionObjetivo = contrato.getPlan() != null ? contrato.getPlan().getVersionObjetivo() : null;
        List<Inventario> compatibles = buscarInventarioCompatible(versionObjetivo);
        return compatibles.stream().map(inventarioMapper::toDto).toList();
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
        return elegibilidadEvaluator.evaluar(contrato);
    }

    private List<Inventario> buscarInventarioCompatible(com.concesionaria.app.domain.Version versionObjetivo) {
        if (versionObjetivo == null || versionObjetivo.getId() == null) {
            return inventarioRepository.findDisponiblesByVersionObjetivo(com.concesionaria.app.domain.enumeration.EstadoInventario.DISPONIBLE, null);
        }
        List<Inventario> exactos = inventarioRepository.findDisponiblesByVersionObjetivo(
            com.concesionaria.app.domain.enumeration.EstadoInventario.DISPONIBLE,
            versionObjetivo.getId()
        );
        if (!exactos.isEmpty()) {
            return exactos;
        }
        Long modeloId = versionObjetivo.getModelo() != null ? versionObjetivo.getModelo().getId() : null;
        if (modeloId == null) {
            return List.of();
        }
        return inventarioRepository.findDisponiblesByModeloObjetivo(com.concesionaria.app.domain.enumeration.EstadoInventario.DISPONIBLE, modeloId);
    }

    private ContratoPlanAhorro obtenerContratoConPermisosForUpdate(Long contratoId) {
        if (isAdmin()) {
            return contratoRepository.findByIdForUpdate(contratoId).or(() -> contratoRepository.findById(contratoId)).orElseThrow(() -> new BadRequestException("Contrato inexistente"));
        }
        return contratoRepository
            .findOneByIdAndUserLoginForUpdate(contratoId, currentUserLogin())
            .or(() -> contratoRepository.findOneByIdAndUserLogin(contratoId, currentUserLogin()))
            .orElseThrow(() -> new AccessDeniedException("No tienes permisos sobre este contrato"));
    }

    private AdjudicacionPlanAhorro obtenerAdjudicacionConPermisosForUpdate(Long adjudicacionId) {
        if (isAdmin()) {
            return adjudicacionRepository.findByIdForUpdate(adjudicacionId).or(() -> adjudicacionRepository.findById(adjudicacionId)).orElseThrow(() -> new BadRequestException("Adjudicacion inexistente"));
        }
        LOG.warn("Validando acceso de usuario {} a adjudicacion {}", currentUserLogin(), adjudicacionId);
        return adjudicacionRepository
            .findOneByIdAndUserLoginForUpdate(adjudicacionId, currentUserLogin())
            .or(() -> adjudicacionRepository.findOneByIdAndUserLogin(adjudicacionId, currentUserLogin()))
            .orElseThrow(() -> new AccessDeniedException("No tienes permisos sobre esta adjudicacion"));
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



