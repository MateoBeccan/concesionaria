package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Auto;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Auto entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AutoRepository extends JpaRepository<Auto, Long>, JpaSpecificationExecutor<Auto> {}
