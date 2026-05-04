package com.concesionaria.app.scheduler;

import com.concesionaria.app.service.InventarioService;
import com.concesionaria.app.service.ReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservaScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(ReservaScheduler.class);

    private final InventarioService inventarioService;
    private final ReservaService reservaService;

    public ReservaScheduler(InventarioService inventarioService, ReservaService reservaService) {
        this.inventarioService = inventarioService;
        this.reservaService = reservaService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void expirarReservasVencidas() {
        try {
            long expiradas = inventarioService.expirarReservasVencidas();
            long reservasExpiradas = reservaService.expirarReservasVencidas();
            if (expiradas > 0) {
                LOG.info("Reservas expiradas automaticamente: {}", expiradas);
            } else {
                LOG.debug("No se encontraron reservas vencidas para expirar");
            }
            if (reservasExpiradas > 0) {
                LOG.info("Reservas (entidad) expiradas automaticamente: {}", reservasExpiradas);
            }
        } catch (Exception ex) {
            LOG.error("Error al ejecutar expiracion automatica de reservas", ex);
        }
    }
}

