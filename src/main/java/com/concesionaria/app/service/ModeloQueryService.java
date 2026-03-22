package com.concesionaria.app.service;

import com.concesionaria.app.domain.*; // for static metamodels
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.repository.ModeloRepository;
import com.concesionaria.app.service.criteria.ModeloCriteria;
import com.concesionaria.app.service.dto.ModeloDTO;
import com.concesionaria.app.service.mapper.ModeloMapper;
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
 * Service for executing complex queries for {@link Modelo} entities in the database.
 * The main input is a {@link ModeloCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ModeloDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ModeloQueryService extends QueryService<Modelo> {

    private static final Logger LOG = LoggerFactory.getLogger(ModeloQueryService.class);

    private final ModeloRepository modeloRepository;

    private final ModeloMapper modeloMapper;

    public ModeloQueryService(ModeloRepository modeloRepository, ModeloMapper modeloMapper) {
        this.modeloRepository = modeloRepository;
        this.modeloMapper = modeloMapper;
    }

    /**
     * Return a {@link Page} of {@link ModeloDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ModeloDTO> findByCriteria(ModeloCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Modelo> specification = createSpecification(criteria);
        return modeloRepository.fetchBagRelationships(modeloRepository.findAll(specification, page)).map(modeloMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ModeloCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Modelo> specification = createSpecification(criteria);
        return modeloRepository.count(specification);
    }

    /**
     * Function to convert {@link ModeloCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Modelo> createSpecification(ModeloCriteria criteria) {
        Specification<Modelo> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                buildRangeSpecification(criteria.getId(), Modelo_.id),
                buildStringSpecification(criteria.getNombre(), Modelo_.nombre),
                buildRangeSpecification(criteria.getAnioLanzamiento(), Modelo_.anioLanzamiento),
                buildStringSpecification(criteria.getCarroceria(), Modelo_.carroceria),
                buildSpecification(criteria.getMarcaId(), root -> root.join(Modelo_.marca, JoinType.LEFT).get(Marca_.id)),
                buildSpecification(criteria.getVersionesId(), root -> root.join(Modelo_.versioneses, JoinType.LEFT).get(Version_.id))
            );
        }
        return specification;
    }
}
