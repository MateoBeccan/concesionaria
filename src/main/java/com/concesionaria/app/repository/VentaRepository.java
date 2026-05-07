package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.enumeration.EstadoVenta;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Venta entity.
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    @Query("select venta from Venta venta where venta.user.login = ?#{authentication.name}")
    List<Venta> findByUserIsCurrentUser();

    @Query(value = "select venta from Venta venta where venta.user.login = ?#{authentication.name}", countQuery = "select count(venta) from Venta venta where venta.user.login = ?#{authentication.name}")
    Page<Venta> findByUserIsCurrentUser(Pageable pageable);

    default Optional<Venta> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Venta> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Venta> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select venta from Venta venta left join fetch venta.user left join fetch venta.cliente left join fetch venta.moneda left join fetch venta.vehiculo left join fetch venta.vehiculo.moneda",
        countQuery = "select count(venta) from Venta venta"
    )
    Page<Venta> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        value = "select venta from Venta venta left join fetch venta.user left join fetch venta.cliente left join fetch venta.moneda left join fetch venta.vehiculo left join fetch venta.vehiculo.moneda where venta.user.login = ?#{authentication.name}",
        countQuery = "select count(venta) from Venta venta where venta.user.login = ?#{authentication.name}"
    )
    Page<Venta> findAllCurrentUserWithToOneRelationships(Pageable pageable);

    @Query(
        "select venta from Venta venta left join fetch venta.user left join fetch venta.cliente left join fetch venta.moneda left join fetch venta.vehiculo left join fetch venta.vehiculo.moneda"
    )
    List<Venta> findAllWithToOneRelationships();

    @Query(
        "select venta from Venta venta left join fetch venta.user left join fetch venta.cliente left join fetch venta.moneda left join fetch venta.vehiculo left join fetch venta.vehiculo.moneda where venta.id =:id"
    )
    Optional<Venta> findOneWithToOneRelationships(@Param("id") Long id);

    Optional<Venta> findFirstByReservaIdOrderByFechaDesc(Long reservaId);

    boolean existsByReservaId(Long reservaId);

    boolean existsByReservaIdAndIdNot(Long reservaId, Long ventaId);

    List<Venta> findAllByTasacionUsadoIdOrderByFechaDesc(Long tasacionUsadoId);

    boolean existsByTasacionUsadoIdAndIdNot(Long tasacionUsadoId, Long ventaId);

    boolean existsByVehiculoIdAndEstadoIn(Long vehiculoId, Collection<EstadoVenta> estados);

    boolean existsByVehiculoIdAndEstadoInAndIdNot(Long vehiculoId, Collection<EstadoVenta> estados, Long ventaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select venta from Venta venta where venta.id = :id")
    Optional<Venta> findByIdForUpdate(@Param("id") Long id);
}
