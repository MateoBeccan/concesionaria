package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Pago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoPago;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        value = """
        select p
        from Pago p
        left join p.venta v
        left join v.user vu
        left join p.reserva r
        where vu.login = :login
           or r.usuarioCreacion = :login
        """,
        countQuery = """
        select count(p)
        from Pago p
        left join p.venta v
        left join v.user vu
        left join p.reserva r
        where vu.login = :login
           or r.usuarioCreacion = :login
        """
    )
    Page<Pago> findAllCurrentUser(@Param("login") String login, Pageable pageable);

    @Query(
        """
        select (count(p) > 0)
        from Pago p
        left join p.venta v
        left join v.user vu
        left join p.reserva r
        where p.id = :pagoId
          and (vu.login = :login or r.usuarioCreacion = :login)
        """
    )
    boolean existsAccessibleByIdForUser(@Param("pagoId") Long pagoId, @Param("login") String login);

    @Query(
        "select p from Pago p " +
        "left join fetch p.metodoPago mp " +
        "left join fetch p.moneda m " +
        "left join fetch p.entidadFinanciera ef " +
        "left join fetch p.tasacionUsado tu " +
        "where p.venta.id = :ventaId " +
        "order by p.fecha desc, p.id desc"
    )
    List<Pago> findAllByVentaIdWithRelaciones(@Param("ventaId") Long ventaId);

    @Query(
        "select p from Pago p " +
        "join p.venta v " +
        "join v.user u " +
        "left join fetch p.metodoPago mp " +
        "left join fetch p.moneda m " +
        "left join fetch p.entidadFinanciera ef " +
        "left join fetch p.tasacionUsado tu " +
        "where p.venta.id = :ventaId and u.login = :login " +
        "order by p.fecha desc, p.id desc"
    )
    List<Pago> findAllByVentaIdWithRelacionesForUser(@Param("ventaId") Long ventaId, @Param("login") String login);

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
        "left join fetch p.entidadFinanciera ef " +
        "left join fetch p.tasacionUsado tu " +
        "where p.reserva.id = :reservaId " +
        "order by p.fecha desc, p.id desc"
    )
    List<Pago> findAllByReservaIdWithRelaciones(@Param("reservaId") Long reservaId);

    @Query(
        "select p from Pago p " +
        "join p.reserva r " +
        "left join fetch p.metodoPago mp " +
        "left join fetch p.moneda m " +
        "left join fetch p.entidadFinanciera ef " +
        "left join fetch p.tasacionUsado tu " +
        "where p.reserva.id = :reservaId and r.usuarioCreacion = :login " +
        "order by p.fecha desc, p.id desc"
    )
    List<Pago> findAllByReservaIdWithRelacionesForUser(@Param("reservaId") Long reservaId, @Param("login") String login);

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

    @Query(
        """
        select (count(p) > 0)
        from Pago p
        join p.metodoPago mp
        where p.venta.id = :ventaId
          and upper(mp.codigo) = upper(:codigoMetodo)
          and p.estado = :estado
        """
    )
    boolean existsByVentaIdAndMetodoPagoCodigoAndEstado(
        @Param("ventaId") Long ventaId,
        @Param("codigoMetodo") String codigoMetodo,
        @Param("estado") EstadoPago estado
    );

    boolean existsByAdjudicacionPlanAhorroIdAndEstado(Long adjudicacionPlanAhorroId, EstadoPago estado);
}
