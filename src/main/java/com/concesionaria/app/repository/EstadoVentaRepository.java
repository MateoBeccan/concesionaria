package com.concesionaria.app.repository;

import com.concesionaria.app.domain.EstadoVenta;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the EstadoVenta entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EstadoVentaRepository extends JpaRepository<EstadoVenta, Long> {}
