package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.TasacionUsado;
import com.concesionaria.app.domain.User;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.repository.MotorRepository;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.repository.TasacionUsadoRepository;
import com.concesionaria.app.repository.TipoVehiculoRepository;
import com.concesionaria.app.repository.UserRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.repository.VersionRepository;
import com.concesionaria.app.service.TasacionUsadoService;
import com.concesionaria.app.service.dto.TasacionUsadoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.TasacionUsadoMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TasacionUsadoServiceImpl implements TasacionUsadoService {

    private final TasacionUsadoRepository tasacionUsadoRepository;
    private final VentaRepository ventaRepository;
    private final VersionRepository versionRepository;
    private final MotorRepository motorRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final UserRepository userRepository;
    private final MonedaRepository monedaRepository;
    private final TasacionUsadoMapper tasacionUsadoMapper;

    @Value("${app.negocio.moneda-base-codigo:ARS}")
    private String monedaBaseCodigo;

    public TasacionUsadoServiceImpl(
        TasacionUsadoRepository tasacionUsadoRepository,
        VentaRepository ventaRepository,
        VersionRepository versionRepository,
        MotorRepository motorRepository,
        TipoVehiculoRepository tipoVehiculoRepository,
        UserRepository userRepository,
        MonedaRepository monedaRepository,
        TasacionUsadoMapper tasacionUsadoMapper
    ) {
        this.tasacionUsadoRepository = tasacionUsadoRepository;
        this.ventaRepository = ventaRepository;
        this.versionRepository = versionRepository;
        this.motorRepository = motorRepository;
        this.tipoVehiculoRepository = tipoVehiculoRepository;
        this.userRepository = userRepository;
        this.monedaRepository = monedaRepository;
        this.tasacionUsadoMapper = tasacionUsadoMapper;
    }

    @Override
    public TasacionUsadoDTO save(TasacionUsadoDTO dto) {
        TasacionUsado entity = tasacionUsadoMapper.toEntity(dto);
        normalizar(entity);
        TasacionUsado saved = tasacionUsadoRepository.save(entity);
        return enriquecerConVentaAplicada(tasacionUsadoMapper.toDto(saved));
    }

    @Override
    public TasacionUsadoDTO update(TasacionUsadoDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestException("La tasacion requiere id para actualizar");
        }
        TasacionUsado entity = tasacionUsadoMapper.toEntity(dto);
        normalizar(entity);
        TasacionUsado saved = tasacionUsadoRepository.save(entity);
        return enriquecerConVentaAplicada(tasacionUsadoMapper.toDto(saved));
    }

    @Override
    public Optional<TasacionUsadoDTO> partialUpdate(TasacionUsadoDTO dto) {
        return tasacionUsadoRepository
            .findById(dto.getId())
            .map(existing -> {
                tasacionUsadoMapper.partialUpdate(existing, dto);
                normalizar(existing);
                return existing;
            })
            .map(tasacionUsadoRepository::save)
            .map(saved -> saved)
            .map(tasacionUsadoMapper::toDto)
            .map(this::enriquecerConVentaAplicada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TasacionUsadoDTO> findAll(Pageable pageable) {
        return tasacionUsadoRepository.findAll(pageable).map(tasacionUsadoMapper::toDto).map(this::enriquecerConVentaAplicada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TasacionUsadoDTO> findAllCurrentUser(Pageable pageable) {
        return tasacionUsadoRepository.findAllCurrentUser(pageable).map(tasacionUsadoMapper::toDto).map(this::enriquecerConVentaAplicada);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TasacionUsadoDTO> findOne(Long id) {
        return tasacionUsadoRepository.findById(id).map(tasacionUsadoMapper::toDto).map(this::enriquecerConVentaAplicada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TasacionUsadoDTO> findByVentaId(Long ventaId) {
        if (ventaId == null) {
            return List.of();
        }
        return ventaRepository
            .findById(ventaId)
            .map(venta -> {
                if (venta.getTasacionUsado() == null || venta.getTasacionUsado().getId() == null) {
                    return List.<TasacionUsadoDTO>of();
                }
                return tasacionUsadoRepository
                    .findById(venta.getTasacionUsado().getId())
                    .map(tasacionUsadoMapper::toDto)
                    .map(this::enriquecerConVentaAplicada)
                    .map(List::of)
                    .orElse(List.of());
            })
            .orElse(List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TasacionUsadoDTO> findAceptadasDisponiblesByClienteId(Long clienteId) {
        if (clienteId == null) {
            throw new BadRequestException("El cliente es obligatorio para buscar tasaciones disponibles");
        }
        return tasacionUsadoRepository
            .findAceptadasDisponiblesByClienteId(clienteId)
            .stream()
            .map(tasacionUsadoMapper::toDto)
            .map(this::enriquecerConVentaAplicada)
            .toList();
    }

    @Override
    public void delete(Long id) {
        throw new BadRequestException("No se permite eliminar tasaciones. Se recomienda cambiar su estado");
    }

    private void normalizar(TasacionUsado entity) {
        Moneda monedaBase = monedaRepository
            .findByCodigoIgnoreCase(monedaBaseCodigo)
            .orElseThrow(() -> new BadRequestException("No existe la moneda base configurada"));
        if (entity.getMoneda() == null || entity.getMoneda().getId() == null) {
            entity.setMoneda(monedaBase);
        } else {
            Moneda moneda = monedaRepository
                .findById(entity.getMoneda().getId())
                .orElseThrow(() -> new BadRequestException("La moneda de la tasacion no existe"));
            if (!monedaBase.getId().equals(moneda.getId())) {
                throw new BadRequestException("La tasacion usada solo puede registrarse en moneda base ARS");
            }
            entity.setMoneda(moneda);
        }
        if (entity.getCliente() == null || entity.getCliente().getId() == null) {
            throw new BadRequestException("La tasacion debe asociarse a un cliente");
        }
        if (entity.getMontoTasacion() == null || entity.getMontoTasacion().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto de tasacion es invalido");
        }
        if (entity.getFechaTasacion() == null) {
            throw new BadRequestException("La fecha de tasacion es obligatoria");
        }
        if (entity.getEstado() == null) {
            throw new BadRequestException("El estado de tasacion es obligatorio");
        }
        if (entity.getEstado() == com.concesionaria.app.domain.enumeration.EstadoTasacionUsado.ACEPTADA) {
            if (entity.getVersion() == null || entity.getVersion().getId() == null || !versionRepository.existsById(entity.getVersion().getId())) {
                throw new BadRequestException("Para aceptar la tasacion debes seleccionar una version valida del catalogo");
            }
            if (entity.getTipoVehiculo() == null || entity.getTipoVehiculo().getId() == null || !tipoVehiculoRepository.existsById(entity.getTipoVehiculo().getId())) {
                throw new BadRequestException("Para aceptar la tasacion debes seleccionar un tipo de vehiculo valido");
            }
            if (entity.getMotor() != null && entity.getMotor().getId() != null && !motorRepository.existsById(entity.getMotor().getId())) {
                throw new BadRequestException("El motor seleccionado para la tasacion no existe");
            }
            if (entity.getTasadorUser() == null || entity.getTasadorUser().getId() == null) {
                throw new BadRequestException("Para aceptar la tasacion debes seleccionar un usuario tasador");
            }
            User tasador = userRepository
                .findById(entity.getTasadorUser().getId())
                .orElseThrow(() -> new BadRequestException("El usuario tasador seleccionado no existe"));
            if (!tasador.isActivated()) {
                throw new BadRequestException("El usuario tasador seleccionado esta inactivo");
            }
            entity.setTasadorUser(tasador);
            entity.setUsuarioTasador(tasador.getLogin());
            if ((entity.getPatenteUsado() == null || entity.getPatenteUsado().isBlank()) && (entity.getVinChasisUsado() == null || entity.getVinChasisUsado().isBlank())) {
                throw new BadRequestException("Para aceptar la tasacion debes informar patente o VIN/chasis");
            }
            if (entity.getAnioUsado() == null) {
                throw new BadRequestException("Para aceptar la tasacion debes informar anio del usado");
            }
            int anioActual = Year.now().getValue();
            if (entity.getAnioUsado() < 1950 || entity.getAnioUsado() > anioActual + 1) {
                throw new BadRequestException("El anio del usado debe ser razonable para una tasacion aceptada");
            }
            if (entity.getKmUsado() == null) {
                throw new BadRequestException("Para aceptar la tasacion debes informar kilometraje del usado");
            }
            if (entity.getKmUsado() < 0) {
                throw new BadRequestException("El kilometraje del usado no puede ser negativo");
            }
            if (entity.getColorUsado() == null || entity.getColorUsado().isBlank()) {
                throw new BadRequestException("Para aceptar la tasacion debes informar color del usado");
            }
        } else if (entity.getTasadorUser() != null && entity.getTasadorUser().getId() != null) {
            User tasador = userRepository
                .findById(entity.getTasadorUser().getId())
                .orElseThrow(() -> new BadRequestException("El usuario tasador seleccionado no existe"));
            if (!tasador.isActivated()) {
                throw new BadRequestException("El usuario tasador seleccionado esta inactivo");
            }
            entity.setTasadorUser(tasador);
            entity.setUsuarioTasador(tasador.getLogin());
        }
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(Instant.now());
        }
        entity.setLastModifiedDate(Instant.now());
    }

    private TasacionUsadoDTO enriquecerConVentaAplicada(TasacionUsadoDTO dto) {
        if (dto == null || dto.getId() == null) {
            return dto;
        }
        Long ventaId = ventaRepository.findAllByTasacionUsadoIdOrderByFechaDesc(dto.getId()).stream().findFirst().map(v -> v.getId()).orElse(null);
        dto.setVentaAplicadaId(ventaId);
        return dto;
    }
}
