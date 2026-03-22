package com.concesionaria.app.service;

import com.concesionaria.app.domain.*; // for static metamodels
import com.concesionaria.app.domain.Prueba1;
import com.concesionaria.app.repository.Prueba1Repository;
import com.concesionaria.app.service.criteria.Prueba1Criteria;
import com.concesionaria.app.service.dto.Prueba1DTO;
import com.concesionaria.app.service.mapper.Prueba1Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Prueba1} entities in the database.
 * The main input is a {@link Prueba1Criteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link Prueba1DTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class Prueba1QueryService extends QueryService<Prueba1> {

    private static final Logger LOG = LoggerFactory.getLogger(Prueba1QueryService.class);

    private final Prueba1Repository prueba1Repository;

    private final Prueba1Mapper prueba1Mapper;

    public Prueba1QueryService(Prueba1Repository prueba1Repository, Prueba1Mapper prueba1Mapper) {
        this.prueba1Repository = prueba1Repository;
        this.prueba1Mapper = prueba1Mapper;
    }

    /**
     * Return a {@link Page} of {@link Prueba1DTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Prueba1DTO> findByCriteria(Prueba1Criteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Prueba1> specification = createSpecification(criteria);
        return prueba1Repository.findAll(specification, page).map(prueba1Mapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(Prueba1Criteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Prueba1> specification = createSpecification(criteria);
        return prueba1Repository.count(specification);
    }

    /**
     * Function to convert {@link Prueba1Criteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Prueba1> createSpecification(Prueba1Criteria criteria) {
        Specification<Prueba1> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                buildRangeSpecification(criteria.getId(), Prueba1_.id)
            );
        }
        return specification;
    }
}
