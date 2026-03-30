package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Combustible;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Combustible entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CombustibleRepository extends JpaRepository<Combustible, Long>, JpaSpecificationExecutor<Combustible> {
    Optional<Combustible> findByNombreIgnoreCase(String nombre);
}
