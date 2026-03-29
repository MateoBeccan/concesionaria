package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.TipoDocumentoAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.TipoDocumento;
import com.concesionaria.app.repository.TipoDocumentoRepository;
import com.concesionaria.app.service.dto.TipoDocumentoDTO;
import com.concesionaria.app.service.mapper.TipoDocumentoMapper;
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
 * Integration tests for the {@link TipoDocumentoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TipoDocumentoResourceIT {

    private static final String DEFAULT_CODIGO = "AAAAAAAAAA";
    private static final String UPDATED_CODIGO = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/tipo-documentos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private TipoDocumentoMapper tipoDocumentoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTipoDocumentoMockMvc;

    private TipoDocumento tipoDocumento;

    private TipoDocumento insertedTipoDocumento;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TipoDocumento createEntity() {
        return new TipoDocumento().codigo(DEFAULT_CODIGO).descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TipoDocumento createUpdatedEntity() {
        return new TipoDocumento().codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        tipoDocumento = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTipoDocumento != null) {
            tipoDocumentoRepository.delete(insertedTipoDocumento);
            insertedTipoDocumento = null;
        }
    }

    @Test
    @Transactional
    void createTipoDocumento() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TipoDocumento
        TipoDocumentoDTO tipoDocumentoDTO = tipoDocumentoMapper.toDto(tipoDocumento);
        var returnedTipoDocumentoDTO = om.readValue(
            restTipoDocumentoMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoDocumentoDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TipoDocumentoDTO.class
        );

        // Validate the TipoDocumento in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTipoDocumento = tipoDocumentoMapper.toEntity(returnedTipoDocumentoDTO);
        assertTipoDocumentoUpdatableFieldsEquals(returnedTipoDocumento, getPersistedTipoDocumento(returnedTipoDocumento));

        insertedTipoDocumento = returnedTipoDocumento;
    }

    @Test
    @Transactional
    void createTipoDocumentoWithExistingId() throws Exception {
        // Create the TipoDocumento with an existing ID
        tipoDocumento.setId(1L);
        TipoDocumentoDTO tipoDocumentoDTO = tipoDocumentoMapper.toDto(tipoDocumento);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTipoDocumentoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoDocumentoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TipoDocumento in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodigoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        tipoDocumento.setCodigo(null);

        // Create the TipoDocumento, which fails.
        TipoDocumentoDTO tipoDocumentoDTO = tipoDocumentoMapper.toDto(tipoDocumento);

        restTipoDocumentoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoDocumentoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTipoDocumentos() throws Exception {
        // Initialize the database
        insertedTipoDocumento = tipoDocumentoRepository.saveAndFlush(tipoDocumento);

        // Get all the tipoDocumentoList
        restTipoDocumentoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tipoDocumento.getId().intValue())))
            .andExpect(jsonPath("$.[*].codigo").value(hasItem(DEFAULT_CODIGO)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @Test
    @Transactional
    void getTipoDocumento() throws Exception {
        // Initialize the database
        insertedTipoDocumento = tipoDocumentoRepository.saveAndFlush(tipoDocumento);

        // Get the tipoDocumento
        restTipoDocumentoMockMvc
            .perform(get(ENTITY_API_URL_ID, tipoDocumento.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(tipoDocumento.getId().intValue()))
            .andExpect(jsonPath("$.codigo").value(DEFAULT_CODIGO))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getNonExistingTipoDocumento() throws Exception {
        // Get the tipoDocumento
        restTipoDocumentoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTipoDocumento() throws Exception {
        // Initialize the database
        insertedTipoDocumento = tipoDocumentoRepository.saveAndFlush(tipoDocumento);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoDocumento
        TipoDocumento updatedTipoDocumento = tipoDocumentoRepository.findById(tipoDocumento.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTipoDocumento are not directly saved in db
        em.detach(updatedTipoDocumento);
        updatedTipoDocumento.codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);
        TipoDocumentoDTO tipoDocumentoDTO = tipoDocumentoMapper.toDto(updatedTipoDocumento);

        restTipoDocumentoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tipoDocumentoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoDocumentoDTO))
            )
            .andExpect(status().isOk());

        // Validate the TipoDocumento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTipoDocumentoToMatchAllProperties(updatedTipoDocumento);
    }

    @Test
    @Transactional
    void putNonExistingTipoDocumento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoDocumento.setId(longCount.incrementAndGet());

        // Create the TipoDocumento
        TipoDocumentoDTO tipoDocumentoDTO = tipoDocumentoMapper.toDto(tipoDocumento);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTipoDocumentoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tipoDocumentoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoDocumentoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoDocumento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTipoDocumento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoDocumento.setId(longCount.incrementAndGet());

        // Create the TipoDocumento
        TipoDocumentoDTO tipoDocumentoDTO = tipoDocumentoMapper.toDto(tipoDocumento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoDocumentoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoDocumentoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoDocumento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTipoDocumento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoDocumento.setId(longCount.incrementAndGet());

        // Create the TipoDocumento
        TipoDocumentoDTO tipoDocumentoDTO = tipoDocumentoMapper.toDto(tipoDocumento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoDocumentoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoDocumentoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TipoDocumento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTipoDocumentoWithPatch() throws Exception {
        // Initialize the database
        insertedTipoDocumento = tipoDocumentoRepository.saveAndFlush(tipoDocumento);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoDocumento using partial update
        TipoDocumento partialUpdatedTipoDocumento = new TipoDocumento();
        partialUpdatedTipoDocumento.setId(tipoDocumento.getId());

        partialUpdatedTipoDocumento.descripcion(UPDATED_DESCRIPCION);

        restTipoDocumentoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTipoDocumento.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTipoDocumento))
            )
            .andExpect(status().isOk());

        // Validate the TipoDocumento in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTipoDocumentoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTipoDocumento, tipoDocumento),
            getPersistedTipoDocumento(tipoDocumento)
        );
    }

    @Test
    @Transactional
    void fullUpdateTipoDocumentoWithPatch() throws Exception {
        // Initialize the database
        insertedTipoDocumento = tipoDocumentoRepository.saveAndFlush(tipoDocumento);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoDocumento using partial update
        TipoDocumento partialUpdatedTipoDocumento = new TipoDocumento();
        partialUpdatedTipoDocumento.setId(tipoDocumento.getId());

        partialUpdatedTipoDocumento.codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);

        restTipoDocumentoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTipoDocumento.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTipoDocumento))
            )
            .andExpect(status().isOk());

        // Validate the TipoDocumento in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTipoDocumentoUpdatableFieldsEquals(partialUpdatedTipoDocumento, getPersistedTipoDocumento(partialUpdatedTipoDocumento));
    }

    @Test
    @Transactional
    void patchNonExistingTipoDocumento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoDocumento.setId(longCount.incrementAndGet());

        // Create the TipoDocumento
        TipoDocumentoDTO tipoDocumentoDTO = tipoDocumentoMapper.toDto(tipoDocumento);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTipoDocumentoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, tipoDocumentoDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tipoDocumentoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoDocumento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTipoDocumento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoDocumento.setId(longCount.incrementAndGet());

        // Create the TipoDocumento
        TipoDocumentoDTO tipoDocumentoDTO = tipoDocumentoMapper.toDto(tipoDocumento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoDocumentoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tipoDocumentoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoDocumento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTipoDocumento() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoDocumento.setId(longCount.incrementAndGet());

        // Create the TipoDocumento
        TipoDocumentoDTO tipoDocumentoDTO = tipoDocumentoMapper.toDto(tipoDocumento);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoDocumentoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(tipoDocumentoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TipoDocumento in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTipoDocumento() throws Exception {
        // Initialize the database
        insertedTipoDocumento = tipoDocumentoRepository.saveAndFlush(tipoDocumento);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the tipoDocumento
        restTipoDocumentoMockMvc
            .perform(delete(ENTITY_API_URL_ID, tipoDocumento.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return tipoDocumentoRepository.count();
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

    protected TipoDocumento getPersistedTipoDocumento(TipoDocumento tipoDocumento) {
        return tipoDocumentoRepository.findById(tipoDocumento.getId()).orElseThrow();
    }

    protected void assertPersistedTipoDocumentoToMatchAllProperties(TipoDocumento expectedTipoDocumento) {
        assertTipoDocumentoAllPropertiesEquals(expectedTipoDocumento, getPersistedTipoDocumento(expectedTipoDocumento));
    }

    protected void assertPersistedTipoDocumentoToMatchUpdatableProperties(TipoDocumento expectedTipoDocumento) {
        assertTipoDocumentoAllUpdatablePropertiesEquals(expectedTipoDocumento, getPersistedTipoDocumento(expectedTipoDocumento));
    }
}
