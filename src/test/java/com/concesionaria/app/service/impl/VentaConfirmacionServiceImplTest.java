package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.OperacionIdempotente;
import com.concesionaria.app.repository.OperacionIdempotenteRepository;
import com.concesionaria.app.service.ComprobanteService;
import com.concesionaria.app.service.PagoService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.ComprobanteDTO;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.exception.IdempotencyConflictException;
import com.concesionaria.app.web.rest.vm.ConfirmarVentaRequestVM;
import com.concesionaria.app.web.rest.vm.ConfirmarVentaResponseVM;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class VentaConfirmacionServiceImplTest {

    private static final String TIPO_OPERACION = "CONFIRMAR_VENTA";

    @Mock private VentaService ventaService;
    @Mock private PagoService pagoService;
    @Mock private ComprobanteService comprobanteService;
    @Mock private OperacionIdempotenteRepository operacionIdempotenteRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void confirmaVentaPagosYComprobanteEnOrden() {
        autenticar("admin");
        VentaConfirmacionServiceImpl service = service();
        VentaDTO ventaRequest = ventaDto(null);
        VentaDTO ventaGuardada = ventaDto(10L);
        PagoDTO pagoRequest = new PagoDTO();
        PagoDTO pagoGuardado = new PagoDTO();
        pagoGuardado.setId(20L);
        ComprobanteDTO comprobante = new ComprobanteDTO();
        comprobante.setId(30L);
        ConfirmarVentaRequestVM request = request(ventaRequest, List.of(pagoRequest), 40L);
        prepararOperacionNueva("admin", "key-a");

        when(ventaService.save(ventaRequest)).thenReturn(ventaGuardada);
        when(pagoService.registrarPago(10L, pagoRequest)).thenReturn(pagoGuardado);
        when(comprobanteService.emitirComprobante(10L, 40L)).thenReturn(comprobante);
        when(ventaService.findOne(10L)).thenReturn(Optional.of(ventaGuardada));

        var response = service.confirmarVenta(request, "key-a");

        assertThat(response.getVenta()).isSameAs(ventaGuardada);
        assertThat(response.getPagos()).containsExactly(pagoGuardado);
        assertThat(response.getComprobante()).isSameAs(comprobante);
        ArgumentCaptor<OperacionIdempotente> operacionCaptor = ArgumentCaptor.forClass(OperacionIdempotente.class);
        verify(operacionIdempotenteRepository).save(operacionCaptor.capture());
        assertThat(operacionCaptor.getValue().getVentaId()).isEqualTo(10L);
        assertThat(operacionCaptor.getValue().getResponseData()).isNotBlank();
        var inOrder = org.mockito.Mockito.inOrder(ventaService, pagoService, comprobanteService);
        inOrder.verify(ventaService).save(ventaRequest);
        inOrder.verify(pagoService).registrarPago(10L, pagoRequest);
        inOrder.verify(comprobanteService).emitirComprobante(10L, 40L);
    }

    @Test
    void retryConMismaKeyYPayloadDevuelveRespuestaSinReejecutarWorkflow() throws Exception {
        autenticar("admin");
        VentaConfirmacionServiceImpl service = service();
        ConfirmarVentaRequestVM request = request(ventaDto(null), List.of(new PagoDTO()), 40L);
        ConfirmarVentaResponseVM responseGuardada = new ConfirmarVentaResponseVM();
        responseGuardada.setVenta(ventaDto(10L));
        PagoDTO pagoGuardado = new PagoDTO();
        pagoGuardado.setId(20L);
        responseGuardada.setPagos(List.of(pagoGuardado));
        prepararOperacionRetry("admin", "key-a", objectMapper().writeValueAsString(responseGuardada));

        var response = service.confirmarVenta(request, "key-a");

        assertThat(response.getVenta().getId()).isEqualTo(10L);
        assertThat(response.getPagos()).extracting(PagoDTO::getId).containsExactly(20L);
        verify(ventaService, never()).save(any());
        verify(pagoService, never()).registrarPago(any(), any());
        verify(comprobanteService, never()).emitirComprobante(any(), any());
    }

    @Test
    void mismaKeyConPayloadDistintoDevuelveConflicto() {
        autenticar("admin");
        VentaConfirmacionServiceImpl service = service();
        ConfirmarVentaRequestVM request = request(ventaDto(null), List.of(new PagoDTO()), 40L);
        OperacionIdempotente operacion = operacion("admin", "key-a", "hash-distinto");

        when(operacionIdempotenteRepository.claim(eq("admin"), eq(TIPO_OPERACION), eq("key-a"), any())).thenReturn(0);
        when(operacionIdempotenteRepository.findOneByUsuarioAndTipoOperacionAndIdempotencyKey("admin", TIPO_OPERACION, "key-a"))
            .thenReturn(Optional.of(operacion));

        assertThatThrownBy(() -> service.confirmarVenta(request, "key-a")).isInstanceOf(IdempotencyConflictException.class);

        verify(ventaService, never()).save(any());
        verify(pagoService, never()).registrarPago(any(), any());
    }

    @Test
    void distintaKeyGeneraNuevaOperacion() {
        autenticar("admin");
        VentaConfirmacionServiceImpl service = service();
        VentaDTO ventaRequest = ventaDto(null);
        VentaDTO ventaGuardada = ventaDto(10L);
        ConfirmarVentaRequestVM request = request(ventaRequest, List.of(), null);
        prepararOperacionNueva("admin", "key-b");

        when(ventaService.save(ventaRequest)).thenReturn(ventaGuardada);
        when(ventaService.findOne(10L)).thenReturn(Optional.of(ventaGuardada));

        service.confirmarVenta(request, "key-b");

        verify(operacionIdempotenteRepository).claim(eq("admin"), eq(TIPO_OPERACION), eq("key-b"), any());
        verify(ventaService).save(ventaRequest);
    }

    @Test
    void mismoHeaderDeOtroUsuarioNoColisiona() {
        autenticar("user2");
        VentaConfirmacionServiceImpl service = service();
        VentaDTO ventaRequest = ventaDto(null);
        VentaDTO ventaGuardada = ventaDto(11L);
        ConfirmarVentaRequestVM request = request(ventaRequest, List.of(), null);
        prepararOperacionNueva("user2", "key-a");

        when(ventaService.save(ventaRequest)).thenReturn(ventaGuardada);
        when(ventaService.findOne(11L)).thenReturn(Optional.of(ventaGuardada));

        service.confirmarVenta(request, "key-a");

        verify(operacionIdempotenteRepository).claim(eq("user2"), eq(TIPO_OPERACION), eq("key-a"), any());
        verify(ventaService).save(ventaRequest);
    }

    @Test
    void noCompletaOperacionIdempotenteSiFallaUnPago() {
        autenticar("admin");
        VentaConfirmacionServiceImpl service = service();
        VentaDTO ventaRequest = ventaDto(null);
        VentaDTO ventaGuardada = ventaDto(10L);
        PagoDTO pagoRequest = new PagoDTO();
        ConfirmarVentaRequestVM request = request(ventaRequest, List.of(pagoRequest), 40L);
        prepararOperacionNueva("admin", "key-a");

        when(ventaService.save(ventaRequest)).thenReturn(ventaGuardada);
        when(pagoService.registrarPago(10L, pagoRequest)).thenThrow(new BadRequestException("pago invalido"));

        assertThatThrownBy(() -> service.confirmarVenta(request, "key-a"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("pago invalido");

        verify(comprobanteService, never()).emitirComprobante(10L, 40L);
        verify(operacionIdempotenteRepository, never()).save(any());
    }

    private VentaConfirmacionServiceImpl service() {
        return new VentaConfirmacionServiceImpl(
            ventaService,
            pagoService,
            comprobanteService,
            operacionIdempotenteRepository,
            objectMapper()
        );
    }

    private void prepararOperacionNueva(String usuario, String idempotencyKey) {
        final OperacionIdempotente[] operacion = new OperacionIdempotente[1];
        when(operacionIdempotenteRepository.claim(eq(usuario), eq(TIPO_OPERACION), eq(idempotencyKey), any())).thenAnswer(invocation -> {
            operacion[0] = operacion(usuario, idempotencyKey, invocation.getArgument(3));
            return 1;
        });
        when(operacionIdempotenteRepository.findOneByUsuarioAndTipoOperacionAndIdempotencyKey(usuario, TIPO_OPERACION, idempotencyKey))
            .thenAnswer(invocation -> Optional.of(operacion[0]));
    }

    private void prepararOperacionRetry(String usuario, String idempotencyKey, String responseData) {
        final OperacionIdempotente[] operacion = new OperacionIdempotente[1];
        when(operacionIdempotenteRepository.claim(eq(usuario), eq(TIPO_OPERACION), eq(idempotencyKey), any())).thenAnswer(invocation -> {
            operacion[0] = operacion(usuario, idempotencyKey, invocation.getArgument(3));
            operacion[0].setResponseData(responseData);
            return 0;
        });
        when(operacionIdempotenteRepository.findOneByUsuarioAndTipoOperacionAndIdempotencyKey(usuario, TIPO_OPERACION, idempotencyKey))
            .thenAnswer(invocation -> Optional.of(operacion[0]));
    }

    private OperacionIdempotente operacion(String usuario, String idempotencyKey, String requestHash) {
        OperacionIdempotente operacion = new OperacionIdempotente();
        operacion.setUsuario(usuario);
        operacion.setTipoOperacion(TIPO_OPERACION);
        operacion.setIdempotencyKey(idempotencyKey);
        operacion.setRequestHash(requestHash);
        return operacion;
    }

    private ConfirmarVentaRequestVM request(VentaDTO venta, List<PagoDTO> pagos, Long tipoComprobanteId) {
        ConfirmarVentaRequestVM request = new ConfirmarVentaRequestVM();
        request.setVenta(venta);
        request.setPagos(pagos);
        request.setTipoComprobanteId(tipoComprobanteId);
        return request;
    }

    private VentaDTO ventaDto(Long id) {
        VentaDTO venta = new VentaDTO();
        venta.setId(id);
        venta.setFecha(Instant.now());
        return venta;
    }

    private void autenticar(String username) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(username, "n/a"));
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
