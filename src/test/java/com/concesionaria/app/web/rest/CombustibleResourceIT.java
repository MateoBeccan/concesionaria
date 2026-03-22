package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.CombustibleAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Combustible;
import com.concesionaria.app.domain.Motor;
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
    void getCombustiblesByIdFiltering() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        Long id = combustible.getId();

        defaultCombustibleFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCombustibleFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCombustibleFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCombustiblesByNombreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get all the combustibleList where nombre equals to
        defaultCombustibleFiltering("nombre.equals=" + DEFAULT_NOMBRE, "nombre.equals=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllCombustiblesByNombreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get all the combustibleList where nombre in
        defaultCombustibleFiltering("nombre.in=" + DEFAULT_NOMBRE + "," + UPDATED_NOMBRE, "nombre.in=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllCombustiblesByNombreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get all the combustibleList where nombre is not null
        defaultCombustibleFiltering("nombre.specified=true", "nombre.specified=false");
    }

    @Test
    @Transactional
    void getAllCombustiblesByNombreContainsSomething() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get all the combustibleList where nombre contains
        defaultCombustibleFiltering("nombre.contains=" + DEFAULT_NOMBRE, "nombre.contains=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllCombustiblesByNombreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get all the combustibleList where nombre does not contain
        defaultCombustibleFiltering("nombre.doesNotContain=" + UPDATED_NOMBRE, "nombre.doesNotContain=" + DEFAULT_NOMBRE);
    }

    @Test
    @Transactional
    void getAllCombustiblesByDescripcionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get all the combustibleList where descripcion equals to
        defaultCombustibleFiltering("descripcion.equals=" + DEFAULT_DESCRIPCION, "descripcion.equals=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllCombustiblesByDescripcionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get all the combustibleList where descripcion in
        defaultCombustibleFiltering(
            "descripcion.in=" + DEFAULT_DESCRIPCION + "," + UPDATED_DESCRIPCION,
            "descripcion.in=" + UPDATED_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllCombustiblesByDescripcionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get all the combustibleList where descripcion is not null
        defaultCombustibleFiltering("descripcion.specified=true", "descripcion.specified=false");
    }

    @Test
    @Transactional
    void getAllCombustiblesByDescripcionContainsSomething() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get all the combustibleList where descripcion contains
        defaultCombustibleFiltering("descripcion.contains=" + DEFAULT_DESCRIPCION, "descripcion.contains=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllCombustiblesByDescripcionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCombustible = combustibleRepository.saveAndFlush(combustible);

        // Get all the combustibleList where descripcion does not contain
        defaultCombustibleFiltering(
            "descripcion.doesNotContain=" + UPDATED_DESCRIPCION,
            "descripcion.doesNotContain=" + DEFAULT_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllCombustiblesByMotorIsEqualToSomething() throws Exception {
        Motor motor;
        if (TestUtil.findAll(em, Motor.class).isEmpty()) {
            combustibleRepository.saveAndFlush(combustible);
            motor = MotorResourceIT.createEntity();
        } else {
            motor = TestUtil.findAll(em, Motor.class).get(0);
        }
        em.persist(motor);
        em.flush();
        combustible.setMotor(motor);
        combustibleRepository.saveAndFlush(combustible);
        Long motorId = motor.getId();
        // Get all the combustibleList where motor equals to motorId
        defaultCombustibleShouldBeFound("motorId.equals=" + motorId);

        // Get all the combustibleList where motor equals to (motorId + 1)
        defaultCombustibleShouldNotBeFound("motorId.equals=" + (motorId + 1));
    }

    private void defaultCombustibleFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCombustibleShouldBeFound(shouldBeFound);
        defaultCombustibleShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCombustibleShouldBeFound(String filter) throws Exception {
        restCombustibleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(combustible.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));

        // Check, that the count call also returns 1
        restCombustibleMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCombustibleShouldNotBeFound(String filter) throws Exception {
        restCombustibleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCombustibleMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
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
