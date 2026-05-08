package com.concesionaria.app.repository;

import com.concesionaria.app.domain.EntidadFinanciera;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntidadFinancieraRepository extends JpaRepository<EntidadFinanciera, Long> {
    List<EntidadFinanciera> findByActivaTrueOrderByNombreAsc();
}
