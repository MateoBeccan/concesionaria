package com.concesionaria.app.repository;

import com.concesionaria.app.domain.EntregaChecklistItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntregaChecklistItemRepository extends JpaRepository<EntregaChecklistItem, Long> {
    List<EntregaChecklistItem> findAllByEntregaUnidadIdOrderByIdAsc(Long entregaUnidadId);
}

