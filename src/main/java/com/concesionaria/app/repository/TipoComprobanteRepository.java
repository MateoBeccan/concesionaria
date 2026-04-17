package com.concesionaria.app.repository;

import com.concesionaria.app.domain.TipoComprobante;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TipoComprobante entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TipoComprobanteRepository extends JpaRepository<TipoComprobante, Long> {
    java.util.Optional<TipoComprobante> findByCodigoIgnoreCase(String codigo);
}
