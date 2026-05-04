package com.concesionaria.app.repository;

import com.concesionaria.app.domain.VentaHistorial;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaHistorialRepository extends JpaRepository<VentaHistorial, Long> {
    List<VentaHistorial> findAllByVentaIdOrderByFechaDesc(Long ventaId);
}
