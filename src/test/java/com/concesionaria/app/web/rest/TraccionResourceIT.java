package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.TraccionAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Traccion;
import com.concesionaria.app.repository.TraccionRepository;
import com.concesionaria.app.service.dto.TraccionDTO;
import com.concesionaria.app.service.mapper.TraccionMapper;
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
 * Integration tests for the {@link TraccionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TraccionResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/traccions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TraccionRepository traccionRepository;

    @Autowired
    private TraccionMapper traccionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTraccionMockMvc;

    private Traccion traccion;

    private Traccion insertedTraccion;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Traccion createEntity() {
        return new Traccion().nombre(DEFAULT_NOMBRE).descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Traccion createUpdatedEntity() {
        return new Traccion().nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        traccion = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTraccion != null) {
            traccionRepository.delete(insertedTraccion);
            insertedTraccion = null;
        }
    }

    @Test
    @Transactional
    void createTraccion() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Traccion
        TraccionDTO traccionDTO = traccionMapper.toDto(traccion);
        var returnedTraccionDTO = om.readValue(
            restTraccionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(traccionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TraccionDTO.class
        );

        // Validate the Traccion in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTraccion = traccionMapper.toEntity(returnedTraccionDTO);
        assertTraccionUpdatableFieldsEquals(returnedTraccion, getPersistedTraccion(returnedTraccion));

        insertedTraccion = returnedTraccion;
    }

    @Test
    @Transactional
    void createTraccionWithExistingId() throws Exception {
        // Create the Traccion with an existing ID
        traccion.setId(1L);
        TraccionDTO traccionDTO = traccionMapper.toDto(traccion);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTraccionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(traccionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Traccion in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        traccion.setNombre(null);

        // Create the Traccion, which fails.
        TraccionDTO traccionDTO = traccionMapper.toDto(traccion);

        restTraccionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(traccionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTraccions() throws Exception {
        // Initialize the database
        insertedTraccion = traccionRepository.saveAndFlush(traccion);

        // Get all the traccionList
        restTraccionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(traccion.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @Test
    @Transactional
    void getTraccion() throws Exception {
        // Initialize the database
        insertedTraccion = traccionRepository.saveAndFlush(traccion);

        // Get the traccion
        restTraccionMockMvc
            .perform(get(ENTITY_API_URL_ID, traccion.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(traccion.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getNonExistingTraccion() throws Exception {
        // Get the traccion
        restTraccionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTraccion() throws Exception {
        // Initialize the database
        insertedTraccion = traccionRepository.saveAndFlush(traccion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the traccion
        Traccion updatedTraccion = traccionRepository.findById(traccion.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTraccion are not directly saved in db
        em.detach(updatedTraccion);
        updatedTraccion.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
        TraccionDTO traccionDTO = traccionMapper.toDto(updatedTraccion);

        restTraccionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, traccionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(traccionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Traccion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTraccionToMatchAllProperties(updatedTraccion);
    }

    @Test
    @Transactional
    void putNonExistingTraccion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traccion.setId(longCount.incrementAndGet());

        // Create the Traccion
        TraccionDTO traccionDTO = traccionMapper.toDto(traccion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTraccionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, traccionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(traccionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Traccion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTraccion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traccion.setId(longCount.incrementAndGet());

        // Create the Traccion
        TraccionDTO traccionDTO = traccionMapper.toDto(traccion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTraccionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(traccionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Traccion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTraccion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traccion.setId(longCount.incrementAndGet());

        // Create the Traccion
        TraccionDTO traccionDTO = traccionMapper.toDto(traccion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTraccionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(traccionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Traccion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTraccionWithPatch() throws Exception {
        // Initialize the database
        insertedTraccion = traccionRepository.saveAndFlush(traccion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the traccion using partial update
        Traccion partialUpdatedTraccion = new Traccion();
        partialUpdatedTraccion.setId(traccion.getId());

        partialUpdatedTraccion.descripcion(UPDATED_DESCRIPCION);

        restTraccionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTraccion.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTraccion))
            )
            .andExpect(status().isOk());

        // Validate the Traccion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTraccionUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedTraccion, traccion), getPersistedTraccion(traccion));
    }

    @Test
    @Transactional
    void fullUpdateTraccionWithPatch() throws Exception {
        // Initialize the database
        insertedTraccion = traccionRepository.saveAndFlush(traccion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the traccion using partial update
        Traccion partialUpdatedTraccion = new Traccion();
        partialUpdatedTraccion.setId(traccion.getId());

        partialUpdatedTraccion.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);

        restTraccionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTraccion.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTraccion))
            )
            .andExpect(status().isOk());

        // Validate the Traccion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTraccionUpdatableFieldsEquals(partialUpdatedTraccion, getPersistedTraccion(partialUpdatedTraccion));
    }

    @Test
    @Transactional
    void patchNonExistingTraccion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traccion.setId(longCount.incrementAndGet());

        // Create the Traccion
        TraccionDTO traccionDTO = traccionMapper.toDto(traccion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTraccionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, traccionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(traccionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Traccion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTraccion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traccion.setId(longCount.incrementAndGet());

        // Create the Traccion
        TraccionDTO traccionDTO = traccionMapper.toDto(traccion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTraccionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(traccionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Traccion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTraccion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traccion.setId(longCount.incrementAndGet());

        // Create the Traccion
        TraccionDTO traccionDTO = traccionMapper.toDto(traccion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTraccionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(traccionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Traccion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTraccion() throws Exception {
        // Initialize the database
        insertedTraccion = traccionRepository.saveAndFlush(traccion);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the traccion
        restTraccionMockMvc
            .perform(delete(ENTITY_API_URL_ID, traccion.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return traccionRepository.count();
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

    protected Traccion getPersistedTraccion(Traccion traccion) {
        return traccionRepository.findById(traccion.getId()).orElseThrow();
    }

    protected void assertPersistedTraccionToMatchAllProperties(Traccion expectedTraccion) {
        assertTraccionAllPropertiesEquals(expectedTraccion, getPersistedTraccion(expectedTraccion));
    }

    protected void assertPersistedTraccionToMatchUpdatableProperties(Traccion expectedTraccion) {
        assertTraccionAllUpdatablePropertiesEquals(expectedTraccion, getPersistedTraccion(expectedTraccion));
    }
}
