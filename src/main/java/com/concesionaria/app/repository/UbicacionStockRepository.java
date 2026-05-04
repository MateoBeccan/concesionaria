package com.concesionaria.app.repository;

import com.concesionaria.app.domain.UbicacionStock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UbicacionStockRepository extends JpaRepository<UbicacionStock, Long> {
    Optional<UbicacionStock> findOneByCodigoIgnoreCase(String codigo);
}
