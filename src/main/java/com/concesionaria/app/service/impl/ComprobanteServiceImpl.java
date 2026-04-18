package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.repository.VentaRepository;
import com.concesionaria.app.service.ComprobanteService;
import com.concesionaria.app.service.dto.ComprobanteDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ComprobanteMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Comprobante}.
 */
@Service
@Transactional
public class ComprobanteServiceImpl implements ComprobanteService {

    private static final Logger LOG = LoggerFactory.getLogger(ComprobanteServiceImpl.class);

    private final ComprobanteRepository comprobanteRepository;

    private final ComprobanteMapper comprobanteMapper;

    private final VentaRepository ventaRepository;

    public ComprobanteServiceImpl(
        ComprobanteRepository comprobanteRepository,
        ComprobanteMapper comprobanteMapper,
        VentaRepository ventaRepository
    ) {
        this.comprobanteRepository = comprobanteRepository;
        this.comprobanteMapper = comprobanteMapper;
        this.ventaRepository = ventaRepository;
    }

    @Override
    public ComprobanteDTO save(ComprobanteDTO comprobanteDTO) {
        LOG.debug("Request to save Comprobante : {}", comprobanteDTO);
        Venta venta = validarComprobante(comprobanteDTO);
        Comprobante comprobante = comprobanteMapper.toEntity(comprobanteDTO);
        comprobante.setVenta(venta);
        comprobante.setCreatedDate(Instant.now());
        comprobante = comprobanteRepository.save(comprobante);
        return comprobanteMapper.toDto(comprobante);
    }

    @Override
    public ComprobanteDTO update(ComprobanteDTO comprobanteDTO) {
        LOG.debug("Request to update Comprobante : {}", comprobanteDTO);
        Venta venta = validarComprobante(comprobanteDTO);
        Comprobante comprobante = comprobanteMapper.toEntity(comprobanteDTO);
        comprobante.setVenta(venta);
        comprobante = comprobanteRepository.save(comprobante);
        return comprobanteMapper.toDto(comprobante);
    }

    @Override
    public Optional<ComprobanteDTO> partialUpdate(ComprobanteDTO comprobanteDTO) {
        LOG.debug("Request to partially update Comprobante : {}", comprobanteDTO);

        return comprobanteRepository
            .findById(comprobanteDTO.getId())
            .map(existingComprobante -> {
                comprobanteMapper.partialUpdate(existingComprobante, comprobanteDTO);
                ComprobanteDTO mergedDto = comprobanteMapper.toDto(existingComprobante);
                Venta venta = validarComprobante(mergedDto);
                existingComprobante.setVenta(venta);
                return existingComprobante;
            })
            .map(comprobanteRepository::save)
            .map(comprobanteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComprobanteDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Comprobantes");
        return comprobanteRepository.findAll(pageable).map(comprobanteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ComprobanteDTO> findOne(Long id) {
        LOG.debug("Request to get Comprobante : {}", id);
        return comprobanteRepository.findById(id).map(comprobanteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComprobanteDTO> findByVentaId(Long ventaId) {
        LOG.debug("Request to get Comprobantes by Venta : {}", ventaId);
        return comprobanteRepository.findAllByVentaIdWithRelaciones(ventaId).stream().map(comprobanteMapper::toDto).toList();
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Comprobante : {}", id);
        comprobanteRepository.deleteById(id);
    }

    private Venta validarComprobante(ComprobanteDTO comprobanteDTO) {
        Long ventaId = comprobanteDTO.getVenta() != null ? comprobanteDTO.getVenta().getId() : null;
        if (ventaId == null) {
            throw new BadRequestException("Debe informar la venta asociada al comprobante");
        }

        Venta venta = ventaRepository.findById(ventaId).orElseThrow(() -> new BadRequestException("La venta informada no existe"));
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new BadRequestException("No se puede emitir comprobante para una venta cancelada");
        }

        if (comprobanteDTO.getMoneda() == null || comprobanteDTO.getMoneda().getId() == null) {
            throw new BadRequestException("La moneda del comprobante es obligatoria");
        }
        if (venta.getMoneda() != null && !venta.getMoneda().getId().equals(comprobanteDTO.getMoneda().getId())) {
            throw new BadRequestException("La moneda del comprobante debe coincidir con la moneda de la venta");
        }

        validarImportes(comprobanteDTO, venta);
        return venta;
    }

    private void validarImportes(ComprobanteDTO comprobanteDTO, Venta venta) {
        if (comprobanteDTO.getImporteNeto() == null || comprobanteDTO.getImpuesto() == null || comprobanteDTO.getTotal() == null) {
            throw new BadRequestException("El comprobante debe incluir importe neto, impuesto y total");
        }
        BigDecimal totalCalculado = comprobanteDTO.getImporteNeto().add(comprobanteDTO.getImpuesto());
        if (totalCalculado.compareTo(comprobanteDTO.getTotal()) != 0) {
            throw new BadRequestException("El total del comprobante debe coincidir con importe neto + impuesto");
        }
        if (venta.getTotal() != null && comprobanteDTO.getTotal().compareTo(venta.getTotal()) != 0) {
            throw new BadRequestException("El total del comprobante debe coincidir con el total de la venta");
        }
    }
}
