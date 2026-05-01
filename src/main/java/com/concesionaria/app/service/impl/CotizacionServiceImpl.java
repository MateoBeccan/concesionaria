package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cotizacion;
import com.concesionaria.app.repository.CotizacionRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.CurrencyConversionService;
import com.concesionaria.app.service.CotizacionService;
import com.concesionaria.app.service.dto.CotizacionConversionDTO;
import com.concesionaria.app.service.dto.CotizacionDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.CotizacionMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Cotizacion}.
 */
@Service
@Transactional
public class CotizacionServiceImpl implements CotizacionService {

    private static final Logger LOG = LoggerFactory.getLogger(CotizacionServiceImpl.class);

    private final CotizacionRepository cotizacionRepository;

    private final CotizacionMapper cotizacionMapper;
    private final CurrencyConversionService currencyConversionService;

    public CotizacionServiceImpl(
        CotizacionRepository cotizacionRepository,
        CotizacionMapper cotizacionMapper,
        CurrencyConversionService currencyConversionService
    ) {
        this.cotizacionRepository = cotizacionRepository;
        this.cotizacionMapper = cotizacionMapper;
        this.currencyConversionService = currencyConversionService;
    }

    @Override
    public CotizacionDTO save(CotizacionDTO cotizacionDTO) {
        LOG.debug("Request to save Cotizacion : {}", cotizacionDTO);
        Cotizacion cotizacion = cotizacionMapper.toEntity(cotizacionDTO);
        Instant now = Instant.now();
        String currentUser = currentUserLogin();
        cotizacion.setCreatedDate(now);
        cotizacion.setCreatedBy(currentUser);
        cotizacion.setLastModifiedDate(now);
        cotizacion.setLastModifiedBy(currentUser);
        cotizacion = cotizacionRepository.save(cotizacion);
        return cotizacionMapper.toDto(cotizacion);
    }

    @Override
    public CotizacionDTO update(CotizacionDTO cotizacionDTO) {
        LOG.debug("Request to update Cotizacion : {}", cotizacionDTO);
        Cotizacion existing = cotizacionRepository.findById(cotizacionDTO.getId()).orElseThrow(() ->
            new BadRequestException("La cotizacion no existe")
        );
        Cotizacion cotizacion = cotizacionMapper.toEntity(cotizacionDTO);
        cotizacion.setCreatedDate(existing.getCreatedDate());
        cotizacion.setCreatedBy(existing.getCreatedBy());
        cotizacion.setLastModifiedDate(Instant.now());
        cotizacion.setLastModifiedBy(currentUserLogin());
        cotizacion = cotizacionRepository.save(cotizacion);
        return cotizacionMapper.toDto(cotizacion);
    }

    @Override
    public Optional<CotizacionDTO> partialUpdate(CotizacionDTO cotizacionDTO) {
        LOG.debug("Request to partially update Cotizacion : {}", cotizacionDTO);

        return cotizacionRepository
            .findById(cotizacionDTO.getId())
            .map(existingCotizacion -> {
                cotizacionMapper.partialUpdate(existingCotizacion, cotizacionDTO);
                if (existingCotizacion.getCreatedDate() == null) {
                    existingCotizacion.setCreatedDate(Instant.now());
                }
                if (existingCotizacion.getCreatedBy() == null) {
                    existingCotizacion.setCreatedBy(currentUserLogin());
                }
                existingCotizacion.setLastModifiedDate(Instant.now());
                existingCotizacion.setLastModifiedBy(currentUserLogin());

                return existingCotizacion;
            })
            .map(cotizacionRepository::save)
            .map(cotizacionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CotizacionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Cotizacions");
        return cotizacionRepository.findAll(pageable).map(cotizacionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CotizacionDTO> findOne(Long id) {
        LOG.debug("Request to get Cotizacion : {}", id);
        return cotizacionRepository.findById(id).map(cotizacionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Cotizacion : {}", id);
        cotizacionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CotizacionConversionDTO convertirMonto(BigDecimal monto, Long monedaOrigenId, Long monedaDestinoId, Instant fechaOperacion) {
        return currencyConversionService.convertir(monto, monedaOrigenId, monedaDestinoId, fechaOperacion);
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }
}
