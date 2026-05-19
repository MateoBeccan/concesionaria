package com.concesionaria.app.repository;

import com.concesionaria.app.domain.ReglaAdjudicacionPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReglaAdjudicacionPlanRepository extends JpaRepository<ReglaAdjudicacionPlan, Long> {
    List<ReglaAdjudicacionPlan> findAllByActivoTrueOrderByNombreAsc();

    Optional<ReglaAdjudicacionPlan> findByNombre(String nombre);
}
