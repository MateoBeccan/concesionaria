package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Modelo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class ModeloRepositoryWithBagRelationshipsImpl implements ModeloRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String MODELOS_PARAMETER = "modelos";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Modelo> fetchBagRelationships(Optional<Modelo> modelo) {
        return modelo.map(this::fetchVersioneses);
    }

    @Override
    public Page<Modelo> fetchBagRelationships(Page<Modelo> modelos) {
        return new PageImpl<>(fetchBagRelationships(modelos.getContent()), modelos.getPageable(), modelos.getTotalElements());
    }

    @Override
    public List<Modelo> fetchBagRelationships(List<Modelo> modelos) {
        return Optional.of(modelos).map(this::fetchVersioneses).orElse(Collections.emptyList());
    }

    Modelo fetchVersioneses(Modelo result) {
        return entityManager
            .createQuery("select modelo from Modelo modelo left join fetch modelo.versioneses where modelo.id = :id", Modelo.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<Modelo> fetchVersioneses(List<Modelo> modelos) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, modelos.size()).forEach(index -> order.put(modelos.get(index).getId(), index));
        List<Modelo> result = entityManager
            .createQuery("select modelo from Modelo modelo left join fetch modelo.versioneses where modelo in :modelos", Modelo.class)
            .setParameter(MODELOS_PARAMETER, modelos)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
