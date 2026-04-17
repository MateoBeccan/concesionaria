package com.concesionaria.app.repository;

import com.concesionaria.app.domain.TipoDocumento;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TipoDocumento entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {
    java.util.Optional<TipoDocumento> findByCodigoIgnoreCase(String codigo);
}
