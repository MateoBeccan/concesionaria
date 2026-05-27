package com.concesionaria.app.service.impl;

import com.concesionaria.app.config.BusinessProperties;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.InventarioHistorial;
import com.concesionaria.app.domain.Reserva;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.InventarioHistorialRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.ReservaService;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.dto.ReservaDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ReservaMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {
    private static final Logger LOG = LoggerFactory.getLogger(ReservaServiceImpl.class);

    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;
    private final InventarioRepository inventarioRepository;
    private final InventarioHistorialRepository inventarioHistorialRepository;
    private final PagoRepository pagoRepository;
    private final VentaRepository ventaRepository;
    private final BusinessProperties businessProperties;

    public ReservaServiceImpl(
        ReservaRepository reservaRepository,
        ReservaMapper reservaMapper,
        InventarioRepository inventarioRepository,
        InventarioHistorialRepository inventarioHistorialRepository,
        PagoRepository pagoRepository,
        VentaRepository ventaRepository,
        BusinessProperties businessProperties
    ) {
        this.reservaRepository = reservaRepository;
        this.reservaMapper = reservaMapper;
        this.inventarioRepository = inventarioRepository;
        this.inventarioHistorialRepository = inventarioHistorialRepository;
        this.pagoRepository = pagoRepository;
        this.ventaRepository = ventaRepository;
        this.businessProperties = businessProperties == null ? BusinessProperties.defaults() : businessProperties;
    }

    @Override
    public ReservaDTO save(ReservaDTO dto) {
        Reserva reserva = reservaMapper.toEntity(dto);
        validarReserva(reserva, null);
        Instant now = Instant.now();
        reserva.setCreatedDate(now);
        reserva.setLastModifiedDate(now);
        if (reserva.getUsuarioCreacion() == null || reserva.getUsuarioCreacion().isBlank()) {
            reserva.setUsuarioCreacion(currentUserLogin());
        }
        Reserva saved = reservaRepository.save(reserva);
        aplicarReservaEnInventario(saved, "RESERVA_CREADA", "Reserva creada desde modulo de reservas");
        return reservaMapper.toDto(saved);
    }

    @Override
    public ReservaDTO update(ReservaDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestException("La reserva requiere id para actualizar");
        }
        Reserva reserva = reservaMapper.toEntity(dto);
        validarReserva(reserva, reserva.getId());
        reserva.setLastModifiedDate(Instant.now());
        Reserva saved = reservaRepository.save(reserva);
        aplicarReservaEnInventario(saved, "RESERVA_ACTUALIZADA", "Reserva actualizada");
        return reservaMapper.toDto(saved);
    }

    @Override
    public Optional<ReservaDTO> partialUpdate(ReservaDTO dto) {
        return reservaRepository
            .findById(dto.getId())
            .map(existing -> {
                reservaMapper.partialUpdate(existing, dto);
                validarReserva(existing, existing.getId());
                existing.setLastModifiedDate(Instant.now());
                return existing;
            })
            .map(reservaRepository::save)
            .map(saved -> {
                aplicarReservaEnInventario(saved, "RESERVA_ACTUALIZADA", "Reserva actualizada parcialmente");
                return saved;
            })
            .map(reservaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaDTO> findAll(Pageable pageable) {
        return reservaRepository.findAll(pageable).map(this::toDtoEnriquecido);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaDTO> findAllCurrentUser(Pageable pageable) {
        return reservaRepository.findAllCurrentUser(currentUserLogin(), pageable).map(this::toDtoEnriquecido);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservaDTO> findOne(Long id) {
        validarAccesoReserva(id);
        Optional<ReservaDTO> result = reservaRepository.findById(id).map(this::toDtoEnriquecido);
        if (result.isEmpty()) {
            LOG.warn("Reserva inexistente para id {}", id);
        }
        return result;
    }

    @Override
    public void delete(Long id) {
        throw new BadRequestException("No se permite eliminar reservas. Deben cancelarse para conservar trazabilidad");
    }

    @Override
    public long expirarReservasVencidas() {
        long afectadas = 0L;
        for (Reserva reserva : reservaRepository.findAllByEstadoAndFechaVencimientoBefore(EstadoReserva.ACTIVA, Instant.now())) {
            Inventario inventario = reserva.getInventario();
            if (inventario != null && inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
                continue;
            }
            reserva.setEstado(EstadoReserva.VENCIDA);
            reserva.setLastModifiedDate(Instant.now());
            Reserva updated = reservaRepository.save(reserva);
            liberarInventario(updated, "RESERVA_VENCIDA", "Reserva vencida automaticamente");
            afectadas++;
        }
        return afectadas;
    }

    @Override
    public ReservaDTO cancelarReserva(Long id, String motivo) {
        Reserva reserva = reservaRepository.findById(id).orElseThrow(() -> new BadRequestException("La reserva no existe"));
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new BadRequestException("Solo pueden cancelarse reservas activas");
        }
        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setLastModifiedDate(Instant.now());
        Reserva updated = reservaRepository.save(reserva);
        liberarInventario(updated, "RESERVA_CANCELADA", motivo == null ? "Reserva cancelada" : motivo);
        return reservaMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservaDTO> findActivaByInventarioId(Long inventarioId) {
        return reservaRepository
            .findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(inventarioId, EstadoReserva.ACTIVA)
            .map(this::toDtoEnriquecido);
    }

    private void validarReserva(Reserva reserva, Long idActual) {
        if (reserva == null) {
            throw new BadRequestException("La reserva es obligatoria");
        }
        Long inventarioId = reserva.getInventario() != null ? reserva.getInventario().getId() : null;
        if (inventarioId == null) {
            throw new BadRequestException("La reserva requiere un inventario");
        }
        Inventario inventario = inventarioRepository.findById(inventarioId).orElseThrow(() -> new BadRequestException("El inventario no existe"));
        reserva.setInventario(inventario);

        if (reserva.getCliente() == null || reserva.getCliente().getId() == null) {
            throw new BadRequestException("La reserva requiere un cliente");
        }
        if (reserva.getFechaReserva() == null) {
            reserva.setFechaReserva(Instant.now());
        }
        if (reserva.getFechaVencimiento() == null) {
            reserva.setFechaVencimiento(sumarDiasVencimientoReserva(reserva.getFechaReserva()));
        }
        if (!reserva.getFechaVencimiento().isAfter(reserva.getFechaReserva())) {
            throw new BadRequestException("La fecha de vencimiento debe ser posterior a la fecha de reserva");
        }

        if (idActual == null) {
            if (inventario.getEstadoInventario() != EstadoInventario.DISPONIBLE) {
                throw new BadRequestException("Solo se puede reservar inventario con estado DISPONIBLE");
            }
            if (
                inventario.getVehiculo() != null &&
                inventario.getVehiculo().getId() != null &&
                ventaRepository.existsByVehiculoIdAndEstadoIn(
                    inventario.getVehiculo().getId(),
                    EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA, EstadoVenta.FINALIZADA)
                )
            ) {
                throw new BadRequestException("La unidad ya tiene una operacion activa y no puede reservarse");
            }
            reserva.setEstado(EstadoReserva.ACTIVA);
        } else if (reserva.getEstado() == null) {
            reserva.setEstado(EstadoReserva.ACTIVA);
        }
        reservaRepository
            .findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(reserva.getInventario().getId(), EstadoReserva.ACTIVA)
            .filter(existing -> !Objects.equals(existing.getId(), idActual))
            .ifPresent(existing -> {
                throw new BadRequestException("El inventario ya tiene una reserva activa");
            });
    }

    private void aplicarReservaEnInventario(Reserva reserva, String accion, String detalle) {
        Inventario inventario = inventarioRepository
            .findById(reserva.getInventario().getId())
            .orElseThrow(() -> new BadRequestException("Inventario inexistente para reserva"));

        EstadoInventario estadoAnterior = inventario.getEstadoInventario();
        if (reserva.getEstado() == EstadoReserva.ACTIVA) {
            if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
                throw new BadRequestException("No se puede reservar una unidad vendida");
            }
            inventario.setEstadoInventario(EstadoInventario.RESERVADO);
            inventario.setLastModifiedDate(Instant.now());
            inventario.setLastModifiedBy(currentUserLogin());
            inventarioRepository.save(inventario);
            registrarHistorial(inventario, estadoAnterior, EstadoInventario.RESERVADO, accion, detalle, reserva);
        }
    }

    private void liberarInventario(Reserva reserva, String accion, String detalle) {
        Inventario inventario = inventarioRepository
            .findById(reserva.getInventario().getId())
            .orElseThrow(() -> new BadRequestException("Inventario inexistente para reserva"));
        if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
            return;
        }
        EstadoInventario estadoAnterior = inventario.getEstadoInventario();
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setLastModifiedDate(Instant.now());
        inventario.setLastModifiedBy(currentUserLogin());
        inventarioRepository.save(inventario);
        registrarHistorial(inventario, estadoAnterior, EstadoInventario.DISPONIBLE, accion, detalle, reserva);
    }

    private void registrarHistorial(
        Inventario inventario,
        EstadoInventario estadoAnterior,
        EstadoInventario estadoNuevo,
        String accion,
        String detalle,
        Reserva reserva
    ) {
        InventarioHistorial historial = new InventarioHistorial();
        historial.setInventario(inventario);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setAccion(accion);
        historial.setDetalle(detalle);
        historial.setMotivo(detalle);
        historial.setCliente(reserva.getCliente());
        historial.setReserva(reserva);
        ventaRepository.findFirstByReservaIdOrderByFechaDesc(reserva.getId()).ifPresent(historial::setVenta);
        historial.setUsuario(currentUserLogin());
        historial.setFecha(Instant.now());
        inventarioHistorialRepository.save(historial);
    }

    private Instant sumarDiasVencimientoReserva(Instant base) {
        return base.atZone(ZoneOffset.UTC).plusDays(businessProperties.getReserva().getDiasVencimiento()).toInstant();
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }

    private void validarAccesoReserva(Long reservaId) {
        if (reservaId == null || SecurityUtils.hasCurrentUserAnyOfAuthorities("ROLE_ADMIN")) {
            return;
        }
        String login = currentUserLogin();
        boolean allowed = reservaRepository.existsAccessibleByIdForUser(reservaId, login);
        if (!allowed) {
            LOG.warn("Acceso denegado a reserva {} para usuario {}", reservaId, login);
            throw new AccessDeniedException("No tienes permisos para acceder a esta reserva");
        }
    }

    private ReservaDTO toDtoEnriquecido(Reserva reserva) {
        ReservaDTO dto = reservaMapper.toDto(reserva);
        completarContextoComercial(dto, reserva);
        return dto;
    }

    private void completarContextoComercial(ReservaDTO dto, Reserva reserva) {
        if (dto == null || reserva == null || reserva.getId() == null || reserva.getInventario() == null || reserva.getInventario().getVehiculo() == null) {
            return;
        }
        if (dto.getMoneda() == null && reserva.getMoneda() != null) {
            MonedaDTO monedaDTO = new MonedaDTO();
            monedaDTO.setId(reserva.getMoneda().getId());
            monedaDTO.setCodigo(reserva.getMoneda().getCodigo());
            monedaDTO.setSimbolo(reserva.getMoneda().getSimbolo());
            dto.setMoneda(monedaDTO);
        }
        BigDecimal totalPagadoReserva = pagoRepository.sumMontoByReservaId(reserva.getId());
        BigDecimal totalPagadoReservaNormalizado = totalPagadoReserva != null ? totalPagadoReserva.setScale(2, RoundingMode.HALF_UP) : null;
        if (
            totalPagadoReservaNormalizado != null &&
            (dto.getMontoSenia() == null || dto.getMontoSenia().compareTo(totalPagadoReservaNormalizado) < 0)
        ) {
            dto.setMontoSenia(totalPagadoReservaNormalizado);
        }

        Venta venta = ventaRepository.findFirstByReservaIdOrderByFechaDesc(reserva.getId()).orElse(null);

        if (venta == null) {
            return;
        }

        if (dto.getVentaAsociada() == null) {
            VentaDTO ventaDTO = new VentaDTO();
            ventaDTO.setId(venta.getId());
            ventaDTO.setEstado(venta.getEstado());
            dto.setVentaAsociada(ventaDTO);
        }
        if (dto.getMoneda() == null && venta.getMoneda() != null) {
            MonedaDTO monedaDTO = new MonedaDTO();
            monedaDTO.setId(venta.getMoneda().getId());
            monedaDTO.setCodigo(venta.getMoneda().getCodigo());
            monedaDTO.setSimbolo(venta.getMoneda().getSimbolo());
            dto.setMoneda(monedaDTO);
        }
        if (venta.getId() != null) {
            BigDecimal totalPagado = pagoRepository.sumMontoByVentaId(venta.getId());
            BigDecimal totalPagadoNormalizado = totalPagado != null
                ? totalPagado.setScale(2, RoundingMode.HALF_UP)
                : venta.getTotalPagado() != null
                    ? venta.getTotalPagado().setScale(2, RoundingMode.HALF_UP)
                    : null;

            if (
                totalPagadoNormalizado != null &&
                (dto.getMontoSenia() == null || dto.getMontoSenia().compareTo(totalPagadoNormalizado) < 0)
            ) {
                dto.setMontoSenia(totalPagadoNormalizado);
            }
        }
    }
}
