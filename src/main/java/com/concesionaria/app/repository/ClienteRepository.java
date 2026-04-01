package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Cliente;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Cliente entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmailIgnoreCase(String email);

    Optional<Cliente> findByNroDocumento(String nroDocumento);

}
