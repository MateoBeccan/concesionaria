package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Modelo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Modelo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ModeloRepository extends JpaRepository<Modelo, Long>, JpaSpecificationExecutor<Modelo> {

    boolean existsByNombreIgnoreCaseAndMarcaId(String nombre, Long marcaId);
    Optional<Modelo> findByNombreIgnoreCaseAndMarcaId(String nombre, Long marcaId);

    @Query("select m from Modelo m join fetch m.marca left join fetch m.carroceria where m.marca.id = :marcaId order by m.nombre")
    List<Modelo> findAllByMarcaIdOrderByNombreAsc(Long marcaId);
}
