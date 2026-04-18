package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Pago;
import java.math.BigDecimal;
import java.util.List;
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
        "where p.venta.id = :ventaId " +
        "order by p.fecha desc, p.id desc"
    )
    List<Pago> findAllByVentaIdWithRelaciones(@Param("ventaId") Long ventaId);

    @Query("select coalesce(sum(p.monto), 0) from Pago p where p.venta.id = :ventaId")
    BigDecimal sumMontoByVentaId(@Param("ventaId") Long ventaId);
}
