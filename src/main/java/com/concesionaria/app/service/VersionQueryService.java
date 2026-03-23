package com.concesionaria.app.service;

import com.concesionaria.app.domain.*; // for static metamodels
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.repository.VersionRepository;
import com.concesionaria.app.service.criteria.VersionCriteria;
import com.concesionaria.app.service.dto.VersionDTO;
import com.concesionaria.app.service.mapper.VersionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Version} entities in the database.
 * The main input is a {@link VersionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link VersionDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class VersionQueryService extends QueryService<Version> {

    private static final Logger LOG = LoggerFactory.getLogger(VersionQueryService.class);

    private final VersionRepository versionRepository;

    private final VersionMapper versionMapper;

    public VersionQueryService(VersionRepository versionRepository, VersionMapper versionMapper) {
        this.versionRepository = versionRepository;
        this.versionMapper = versionMapper;
    }

    /**
     * Return a {@link Page} of {@link VersionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<VersionDTO> findByCriteria(VersionCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Version> specification = createSpecification(criteria);
        return versionRepository.findAll(specification, page).map(versionMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(VersionCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Version> specification = createSpecification(criteria);
        return versionRepository.count(specification);
    }

    /**
     * Function to convert {@link VersionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Version> createSpecification(VersionCriteria criteria) {
        Specification<Version> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                buildRangeSpecification(criteria.getId(), Version_.id)
            );
        }
        return specification;
    }
}
