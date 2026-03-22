package com.concesionaria.app.service;

import com.concesionaria.app.domain.*; // for static metamodels
import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.repository.MotorRepository;
import com.concesionaria.app.service.criteria.MotorCriteria;
import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.mapper.MotorMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Motor} entities in the database.
 * The main input is a {@link MotorCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link MotorDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class MotorQueryService extends QueryService<Motor> {

    private static final Logger LOG = LoggerFactory.getLogger(MotorQueryService.class);

    private final MotorRepository motorRepository;

    private final MotorMapper motorMapper;

    public MotorQueryService(MotorRepository motorRepository, MotorMapper motorMapper) {
        this.motorRepository = motorRepository;
        this.motorMapper = motorMapper;
    }

    /**
     * Return a {@link Page} of {@link MotorDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<MotorDTO> findByCriteria(MotorCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Motor> specification = createSpecification(criteria);
        return motorRepository.findAll(specification, page).map(motorMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(MotorCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Motor> specification = createSpecification(criteria);
        return motorRepository.count(specification);
    }

    /**
     * Function to convert {@link MotorCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Motor> createSpecification(MotorCriteria criteria) {
        Specification<Motor> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                buildRangeSpecification(criteria.getId(), Motor_.id),
                buildStringSpecification(criteria.getNombre(), Motor_.nombre),
                buildRangeSpecification(criteria.getCilindradaCc(), Motor_.cilindradaCc),
                buildRangeSpecification(criteria.getCilindroCant(), Motor_.cilindroCant),
                buildRangeSpecification(criteria.getPotenciaHp(), Motor_.potenciaHp),
                buildSpecification(criteria.getTurbo(), Motor_.turbo),
                buildSpecification(criteria.getVersionesId(), root -> root.join(Motor_.versioneses, JoinType.LEFT).get(Version_.id))
            );
        }
        return specification;
    }
}
