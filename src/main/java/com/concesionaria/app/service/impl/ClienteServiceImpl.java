package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.ClienteService;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ClienteMapper;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteServiceImpl.class);

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteServiceImpl(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Override
    public ClienteDTO save(ClienteDTO clienteDTO) {
        LOG.debug("Request to save Cliente : {}", clienteDTO);

        validarCliente(clienteDTO, null);
        Instant now = Instant.now();
        String currentUser = currentUserLogin();
        if (clienteDTO.getFechaAlta() == null) {
            clienteDTO.setFechaAlta(now);
        }
        clienteDTO.setCreatedDate(now);
        clienteDTO.setCreatedBy(currentUser);
        clienteDTO.setLastModifiedDate(now);
        clienteDTO.setLastModifiedBy(currentUser);

        Cliente cliente = clienteMapper.toEntity(clienteDTO);
        return clienteMapper.toDto(clienteRepository.save(cliente));
    }

    @Override
    public ClienteDTO update(ClienteDTO clienteDTO) {
        LOG.debug("Request to update Cliente : {}", clienteDTO);

        Cliente existente = clienteRepository.findById(clienteDTO.getId()).orElseThrow(() -> new BadRequestException("El cliente no existe"));
        validarCliente(clienteDTO, clienteDTO.getId());

        if (clienteDTO.getFechaAlta() == null) {
            clienteDTO.setFechaAlta(existente.getFechaAlta());
        }
        clienteDTO.setCreatedDate(existente.getCreatedDate());
        clienteDTO.setCreatedBy(existente.getCreatedBy());
        clienteDTO.setLastModifiedDate(Instant.now());
        clienteDTO.setLastModifiedBy(currentUserLogin());

        Cliente cliente = clienteMapper.toEntity(clienteDTO);
        return clienteMapper.toDto(clienteRepository.save(cliente));
    }

    @Override
    public Optional<ClienteDTO> partialUpdate(ClienteDTO clienteDTO) {
        LOG.debug("Request to partially update Cliente : {}", clienteDTO);

        return clienteRepository
            .findById(clienteDTO.getId())
            .map(existing -> {
                clienteMapper.partialUpdate(existing, clienteDTO);

                ClienteDTO merged = clienteMapper.toDto(existing);
                validarCliente(merged, existing.getId());
                existing.setNombre(merged.getNombre());
                existing.setApellido(merged.getApellido());
                existing.setNroDocumento(merged.getNroDocumento());
                existing.setEmail(merged.getEmail());
                existing.setTelefono(merged.getTelefono());
                existing.setFechaAlta(merged.getFechaAlta());

                if (existing.getCreatedDate() == null) {
                    existing.setCreatedDate(Instant.now());
                }
                if (existing.getCreatedBy() == null) {
                    existing.setCreatedBy(currentUserLogin());
                }
                existing.setLastModifiedDate(Instant.now());
                existing.setLastModifiedBy(currentUserLogin());
                return existing;
            })
            .map(clienteRepository::save)
            .map(clienteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> findAll(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(clienteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> findOne(Long id) {
        return clienteRepository.findById(id).map(clienteMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        clienteRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> findByNroDocumento(String nroDocumento) {
        if (nroDocumento == null || nroDocumento.isBlank()) {
            return Optional.empty();
        }
        return clienteRepository.findByNroDocumento(nroDocumento.trim()).map(clienteMapper::toDto);
    }

    private void validarCliente(ClienteDTO dto, Long idActual) {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre es obligatorio");
        }
        dto.setNombre(dto.getNombre().trim().toUpperCase(Locale.ROOT));

        if (dto.getApellido() == null || dto.getApellido().trim().isEmpty()) {
            throw new BadRequestException("El apellido es obligatorio");
        }
        dto.setApellido(dto.getApellido().trim().toUpperCase(Locale.ROOT));

        if (dto.getTipoDocumento() == null || dto.getTipoDocumento().getId() == null) {
            throw new BadRequestException("El tipo de documento es obligatorio");
        }

        if (dto.getNroDocumento() == null || dto.getNroDocumento().trim().isEmpty()) {
            throw new BadRequestException("El documento es obligatorio");
        }
        String documento = dto.getNroDocumento().trim();
        if (!documento.matches("\\d{7,11}")) {
            throw new BadRequestException("El documento debe tener entre 7 y 11 digitos");
        }

        clienteRepository
            .findByTipoDocumentoIdAndNroDocumento(dto.getTipoDocumento().getId(), documento)
            .filter(existing -> !existing.getId().equals(idActual))
            .ifPresent(existing -> {
                throw new BadRequestException("Ya existe un cliente con ese tipo y numero de documento");
            });
        dto.setNroDocumento(documento);

        String codigoTipoDocumento = dto.getTipoDocumento().getCodigo() != null
            ? dto.getTipoDocumento().getCodigo().trim().toUpperCase(Locale.ROOT)
            : "";
        if ((codigoTipoDocumento.contains("CUIT") || codigoTipoDocumento.contains("CUIL")) && documento.length() != 11) {
            throw new BadRequestException("El documento debe tener 11 digitos para el tipo seleccionado");
        }
        if (codigoTipoDocumento.contains("DNI") && (documento.length() < 7 || documento.length() > 8)) {
            throw new BadRequestException("El DNI debe tener entre 7 y 8 digitos");
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new BadRequestException("El email es obligatorio");
        }
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BadRequestException("Email invalido");
        }
        clienteRepository
            .findByEmailIgnoreCase(email)
            .filter(existing -> !existing.getId().equals(idActual))
            .ifPresent(existing -> {
                throw new BadRequestException("Ya existe un cliente con ese email");
            });
        dto.setEmail(email);

        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            dto.setTelefono(dto.getTelefono().trim());
        }

        if (dto.getFechaAlta() == null && idActual != null) {
            throw new BadRequestException("La fecha de alta es obligatoria");
        }
    }

    private String currentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElse("system");
    }
}
