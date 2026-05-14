package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.EntregaChecklistItem;
import com.concesionaria.app.domain.EntregaUnidad;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.VentaHistorial;
import com.concesionaria.app.domain.enumeration.EstadoEntregaUnidad;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.EntregaChecklistItemRepository;
import com.concesionaria.app.repository.EntregaUnidadRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.VentaHistorialRepository;
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
import java.util.EnumSet;
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
    private static final List<ChecklistTemplate> CHECKLIST_DEFAULT = List.of(
        new ChecklistTemplate("DOC_VEHICULO", "Documentacion del vehiculo", true),
        new ChecklistTemplate("CEDULA_CONSTANCIA", "Cedula/constancia", true),
        new ChecklistTemplate("MANUAL_PROPIETARIO", "Manual del propietario", true),
        new ChecklistTemplate("SEGUNDA_LLAVE", "Segunda llave", true),
        new ChecklistTemplate("SEGURO_VIGENTE", "Seguro vigente", true),
        new ChecklistTemplate("PATENTE_COLOCADA", "Patente colocada", true),
        new ChecklistTemplate("LAVADO_REALIZADO", "Lavado realizado", true),
        new ChecklistTemplate("PDI", "Inspeccion previa/PDI", true),
        new ChecklistTemplate("KIT_SEGURIDAD", "Kit de seguridad", true),
        new ChecklistTemplate("ACCESORIOS_ENTREGADOS", "Accesorios entregados", false)
    );

    private final EntregaUnidadRepository entregaUnidadRepository;
    private final EntregaChecklistItemRepository checklistItemRepository;
    private final VentaRepository ventaRepository;
    private final InventarioRepository inventarioRepository;
    private final VentaHistorialRepository ventaHistorialRepository;
    private final ClienteMapper clienteMapper;
    private final VehiculoMapper vehiculoMapper;
    private final InventarioMapper inventarioMapper;
    private final VentaMapper ventaMapper;

    public EntregaUnidadServiceImpl(
        EntregaUnidadRepository entregaUnidadRepository,
        EntregaChecklistItemRepository checklistItemRepository,
        VentaRepository ventaRepository,
        InventarioRepository inventarioRepository,
        VentaHistorialRepository ventaHistorialRepository,
        ClienteMapper clienteMapper,
        VehiculoMapper vehiculoMapper,
        InventarioMapper inventarioMapper,
        VentaMapper ventaMapper
    ) {
        this.entregaUnidadRepository = entregaUnidadRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.ventaRepository = ventaRepository;
        this.inventarioRepository = inventarioRepository;
        this.ventaHistorialRepository = ventaHistorialRepository;
        this.clienteMapper = clienteMapper;
        this.vehiculoMapper = vehiculoMapper;
        this.inventarioMapper = inventarioMapper;
        this.ventaMapper = ventaMapper;
    }

    @Override
    public EntregaUnidadDTO programarEntrega(Long ventaId, Instant fechaProgramada, String observaciones) {
        Venta venta = obtenerVentaConPermisos(ventaId);
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new BadRequestException("No se puede programar entrega para una venta cancelada");
        }
        if (venta.getEstado() != EstadoVenta.PAGADA && venta.getEstado() != EstadoVenta.FINALIZADA) {
            throw new BadRequestException("Solo se puede programar entrega para venta PAGADA o FINALIZADA");
        }
        if (
            entregaUnidadRepository.existsByVentaIdAndEstadoIn(
                ventaId,
                EnumSet.of(EstadoEntregaUnidad.PENDIENTE, EstadoEntregaUnidad.PROGRAMADA, EstadoEntregaUnidad.ENTREGADA)
            )
        ) {
            throw new BadRequestException("La venta ya tiene una entrega activa");
        }
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

        for (ChecklistTemplate template : CHECKLIST_DEFAULT) {
            EntregaChecklistItem item = new EntregaChecklistItem();
            item.setEntregaUnidad(entrega);
            item.setCodigo(template.codigo());
            item.setDescripcion(template.descripcion());
            item.setObligatorio(template.obligatorio());
            item.setCompletado(false);
            checklistItemRepository.save(item);
        }

        return toDto(entrega);
    }

    @Override
    public EntregaUnidadDTO actualizarChecklist(Long entregaId, List<EntregaChecklistItemDTO> items) {
        EntregaUnidad entrega = obtenerEntregaConPermisos(entregaId);
        if (entrega.getEstado() == EstadoEntregaUnidad.ENTREGADA || entrega.getEstado() == EstadoEntregaUnidad.CANCELADA) {
            throw new BadRequestException("No se puede modificar checklist para una entrega cerrada");
        }
        List<EntregaChecklistItem> actuales = checklistItemRepository.findAllByEntregaUnidadIdOrderByIdAsc(entregaId);
        for (EntregaChecklistItem actual : actuales) {
            for (EntregaChecklistItemDTO input : items) {
                if (actual.getId().equals(input.getId())) {
                    actual.setCompletado(Boolean.TRUE.equals(input.getCompletado()));
                    actual.setObservaciones(input.getObservaciones());
                }
            }
        }
        checklistItemRepository.saveAll(actuales);
        entrega.setLastModifiedDate(Instant.now());
        entregaUnidadRepository.save(entrega);
        return toDto(entrega);
    }

    @Override
    public EntregaUnidadDTO confirmarEntrega(Long entregaId, Integer kilometrajeEntrega, String nivelCombustible, String observaciones) {
        EntregaUnidad entrega = obtenerEntregaConPermisos(entregaId);
        if (entrega.getEstado() == EstadoEntregaUnidad.CANCELADA) {
            throw new BadRequestException("La entrega esta cancelada");
        }
        if (entrega.getEstado() == EstadoEntregaUnidad.ENTREGADA) {
            return toDto(entrega);
        }
        List<EntregaChecklistItem> checklist = checklistItemRepository.findAllByEntregaUnidadIdOrderByIdAsc(entregaId);
        boolean pendientesObligatorios = checklist.stream().anyMatch(i -> Boolean.TRUE.equals(i.getObligatorio()) && !Boolean.TRUE.equals(i.getCompletado()));
        if (pendientesObligatorios) {
            throw new BadRequestException("No se puede confirmar entrega con checklist obligatorio incompleto");
        }

        entrega.setEstado(EstadoEntregaUnidad.ENTREGADA);
        entrega.setFechaEntrega(Instant.now());
        entrega.setUsuarioEntrega(currentUserLogin());
        entrega.setKilometrajeEntrega(kilometrajeEntrega);
        entrega.setNivelCombustible(nivelCombustible);
        entrega.setObservaciones(observaciones);
        entrega.setLastModifiedDate(Instant.now());
        entregaUnidadRepository.save(entrega);

        Inventario inventario = inventarioRepository
            .findById(entrega.getInventario().getId())
            .orElseThrow(() -> new BadRequestException("Inventario no encontrado"));
        inventario.setEstadoInventario(EstadoInventario.VENDIDO);
        inventario.setFechaEgreso(Instant.now());
        inventarioRepository.save(inventario);

        registrarHistorialEntrega(entrega.getVenta(), "ENTREGA_CONFIRMADA", "Entrega de unidad confirmada");
        return toDto(entrega);
    }

    @Override
    public EntregaUnidadDTO cancelarEntrega(Long entregaId, String motivo) {
        EntregaUnidad entrega = obtenerEntregaConPermisos(entregaId);
        if (entrega.getEstado() == EstadoEntregaUnidad.ENTREGADA) {
            throw new BadRequestException("No se puede cancelar una entrega ya confirmada");
        }
        entrega.setEstado(EstadoEntregaUnidad.CANCELADA);
        entrega.setObservaciones(motivo);
        entrega.setLastModifiedDate(Instant.now());
        entregaUnidadRepository.save(entrega);
        registrarHistorialEntrega(entrega.getVenta(), "ENTREGA_CANCELADA", motivo != null ? motivo : "Entrega cancelada");
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

    private void registrarHistorialEntrega(Venta venta, String accion, String detalle) {
        VentaHistorial historial = new VentaHistorial();
        historial.setVenta(venta);
        historial.setFecha(Instant.now());
        historial.setEstadoAnterior(venta.getEstado());
        historial.setEstadoNuevo(venta.getEstado());
        historial.setAccion(accion);
        historial.setDetalle(detalle);
        historial.setUsuario(currentUserLogin());
        ventaHistorialRepository.save(historial);
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
        for (EntregaChecklistItem item : checklistItemRepository.findAllByEntregaUnidadIdOrderByIdAsc(entrega.getId())) {
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

    private record ChecklistTemplate(String codigo, String descripcion, boolean obligatorio) {}
}

