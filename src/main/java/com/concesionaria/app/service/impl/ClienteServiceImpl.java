package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.service.ClienteService;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ClienteMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.concesionaria.app.domain.Cliente}.
 */
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

        Cliente cliente = clienteMapper.toEntity(clienteDTO);
        cliente = clienteRepository.save(cliente);

        return clienteMapper.toDto(cliente);
    }

    @Override
    public ClienteDTO update(ClienteDTO clienteDTO) {
        LOG.debug("Request to update Cliente : {}", clienteDTO);

        validarCliente(clienteDTO, clienteDTO.getId());

        Cliente cliente = clienteMapper.toEntity(clienteDTO);
        cliente = clienteRepository.save(cliente);

        return clienteMapper.toDto(cliente);
    }

    @Override
    public Optional<ClienteDTO> partialUpdate(ClienteDTO clienteDTO) {
        LOG.debug("Request to partially update Cliente : {}", clienteDTO);

        return clienteRepository
            .findById(clienteDTO.getId())
            .map(existing -> {
                clienteMapper.partialUpdate(existing, clienteDTO);

                validarCliente(clienteMapper.toDto(existing), existing.getId());

                return existing;
            })
            .map(clienteRepository::save)
            .map(clienteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Clientes");
        return clienteRepository.findAll(pageable).map(clienteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> findOne(Long id) {
        LOG.debug("Request to get Cliente : {}", id);
        return clienteRepository.findById(id).map(clienteMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Cliente : {}", id);
        clienteRepository.deleteById(id);
    }

    private void validarCliente(ClienteDTO dto, Long idActual) {

        // ======================
        // NOMBRE
        // ======================
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre es obligatorio");
        }

        dto.setNombre(dto.getNombre().trim().toUpperCase());

        // ======================
        // APELLIDO
        // ======================
        if (dto.getApellido() == null || dto.getApellido().trim().isEmpty()) {
            throw new BadRequestException("El apellido es obligatorio");
        }

        dto.setApellido(dto.getApellido().trim().toUpperCase());

        // ======================
        // DOCUMENTO
        // ======================
        // ======================
// DOCUMENTO
// ======================
        if (dto.getNroDocumento() == null || dto.getNroDocumento().trim().isEmpty()) {
            throw new BadRequestException("El documento es obligatorio");
        }

        String documento = dto.getNroDocumento().trim();

        if (!documento.matches("\\d{7,11}")) {
            throw new BadRequestException("El documento debe tener entre 7 y 11 dígitos");
        }

// duplicado documento
        Optional<Cliente> existenteDoc = clienteRepository.findByNroDocumento(documento);

        if (existenteDoc.isPresent() &&
            !existenteDoc.get().getId().equals(idActual)) {
            throw new BadRequestException("Ya existe un cliente con ese documento");
        }

        dto.setNroDocumento(documento);

        // ======================
        // EMAIL
        // ======================
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {

            String email = dto.getEmail().trim().toLowerCase();

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new BadRequestException("Email inválido");
            }

            Optional<Cliente> existenteEmail = clienteRepository.findByEmailIgnoreCase(email);

            if (existenteEmail.isPresent() &&
                !existenteEmail.get().getId().equals(idActual)) {
                throw new BadRequestException("Ya existe un cliente con ese email");
            }

            dto.setEmail(email);
        }
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> findByNroDocumento(String nroDocumento) {
        return clienteRepository.findByNroDocumento(nroDocumento)
            .map(clienteMapper::toDto);
    }

}
