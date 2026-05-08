package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Comprobante entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    @Query(
        value = """
        select c
        from Comprobante c
        join c.venta v
        join v.user u
        where u.login = :login
        """,
        countQuery = """
        select count(c)
        from Comprobante c
        join c.venta v
        join v.user u
        where u.login = :login
        """
    )
    Page<Comprobante> findAllCurrentUser(@Param("login") String login, Pageable pageable);

    @Query(
        """
        select (count(c) > 0)
        from Comprobante c
        join c.venta v
        join v.user u
        where c.id = :comprobanteId
          and u.login = :login
        """
    )
    boolean existsAccessibleByIdForUser(@Param("comprobanteId") Long comprobanteId, @Param("login") String login);

    @Query(
        "select c from Comprobante c " +
        "left join fetch c.tipoComprobante tc " +
        "left join fetch c.moneda m " +
        "where c.venta.id = :ventaId " +
        "order by c.fechaEmision desc, c.id desc"
    )
    List<Comprobante> findAllByVentaIdWithRelaciones(@Param("ventaId") Long ventaId);

    @Query(
        "select c from Comprobante c " +
        "join c.venta v " +
        "join v.user u " +
        "left join fetch c.tipoComprobante tc " +
        "left join fetch c.moneda m " +
        "where c.venta.id = :ventaId and u.login = :login " +
        "order by c.fechaEmision desc, c.id desc"
    )
    List<Comprobante> findAllByVentaIdWithRelacionesForUser(@Param("ventaId") Long ventaId, @Param("login") String login);

    @Query(
        "select c from Comprobante c " +
        "left join fetch c.tipoComprobante tc " +
        "left join fetch c.moneda m " +
        "left join fetch c.venta v " +
        "left join fetch v.cliente cli " +
        "left join fetch cli.condicionIva ci " +
        "left join fetch v.vehiculo vh " +
        "left join fetch vh.version ver " +
        "left join fetch ver.modelo mod " +
        "left join fetch mod.marca mar " +
        "where c.id = :id"
    )
    Optional<Comprobante> findOneForPdf(@Param("id") Long id);

    boolean existsByVentaIdAndEstado(Long ventaId, EstadoComprobante estado);

    boolean existsByVentaIdAndTipoComprobanteIdAndEstado(Long ventaId, Long tipoComprobanteId, EstadoComprobante estado);

    boolean existsByPagoIdAndTipoComprobanteIdAndEstado(Long pagoId, Long tipoComprobanteId, EstadoComprobante estado);

    List<Comprobante> findAllByPagoIdOrderByFechaEmisionDescIdDesc(Long pagoId);

    @Query(
        "select c from Comprobante c " +
        "join c.venta v " +
        "join v.user u " +
        "where c.pago.id = :pagoId and u.login = :login " +
        "order by c.fechaEmision desc, c.id desc"
    )
    List<Comprobante> findAllByPagoIdForUser(@Param("pagoId") Long pagoId, @Param("login") String login);

    @Query(
        value = "select coalesce(max(cast(substring_index(c.numero_comprobante, '-', -1) as unsigned)), 0) from comprobante c where c.tipo_comprobante_id = :tipoComprobanteId",
        nativeQuery = true
    )
    Long findMaxNumeroCorrelativoByTipoComprobanteId(@Param("tipoComprobanteId") Long tipoComprobanteId);
}
