package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Vehiculo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Vehiculo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPatente(String patente);
    Optional<Vehiculo> findByPatente(String patente);
    Optional<Vehiculo> findByPatenteIgnoreCase(String patente);
}
