package com.concesionaria.app.repository;

import com.concesionaria.app.domain.DetalleVenta;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the DetalleVenta entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    Optional<DetalleVenta> findFirstByVentaId(Long ventaId);

    List<DetalleVenta> findAllByVentaId(Long ventaId);

    boolean existsByVehiculoIdAndVentaEstadoIn(Long vehiculoId, Collection<EstadoVenta> estados);

    @Query(
        "select distinct d from DetalleVenta d " +
        "left join fetch d.vehiculo v " +
        "left join fetch v.version ver " +
        "left join fetch ver.modelo mod " +
        "left join fetch mod.marca mar " +
        "left join fetch v.inventario inv " +
        "where d.venta.id = :ventaId " +
        "order by d.id"
    )
    List<DetalleVenta> findAllByVentaIdWithVehiculo(@Param("ventaId") Long ventaId);
}
