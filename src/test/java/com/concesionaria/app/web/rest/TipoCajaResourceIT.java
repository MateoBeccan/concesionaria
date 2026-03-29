package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.TipoCajaAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.TipoCaja;
import com.concesionaria.app.repository.TipoCajaRepository;
import com.concesionaria.app.service.dto.TipoCajaDTO;
import com.concesionaria.app.service.mapper.TipoCajaMapper;
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
 * Integration tests for the {@link TipoCajaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TipoCajaResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/tipo-cajas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TipoCajaRepository tipoCajaRepository;

    @Autowired
    private TipoCajaMapper tipoCajaMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTipoCajaMockMvc;

    private TipoCaja tipoCaja;

    private TipoCaja insertedTipoCaja;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TipoCaja createEntity() {
        return new TipoCaja().nombre(DEFAULT_NOMBRE).descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TipoCaja createUpdatedEntity() {
        return new TipoCaja().nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        tipoCaja = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTipoCaja != null) {
            tipoCajaRepository.delete(insertedTipoCaja);
            insertedTipoCaja = null;
        }
    }

    @Test
    @Transactional
    void createTipoCaja() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TipoCaja
        TipoCajaDTO tipoCajaDTO = tipoCajaMapper.toDto(tipoCaja);
        var returnedTipoCajaDTO = om.readValue(
            restTipoCajaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoCajaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TipoCajaDTO.class
        );

        // Validate the TipoCaja in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTipoCaja = tipoCajaMapper.toEntity(returnedTipoCajaDTO);
        assertTipoCajaUpdatableFieldsEquals(returnedTipoCaja, getPersistedTipoCaja(returnedTipoCaja));

        insertedTipoCaja = returnedTipoCaja;
    }

    @Test
    @Transactional
    void createTipoCajaWithExistingId() throws Exception {
        // Create the TipoCaja with an existing ID
        tipoCaja.setId(1L);
        TipoCajaDTO tipoCajaDTO = tipoCajaMapper.toDto(tipoCaja);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTipoCajaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoCajaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TipoCaja in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        tipoCaja.setNombre(null);

        // Create the TipoCaja, which fails.
        TipoCajaDTO tipoCajaDTO = tipoCajaMapper.toDto(tipoCaja);

        restTipoCajaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoCajaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTipoCajas() throws Exception {
        // Initialize the database
        insertedTipoCaja = tipoCajaRepository.saveAndFlush(tipoCaja);

        // Get all the tipoCajaList
        restTipoCajaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tipoCaja.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @Test
    @Transactional
    void getTipoCaja() throws Exception {
        // Initialize the database
        insertedTipoCaja = tipoCajaRepository.saveAndFlush(tipoCaja);

        // Get the tipoCaja
        restTipoCajaMockMvc
            .perform(get(ENTITY_API_URL_ID, tipoCaja.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(tipoCaja.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getNonExistingTipoCaja() throws Exception {
        // Get the tipoCaja
        restTipoCajaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTipoCaja() throws Exception {
        // Initialize the database
        insertedTipoCaja = tipoCajaRepository.saveAndFlush(tipoCaja);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoCaja
        TipoCaja updatedTipoCaja = tipoCajaRepository.findById(tipoCaja.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTipoCaja are not directly saved in db
        em.detach(updatedTipoCaja);
        updatedTipoCaja.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
        TipoCajaDTO tipoCajaDTO = tipoCajaMapper.toDto(updatedTipoCaja);

        restTipoCajaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tipoCajaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoCajaDTO))
            )
            .andExpect(status().isOk());

        // Validate the TipoCaja in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTipoCajaToMatchAllProperties(updatedTipoCaja);
    }

    @Test
    @Transactional
    void putNonExistingTipoCaja() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoCaja.setId(longCount.incrementAndGet());

        // Create the TipoCaja
        TipoCajaDTO tipoCajaDTO = tipoCajaMapper.toDto(tipoCaja);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTipoCajaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tipoCajaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoCajaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoCaja in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTipoCaja() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoCaja.setId(longCount.incrementAndGet());

        // Create the TipoCaja
        TipoCajaDTO tipoCajaDTO = tipoCajaMapper.toDto(tipoCaja);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoCajaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoCajaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoCaja in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTipoCaja() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoCaja.setId(longCount.incrementAndGet());

        // Create the TipoCaja
        TipoCajaDTO tipoCajaDTO = tipoCajaMapper.toDto(tipoCaja);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoCajaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoCajaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TipoCaja in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTipoCajaWithPatch() throws Exception {
        // Initialize the database
        insertedTipoCaja = tipoCajaRepository.saveAndFlush(tipoCaja);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoCaja using partial update
        TipoCaja partialUpdatedTipoCaja = new TipoCaja();
        partialUpdatedTipoCaja.setId(tipoCaja.getId());

        restTipoCajaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTipoCaja.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTipoCaja))
            )
            .andExpect(status().isOk());

        // Validate the TipoCaja in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTipoCajaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedTipoCaja, tipoCaja), getPersistedTipoCaja(tipoCaja));
    }

    @Test
    @Transactional
    void fullUpdateTipoCajaWithPatch() throws Exception {
        // Initialize the database
        insertedTipoCaja = tipoCajaRepository.saveAndFlush(tipoCaja);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoCaja using partial update
        TipoCaja partialUpdatedTipoCaja = new TipoCaja();
        partialUpdatedTipoCaja.setId(tipoCaja.getId());

        partialUpdatedTipoCaja.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);

        restTipoCajaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTipoCaja.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTipoCaja))
            )
            .andExpect(status().isOk());

        // Validate the TipoCaja in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTipoCajaUpdatableFieldsEquals(partialUpdatedTipoCaja, getPersistedTipoCaja(partialUpdatedTipoCaja));
    }

    @Test
    @Transactional
    void patchNonExistingTipoCaja() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoCaja.setId(longCount.incrementAndGet());

        // Create the TipoCaja
        TipoCajaDTO tipoCajaDTO = tipoCajaMapper.toDto(tipoCaja);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTipoCajaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, tipoCajaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tipoCajaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoCaja in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTipoCaja() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoCaja.setId(longCount.incrementAndGet());

        // Create the TipoCaja
        TipoCajaDTO tipoCajaDTO = tipoCajaMapper.toDto(tipoCaja);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoCajaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tipoCajaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoCaja in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTipoCaja() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoCaja.setId(longCount.incrementAndGet());

        // Create the TipoCaja
        TipoCajaDTO tipoCajaDTO = tipoCajaMapper.toDto(tipoCaja);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoCajaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(tipoCajaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TipoCaja in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTipoCaja() throws Exception {
        // Initialize the database
        insertedTipoCaja = tipoCajaRepository.saveAndFlush(tipoCaja);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the tipoCaja
        restTipoCajaMockMvc
            .perform(delete(ENTITY_API_URL_ID, tipoCaja.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return tipoCajaRepository.count();
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

    protected TipoCaja getPersistedTipoCaja(TipoCaja tipoCaja) {
        return tipoCajaRepository.findById(tipoCaja.getId()).orElseThrow();
    }

    protected void assertPersistedTipoCajaToMatchAllProperties(TipoCaja expectedTipoCaja) {
        assertTipoCajaAllPropertiesEquals(expectedTipoCaja, getPersistedTipoCaja(expectedTipoCaja));
    }

    protected void assertPersistedTipoCajaToMatchUpdatableProperties(TipoCaja expectedTipoCaja) {
        assertTipoCajaAllUpdatablePropertiesEquals(expectedTipoCaja, getPersistedTipoCaja(expectedTipoCaja));
    }
}
