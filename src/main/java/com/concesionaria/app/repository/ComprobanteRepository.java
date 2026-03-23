package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Comprobante;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Comprobante entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {}
