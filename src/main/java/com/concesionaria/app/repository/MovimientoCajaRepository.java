package com.concesionaria.app.repository;

import com.concesionaria.app.domain.MovimientoCaja;
import com.concesionaria.app.domain.enumeration.EstadoPago;
import com.concesionaria.app.domain.enumeration.TipoMovimientoCaja;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {
    boolean existsByPagoIdAndTipoMovimiento(Long pagoId, TipoMovimientoCaja tipoMovimiento);

    @Query(
        value = """
        select mc
        from MovimientoCaja mc
        left join mc.metodoPago mp
        left join mc.entidadFinanciera ef
        where (:fechaDesde is null or mc.fecha >= :fechaDesde)
          and (:fechaHasta is null or mc.fecha <= :fechaHasta)
          and (:usuario is null or lower(mc.usuario) like lower(concat('%', :usuario, '%')))
          and (:metodoPagoId is null or mp.id = :metodoPagoId)
          and (:entidadFinancieraId is null or ef.id = :entidadFinancieraId)
          and (:tipo is null or mc.tipoMovimiento = :tipo)
          and (:estado is null or mc.estado = :estado)
        """,
        countQuery = """
        select count(mc)
        from MovimientoCaja mc
        left join mc.metodoPago mp
        left join mc.entidadFinanciera ef
        where (:fechaDesde is null or mc.fecha >= :fechaDesde)
          and (:fechaHasta is null or mc.fecha <= :fechaHasta)
          and (:usuario is null or lower(mc.usuario) like lower(concat('%', :usuario, '%')))
          and (:metodoPagoId is null or mp.id = :metodoPagoId)
          and (:entidadFinancieraId is null or ef.id = :entidadFinancieraId)
          and (:tipo is null or mc.tipoMovimiento = :tipo)
          and (:estado is null or mc.estado = :estado)
        """
    )
    Page<MovimientoCaja> findAllByFiltrosAdmin(
        @Param("fechaDesde") Instant fechaDesde,
        @Param("fechaHasta") Instant fechaHasta,
        @Param("usuario") String usuario,
        @Param("metodoPagoId") Long metodoPagoId,
        @Param("entidadFinancieraId") Long entidadFinancieraId,
        @Param("tipo") TipoMovimientoCaja tipo,
        @Param("estado") EstadoPago estado,
        Pageable pageable
    );

    @Query(
        value = """
        select mc
        from MovimientoCaja mc
        left join mc.metodoPago mp
        left join mc.entidadFinanciera ef
        where mc.usuario = :usuarioLogin
          and (:fechaDesde is null or mc.fecha >= :fechaDesde)
          and (:fechaHasta is null or mc.fecha <= :fechaHasta)
          and (:metodoPagoId is null or mp.id = :metodoPagoId)
          and (:entidadFinancieraId is null or ef.id = :entidadFinancieraId)
          and (:tipo is null or mc.tipoMovimiento = :tipo)
          and (:estado is null or mc.estado = :estado)
        """,
        countQuery = """
        select count(mc)
        from MovimientoCaja mc
        left join mc.metodoPago mp
        left join mc.entidadFinanciera ef
        where mc.usuario = :usuarioLogin
          and (:fechaDesde is null or mc.fecha >= :fechaDesde)
          and (:fechaHasta is null or mc.fecha <= :fechaHasta)
          and (:metodoPagoId is null or mp.id = :metodoPagoId)
          and (:entidadFinancieraId is null or ef.id = :entidadFinancieraId)
          and (:tipo is null or mc.tipoMovimiento = :tipo)
          and (:estado is null or mc.estado = :estado)
        """
    )
    Page<MovimientoCaja> findAllByFiltrosUsuario(
        @Param("usuarioLogin") String usuarioLogin,
        @Param("fechaDesde") Instant fechaDesde,
        @Param("fechaHasta") Instant fechaHasta,
        @Param("metodoPagoId") Long metodoPagoId,
        @Param("entidadFinancieraId") Long entidadFinancieraId,
        @Param("tipo") TipoMovimientoCaja tipo,
        @Param("estado") EstadoPago estado,
        Pageable pageable
    );
}
