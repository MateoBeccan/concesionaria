package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Pago entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    @Query(
        "select p from Pago p " +
        "left join fetch p.metodoPago mp " +
        "left join fetch p.moneda m " +
        "left join fetch p.tasacionUsado tu " +
        "where p.venta.id = :ventaId " +
        "order by p.fecha desc, p.id desc"
    )
    List<Pago> findAllByVentaIdWithRelaciones(@Param("ventaId") Long ventaId);

    @Query(
        "select coalesce(sum(coalesce(p.montoAplicadoVenta, p.monto)), 0) " +
        "from Pago p " +
        "where p.venta.id = :ventaId and p.estado = com.concesionaria.app.domain.enumeration.EstadoPago.REGISTRADO"
    )
    BigDecimal sumMontoByVentaId(@Param("ventaId") Long ventaId);

    @Query(
        "select p from Pago p " +
        "left join fetch p.metodoPago mp " +
        "left join fetch p.moneda m " +
        "left join fetch p.tasacionUsado tu " +
        "where p.reserva.id = :reservaId " +
        "order by p.fecha desc, p.id desc"
    )
    List<Pago> findAllByReservaIdWithRelaciones(@Param("reservaId") Long reservaId);

    @Query(
        "select coalesce(sum(coalesce(p.montoAplicadoVenta, p.monto)), 0) " +
        "from Pago p " +
        "where p.reserva.id = :reservaId and p.estado = com.concesionaria.app.domain.enumeration.EstadoPago.REGISTRADO"
    )
    BigDecimal sumMontoByReservaId(@Param("reservaId") Long reservaId);

    long countByVentaIdAndEstado(Long ventaId, EstadoPago estado);

    long countByTasacionUsadoIdAndEstado(Long tasacionUsadoId, EstadoPago estado);

    long countByReservaIdAndEstado(Long reservaId, EstadoPago estado);

    Optional<Pago> findFirstByVentaIdAndEstadoAndTipoMovimientoAndTasacionUsadoIsNotNullOrderByFechaDescIdDesc(
        Long ventaId,
        EstadoPago estado,
        TipoMovimientoPago tipoMovimiento
    );
}
