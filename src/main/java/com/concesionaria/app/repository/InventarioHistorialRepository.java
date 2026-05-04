package com.concesionaria.app.repository;

import com.concesionaria.app.domain.InventarioHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventarioHistorialRepository extends JpaRepository<InventarioHistorial, Long> {}
