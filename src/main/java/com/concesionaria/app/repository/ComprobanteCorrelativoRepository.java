package com.concesionaria.app.repository;

import com.concesionaria.app.domain.ComprobanteCorrelativo;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComprobanteCorrelativoRepository extends JpaRepository<ComprobanteCorrelativo, Long> {
    @Modifying
    @Query(
        value = "insert ignore into comprobante_correlativo (tipo_comprobante_id, ultimo_numero) values (:tipoComprobanteId, 0)",
        nativeQuery = true
    )
    void asegurarFila(@Param("tipoComprobanteId") Long tipoComprobanteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ComprobanteCorrelativo c where c.tipoComprobanteId = :tipoComprobanteId")
    Optional<ComprobanteCorrelativo> findByTipoComprobanteIdForUpdate(@Param("tipoComprobanteId") Long tipoComprobanteId);
}
