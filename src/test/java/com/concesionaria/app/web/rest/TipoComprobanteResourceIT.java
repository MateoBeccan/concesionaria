package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.TipoComprobanteAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.repository.TipoComprobanteRepository;
import com.concesionaria.app.service.dto.TipoComprobanteDTO;
import com.concesionaria.app.service.mapper.TipoComprobanteMapper;
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
 * Integration tests for the {@link TipoComprobanteResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TipoComprobanteResourceIT {

    private static final String DEFAULT_CODIGO = "AAAAAAAAAA";
    private static final String UPDATED_CODIGO = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/tipo-comprobantes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TipoComprobanteRepository tipoComprobanteRepository;

    @Autowired
    private TipoComprobanteMapper tipoComprobanteMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTipoComprobanteMockMvc;

    private TipoComprobante tipoComprobante;

    private TipoComprobante insertedTipoComprobante;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TipoComprobante createEntity() {
        return new TipoComprobante().codigo(DEFAULT_CODIGO).descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TipoComprobante createUpdatedEntity() {
        return new TipoComprobante().codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        tipoComprobante = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTipoComprobante != null) {
            tipoComprobanteRepository.delete(insertedTipoComprobante);
            insertedTipoComprobante = null;
        }
    }

    @Test
    @Transactional
    void createTipoComprobante() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TipoComprobante
        TipoComprobanteDTO tipoComprobanteDTO = tipoComprobanteMapper.toDto(tipoComprobante);
        var returnedTipoComprobanteDTO = om.readValue(
            restTipoComprobanteMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoComprobanteDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TipoComprobanteDTO.class
        );

        // Validate the TipoComprobante in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTipoComprobante = tipoComprobanteMapper.toEntity(returnedTipoComprobanteDTO);
        assertTipoComprobanteUpdatableFieldsEquals(returnedTipoComprobante, getPersistedTipoComprobante(returnedTipoComprobante));

        insertedTipoComprobante = returnedTipoComprobante;
    }

    @Test
    @Transactional
    void createTipoComprobanteWithExistingId() throws Exception {
        // Create the TipoComprobante with an existing ID
        tipoComprobante.setId(1L);
        TipoComprobanteDTO tipoComprobanteDTO = tipoComprobanteMapper.toDto(tipoComprobante);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTipoComprobanteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoComprobanteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TipoComprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodigoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        tipoComprobante.setCodigo(null);

        // Create the TipoComprobante, which fails.
        TipoComprobanteDTO tipoComprobanteDTO = tipoComprobanteMapper.toDto(tipoComprobante);

        restTipoComprobanteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoComprobanteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTipoComprobantes() throws Exception {
        // Initialize the database
        insertedTipoComprobante = tipoComprobanteRepository.saveAndFlush(tipoComprobante);

        // Get all the tipoComprobanteList
        restTipoComprobanteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tipoComprobante.getId().intValue())))
            .andExpect(jsonPath("$.[*].codigo").value(hasItem(DEFAULT_CODIGO)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @Test
    @Transactional
    void getTipoComprobante() throws Exception {
        // Initialize the database
        insertedTipoComprobante = tipoComprobanteRepository.saveAndFlush(tipoComprobante);

        // Get the tipoComprobante
        restTipoComprobanteMockMvc
            .perform(get(ENTITY_API_URL_ID, tipoComprobante.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(tipoComprobante.getId().intValue()))
            .andExpect(jsonPath("$.codigo").value(DEFAULT_CODIGO))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getNonExistingTipoComprobante() throws Exception {
        // Get the tipoComprobante
        restTipoComprobanteMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTipoComprobante() throws Exception {
        // Initialize the database
        insertedTipoComprobante = tipoComprobanteRepository.saveAndFlush(tipoComprobante);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoComprobante
        TipoComprobante updatedTipoComprobante = tipoComprobanteRepository.findById(tipoComprobante.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTipoComprobante are not directly saved in db
        em.detach(updatedTipoComprobante);
        updatedTipoComprobante.codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);
        TipoComprobanteDTO tipoComprobanteDTO = tipoComprobanteMapper.toDto(updatedTipoComprobante);

        restTipoComprobanteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tipoComprobanteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoComprobanteDTO))
            )
            .andExpect(status().isOk());

        // Validate the TipoComprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTipoComprobanteToMatchAllProperties(updatedTipoComprobante);
    }

    @Test
    @Transactional
    void putNonExistingTipoComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoComprobante.setId(longCount.incrementAndGet());

        // Create the TipoComprobante
        TipoComprobanteDTO tipoComprobanteDTO = tipoComprobanteMapper.toDto(tipoComprobante);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTipoComprobanteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tipoComprobanteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoComprobanteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoComprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTipoComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoComprobante.setId(longCount.incrementAndGet());

        // Create the TipoComprobante
        TipoComprobanteDTO tipoComprobanteDTO = tipoComprobanteMapper.toDto(tipoComprobante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoComprobanteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoComprobanteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoComprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTipoComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoComprobante.setId(longCount.incrementAndGet());

        // Create the TipoComprobante
        TipoComprobanteDTO tipoComprobanteDTO = tipoComprobanteMapper.toDto(tipoComprobante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoComprobanteMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoComprobanteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TipoComprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTipoComprobanteWithPatch() throws Exception {
        // Initialize the database
        insertedTipoComprobante = tipoComprobanteRepository.saveAndFlush(tipoComprobante);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoComprobante using partial update
        TipoComprobante partialUpdatedTipoComprobante = new TipoComprobante();
        partialUpdatedTipoComprobante.setId(tipoComprobante.getId());

        partialUpdatedTipoComprobante.descripcion(UPDATED_DESCRIPCION);

        restTipoComprobanteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTipoComprobante.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTipoComprobante))
            )
            .andExpect(status().isOk());

        // Validate the TipoComprobante in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTipoComprobanteUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTipoComprobante, tipoComprobante),
            getPersistedTipoComprobante(tipoComprobante)
        );
    }

    @Test
    @Transactional
    void fullUpdateTipoComprobanteWithPatch() throws Exception {
        // Initialize the database
        insertedTipoComprobante = tipoComprobanteRepository.saveAndFlush(tipoComprobante);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoComprobante using partial update
        TipoComprobante partialUpdatedTipoComprobante = new TipoComprobante();
        partialUpdatedTipoComprobante.setId(tipoComprobante.getId());

        partialUpdatedTipoComprobante.codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);

        restTipoComprobanteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTipoComprobante.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTipoComprobante))
            )
            .andExpect(status().isOk());

        // Validate the TipoComprobante in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTipoComprobanteUpdatableFieldsEquals(
            partialUpdatedTipoComprobante,
            getPersistedTipoComprobante(partialUpdatedTipoComprobante)
        );
    }

    @Test
    @Transactional
    void patchNonExistingTipoComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoComprobante.setId(longCount.incrementAndGet());

        // Create the TipoComprobante
        TipoComprobanteDTO tipoComprobanteDTO = tipoComprobanteMapper.toDto(tipoComprobante);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTipoComprobanteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, tipoComprobanteDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tipoComprobanteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoComprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTipoComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoComprobante.setId(longCount.incrementAndGet());

        // Create the TipoComprobante
        TipoComprobanteDTO tipoComprobanteDTO = tipoComprobanteMapper.toDto(tipoComprobante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoComprobanteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tipoComprobanteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoComprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTipoComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoComprobante.setId(longCount.incrementAndGet());

        // Create the TipoComprobante
        TipoComprobanteDTO tipoComprobanteDTO = tipoComprobanteMapper.toDto(tipoComprobante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoComprobanteMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(tipoComprobanteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TipoComprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTipoComprobante() throws Exception {
        // Initialize the database
        insertedTipoComprobante = tipoComprobanteRepository.saveAndFlush(tipoComprobante);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the tipoComprobante
        restTipoComprobanteMockMvc
            .perform(delete(ENTITY_API_URL_ID, tipoComprobante.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return tipoComprobanteRepository.count();
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

    protected TipoComprobante getPersistedTipoComprobante(TipoComprobante tipoComprobante) {
        return tipoComprobanteRepository.findById(tipoComprobante.getId()).orElseThrow();
    }

    protected void assertPersistedTipoComprobanteToMatchAllProperties(TipoComprobante expectedTipoComprobante) {
        assertTipoComprobanteAllPropertiesEquals(expectedTipoComprobante, getPersistedTipoComprobante(expectedTipoComprobante));
    }

    protected void assertPersistedTipoComprobanteToMatchUpdatableProperties(TipoComprobante expectedTipoComprobante) {
        assertTipoComprobanteAllUpdatablePropertiesEquals(expectedTipoComprobante, getPersistedTipoComprobante(expectedTipoComprobante));
    }
}
