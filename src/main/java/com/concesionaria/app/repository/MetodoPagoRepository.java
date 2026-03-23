package com.concesionaria.app.repository;

import com.concesionaria.app.domain.MetodoPago;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MetodoPago entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {}
