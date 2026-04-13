package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Version;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Version entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VersionRepository extends JpaRepository<Version, Long>, JpaSpecificationExecutor<Version> {
    boolean existsByNombreIgnoreCaseAndModeloId(String nombre, Long modeloId);

    Optional<Version> findByNombreIgnoreCaseAndModeloId(String nombre, Long modeloId);
}
