package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Cliente;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Cliente entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmailIgnoreCase(String email);

    Optional<Cliente> findByNroDocumento(String nroDocumento);

    @Query("""
SELECT c FROM Cliente c
WHERE LOWER(c.nombre) LIKE %:q%
   OR LOWER(c.apellido) LIKE %:q%
   OR LOWER(c.email) LIKE %:q%
   OR c.nroDocumento LIKE %:q%
""")
    List<Cliente> buscarGeneral(@Param("q") String q);
}
