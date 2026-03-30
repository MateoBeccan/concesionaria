package com.concesionaria.app.repository;

import com.concesionaria.app.domain.TipoCaja;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the TipoCaja entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TipoCajaRepository extends JpaRepository<TipoCaja, Long> {
    Optional<TipoCaja> findByNombreIgnoreCase(String nombre);
}
