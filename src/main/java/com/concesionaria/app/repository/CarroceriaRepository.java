package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Carroceria;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Carroceria entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CarroceriaRepository extends JpaRepository<Carroceria, Long> {
    Optional<Carroceria> findByNombreIgnoreCase(String nombre);
}
