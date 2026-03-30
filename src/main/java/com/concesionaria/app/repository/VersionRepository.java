package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Version;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Version entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VersionRepository extends JpaRepository<Version, Long>, JpaSpecificationExecutor<Version> {

    boolean existsByNombreIgnoreCaseAndModeloId(String nombre, Long modeloId);
    Optional<Version> findByNombreIgnoreCaseAndModeloId(String nombre, Long modeloId);

}
