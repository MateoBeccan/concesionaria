package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.AdjudicacionPlanAhorro;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.service.VentaService;
import com.concesionaria.app.service.dto.ClienteDTO;
import com.concesionaria.app.service.dto.VehiculoDTO;
import com.concesionaria.app.service.dto.VentaDTO;
import com.concesionaria.app.service.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class GeneradorVentaPlanAhorro {

    private static final BigDecimal PORCENTAJE_IMPUESTO = new BigDecimal("21.00");

    private final InventarioRepository inventarioRepository;
    private final VentaService ventaService;

    public GeneradorVentaPlanAhorro(InventarioRepository inventarioRepository, VentaService ventaService) {
        this.inventarioRepository = inventarioRepository;
        this.ventaService = ventaService;
    }

    public VentaDTO generarVentaDesdeAdjudicacion(AdjudicacionPlanAhorro adjudicacion) {
        Inventario inventario = inventarioRepository
            .findById(adjudicacion.getInventario().getId())
            .orElseThrow(() -> new BadRequestException("El inventario asignado no existe"));
        if (inventario.getEstadoInventario() != EstadoInventario.DISPONIBLE) {
            throw new BadRequestException("El inventario asignado ya no esta disponible");
        }

        VentaDTO nuevaVenta = new VentaDTO();
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setId(adjudicacion.getCliente().getId());
        nuevaVenta.setCliente(clienteDTO);
        VehiculoDTO vehiculoDTO = new VehiculoDTO();
        vehiculoDTO.setId(inventario.getVehiculo().getId());
        nuevaVenta.setVehiculo(vehiculoDTO);
        nuevaVenta.setFecha(Instant.now());
        nuevaVenta.setEstado(EstadoVenta.PENDIENTE);
        nuevaVenta.setPorcentajeImpuesto(PORCENTAJE_IMPUESTO);
        nuevaVenta.setTotalPagado(BigDecimal.ZERO);
        nuevaVenta.setObservaciones("Venta generada desde plan de ahorro contrato NÂ° " + adjudicacion.getContratoPlanAhorro().getNumeroContrato());
        return ventaService.saveDesdePlanAhorro(nuevaVenta);
    }

    public void actualizarDiferenciaAPagar(AdjudicacionPlanAhorro adjudicacion, Inventario inventario) {
        BigDecimal precioVehiculo = inventario.getVehiculo() != null && inventario.getVehiculo().getPrecio() != null
            ? inventario.getVehiculo().getPrecio().setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal diferencia = precioVehiculo.subtract(adjudicacion.getMontoReconocidoCuotas()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        adjudicacion.setDiferenciaAPagar(diferencia);
    }
}

