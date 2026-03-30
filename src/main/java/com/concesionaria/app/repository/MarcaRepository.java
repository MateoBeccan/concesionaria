package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Marca;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Marca entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long>, JpaSpecificationExecutor<Marca> {

    boolean existsByNombreIgnoreCase(String nombre);
}
