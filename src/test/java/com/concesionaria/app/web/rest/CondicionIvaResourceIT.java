package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.CondicionIvaAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.CondicionIva;
import com.concesionaria.app.repository.CondicionIvaRepository;
import com.concesionaria.app.service.dto.CondicionIvaDTO;
import com.concesionaria.app.service.mapper.CondicionIvaMapper;
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
 * Integration tests for the {@link CondicionIvaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CondicionIvaResourceIT {

    private static final String DEFAULT_CODIGO = "AAAAAAAAAA";
    private static final String UPDATED_CODIGO = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/condicion-ivas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CondicionIvaRepository condicionIvaRepository;

    @Autowired
    private CondicionIvaMapper condicionIvaMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCondicionIvaMockMvc;

    private CondicionIva condicionIva;

    private CondicionIva insertedCondicionIva;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CondicionIva createEntity() {
        return new CondicionIva().codigo(DEFAULT_CODIGO).descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CondicionIva createUpdatedEntity() {
        return new CondicionIva().codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        condicionIva = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCondicionIva != null) {
            condicionIvaRepository.delete(insertedCondicionIva);
            insertedCondicionIva = null;
        }
    }

    @Test
    @Transactional
    void createCondicionIva() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the CondicionIva
        CondicionIvaDTO condicionIvaDTO = condicionIvaMapper.toDto(condicionIva);
        var returnedCondicionIvaDTO = om.readValue(
            restCondicionIvaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(condicionIvaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CondicionIvaDTO.class
        );

        // Validate the CondicionIva in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCondicionIva = condicionIvaMapper.toEntity(returnedCondicionIvaDTO);
        assertCondicionIvaUpdatableFieldsEquals(returnedCondicionIva, getPersistedCondicionIva(returnedCondicionIva));

        insertedCondicionIva = returnedCondicionIva;
    }

    @Test
    @Transactional
    void createCondicionIvaWithExistingId() throws Exception {
        // Create the CondicionIva with an existing ID
        condicionIva.setId(1L);
        CondicionIvaDTO condicionIvaDTO = condicionIvaMapper.toDto(condicionIva);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCondicionIvaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(condicionIvaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the CondicionIva in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodigoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        condicionIva.setCodigo(null);

        // Create the CondicionIva, which fails.
        CondicionIvaDTO condicionIvaDTO = condicionIvaMapper.toDto(condicionIva);

        restCondicionIvaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(condicionIvaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCondicionIvas() throws Exception {
        // Initialize the database
        insertedCondicionIva = condicionIvaRepository.saveAndFlush(condicionIva);

        // Get all the condicionIvaList
        restCondicionIvaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(condicionIva.getId().intValue())))
            .andExpect(jsonPath("$.[*].codigo").value(hasItem(DEFAULT_CODIGO)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @Test
    @Transactional
    void getCondicionIva() throws Exception {
        // Initialize the database
        insertedCondicionIva = condicionIvaRepository.saveAndFlush(condicionIva);

        // Get the condicionIva
        restCondicionIvaMockMvc
            .perform(get(ENTITY_API_URL_ID, condicionIva.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(condicionIva.getId().intValue()))
            .andExpect(jsonPath("$.codigo").value(DEFAULT_CODIGO))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getNonExistingCondicionIva() throws Exception {
        // Get the condicionIva
        restCondicionIvaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCondicionIva() throws Exception {
        // Initialize the database
        insertedCondicionIva = condicionIvaRepository.saveAndFlush(condicionIva);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the condicionIva
        CondicionIva updatedCondicionIva = condicionIvaRepository.findById(condicionIva.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCondicionIva are not directly saved in db
        em.detach(updatedCondicionIva);
        updatedCondicionIva.codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);
        CondicionIvaDTO condicionIvaDTO = condicionIvaMapper.toDto(updatedCondicionIva);

        restCondicionIvaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, condicionIvaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(condicionIvaDTO))
            )
            .andExpect(status().isOk());

        // Validate the CondicionIva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCondicionIvaToMatchAllProperties(updatedCondicionIva);
    }

    @Test
    @Transactional
    void putNonExistingCondicionIva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        condicionIva.setId(longCount.incrementAndGet());

        // Create the CondicionIva
        CondicionIvaDTO condicionIvaDTO = condicionIvaMapper.toDto(condicionIva);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCondicionIvaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, condicionIvaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(condicionIvaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CondicionIva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCondicionIva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        condicionIva.setId(longCount.incrementAndGet());

        // Create the CondicionIva
        CondicionIvaDTO condicionIvaDTO = condicionIvaMapper.toDto(condicionIva);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCondicionIvaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(condicionIvaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CondicionIva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCondicionIva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        condicionIva.setId(longCount.incrementAndGet());

        // Create the CondicionIva
        CondicionIvaDTO condicionIvaDTO = condicionIvaMapper.toDto(condicionIva);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCondicionIvaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(condicionIvaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CondicionIva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCondicionIvaWithPatch() throws Exception {
        // Initialize the database
        insertedCondicionIva = condicionIvaRepository.saveAndFlush(condicionIva);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the condicionIva using partial update
        CondicionIva partialUpdatedCondicionIva = new CondicionIva();
        partialUpdatedCondicionIva.setId(condicionIva.getId());

        partialUpdatedCondicionIva.descripcion(UPDATED_DESCRIPCION);

        restCondicionIvaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCondicionIva.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCondicionIva))
            )
            .andExpect(status().isOk());

        // Validate the CondicionIva in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCondicionIvaUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCondicionIva, condicionIva),
            getPersistedCondicionIva(condicionIva)
        );
    }

    @Test
    @Transactional
    void fullUpdateCondicionIvaWithPatch() throws Exception {
        // Initialize the database
        insertedCondicionIva = condicionIvaRepository.saveAndFlush(condicionIva);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the condicionIva using partial update
        CondicionIva partialUpdatedCondicionIva = new CondicionIva();
        partialUpdatedCondicionIva.setId(condicionIva.getId());

        partialUpdatedCondicionIva.codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);

        restCondicionIvaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCondicionIva.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCondicionIva))
            )
            .andExpect(status().isOk());

        // Validate the CondicionIva in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCondicionIvaUpdatableFieldsEquals(partialUpdatedCondicionIva, getPersistedCondicionIva(partialUpdatedCondicionIva));
    }

    @Test
    @Transactional
    void patchNonExistingCondicionIva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        condicionIva.setId(longCount.incrementAndGet());

        // Create the CondicionIva
        CondicionIvaDTO condicionIvaDTO = condicionIvaMapper.toDto(condicionIva);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCondicionIvaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, condicionIvaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(condicionIvaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CondicionIva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCondicionIva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        condicionIva.setId(longCount.incrementAndGet());

        // Create the CondicionIva
        CondicionIvaDTO condicionIvaDTO = condicionIvaMapper.toDto(condicionIva);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCondicionIvaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(condicionIvaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CondicionIva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCondicionIva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        condicionIva.setId(longCount.incrementAndGet());

        // Create the CondicionIva
        CondicionIvaDTO condicionIvaDTO = condicionIvaMapper.toDto(condicionIva);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCondicionIvaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(condicionIvaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CondicionIva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCondicionIva() throws Exception {
        // Initialize the database
        insertedCondicionIva = condicionIvaRepository.saveAndFlush(condicionIva);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the condicionIva
        restCondicionIvaMockMvc
            .perform(delete(ENTITY_API_URL_ID, condicionIva.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return condicionIvaRepository.count();
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

    protected CondicionIva getPersistedCondicionIva(CondicionIva condicionIva) {
        return condicionIvaRepository.findById(condicionIva.getId()).orElseThrow();
    }

    protected void assertPersistedCondicionIvaToMatchAllProperties(CondicionIva expectedCondicionIva) {
        assertCondicionIvaAllPropertiesEquals(expectedCondicionIva, getPersistedCondicionIva(expectedCondicionIva));
    }

    protected void assertPersistedCondicionIvaToMatchUpdatableProperties(CondicionIva expectedCondicionIva) {
        assertCondicionIvaAllUpdatablePropertiesEquals(expectedCondicionIva, getPersistedCondicionIva(expectedCondicionIva));
    }
}
