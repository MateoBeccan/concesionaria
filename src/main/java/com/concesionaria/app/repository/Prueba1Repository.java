package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Prueba1;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Prueba1 entity.
 */
@SuppressWarnings("unused")
@Repository
public interface Prueba1Repository extends JpaRepository<Prueba1, Long>, JpaSpecificationExecutor<Prueba1> {}
