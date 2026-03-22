package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Modelo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ModeloRepositoryWithBagRelationships {
    Optional<Modelo> fetchBagRelationships(Optional<Modelo> modelo);

    List<Modelo> fetchBagRelationships(List<Modelo> modelos);

    Page<Modelo> fetchBagRelationships(Page<Modelo> modelos);
}
