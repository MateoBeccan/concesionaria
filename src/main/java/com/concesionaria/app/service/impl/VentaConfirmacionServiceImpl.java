package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.OperacionIdempotente;
import com.concesionaria.app.repository.OperacionIdempotenteRepository;
import com.concesionaria.app.security.SecurityUtils;
import com.concesionaria.app.service.ComprobanteService;
import com.concesionaria.app.service.PagoService;
import com.concesionaria.app.service.VentaConfirmacionService;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.ComprobanteDTO;
import com.concesionaria.app.service.dto.PagoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import com.concesionaria.app.service.exception.IdempotencyConflictException;
import com.concesionaria.app.web.rest.vm.ConfirmarVentaRequestVM;
import com.concesionaria.app.web.rest.vm.ConfirmarVentaResponseVM;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VentaConfirmacionServiceImpl implements VentaConfirmacionService {

    private static final String TIPO_OPERACION_CONFIRMAR_VENTA = "CONFIRMAR_VENTA";

    private final VentaService ventaService;
    private final PagoService pagoService;
    private final ComprobanteService comprobanteService;
    private final OperacionIdempotenteRepository operacionIdempotenteRepository;
    private final ObjectMapper objectMapper;
    private final ObjectMapper deterministicObjectMapper;

    public VentaConfirmacionServiceImpl(
        VentaService ventaService,
        PagoService pagoService,
        ComprobanteService comprobanteService,
        OperacionIdempotenteRepository operacionIdempotenteRepository,
        ObjectMapper objectMapper
    ) {
        this.ventaService = ventaService;
        this.pagoService = pagoService;
        this.comprobanteService = comprobanteService;
        this.operacionIdempotenteRepository = operacionIdempotenteRepository;
        this.objectMapper = objectMapper;
        this.deterministicObjectMapper = objectMapper
            .copy()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    @Override
    public ConfirmarVentaResponseVM confirmarVenta(ConfirmarVentaRequestVM request, String idempotencyKey) {
        if (request == null || request.getVenta() == null) {
            throw new BadRequestException("Debe informar la venta a confirmar");
        }

        String usuario = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new BadRequestException("Usuario autenticado requerido"));
        String requestHash = requestHash(request);
        OperacionIdempotente operacion = reclamarOperacion(usuario, idempotencyKey, requestHash);
        if (!requestHash.equals(operacion.getRequestHash())) {
            throw new IdempotencyConflictException("La Idempotency-Key ya fue utilizada con otro payload");
        }
        if (operacion.getResponseData() != null && !operacion.getResponseData().isBlank()) {
            return responseFromJson(operacion.getResponseData());
        }

        VentaDTO ventaRequest = request.getVenta();
        VentaDTO ventaGuardada = ventaRequest.getId() == null ? ventaService.save(ventaRequest) : ventaService.update(ventaRequest);
        if (ventaGuardada.getId() == null) {
            throw new BadRequestException("No se pudo resolver la venta confirmada");
        }

        List<PagoDTO> pagosRegistrados = new ArrayList<>();
        for (PagoDTO pago : pagosNuevos(request.getPagos())) {
            pagosRegistrados.add(pagoService.registrarPago(ventaGuardada.getId(), pago));
        }

        ComprobanteDTO comprobante = null;
        if (request.getTipoComprobanteId() != null) {
            comprobante = comprobanteService.emitirComprobante(ventaGuardada.getId(), request.getTipoComprobanteId());
        }

        ConfirmarVentaResponseVM response = new ConfirmarVentaResponseVM();
        response.setVenta(ventaService.findOne(ventaGuardada.getId()).orElse(ventaGuardada));
        response.setPagos(pagosRegistrados);
        response.setComprobante(comprobante);
        operacion.setVentaId(response.getVenta().getId());
        operacion.setResponseData(responseToJson(response));
        operacionIdempotenteRepository.save(operacion);
        return response;
    }

    private OperacionIdempotente reclamarOperacion(String usuario, String idempotencyKey, String requestHash) {
        operacionIdempotenteRepository.claim(usuario, TIPO_OPERACION_CONFIRMAR_VENTA, idempotencyKey, requestHash);
        return operacionIdempotenteRepository
            .findOneByUsuarioAndTipoOperacionAndIdempotencyKey(usuario, TIPO_OPERACION_CONFIRMAR_VENTA, idempotencyKey)
            .orElseThrow(() -> new IdempotencyConflictException("No se pudo reclamar la operacion idempotente"));
    }

    private String requestHash(ConfirmarVentaRequestVM request) {
        try {
            byte[] payload = deterministicObjectMapper.writeValueAsBytes(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no esta disponible", e);
        } catch (Exception e) {
            throw new BadRequestException("No se pudo calcular el hash de la confirmacion", e);
        }
    }

    private String responseToJson(ConfirmarVentaResponseVM response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new IdempotencyConflictException("No se pudo almacenar la respuesta idempotente", e);
        }
    }

    private ConfirmarVentaResponseVM responseFromJson(String responseData) {
        try {
            return objectMapper.readValue(responseData.getBytes(StandardCharsets.UTF_8), ConfirmarVentaResponseVM.class);
        } catch (Exception e) {
            throw new IdempotencyConflictException("No se pudo recuperar la respuesta idempotente", e);
        }
    }

    private List<PagoDTO> pagosNuevos(List<PagoDTO> pagos) {
        if (pagos == null) {
            return List.of();
        }
        for (PagoDTO pago : pagos) {
            if (pago != null && pago.getId() != null) {
                throw new BadRequestException("La confirmacion transaccional solo acepta pagos nuevos");
            }
        }
        return pagos.stream().filter(pago -> pago != null).toList();
    }
}
