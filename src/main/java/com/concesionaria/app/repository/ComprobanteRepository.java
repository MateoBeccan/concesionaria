package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Comprobante;
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
}
