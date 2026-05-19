package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Inventario entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByVehiculoId(Long vehiculoId);

    List<Inventario> findAllByEstadoInventario(EstadoInventario estadoInventario);

    @Query(
        """
        select i
        from Inventario i
        join i.vehiculo v
        where i.estadoInventario = :estado
          and (:versionId is null or v.version.id = :versionId)
        """
    )
    List<Inventario> findDisponiblesByVersionObjetivo(
        @Param("estado") EstadoInventario estado,
        @Param("versionId") Long versionId
    );

    @Query(
        """
        select i
        from Inventario i
        join i.vehiculo v
        join v.version ver
        join ver.modelo m
        where i.estadoInventario = :estado
          and m.id = :modeloId
        """
    )
    List<Inventario> findDisponiblesByModeloObjetivo(@Param("estado") EstadoInventario estado, @Param("modeloId") Long modeloId);

}
