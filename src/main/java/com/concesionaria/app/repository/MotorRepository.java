package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Motor;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Motor entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MotorRepository extends JpaRepository<Motor, Long>, JpaSpecificationExecutor<Motor> {

    boolean existsByNombreIgnoreCase(String nombre);
}
