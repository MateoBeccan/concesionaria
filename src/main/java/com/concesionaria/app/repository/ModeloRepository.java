package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Modelo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Modelo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ModeloRepository extends JpaRepository<Modelo, Long>,JpaSpecificationExecutor<Modelo> {}
