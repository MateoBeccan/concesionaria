package com.concesionaria.app.service;

import com.concesionaria.app.domain.*; // for static metamodels
import com.concesionaria.app.domain.Combustible;
import com.concesionaria.app.repository.CombustibleRepository;
import com.concesionaria.app.service.criteria.CombustibleCriteria;
import com.concesionaria.app.service.dto.CombustibleDTO;
import com.concesionaria.app.service.mapper.CombustibleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Combustible} entities in the database.
 * The main input is a {@link CombustibleCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link CombustibleDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CombustibleQueryService extends QueryService<Combustible> {

    private static final Logger LOG = LoggerFactory.getLogger(CombustibleQueryService.class);

    private final CombustibleRepository combustibleRepository;

    private final CombustibleMapper combustibleMapper;

    public CombustibleQueryService(CombustibleRepository combustibleRepository, CombustibleMapper combustibleMapper) {
        this.combustibleRepository = combustibleRepository;
        this.combustibleMapper = combustibleMapper;
    }

    /**
     * Return a {@link Page} of {@link CombustibleDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CombustibleDTO> findByCriteria(CombustibleCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Combustible> specification = createSpecification(criteria);
        return combustibleRepository.findAll(specification, page).map(combustibleMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CombustibleCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Combustible> specification = createSpecification(criteria);
        return combustibleRepository.count(specification);
    }

    /**
     * Function to convert {@link CombustibleCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Combustible> createSpecification(CombustibleCriteria criteria) {
        Specification<Combustible> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                buildRangeSpecification(criteria.getId(), Combustible_.id)
            );
        }
        return specification;
    }
}
