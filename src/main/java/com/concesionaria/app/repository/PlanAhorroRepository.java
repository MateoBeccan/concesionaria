package com.concesionaria.app.repository;

import com.concesionaria.app.domain.PlanAhorro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanAhorroRepository extends JpaRepository<PlanAhorro, Long> {}

