package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.EntregaChecklistItem;
import com.concesionaria.app.domain.EntregaUnidad;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoEntregaUnidad;
import com.concesionaria.app.repository.EntregaUnidadRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.EntregaUnidadService;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.EntregaChecklistItemDTO;
import com.concesionaria.app.service.dto.EntregaUnidadDTO;
import com.concesionaria.app.service.dto.InventarioDTO;
import com.concesionaria.app.service.dto.VehiculoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ClienteMapper;
import com.concesionaria.app.service.mapper.InventarioMapper;
import com.concesionaria.app.service.mapper.VentaMapper;
import com.concesionaria.app.service.mapper.VehiculoMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EntregaUnidadServiceImpl implements EntregaUnidadService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final EntregaUnidadRepository entregaUnidadRepository;
    private final VentaRepository ventaRepository;
    private final InventarioRepository inventarioRepository;
    private final ClienteMapper clienteMapper;
    private final VehiculoMapper vehiculoMapper;
    private final InventarioMapper inventarioMapper;
    private final VentaMapper ventaMapper;
    private final EntregaUnidadValidator entregaUnidadValidator;
    private final EntregaChecklistManager entregaChecklistManager;
    private final EntregaUnidadInventarioSync entregaUnidadInventarioSync;
    private final EntregaUnidadHistorialService entregaUnidadHistorialService;

    public EntregaUnidadServiceImpl(
        EntregaUnidadRepository entregaUnidadRepository,
        VentaRepository ventaRepository,
        InventarioRepository inventarioRepository,
        ClienteMapper clienteMapper,
        VehiculoMapper vehiculoMapper,
        InventarioMapper inventarioMapper,
        VentaMapper ventaMapper,
        EntregaUnidadValidator entregaUnidadValidator,
        EntregaChecklistManager entregaChecklistManager,
        EntregaUnidadInventarioSync entregaUnidadInventarioSync,
        EntregaUnidadHistorialService entregaUnidadHistorialService
    ) {
        this.entregaUnidadRepository = entregaUnidadRepository;
        this.ventaRepository = ventaRepository;
        this.inventarioRepository = inventarioRepository;
        this.clienteMapper = clienteMapper;
        this.vehiculoMapper = vehiculoMapper;
        this.inventarioMapper = inventarioMapper;
        this.ventaMapper = ventaMapper;
        this.entregaUnidadValidator = entregaUnidadValidator;
        this.entregaChecklistManager = entregaChecklistManager;
        this.entregaUnidadInventarioSync = entregaUnidadInventarioSync;
        this.entregaUnidadHistorialService = entregaUnidadHistorialService;
    }

    @Override
    public EntregaUnidadDTO programarEntrega(Long ventaId, Instant fechaProgramada, String observaciones) {
        Venta venta = obtenerVentaConPermisosForUpdate(ventaId);
        entregaUnidadValidator.validarVentaEntregable(venta);
        entregaUnidadValidator.validarNoDuplicarEntregaActiva(ventaId);
        Inventario inventario = inventarioRepository
            .findByVehiculoId(venta.getVehiculo().getId())
            .orElseThrow(() -> new BadRequestException("No se encontro inventario para el vehiculo de la venta"));

        EntregaUnidad entrega = new EntregaUnidad();
        entrega.setVenta(venta);
        entrega.setCliente(venta.getCliente());
        entrega.setVehiculo(venta.getVehiculo());
        entrega.setInventario(inventario);
        entrega.setFechaProgramada(fechaProgramada != null ? fechaProgramada : Instant.now());
        entrega.setEstado(EstadoEntregaUnidad.PROGRAMADA);
        entrega.setUsuarioProgramacion(currentUserLogin());
        entrega.setObservaciones(observaciones);
        entrega.setCreatedDate(Instant.now());
        entrega.setLastModifiedDate(Instant.now());
        entrega = entregaUnidadRepository.save(entrega);
        entregaChecklistManager.crearChecklistInicial(entrega);

        return toDto(entrega);
    }

    @Override
    public EntregaUnidadDTO actualizarChecklist(Long entregaId, List<EntregaChecklistItemDTO> items) {
        EntregaUnidad entrega = obtenerEntregaConPermisosForUpdate(entregaId);
        entregaUnidadValidator.validarEstadoPermiteActualizarChecklist(entrega);
        List<EntregaChecklistItem> actuales = entregaChecklistManager.obtenerChecklist(entregaId);
        entregaChecklistManager.actualizarChecklist(actuales, items);
        entrega.setLastModifiedDate(Instant.now());
        entregaUnidadRepository.save(entrega);
        return toDto(entrega);
    }

    @Override
    public EntregaUnidadDTO confirmarEntrega(Long entregaId, Integer kilometrajeEntrega, String nivelCombustible, String observaciones) {
        EntregaUnidad entrega = obtenerEntregaConPermisosForUpdate(entregaId);
        entregaUnidadValidator.validarEstadoPermiteConfirmar(entrega);
        if (entrega.getEstado() == EstadoEntregaUnidad.ENTREGADA) {
            return toDto(entrega);
        }
        List<EntregaChecklistItem> checklist = entregaChecklistManager.obtenerChecklist(entregaId);
        entregaUnidadValidator.validarChecklistObligatorioCompleto(checklist);

        entrega.setEstado(EstadoEntregaUnidad.ENTREGADA);
        entrega.setFechaEntrega(Instant.now());
        entrega.setUsuarioEntrega(currentUserLogin());
        entrega.setKilometrajeEntrega(kilometrajeEntrega);
        entrega.setNivelCombustible(nivelCombustible);
        entrega.setObservaciones(observaciones);
        entrega.setLastModifiedDate(Instant.now());
        entregaUnidadRepository.save(entrega);

        entregaUnidadInventarioSync.marcarEntregado(entrega);

        entregaUnidadHistorialService.registrarHistorialEntrega(
            entrega.getVenta(),
            "ENTREGA_CONFIRMADA",
            "Entrega de unidad confirmada",
            currentUserLogin()
        );
        return toDto(entrega);
    }

    @Override
    public EntregaUnidadDTO cancelarEntrega(Long entregaId, String motivo) {
        EntregaUnidad entrega = obtenerEntregaConPermisosForUpdate(entregaId);
        entregaUnidadValidator.validarEstadoPermiteCancelar(entrega);
        entrega.setEstado(EstadoEntregaUnidad.CANCELADA);
        entrega.setObservaciones(motivo);
        entrega.setLastModifiedDate(Instant.now());
        entregaUnidadRepository.save(entrega);
        entregaUnidadHistorialService.registrarHistorialEntrega(
            entrega.getVenta(),
            "ENTREGA_CANCELADA",
            motivo != null ? motivo : "Entrega cancelada",
            currentUserLogin()
        );
        return toDto(entrega);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EntregaUnidadDTO> findByVentaId(Long ventaId) {
        Optional<EntregaUnidad> entrega = isAdmin()
            ? entregaUnidadRepository.findByVentaIdWithEager(ventaId)
            : entregaUnidadRepository.findByVentaIdWithEager(ventaId).filter(e -> esVentaDelUsuario(e.getVenta()));
        return entrega.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EntregaUnidadDTO> findAll(Pageable pageable) {
        Page<EntregaUnidad> page = isAdmin()
            ? entregaUnidadRepository.findAllWithEager(pageable)
            : entregaUnidadRepository.findAllCurrentUserWithEager(currentUserLogin(), pageable);
        return page.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EntregaUnidadDTO> search(
        Pageable pageable,
        EstadoEntregaUnidad estado,
        Instant fromDate,
        Instant toDate,
        String cliente,
        Long ventaId
    ) {
        Page<EntregaUnidad> page = isAdmin()
            ? entregaUnidadRepository.searchAdmin(estado, fromDate, toDate, cliente, ventaId, pageable)
            : entregaUnidadRepository.searchCurrentUser(currentUserLogin(), estado, fromDate, toDate, cliente, ventaId, pageable);
        return page.map(this::toDto);
    }

    private Venta obtenerVentaConPermisosForUpdate(Long ventaId) {
        Venta venta = ventaRepository.findByIdForUpdate(ventaId).or(() -> ventaRepository.findById(ventaId)).orElseThrow(() -> new BadRequestException("La venta no existe"));
        if (!isAdmin() && !esVentaDelUsuario(venta)) {
            throw new AccessDeniedException("No tienes permisos para operar esta venta");
        }
        return venta;
    }

    private EntregaUnidad obtenerEntregaConPermisosForUpdate(Long entregaId) {
        if (isAdmin()) {
            return entregaUnidadRepository.findByIdForUpdate(entregaId).or(() -> entregaUnidadRepository.findById(entregaId)).orElseThrow(() -> new BadRequestException("Entrega no encontrada"));
        }
        return entregaUnidadRepository
            .findOneByIdAndUserLoginForUpdate(entregaId, currentUserLogin())
            .or(() -> entregaUnidadRepository.findOneByIdAndUserLogin(entregaId, currentUserLogin()))
            .orElseThrow(() -> new AccessDeniedException("No tienes permisos sobre esta entrega"));
    }
    private Venta obtenerVentaConPermisos(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta no existe"));
        if (!isAdmin() && !esVentaDelUsuario(venta)) {
            throw new AccessDeniedException("No tienes permisos para operar esta venta");
        }
        return venta;
    }

    private EntregaUnidad obtenerEntregaConPermisos(Long entregaId) {
        if (isAdmin()) {
            return entregaUnidadRepository.findById(entregaId).orElseThrow(() -> new BadRequestException("Entrega no encontrada"));
        }
        return entregaUnidadRepository
            .findOneByIdAndUserLogin(entregaId, currentUserLogin())
            .orElseThrow(() -> new AccessDeniedException("No tienes permisos sobre esta entrega"));
    }

    private boolean esVentaDelUsuario(Venta venta) {
        return venta != null && venta.getUser() != null && currentUserLogin().equalsIgnoreCase(venta.getUser().getLogin());
    }

    private EntregaUnidadDTO toDto(EntregaUnidad entrega) {
        EntregaUnidadDTO dto = new EntregaUnidadDTO();
        dto.setId(entrega.getId());
        dto.setFechaProgramada(entrega.getFechaProgramada());
        dto.setFechaEntrega(entrega.getFechaEntrega());
        dto.setEstado(entrega.getEstado());
        dto.setUsuarioProgramacion(entrega.getUsuarioProgramacion());
        dto.setUsuarioEntrega(entrega.getUsuarioEntrega());
        dto.setKilometrajeEntrega(entrega.getKilometrajeEntrega());
        dto.setNivelCombustible(entrega.getNivelCombustible());
        dto.setObservaciones(entrega.getObservaciones());
        dto.setCreatedDate(entrega.getCreatedDate());
        dto.setLastModifiedDate(entrega.getLastModifiedDate());

        VentaDTO ventaDTO = ventaMapper.toDto(entrega.getVenta());
        dto.setVenta(ventaDTO);
        ClienteDTO clienteDTO = clienteMapper.toDto(entrega.getCliente());
        dto.setCliente(clienteDTO);
        VehiculoDTO vehiculoDTO = vehiculoMapper.toDto(entrega.getVehiculo());
        dto.setVehiculo(vehiculoDTO);
        InventarioDTO inventarioDTO = inventarioMapper.toDto(entrega.getInventario());
        dto.setInventario(inventarioDTO);

        List<EntregaChecklistItemDTO> checklist = new ArrayList<>();
        for (EntregaChecklistItem item : entregaChecklistManager.obtenerChecklist(entrega.getId())) {
            EntregaChecklistItemDTO itemDTO = new EntregaChecklistItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setCodigo(item.getCodigo());
            itemDTO.setDescripcion(item.getDescripcion());
            itemDTO.setObligatorio(item.getObligatorio());
            itemDTO.setCompletado(item.getCompletado());
            itemDTO.setObservaciones(item.getObservaciones());
            checklist.add(itemDTO);
        }
        dto.setChecklist(checklist);
        return dto;
    }

    private boolean isAdmin() {
        return SecurityUtils.hasCurrentUserAnyOfAuthorities(ROLE_ADMIN);
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }
}


