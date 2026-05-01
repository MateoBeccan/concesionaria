package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Cotizacion;
import java.time.Instant;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Cotizacion entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    Optional<Cotizacion> findTopByOrderByFechaDesc();

    Optional<Cotizacion> findTopByMonedaIdAndActivoTrueOrderByFechaDesc(Long monedaId);

    Optional<Cotizacion> findTopByMonedaIdOrderByFechaDesc(Long monedaId);

    Optional<Cotizacion> findTopByMonedaIdAndActivoTrueAndFechaLessThanEqualOrderByFechaDesc(Long monedaId, Instant fecha);

    Optional<Cotizacion> findTopByMonedaIdAndFechaLessThanEqualOrderByFechaDesc(Long monedaId, Instant fecha);
}
