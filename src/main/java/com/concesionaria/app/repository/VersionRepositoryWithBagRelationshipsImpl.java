package com.concesionaria.app.repository;

import com.concesionaria.app.domain.Version;
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
public class VersionRepositoryWithBagRelationshipsImpl implements VersionRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String VERSIONS_PARAMETER = "versions";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Version> fetchBagRelationships(Optional<Version> version) {
        return version.map(this::fetchMotoreses);
    }

    @Override
    public Page<Version> fetchBagRelationships(Page<Version> versions) {
        return new PageImpl<>(fetchBagRelationships(versions.getContent()), versions.getPageable(), versions.getTotalElements());
    }

    @Override
    public List<Version> fetchBagRelationships(List<Version> versions) {
        return Optional.of(versions).map(this::fetchMotoreses).orElse(Collections.emptyList());
    }

    Version fetchMotoreses(Version result) {
        return entityManager
            .createQuery("select version from Version version left join fetch version.motoreses where version.id = :id", Version.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<Version> fetchMotoreses(List<Version> versions) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, versions.size()).forEach(index -> order.put(versions.get(index).getId(), index));
        List<Version> result = entityManager
            .createQuery("select version from Version version left join fetch version.motoreses where version in :versions", Version.class)
            .setParameter(VERSIONS_PARAMETER, versions)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
