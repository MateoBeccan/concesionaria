package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Version;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Version entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VersionRepository extends JpaRepository<Version, Long>, JpaSpecificationExecutor<Version> {

    boolean existsByNombreIgnoreCaseAndModeloId(String nombre, Long modeloId);
}
