package com.concesionaria.app.repository;

import com.concesionaria.app.domain.TipoVehiculo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the TipoVehiculo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TipoVehiculoRepository extends JpaRepository<TipoVehiculo, Long> {
    Optional<TipoVehiculo> findByNombreIgnoreCase(String nombre);
}
