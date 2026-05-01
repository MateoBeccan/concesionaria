package com.concesionaria.app.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.concesionaria.app.domain.Vehiculo;

/**
 * Spring Data JPA repository for the Vehiculo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPatente(String patente);
    Optional<Vehiculo> findByPatente(String patente);
    Optional<Vehiculo> findByPatenteIgnoreCase(String patente);
    Optional<Vehiculo> findByVinChasisIgnoreCase(String vinChasis);

    @EntityGraph(attributePaths = { "version", "version.modelo", "version.modelo.marca", "motor", "tipoVehiculo", "moneda", "inventario" })
    Page<Vehiculo> findAllBy(Pageable pageable);
}
