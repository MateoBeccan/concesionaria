package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.CombustibleAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Combustible;
import com.concesionaria.app.repository.CombustibleRepository;
import com.concesionaria.app.service.dto.CombustibleDTO;
import com.concesionaria.app.service.mapper.CombustibleMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link CombustibleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CombustibleResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/combustibles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CombustibleRepository combustibleRepository;

    @Autowired
    private CombustibleMapper combustibleMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCombustibleMockMvc;

    private Combustible combustible;

    private Combustible insertedCombustible;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Combustible createEntity() {
        return new Combustible().nombre(DEFAULT_NOMBRE).descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Combustible createUpdatedEntity() {
        return new Combustible().nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        combustible = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCombustible != null) {
            combustibleRepository.delete(insertedCombustible);
            insertedCombustible = null;
        }
    }

    @Test
    @Transactional
    void createCombustible() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Combustible
        CombustibleDTO combustibleDTO = combustibleMapper.toDto(combustible);
        var returnedCombustibleDTO = om.readValue(
            restCombustibleMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(combustibleDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CombustibleDTO.class
        );

        // Validate the Combustible in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCombustible = combustibleMapper.toEntity(returnedCombustibleDTO);
        assertCombustibleUpdatableFieldsEquals(returnedCombustible, getPersistedCombustible(returnedCombustible));

        insertedCombustible = returnedCombustible;
    }

    @Test
    @Transactional
    void createCombustibleWithExistingId() throws Exception {
        // Create the Combustible with an existing ID
        combustible.setId(1L);
        CombustibleDTO combustibleDTO = combustibleMapper.toDto(combustible);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCombustibleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(combustibleDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Combustible in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        combustible.setNombre(null);

        // Create the Combustible, which fails.
        CombustibleDTO combustibleDTO = combustibleMapper.toDto(combustible);

        restCombustibleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(combustibleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCombustibles() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get all the combustibleList
        restCombustibleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(combustible.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @Test
    @Transactional
    void getCombustible() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get the combustible
        restCombustibleMockMvc
            .perform(get(ENTITY_API_URL_ID, combustible.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(combustible.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getNonExistingCombustible() throws Exception {
        // Get the combustible
        restCombustibleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCombustible() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the combustible
        Combustible updatedCombustible = combustibleRepository.findById(combustible.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCombustible are not directly saved in db
        em.detach(updatedCombustible);
        updatedCombustible.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
        CombustibleDTO combustibleDTO = combustibleMapper.toDto(updatedCombustible);

        restCombustibleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, combustibleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(combustibleDTO))
            )
            .andExpect(status().isOk());

        // Validate the Combustible in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCombustibleToMatchAllProperties(updatedCombustible);
    }

    @Test
    @Transactional
    void putNonExistingCombustible() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        combustible.setId(longCount.incrementAndGet());

        // Create the Combustible
        CombustibleDTO combustibleDTO = combustibleMapper.toDto(combustible);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCombustibleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, combustibleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(combustibleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Combustible in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCombustible() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        combustible.setId(longCount.incrementAndGet());

        // Create the Combustible
        CombustibleDTO combustibleDTO = combustibleMapper.toDto(combustible);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCombustibleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(combustibleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Combustible in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCombustible() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        combustible.setId(longCount.incrementAndGet());

        // Create the Combustible
        CombustibleDTO combustibleDTO = combustibleMapper.toDto(combustible);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCombustibleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(combustibleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Combustible in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCombustibleWithPatch() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the combustible using partial update
        Combustible partialUpdatedCombustible = new Combustible();
        partialUpdatedCombustible.setId(combustible.getId());

        partialUpdatedCombustible.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);

        restCombustibleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCombustible.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCombustible))
            )
            .andExpect(status().isOk());

        // Validate the Combustible in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCombustibleUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCombustible, combustible),
            getPersistedCombustible(combustible)
        );
    }

    @Test
    @Transactional
    void fullUpdateCombustibleWithPatch() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the combustible using partial update
        Combustible partialUpdatedCombustible = new Combustible();
        partialUpdatedCombustible.setId(combustible.getId());

        partialUpdatedCombustible.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);

        restCombustibleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCombustible.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCombustible))
            )
            .andExpect(status().isOk());

        // Validate the Combustible in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCombustibleUpdatableFieldsEquals(partialUpdatedCombustible, getPersistedCombustible(partialUpdatedCombustible));
    }

    @Test
    @Transactional
    void patchNonExistingCombustible() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        combustible.setId(longCount.incrementAndGet());

        // Create the Combustible
        CombustibleDTO combustibleDTO = combustibleMapper.toDto(combustible);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCombustibleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, combustibleDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(combustibleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Combustible in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCombustible() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        combustible.setId(longCount.incrementAndGet());

        // Create the Combustible
        CombustibleDTO combustibleDTO = combustibleMapper.toDto(combustible);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCombustibleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(combustibleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Combustible in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCombustible() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        combustible.setId(longCount.incrementAndGet());

        // Create the Combustible
        CombustibleDTO combustibleDTO = combustibleMapper.toDto(combustible);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCombustibleMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(combustibleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Combustible in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCombustible() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the combustible
        restCombustibleMockMvc
            .perform(delete(ENTITY_API_URL_ID, combustible.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return combustibleRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Combustible getPersistedCombustible(Combustible combustible) {
        return combustibleRepository.findById(combustible.getId()).orElseThrow();
    }

    protected void assertPersistedCombustibleToMatchAllProperties(Combustible expectedCombustible) {
        assertCombustibleAllPropertiesEquals(expectedCombustible, getPersistedCombustible(expectedCombustible));
    }

    protected void assertPersistedCombustibleToMatchUpdatableProperties(Combustible expectedCombustible) {
        assertCombustibleAllUpdatablePropertiesEquals(expectedCombustible, getPersistedCombustible(expectedCombustible));
    }
}
