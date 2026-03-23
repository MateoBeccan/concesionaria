package com.concesionaria.app.service;

import com.concesionaria.app.domain.*; // for static metamodels
import com.concesionaria.app.domain.Auto;
import com.concesionaria.app.repository.AutoRepository;
import com.concesionaria.app.service.criteria.AutoCriteria;
import com.concesionaria.app.service.dto.AutoDTO;
import com.concesionaria.app.service.mapper.AutoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Auto} entities in the database.
 * The main input is a {@link AutoCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link AutoDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AutoQueryService extends QueryService<Auto> {

    private static final Logger LOG = LoggerFactory.getLogger(AutoQueryService.class);

    private final AutoRepository autoRepository;

    private final AutoMapper autoMapper;

    public AutoQueryService(AutoRepository autoRepository, AutoMapper autoMapper) {
        this.autoRepository = autoRepository;
        this.autoMapper = autoMapper;
    }

    /**
     * Return a {@link Page} of {@link AutoDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AutoDTO> findByCriteria(AutoCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Auto> specification = createSpecification(criteria);
        return autoRepository.findAll(specification, page).map(autoMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AutoCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Auto> specification = createSpecification(criteria);
        return autoRepository.count(specification);
    }

    /**
     * Function to convert {@link AutoCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Auto> createSpecification(AutoCriteria criteria) {
        Specification<Auto> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                buildRangeSpecification(criteria.getId(), Auto_.id)
            );
        }
        return specification;
    }
}
