package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.InventarioHistorial;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoReserva;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.domain.enumeration.EstadoVehiculo;
import com.concesionaria.app.repository.InventarioHistorialRepository;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.repository.ReservaRepository;
import com.concesionaria.app.repository.VehiculoRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.repository.VersionMotorRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.VehiculoService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.VehiculoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.VehiculoMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VehiculoServiceImpl implements VehiculoService {
    private static final int VIN_CHASIS_MIN_LENGTH = 8;
    private static final int VIN_CHASIS_MAX_LENGTH = 30;
    private static final int VIN_STANDARD_LENGTH = 17;
    private static final int[] VIN_WEIGHTS = { 8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2 };

    private final VehiculoRepository vehiculoRepository;
    private final VehiculoMapper vehiculoMapper;
    private final InventarioRepository inventarioRepository;
    private final VentaService ventaService;
    private final VentaRepository ventaRepository;
    private final VersionMotorRepository versionMotorRepository;
    private final InventarioHistorialRepository inventarioHistorialRepository;
    private final ReservaRepository reservaRepository;
    private final LegacySchemaCompatibilityService legacySchemaCompatibilityService;

    public VehiculoServiceImpl(
        VehiculoRepository vehiculoRepository,
        VehiculoMapper vehiculoMapper,
        InventarioRepository inventarioRepository,
        VentaService ventaService,
        VentaRepository ventaRepository,
        VersionMotorRepository versionMotorRepository,
        InventarioHistorialRepository inventarioHistorialRepository,
        ReservaRepository reservaRepository,
        LegacySchemaCompatibilityService legacySchemaCompatibilityService
    ) {
        this.vehiculoRepository = vehiculoRepository;
        this.vehiculoMapper = vehiculoMapper;
        this.inventarioRepository = inventarioRepository;
        this.ventaService = ventaService;
        this.ventaRepository = ventaRepository;
        this.versionMotorRepository = versionMotorRepository;
        this.inventarioHistorialRepository = inventarioHistorialRepository;
        this.reservaRepository = reservaRepository;
        this.legacySchemaCompatibilityService = legacySchemaCompatibilityService;
    }

    @Override
    public VehiculoDTO save(VehiculoDTO dto) {
        validarVehiculo(dto, null);
        Instant now = Instant.now();
        String currentUser = currentUserLogin();

        Vehiculo vehiculo = vehiculoMapper.toEntity(dto);
        vehiculo.setCreatedDate(now);
        vehiculo.setCreatedBy(currentUser);
        vehiculo.setLastModifiedDate(now);
        vehiculo.setLastModifiedBy(currentUser);

        Vehiculo saved = vehiculoRepository.save(vehiculo);

        Inventario inventario = new Inventario();
        inventario.setVehiculo(saved);
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setFechaIngreso(now);
        inventario.setCodigoInternoStock(generarCodigoInternoStock(saved));
        inventario.setNumeroInternoStock(inventario.getCodigoInternoStock());
        inventario.setCreatedDate(now);
        inventario.setCreatedBy(currentUser);
        inventario.setLastModifiedDate(now);
        inventario.setLastModifiedBy(currentUser);
        legacySchemaCompatibilityService.ensureInventarioDisponibleDefault();
        inventario = inventarioRepository.save(inventario);

        registrarHistorial(inventario, null, EstadoInventario.DISPONIBLE, "ALTA_VEHICULO", "Ingreso inicial desde alta de vehiculo");
        return vehiculoMapper.toDto(saved);
    }

    @Override
    public VehiculoDTO update(VehiculoDTO vehiculoDTO) {
        validarVehiculo(vehiculoDTO, vehiculoDTO.getId());

        Vehiculo existente = vehiculoRepository.findById(vehiculoDTO.getId()).orElseThrow(() -> new BadRequestException("El vehiculo no existe"));
        Vehiculo vehiculo = vehiculoMapper.toEntity(vehiculoDTO);
        vehiculo.setCreatedDate(existente.getCreatedDate());
        vehiculo.setCreatedBy(existente.getCreatedBy());
        vehiculo.setLastModifiedDate(Instant.now());
        vehiculo.setLastModifiedBy(currentUserLogin());

        return vehiculoMapper.toDto(vehiculoRepository.save(vehiculo));
    }

    @Override
    public Optional<VehiculoDTO> partialUpdate(VehiculoDTO vehiculoDTO) {
        return vehiculoRepository
            .findById(vehiculoDTO.getId())
            .map(existente -> {
                if (vehiculoDTO.getPatente() != null) {
                    vehiculoDTO.setPatente(normalizarPatenteNullable(vehiculoDTO.getPatente()));
                }
                if (vehiculoDTO.getVinChasis() != null) {
                    vehiculoDTO.setVinChasis(normalizarVinChasisNullable(vehiculoDTO.getVinChasis()));
                }

                vehiculoMapper.partialUpdate(existente, vehiculoDTO);
                validarVehiculo(vehiculoMapper.toDto(existente), existente.getId());
                if (existente.getCreatedBy() == null) {
                    existente.setCreatedBy(currentUserLogin());
                }
                existente.setLastModifiedDate(Instant.now());
                existente.setLastModifiedBy(currentUserLogin());
                return existente;
            })
            .map(vehiculoRepository::save)
            .map(vehiculoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculoDTO> findAll(Pageable pageable) {
        return vehiculoRepository.findAllBy(pageable).map(vehiculoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoDTO> findAllWhereInventarioIsNull() {
        return vehiculoRepository.findAll().stream().filter(vehiculo -> vehiculo.getInventario() == null).map(vehiculoMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehiculoDTO> findOne(Long id) {
        return vehiculoRepository.findById(id).map(vehiculoMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        inventarioRepository.findByVehiculoId(id).ifPresent(inventarioRepository::delete);
        vehiculoRepository.deleteById(id);
    }

    @Override
    public void reservarVehiculo(Long vehiculoId, Long clienteId) {
        throw new BadRequestException("La reserva directa no esta permitida. Inicia una venta y registra la seña minima");
    }

    @Override
    public void venderVehiculo(Long vehiculoId, Long clienteId) {
        ventaService.crearVenta(vehiculoId, clienteId);
    }

    @Override
    public void cancelarReserva(Long vehiculoId) {
        throw new BadRequestException("La cancelacion de reserva se gestiona desde la entidad Reserva");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehiculoDTO> findByPatente(String patente) {
        String patenteNormalizada = normalizarPatenteNullable(patente);
        if (patenteNormalizada == null) {
            return Optional.empty();
        }
        return vehiculoRepository.findByPatente(patenteNormalizada).map(vehiculoMapper::toDto);
    }

    private void validarVehiculo(VehiculoDTO vehiculoDTO, Long idActual) {
        if (vehiculoDTO == null) {
            throw new BadRequestException("El vehiculo es obligatorio");
        }

        if (idActual != null) {
            inventarioRepository.findByVehiculoId(idActual).ifPresent(inventario -> {
                if (inventario.getEstadoInventario() == EstadoInventario.VENDIDO) {
                    throw new BadRequestException("No se puede editar un vehiculo ya vendido");
                }
            });
        }

        String patenteNormalizada = normalizarPatenteNullable(vehiculoDTO.getPatente());
        vehiculoDTO.setPatente(patenteNormalizada);
        String vinChasisNormalizado = normalizarVinChasisNullable(vehiculoDTO.getVinChasis());
        vehiculoDTO.setVinChasis(vinChasisNormalizado);

        if (EstadoVehiculo.USADO.equals(vehiculoDTO.getEstado()) && patenteNormalizada == null) {
            throw new BadRequestException("La patente es obligatoria para vehiculos usados");
        }
        if (vehiculoDTO.getPrecio() == null || vehiculoDTO.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El precio del vehiculo debe ser mayor a 0");
        }
        if (vehiculoDTO.getMoneda() == null || vehiculoDTO.getMoneda().getId() == null) {
            throw new BadRequestException("La moneda del vehiculo es obligatoria");
        }

        if (patenteNormalizada != null) {
            vehiculoRepository
                .findByPatenteIgnoreCase(patenteNormalizada)
                .filter(vehiculo -> !Objects.equals(vehiculo.getId(), idActual))
                .ifPresent(vehiculo -> {
                    throw new BadRequestException("Ya existe un vehiculo con esa patente");
                });
        }
        validarVinChasis(vinChasisNormalizado);
        if (vinChasisNormalizado != null) {
            vehiculoRepository
                .findByVinChasisIgnoreCase(vinChasisNormalizado)
                .filter(vehiculo -> !Objects.equals(vehiculo.getId(), idActual))
                .ifPresent(vehiculo -> {
                    throw new BadRequestException("Ya existe un vehiculo con ese numero de chasis");
                });
        }

        if (
            vehiculoDTO.getVersion() != null &&
            vehiculoDTO.getVersion().getId() != null &&
            vehiculoDTO.getMotor() != null &&
            vehiculoDTO.getMotor().getId() != null &&
            !versionMotorRepository.existsByVersionIdAndMotorId(vehiculoDTO.getVersion().getId(), vehiculoDTO.getMotor().getId())
        ) {
            throw new BadRequestException("El motor seleccionado no es compatible con la version elegida");
        }
    }

    private String normalizarPatenteNullable(String patente) {
        if (patente == null || patente.trim().isEmpty()) {
            return null;
        }
        String patenteNormalizada = patente.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return patenteNormalizada.isEmpty() ? null : patenteNormalizada;
    }

    private String normalizarVinChasisNullable(String vinChasis) {
        if (vinChasis == null || vinChasis.trim().isEmpty()) {
            return null;
        }
        String vinNormalizado = vinChasis.trim().toUpperCase(Locale.ROOT).replaceAll("[\\s-]", "");
        return vinNormalizado.isEmpty() ? null : vinNormalizado;
    }

    private void validarVinChasis(String vinChasis) {
        if (vinChasis == null) {
            return;
        }
        if (vinChasis.length() < VIN_CHASIS_MIN_LENGTH || vinChasis.length() > VIN_CHASIS_MAX_LENGTH) {
            throw new BadRequestException("El numero de chasis debe tener entre 8 y 30 caracteres");
        }
        if (!vinChasis.matches("^[A-Z0-9]+$")) {
            throw new BadRequestException("El numero de chasis solo puede contener letras y numeros");
        }
        if (vinChasis.length() == VIN_STANDARD_LENGTH) {
            if (!vinChasis.matches("^[A-HJ-NPR-Z0-9]{17}$")) {
                throw new BadRequestException("El VIN no puede contener los caracteres I, O o Q");
            }
            if (!esVinCheckDigitValido(vinChasis)) {
                throw new BadRequestException("El VIN informado no supera la validacion del digito verificador");
            }
        }
    }

    private boolean esVinCheckDigitValido(String vin) {
        int suma = 0;
        for (int i = 0; i < VIN_STANDARD_LENGTH; i++) {
            int valor = transliterarVin(vin.charAt(i));
            if (valor < 0) {
                return false;
            }
            suma += valor * VIN_WEIGHTS[i];
        }
        int resto = suma % 11;
        char esperado = resto == 10 ? 'X' : Character.forDigit(resto, 10);
        return vin.charAt(8) == esperado;
    }

    private int transliterarVin(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        return switch (c) {
            case 'A', 'J' -> 1;
            case 'B', 'K', 'S' -> 2;
            case 'C', 'L', 'T' -> 3;
            case 'D', 'M', 'U' -> 4;
            case 'E', 'N', 'V' -> 5;
            case 'F', 'W' -> 6;
            case 'G', 'P', 'X' -> 7;
            case 'H', 'Y' -> 8;
            case 'R', 'Z' -> 9;
            default -> -1;
        };
    }

    private String generarCodigoInternoStock(Vehiculo vehiculo) {
        String sufijo = vehiculo != null && vehiculo.getId() != null ? vehiculo.getId().toString() : String.valueOf(System.currentTimeMillis());
        return ("INV-" + sufijo).toUpperCase(Locale.ROOT);
    }

    private void registrarHistorial(
        Inventario inventario,
        EstadoInventario estadoAnterior,
        EstadoInventario estadoNuevo,
        String accion,
        String detalle
    ) {
        InventarioHistorial historial = new InventarioHistorial();
        historial.setInventario(inventario);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setAccion(accion);
        historial.setDetalle(detalle);
        historial.setMotivo(detalle);
        historial.setFecha(Instant.now());
        historial.setUsuario(currentUserLogin());
        inventarioHistorialRepository.save(historial);
    }

    private void liberarReservaSiVencida(Inventario inventario) {
        if (inventario.getEstadoInventario() != EstadoInventario.RESERVADO) {
            return;
        }
        var reservaActivaOpt = reservaRepository.findFirstByInventarioIdAndEstadoOrderByFechaReservaDesc(inventario.getId(), EstadoReserva.ACTIVA);
        if (reservaActivaOpt.isEmpty()) {
            return;
        }
        var reservaActiva = reservaActivaOpt.get();
        Instant vencimiento = reservaActiva.getFechaVencimiento();
        if (vencimiento == null || vencimiento.isAfter(Instant.now())) {
            return;
        }
        if (inventario.getVehiculo() != null && inventario.getVehiculo().getId() != null) {
            boolean ventaActiva = ventaRepository.existsByVehiculoIdAndEstadoIn(
                inventario.getVehiculo().getId(),
                EnumSet.of(EstadoVenta.PENDIENTE, EstadoVenta.RESERVADA, EstadoVenta.PAGADA, EstadoVenta.FINALIZADA)
            );
            if (ventaActiva) {
                return;
            }
        }

        EstadoInventario anterior = inventario.getEstadoInventario();
        inventario.setEstadoInventario(EstadoInventario.DISPONIBLE);
        inventario.setLastModifiedDate(Instant.now());
        inventario.setLastModifiedBy(currentUserLogin());
        inventarioRepository.save(inventario);

        reservaActiva.setEstado(EstadoReserva.VENCIDA);
        reservaActiva.setLastModifiedDate(Instant.now());
        reservaRepository.save(reservaActiva);

        registrarHistorial(inventario, anterior, EstadoInventario.DISPONIBLE, "RESERVA_EXPIRADA", "Reserva vencida automaticamente");
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }

}
