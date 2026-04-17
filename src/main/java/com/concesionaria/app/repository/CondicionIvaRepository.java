package com.concesionaria.app.repository;

import com.concesionaria.app.domain.CondicionIva;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CondicionIva entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CondicionIvaRepository extends JpaRepository<CondicionIva, Long> {
    java.util.Optional<CondicionIva> findByCodigoIgnoreCase(String codigo);
}
