package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Modelo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Modelo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ModeloRepository extends JpaRepository<Modelo, Long>,JpaSpecificationExecutor<Modelo> {

    @Query("select m from Modelo m left join fetch m.marca")
    List<Modelo> findAllWithMarca();
}
