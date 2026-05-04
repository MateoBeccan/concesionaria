package com.concesionaria.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.repository.ClienteRepository;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.CondicionIvaDTO;
import com.concesionaria.app.service.dto.TipoDocumentoDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.mapper.ClienteMapper;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplBusinessTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    private ClienteServiceImpl clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteServiceImpl(clienteRepository, clienteMapper);
    }

    @Test
    void noPermiteDuplicadoPorTipoDocumentoYNroDocumento() {
        ClienteDTO dto = new ClienteDTO();
        dto.setNombre("Juan");
        dto.setApellido("Perez");
        dto.setNroDocumento("30111222");
        dto.setEmail("juan@test.com");
        dto.setTelefono("1122334455");
        dto.setFechaAlta(Instant.now());
        dto.setActivo(true);

        TipoDocumentoDTO tipoDocumento = new TipoDocumentoDTO();
        tipoDocumento.setId(1L);
        tipoDocumento.setCodigo("DNI");
        dto.setTipoDocumento(tipoDocumento);

        dto.setCondicionIva(new CondicionIvaDTO());

        Cliente existente = new Cliente();
        existente.setId(99L);

        when(clienteRepository.findByTipoDocumentoIdAndNroDocumento(1L, "30111222")).thenReturn(Optional.of(existente));

        assertThrows(BadRequestException.class, () -> clienteService.save(dto));
    }
}
