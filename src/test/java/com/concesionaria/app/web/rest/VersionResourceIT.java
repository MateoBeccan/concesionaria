package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.VersionAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.repository.VersionRepository;
import com.concesionaria.app.service.dto.VersionDTO;
import com.concesionaria.app.service.mapper.VersionMapper;
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
 * Integration tests for the {@link VersionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class VersionResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final Integer DEFAULT_ANIO_INICIO = 1;
    private static final Integer UPDATED_ANIO_INICIO = 2;

    private static final Integer DEFAULT_ANIO_FIN = 1;
    private static final Integer UPDATED_ANIO_FIN = 2;

    private static final String ENTITY_API_URL = "/api/versions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private VersionMapper versionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVersionMockMvc;

    private Version version;

    private Version insertedVersion;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Version createEntity() {
        return new Version()
            .nombre(DEFAULT_NOMBRE)
            .descripcion(DEFAULT_DESCRIPCION)
            .anioInicio(DEFAULT_ANIO_INICIO)
            .anioFin(DEFAULT_ANIO_FIN);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Version createUpdatedEntity() {
        return new Version()
            .nombre(UPDATED_NOMBRE)
            .descripcion(UPDATED_DESCRIPCION)
            .anioInicio(UPDATED_ANIO_INICIO)
            .anioFin(UPDATED_ANIO_FIN);
    }

    @BeforeEach
    void initTest() {
        version = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedVersion != null) {
            versionRepository.delete(insertedVersion);
            insertedVersion = null;
        }
    }

    @Test
    @Transactional
    void createVersion() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(version);
        var returnedVersionDTO = om.readValue(
            restVersionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(versionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            VersionDTO.class
        );

        // Validate the Version in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedVersion = versionMapper.toEntity(returnedVersionDTO);
        assertVersionUpdatableFieldsEquals(returnedVersion, getPersistedVersion(returnedVersion));

        insertedVersion = returnedVersion;
    }

    @Test
    @Transactional
    void createVersionWithExistingId() throws Exception {
        // Create the Version with an existing ID
        version.setId(1L);
        VersionDTO versionDTO = versionMapper.toDto(version);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restVersionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(versionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Version in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        version.setNombre(null);

        // Create the Version, which fails.
        VersionDTO versionDTO = versionMapper.toDto(version);

        restVersionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(versionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllVersions() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList
        restVersionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(version.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].anioInicio").value(hasItem(DEFAULT_ANIO_INICIO)))
            .andExpect(jsonPath("$.[*].anioFin").value(hasItem(DEFAULT_ANIO_FIN)));
    }

    @Test
    @Transactional
    void getVersion() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get the version
        restVersionMockMvc
            .perform(get(ENTITY_API_URL_ID, version.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(version.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION))
            .andExpect(jsonPath("$.anioInicio").value(DEFAULT_ANIO_INICIO))
            .andExpect(jsonPath("$.anioFin").value(DEFAULT_ANIO_FIN));
    }

    @Test
    @Transactional
    void getNonExistingVersion() throws Exception {
        // Get the version
        restVersionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingVersion() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the version
        Version updatedVersion = versionRepository.findById(version.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedVersion are not directly saved in db
        em.detach(updatedVersion);
        updatedVersion.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION).anioInicio(UPDATED_ANIO_INICIO).anioFin(UPDATED_ANIO_FIN);
        VersionDTO versionDTO = versionMapper.toDto(updatedVersion);

        restVersionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, versionDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(versionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Version in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedVersionToMatchAllProperties(updatedVersion);
    }

    @Test
    @Transactional
    void putNonExistingVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        version.setId(longCount.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(version);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, versionDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(versionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Version in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        version.setId(longCount.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(version);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(versionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Version in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        version.setId(longCount.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(version);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(versionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Version in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateVersionWithPatch() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the version using partial update
        Version partialUpdatedVersion = new Version();
        partialUpdatedVersion.setId(version.getId());

        partialUpdatedVersion.descripcion(UPDATED_DESCRIPCION).anioInicio(UPDATED_ANIO_INICIO);

        restVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVersion.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVersion))
            )
            .andExpect(status().isOk());

        // Validate the Version in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVersionUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedVersion, version), getPersistedVersion(version));
    }

    @Test
    @Transactional
    void fullUpdateVersionWithPatch() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the version using partial update
        Version partialUpdatedVersion = new Version();
        partialUpdatedVersion.setId(version.getId());

        partialUpdatedVersion
            .nombre(UPDATED_NOMBRE)
            .descripcion(UPDATED_DESCRIPCION)
            .anioInicio(UPDATED_ANIO_INICIO)
            .anioFin(UPDATED_ANIO_FIN);

        restVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVersion.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVersion))
            )
            .andExpect(status().isOk());

        // Validate the Version in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVersionUpdatableFieldsEquals(partialUpdatedVersion, getPersistedVersion(partialUpdatedVersion));
    }

    @Test
    @Transactional
    void patchNonExistingVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        version.setId(longCount.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(version);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, versionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(versionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Version in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        version.setId(longCount.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(version);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(versionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Version in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        version.setId(longCount.incrementAndGet());

        // Create the Version
        VersionDTO versionDTO = versionMapper.toDto(version);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVersionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(versionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Version in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteVersion() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the version
        restVersionMockMvc
            .perform(delete(ENTITY_API_URL_ID, version.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return versionRepository.count();
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

    protected Version getPersistedVersion(Version version) {
        return versionRepository.findById(version.getId()).orElseThrow();
    }

    protected void assertPersistedVersionToMatchAllProperties(Version expectedVersion) {
        assertVersionAllPropertiesEquals(expectedVersion, getPersistedVersion(expectedVersion));
    }

    protected void assertPersistedVersionToMatchUpdatableProperties(Version expectedVersion) {
        assertVersionAllUpdatablePropertiesEquals(expectedVersion, getPersistedVersion(expectedVersion));
    }
}
