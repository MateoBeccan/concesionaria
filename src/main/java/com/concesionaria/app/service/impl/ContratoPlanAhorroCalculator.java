package com.concesionaria.app.service.impl;

import com.concesionaria.app.domain.ContratoPlanAhorro;
import com.concesionaria.app.domain.CuotaPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoContratoPlanAhorro;
import com.concesionaria.app.domain.enumeration.EstadoCuotaPlanAhorro;
import com.concesionaria.app.repository.ContratoPlanAhorroRepository;
import com.concesionaria.app.repository.CuotaPlanAhorroRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContratoPlanAhorroCalculator {

    private final CuotaPlanAhorroRepository cuotaRepository;
    private final ContratoPlanAhorroRepository contratoRepository;

    public ContratoPlanAhorroCalculator(CuotaPlanAhorroRepository cuotaRepository, ContratoPlanAhorroRepository contratoRepository) {
        this.cuotaRepository = cuotaRepository;
        this.contratoRepository = contratoRepository;
    }

    public void recalcularContrato(ContratoPlanAhorro contrato) {
        List<CuotaPlanAhorro> cuotas = cuotaRepository.findAllByContratoIdOrderByNumeroCuotaAsc(contrato.getId());
        long pagadas = cuotas.stream().filter(c -> c.getEstado() == EstadoCuotaPlanAhorro.PAGADA).count();
        BigDecimal saldo = cuotas
            .stream()
            .filter(c -> c.getEstado() == EstadoCuotaPlanAhorro.PENDIENTE || c.getEstado() == EstadoCuotaPlanAhorro.VENCIDA)
            .map(CuotaPlanAhorro::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        contrato.setCuotasPagadas((int) pagadas);
        contrato.setSaldoPendiente(saldo);
        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            contrato.setEstado(EstadoContratoPlanAhorro.FINALIZADO);
        }
        contratoRepository.save(contrato);
    }
}

