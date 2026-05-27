package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.PlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

@Service
public class CuotaPlanAhorroGenerator {

    private final CuotaPlanAhorroRepository cuotaRepository;

    public CuotaPlanAhorroGenerator(CuotaPlanAhorroRepository cuotaRepository) {
        this.cuotaRepository = cuotaRepository;
    }

    public void generarCuotas(ContratoPlanAhorro contrato, PlanAhorro plan, Instant fechaInicio) {
        int cantidadCuotas = plan.getCantidadCuotas();
        BigDecimal valorTotal = plan.getValorMovil().setScale(2, RoundingMode.HALF_UP);
        BigDecimal importeBaseCuota = valorTotal.divide(BigDecimal.valueOf(cantidadCuotas), 2, RoundingMode.HALF_UP);
        BigDecimal acumulado = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (int numero = 1; numero <= cantidadCuotas; numero++) {
            CuotaPlanAhorro cuota = new CuotaPlanAhorro();
            cuota.setContrato(contrato);
            cuota.setNumeroCuota(numero);
            BigDecimal importeCuota = numero == cantidadCuotas ? valorTotal.subtract(acumulado).setScale(2, RoundingMode.HALF_UP) : importeBaseCuota;
            cuota.setImporte(importeCuota);
            cuota.setEstado(EstadoCuotaPlanAhorro.PENDIENTE);
            cuota.setFechaVencimiento(fechaInicio.plus(30L * numero, ChronoUnit.DAYS));
            cuotaRepository.save(cuota);
            acumulado = acumulado.add(importeCuota).setScale(2, RoundingMode.HALF_UP);
        }
    }
}

