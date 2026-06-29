package com.concesionaria.app.scheduler;

import com.concesionaria.app.config.BusinessProperties;
import com.concesionaria.app.service.ReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservaScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(ReservaScheduler.class);

    private final ReservaService reservaService;
    private final BusinessProperties businessProperties;

    public ReservaScheduler(ReservaService reservaService, BusinessProperties businessProperties) {
        this.reservaService = reservaService;
        this.businessProperties = businessProperties == null ? BusinessProperties.defaults() : businessProperties;
    }

    @Scheduled(cron = "${app.negocio.reserva.expiracion-cron:0 */15 * * * *}")
    public void expirarReservasVencidas() {
        if (!businessProperties.getReserva().isExpiracionHabilitada()) {
            LOG.debug("Expiracion automatica de reservas deshabilitada por configuracion");
            return;
        }
        try {
            long reservasExpiradas = reservaService.expirarReservasVencidas();
            if (reservasExpiradas > 0) {
                LOG.info("Reservas expiradas automaticamente: {}", reservasExpiradas);
            } else {
                LOG.debug("No se encontraron reservas vencidas para expirar");
            }
        } catch (Exception ex) {
            LOG.error("Error al ejecutar expiracion automatica de reservas", ex);
        }
    }
}
