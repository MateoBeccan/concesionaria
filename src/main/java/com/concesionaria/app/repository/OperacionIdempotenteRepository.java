package com.concesionaria.app.repository;

import com.concesionaria.app.domain.OperacionIdempotente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperacionIdempotenteRepository extends JpaRepository<OperacionIdempotente, Long> {
    Optional<OperacionIdempotente> findOneByUsuarioAndTipoOperacionAndIdempotencyKey(
        String usuario,
        String tipoOperacion,
        String idempotencyKey
    );

    @Modifying
    @Query(
        value = """
            insert into operacion_idempotente (usuario, tipo_operacion, idempotency_key, request_hash, created_date)
            values (:usuario, :tipoOperacion, :idempotencyKey, :requestHash, current_timestamp(6))
            on duplicate key update id = id
            """,
        nativeQuery = true
    )
    int claim(
        @Param("usuario") String usuario,
        @Param("tipoOperacion") String tipoOperacion,
        @Param("idempotencyKey") String idempotencyKey,
        @Param("requestHash") String requestHash
    );
}
