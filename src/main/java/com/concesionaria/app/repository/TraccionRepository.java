package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Traccion;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Traccion entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TraccionRepository extends JpaRepository<Traccion, Long> {
    Optional<Traccion> findByNombreIgnoreCase(String nombre);
}
