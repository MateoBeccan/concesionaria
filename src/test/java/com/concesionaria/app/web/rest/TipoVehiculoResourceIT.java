package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.TipoVehiculoAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.TipoVehiculo;
import com.concesionaria.app.repository.TipoVehiculoRepository;
import com.concesionaria.app.service.dto.TipoVehiculoDTO;
import com.concesionaria.app.service.mapper.TipoVehiculoMapper;
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
 * Integration tests for the {@link TipoVehiculoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TipoVehiculoResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/tipo-vehiculos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TipoVehiculoRepository tipoVehiculoRepository;

    @Autowired
    private TipoVehiculoMapper tipoVehiculoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTipoVehiculoMockMvc;

    private TipoVehiculo tipoVehiculo;

    private TipoVehiculo insertedTipoVehiculo;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TipoVehiculo createEntity() {
        return new TipoVehiculo().nombre(DEFAULT_NOMBRE).descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TipoVehiculo createUpdatedEntity() {
        return new TipoVehiculo().nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        tipoVehiculo = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTipoVehiculo != null) {
            tipoVehiculoRepository.delete(insertedTipoVehiculo);
            insertedTipoVehiculo = null;
        }
    }

    @Test
    @Transactional
    void createTipoVehiculo() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TipoVehiculo
        TipoVehiculoDTO tipoVehiculoDTO = tipoVehiculoMapper.toDto(tipoVehiculo);
        var returnedTipoVehiculoDTO = om.readValue(
            restTipoVehiculoMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoVehiculoDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TipoVehiculoDTO.class
        );

        // Validate the TipoVehiculo in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTipoVehiculo = tipoVehiculoMapper.toEntity(returnedTipoVehiculoDTO);
        assertTipoVehiculoUpdatableFieldsEquals(returnedTipoVehiculo, getPersistedTipoVehiculo(returnedTipoVehiculo));

        insertedTipoVehiculo = returnedTipoVehiculo;
    }

    @Test
    @Transactional
    void createTipoVehiculoWithExistingId() throws Exception {
        // Create the TipoVehiculo with an existing ID
        tipoVehiculo.setId(1L);
        TipoVehiculoDTO tipoVehiculoDTO = tipoVehiculoMapper.toDto(tipoVehiculo);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTipoVehiculoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoVehiculoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TipoVehiculo in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        tipoVehiculo.setNombre(null);

        // Create the TipoVehiculo, which fails.
        TipoVehiculoDTO tipoVehiculoDTO = tipoVehiculoMapper.toDto(tipoVehiculo);

        restTipoVehiculoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoVehiculoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTipoVehiculos() throws Exception {
        // Initialize the database
        insertedTipoVehiculo = tipoVehiculoRepository.saveAndFlush(tipoVehiculo);

        // Get all the tipoVehiculoList
        restTipoVehiculoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tipoVehiculo.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @Test
    @Transactional
    void getTipoVehiculo() throws Exception {
        // Initialize the database
        insertedTipoVehiculo = tipoVehiculoRepository.saveAndFlush(tipoVehiculo);

        // Get the tipoVehiculo
        restTipoVehiculoMockMvc
            .perform(get(ENTITY_API_URL_ID, tipoVehiculo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(tipoVehiculo.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getNonExistingTipoVehiculo() throws Exception {
        // Get the tipoVehiculo
        restTipoVehiculoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTipoVehiculo() throws Exception {
        // Initialize the database
        insertedTipoVehiculo = tipoVehiculoRepository.saveAndFlush(tipoVehiculo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoVehiculo
        TipoVehiculo updatedTipoVehiculo = tipoVehiculoRepository.findById(tipoVehiculo.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTipoVehiculo are not directly saved in db
        em.detach(updatedTipoVehiculo);
        updatedTipoVehiculo.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
        TipoVehiculoDTO tipoVehiculoDTO = tipoVehiculoMapper.toDto(updatedTipoVehiculo);

        restTipoVehiculoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tipoVehiculoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoVehiculoDTO))
            )
            .andExpect(status().isOk());

        // Validate the TipoVehiculo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTipoVehiculoToMatchAllProperties(updatedTipoVehiculo);
    }

    @Test
    @Transactional
    void putNonExistingTipoVehiculo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoVehiculo.setId(longCount.incrementAndGet());

        // Create the TipoVehiculo
        TipoVehiculoDTO tipoVehiculoDTO = tipoVehiculoMapper.toDto(tipoVehiculo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTipoVehiculoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tipoVehiculoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoVehiculoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoVehiculo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTipoVehiculo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoVehiculo.setId(longCount.incrementAndGet());

        // Create the TipoVehiculo
        TipoVehiculoDTO tipoVehiculoDTO = tipoVehiculoMapper.toDto(tipoVehiculo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoVehiculoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tipoVehiculoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoVehiculo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTipoVehiculo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoVehiculo.setId(longCount.incrementAndGet());

        // Create the TipoVehiculo
        TipoVehiculoDTO tipoVehiculoDTO = tipoVehiculoMapper.toDto(tipoVehiculo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoVehiculoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tipoVehiculoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TipoVehiculo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTipoVehiculoWithPatch() throws Exception {
        // Initialize the database
        insertedTipoVehiculo = tipoVehiculoRepository.saveAndFlush(tipoVehiculo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoVehiculo using partial update
        TipoVehiculo partialUpdatedTipoVehiculo = new TipoVehiculo();
        partialUpdatedTipoVehiculo.setId(tipoVehiculo.getId());

        partialUpdatedTipoVehiculo.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);

        restTipoVehiculoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTipoVehiculo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTipoVehiculo))
            )
            .andExpect(status().isOk());

        // Validate the TipoVehiculo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTipoVehiculoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTipoVehiculo, tipoVehiculo),
            getPersistedTipoVehiculo(tipoVehiculo)
        );
    }

    @Test
    @Transactional
    void fullUpdateTipoVehiculoWithPatch() throws Exception {
        // Initialize the database
        insertedTipoVehiculo = tipoVehiculoRepository.saveAndFlush(tipoVehiculo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tipoVehiculo using partial update
        TipoVehiculo partialUpdatedTipoVehiculo = new TipoVehiculo();
        partialUpdatedTipoVehiculo.setId(tipoVehiculo.getId());

        partialUpdatedTipoVehiculo.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);

        restTipoVehiculoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTipoVehiculo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTipoVehiculo))
            )
            .andExpect(status().isOk());

        // Validate the TipoVehiculo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTipoVehiculoUpdatableFieldsEquals(partialUpdatedTipoVehiculo, getPersistedTipoVehiculo(partialUpdatedTipoVehiculo));
    }

    @Test
    @Transactional
    void patchNonExistingTipoVehiculo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoVehiculo.setId(longCount.incrementAndGet());

        // Create the TipoVehiculo
        TipoVehiculoDTO tipoVehiculoDTO = tipoVehiculoMapper.toDto(tipoVehiculo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTipoVehiculoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, tipoVehiculoDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tipoVehiculoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoVehiculo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTipoVehiculo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoVehiculo.setId(longCount.incrementAndGet());

        // Create the TipoVehiculo
        TipoVehiculoDTO tipoVehiculoDTO = tipoVehiculoMapper.toDto(tipoVehiculo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoVehiculoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tipoVehiculoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TipoVehiculo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTipoVehiculo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tipoVehiculo.setId(longCount.incrementAndGet());

        // Create the TipoVehiculo
        TipoVehiculoDTO tipoVehiculoDTO = tipoVehiculoMapper.toDto(tipoVehiculo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTipoVehiculoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(tipoVehiculoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TipoVehiculo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTipoVehiculo() throws Exception {
        // Initialize the database
        insertedTipoVehiculo = tipoVehiculoRepository.saveAndFlush(tipoVehiculo);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the tipoVehiculo
        restTipoVehiculoMockMvc
            .perform(delete(ENTITY_API_URL_ID, tipoVehiculo.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return tipoVehiculoRepository.count();
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

    protected TipoVehiculo getPersistedTipoVehiculo(TipoVehiculo tipoVehiculo) {
        return tipoVehiculoRepository.findById(tipoVehiculo.getId()).orElseThrow();
    }

    protected void assertPersistedTipoVehiculoToMatchAllProperties(TipoVehiculo expectedTipoVehiculo) {
        assertTipoVehiculoAllPropertiesEquals(expectedTipoVehiculo, getPersistedTipoVehiculo(expectedTipoVehiculo));
    }

    protected void assertPersistedTipoVehiculoToMatchUpdatableProperties(TipoVehiculo expectedTipoVehiculo) {
        assertTipoVehiculoAllUpdatablePropertiesEquals(expectedTipoVehiculo, getPersistedTipoVehiculo(expectedTipoVehiculo));
    }
}
