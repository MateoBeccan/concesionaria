package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.ComprobantePlanAhorro;
import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.repository.ComprobantePlanAhorroRepository;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import com.concesionaria.app.repository.PlanAhorroRepository;
import com.concesionaria.app.repository.UserRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.ContratoPlanAhorroService;
import com.concesionaria.app.service.dto.ContratoPlanAhorroDTO;
import com.concesionaria.app.service.dto.CuotaPlanAhorroDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ContratoPlanAhorroMapper;
import com.concesionaria.app.service.mapper.CuotaPlanAhorroMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
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
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final ContratoPlanAhorroRepository contratoRepository;
    private final CuotaPlanAhorroRepository cuotaRepository;
    private final PlanAhorroRepository planRepository;
    private final ClienteRepository clienteRepository;
    private final UserRepository userRepository;
    private final ComprobantePlanAhorroRepository comprobantePlanAhorroRepository;
    private final ContratoPlanAhorroValidator contratoValidator;
    private final CuotaPlanAhorroGenerator cuotaGenerator;
    private final PagoCuotaPlanAhorroProcessor pagoCuotaProcessor;
    private final ContratoPlanAhorroMapper contratoMapper;
    private final CuotaPlanAhorroMapper cuotaMapper;

    public ContratoPlanAhorroServiceImpl(
        ContratoPlanAhorroRepository contratoRepository,
        CuotaPlanAhorroRepository cuotaRepository,
        PlanAhorroRepository planRepository,
        ClienteRepository clienteRepository,
        UserRepository userRepository,
        ComprobantePlanAhorroRepository comprobantePlanAhorroRepository,
        ContratoPlanAhorroValidator contratoValidator,
        CuotaPlanAhorroGenerator cuotaGenerator,
        PagoCuotaPlanAhorroProcessor pagoCuotaProcessor,
        ContratoPlanAhorroMapper contratoMapper,
        CuotaPlanAhorroMapper cuotaMapper
    ) {
        this.contratoRepository = contratoRepository;
        this.cuotaRepository = cuotaRepository;
        this.planRepository = planRepository;
        this.clienteRepository = clienteRepository;
        this.userRepository = userRepository;
        this.comprobantePlanAhorroRepository = comprobantePlanAhorroRepository;
        this.contratoValidator = contratoValidator;
        this.cuotaGenerator = cuotaGenerator;
        this.pagoCuotaProcessor = pagoCuotaProcessor;
        this.contratoMapper = contratoMapper;
        this.cuotaMapper = cuotaMapper;
    }

    @Override
    public ContratoPlanAhorroDTO crearContrato(ContratoPlanAhorroDTO dto) {
        contratoValidator.validarContratoNuevo(dto);

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
        cuotaGenerator.generarCuotas(saved, plan, contrato.getFechaInicio());
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
        CuotaPlanAhorro cuota = obtenerCuotaConPermisos(cuotaId);
        return pagoCuotaProcessor.pagarCuota(cuota, monto, observaciones, currentUserLogin());
    }

    @Override
    public List<CuotaPlanAhorroDTO> pagarCuotas(
        List<Long> cuotaIds,
        BigDecimal montoTotal,
        String observaciones,
        Long metodoPagoId,
        Long monedaId
    ) {
        List<CuotaPlanAhorro> cuotas = cuotaIds.stream().distinct().map(this::obtenerCuotaConPermisos).sorted(Comparator.comparing(CuotaPlanAhorro::getNumeroCuota)).toList();
        return pagoCuotaProcessor.pagarCuotas(cuotas, montoTotal, observaciones, metodoPagoId, monedaId, currentUserLogin());
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
