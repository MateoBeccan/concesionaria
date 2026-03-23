package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.MarcaAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.repository.MarcaRepository;
import com.concesionaria.app.service.dto.MarcaDTO;
import com.concesionaria.app.service.mapper.MarcaMapper;
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
 * Integration tests for the {@link MarcaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MarcaResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_PAIS_ORIGEN = "AAAAAAAAAA";
    private static final String UPDATED_PAIS_ORIGEN = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/marcas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private MarcaMapper marcaMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMarcaMockMvc;

    private Marca marca;

    private Marca insertedMarca;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Marca createEntity() {
        return new Marca().nombre(DEFAULT_NOMBRE).paisOrigen(DEFAULT_PAIS_ORIGEN);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Marca createUpdatedEntity() {
        return new Marca().nombre(UPDATED_NOMBRE).paisOrigen(UPDATED_PAIS_ORIGEN);
    }

    @BeforeEach
    void initTest() {
        marca = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMarca != null) {
            marcaRepository.delete(insertedMarca);
            insertedMarca = null;
        }
    }

    @Test
    @Transactional
    void createMarca() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Marca
        MarcaDTO marcaDTO = marcaMapper.toDto(marca);
        var returnedMarcaDTO = om.readValue(
            restMarcaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marcaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MarcaDTO.class
        );

        // Validate the Marca in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMarca = marcaMapper.toEntity(returnedMarcaDTO);
        assertMarcaUpdatableFieldsEquals(returnedMarca, getPersistedMarca(returnedMarca));

        insertedMarca = returnedMarca;
    }

    @Test
    @Transactional
    void createMarcaWithExistingId() throws Exception {
        // Create the Marca with an existing ID
        marca.setId(1L);
        MarcaDTO marcaDTO = marcaMapper.toDto(marca);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMarcaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marcaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Marca in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        marca.setNombre(null);

        // Create the Marca, which fails.
        MarcaDTO marcaDTO = marcaMapper.toDto(marca);

        restMarcaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marcaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMarcas() throws Exception {
        // Initialize the database
        insertedMarca = marcaRepository.saveAndFlush(marca);

        // Get all the marcaList
        restMarcaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(marca.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].paisOrigen").value(hasItem(DEFAULT_PAIS_ORIGEN)));
    }

    @Test
    @Transactional
    void getMarca() throws Exception {
        // Initialize the database
        insertedMarca = marcaRepository.saveAndFlush(marca);

        // Get the marca
        restMarcaMockMvc
            .perform(get(ENTITY_API_URL_ID, marca.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(marca.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.paisOrigen").value(DEFAULT_PAIS_ORIGEN));
    }

    @Test
    @Transactional
    void getNonExistingMarca() throws Exception {
        // Get the marca
        restMarcaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMarca() throws Exception {
        // Initialize the database
        insertedMarca = marcaRepository.saveAndFlush(marca);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the marca
        Marca updatedMarca = marcaRepository.findById(marca.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMarca are not directly saved in db
        em.detach(updatedMarca);
        updatedMarca.nombre(UPDATED_NOMBRE).paisOrigen(UPDATED_PAIS_ORIGEN);
        MarcaDTO marcaDTO = marcaMapper.toDto(updatedMarca);

        restMarcaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, marcaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marcaDTO))
            )
            .andExpect(status().isOk());

        // Validate the Marca in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMarcaToMatchAllProperties(updatedMarca);
    }

    @Test
    @Transactional
    void putNonExistingMarca() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marca.setId(longCount.incrementAndGet());

        // Create the Marca
        MarcaDTO marcaDTO = marcaMapper.toDto(marca);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMarcaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, marcaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marcaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Marca in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMarca() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marca.setId(longCount.incrementAndGet());

        // Create the Marca
        MarcaDTO marcaDTO = marcaMapper.toDto(marca);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarcaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(marcaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Marca in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMarca() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marca.setId(longCount.incrementAndGet());

        // Create the Marca
        MarcaDTO marcaDTO = marcaMapper.toDto(marca);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarcaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marcaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Marca in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMarcaWithPatch() throws Exception {
        // Initialize the database
        insertedMarca = marcaRepository.saveAndFlush(marca);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the marca using partial update
        Marca partialUpdatedMarca = new Marca();
        partialUpdatedMarca.setId(marca.getId());

        partialUpdatedMarca.paisOrigen(UPDATED_PAIS_ORIGEN);

        restMarcaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMarca.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMarca))
            )
            .andExpect(status().isOk());

        // Validate the Marca in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMarcaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedMarca, marca), getPersistedMarca(marca));
    }

    @Test
    @Transactional
    void fullUpdateMarcaWithPatch() throws Exception {
        // Initialize the database
        insertedMarca = marcaRepository.saveAndFlush(marca);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the marca using partial update
        Marca partialUpdatedMarca = new Marca();
        partialUpdatedMarca.setId(marca.getId());

        partialUpdatedMarca.nombre(UPDATED_NOMBRE).paisOrigen(UPDATED_PAIS_ORIGEN);

        restMarcaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMarca.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMarca))
            )
            .andExpect(status().isOk());

        // Validate the Marca in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMarcaUpdatableFieldsEquals(partialUpdatedMarca, getPersistedMarca(partialUpdatedMarca));
    }

    @Test
    @Transactional
    void patchNonExistingMarca() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marca.setId(longCount.incrementAndGet());

        // Create the Marca
        MarcaDTO marcaDTO = marcaMapper.toDto(marca);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMarcaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, marcaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(marcaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Marca in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMarca() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marca.setId(longCount.incrementAndGet());

        // Create the Marca
        MarcaDTO marcaDTO = marcaMapper.toDto(marca);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarcaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(marcaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Marca in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMarca() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marca.setId(longCount.incrementAndGet());

        // Create the Marca
        MarcaDTO marcaDTO = marcaMapper.toDto(marca);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarcaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(marcaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Marca in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMarca() throws Exception {
        // Initialize the database
        insertedMarca = marcaRepository.saveAndFlush(marca);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the marca
        restMarcaMockMvc
            .perform(delete(ENTITY_API_URL_ID, marca.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return marcaRepository.count();
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

    protected Marca getPersistedMarca(Marca marca) {
        return marcaRepository.findById(marca.getId()).orElseThrow();
    }

    protected void assertPersistedMarcaToMatchAllProperties(Marca expectedMarca) {
        assertMarcaAllPropertiesEquals(expectedMarca, getPersistedMarca(expectedMarca));
    }

    protected void assertPersistedMarcaToMatchUpdatableProperties(Marca expectedMarca) {
        assertMarcaAllUpdatablePropertiesEquals(expectedMarca, getPersistedMarca(expectedMarca));
    }
}
