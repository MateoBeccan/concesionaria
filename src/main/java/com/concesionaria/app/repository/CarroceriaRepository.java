package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Carroceria;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Carroceria entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CarroceriaRepository extends JpaRepository<Carroceria, Long> {}
