package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import java.util.List;
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
        "select c from Comprobante c " +
        "left join fetch c.tipoComprobante tc " +
        "left join fetch c.moneda m " +
        "where c.venta.id = :ventaId " +
        "order by c.fechaEmision desc, c.id desc"
    )
    List<Comprobante> findAllByVentaIdWithRelaciones(@Param("ventaId") Long ventaId);

    boolean existsByVentaIdAndEstado(Long ventaId, EstadoComprobante estado);

    @Query(
        value = "select coalesce(max(cast(substring_index(c.numero_comprobante, '-', -1) as unsigned)), 0) from comprobante c where c.tipo_comprobante_id = :tipoComprobanteId",
        nativeQuery = true
    )
    Long findMaxNumeroCorrelativoByTipoComprobanteId(@Param("tipoComprobanteId") Long tipoComprobanteId);
}
