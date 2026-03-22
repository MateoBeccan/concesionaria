package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.VersionAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.repository.VersionRepository;
import com.concesionaria.app.service.VersionService;
import com.concesionaria.app.service.dto.VersionDTO;
import com.concesionaria.app.service.mapper.VersionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link VersionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class VersionResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final Integer DEFAULT_ANIO_INICIO = 1;
    private static final Integer UPDATED_ANIO_INICIO = 2;
    private static final Integer SMALLER_ANIO_INICIO = 1 - 1;

    private static final Integer DEFAULT_ANIO_FIN = 1;
    private static final Integer UPDATED_ANIO_FIN = 2;
    private static final Integer SMALLER_ANIO_FIN = 1 - 1;

    private static final String ENTITY_API_URL = "/api/versions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VersionRepository versionRepository;

    @Mock
    private VersionRepository versionRepositoryMock;

    @Autowired
    private VersionMapper versionMapper;

    @Mock
    private VersionService versionServiceMock;

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

    @SuppressWarnings({ "unchecked" })
    void getAllVersionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(versionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVersionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(versionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVersionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(versionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVersionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(versionRepositoryMock, times(1)).findAll(any(Pageable.class));
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
    void getVersionsByIdFiltering() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        Long id = version.getId();

        defaultVersionFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultVersionFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultVersionFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllVersionsByNombreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where nombre equals to
        defaultVersionFiltering("nombre.equals=" + DEFAULT_NOMBRE, "nombre.equals=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllVersionsByNombreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where nombre in
        defaultVersionFiltering("nombre.in=" + DEFAULT_NOMBRE + "," + UPDATED_NOMBRE, "nombre.in=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllVersionsByNombreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where nombre is not null
        defaultVersionFiltering("nombre.specified=true", "nombre.specified=false");
    }

    @Test
    @Transactional
    void getAllVersionsByNombreContainsSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where nombre contains
        defaultVersionFiltering("nombre.contains=" + DEFAULT_NOMBRE, "nombre.contains=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllVersionsByNombreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where nombre does not contain
        defaultVersionFiltering("nombre.doesNotContain=" + UPDATED_NOMBRE, "nombre.doesNotContain=" + DEFAULT_NOMBRE);
    }

    @Test
    @Transactional
    void getAllVersionsByDescripcionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where descripcion equals to
        defaultVersionFiltering("descripcion.equals=" + DEFAULT_DESCRIPCION, "descripcion.equals=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllVersionsByDescripcionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where descripcion in
        defaultVersionFiltering(
            "descripcion.in=" + DEFAULT_DESCRIPCION + "," + UPDATED_DESCRIPCION,
            "descripcion.in=" + UPDATED_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllVersionsByDescripcionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where descripcion is not null
        defaultVersionFiltering("descripcion.specified=true", "descripcion.specified=false");
    }

    @Test
    @Transactional
    void getAllVersionsByDescripcionContainsSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where descripcion contains
        defaultVersionFiltering("descripcion.contains=" + DEFAULT_DESCRIPCION, "descripcion.contains=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllVersionsByDescripcionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where descripcion does not contain
        defaultVersionFiltering("descripcion.doesNotContain=" + UPDATED_DESCRIPCION, "descripcion.doesNotContain=" + DEFAULT_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllVersionsByAnioInicioIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioInicio equals to
        defaultVersionFiltering("anioInicio.equals=" + DEFAULT_ANIO_INICIO, "anioInicio.equals=" + UPDATED_ANIO_INICIO);
    }

    @Test
    @Transactional
    void getAllVersionsByAnioInicioIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioInicio in
        defaultVersionFiltering("anioInicio.in=" + DEFAULT_ANIO_INICIO + "," + UPDATED_ANIO_INICIO, "anioInicio.in=" + UPDATED_ANIO_INICIO);
    }

    @Test
    @Transactional
    void getAllVersionsByAnioInicioIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioInicio is not null
        defaultVersionFiltering("anioInicio.specified=true", "anioInicio.specified=false");
    }

    @Test
    @Transactional
    void getAllVersionsByAnioInicioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioInicio is greater than or equal to
        defaultVersionFiltering(
            "anioInicio.greaterThanOrEqual=" + DEFAULT_ANIO_INICIO,
            "anioInicio.greaterThanOrEqual=" + UPDATED_ANIO_INICIO
        );
    }

    @Test
    @Transactional
    void getAllVersionsByAnioInicioIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioInicio is less than or equal to
        defaultVersionFiltering("anioInicio.lessThanOrEqual=" + DEFAULT_ANIO_INICIO, "anioInicio.lessThanOrEqual=" + SMALLER_ANIO_INICIO);
    }

    @Test
    @Transactional
    void getAllVersionsByAnioInicioIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioInicio is less than
        defaultVersionFiltering("anioInicio.lessThan=" + UPDATED_ANIO_INICIO, "anioInicio.lessThan=" + DEFAULT_ANIO_INICIO);
    }

    @Test
    @Transactional
    void getAllVersionsByAnioInicioIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioInicio is greater than
        defaultVersionFiltering("anioInicio.greaterThan=" + SMALLER_ANIO_INICIO, "anioInicio.greaterThan=" + DEFAULT_ANIO_INICIO);
    }

    @Test
    @Transactional
    void getAllVersionsByAnioFinIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioFin equals to
        defaultVersionFiltering("anioFin.equals=" + DEFAULT_ANIO_FIN, "anioFin.equals=" + UPDATED_ANIO_FIN);
    }

    @Test
    @Transactional
    void getAllVersionsByAnioFinIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioFin in
        defaultVersionFiltering("anioFin.in=" + DEFAULT_ANIO_FIN + "," + UPDATED_ANIO_FIN, "anioFin.in=" + UPDATED_ANIO_FIN);
    }

    @Test
    @Transactional
    void getAllVersionsByAnioFinIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioFin is not null
        defaultVersionFiltering("anioFin.specified=true", "anioFin.specified=false");
    }

    @Test
    @Transactional
    void getAllVersionsByAnioFinIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioFin is greater than or equal to
        defaultVersionFiltering("anioFin.greaterThanOrEqual=" + DEFAULT_ANIO_FIN, "anioFin.greaterThanOrEqual=" + UPDATED_ANIO_FIN);
    }

    @Test
    @Transactional
    void getAllVersionsByAnioFinIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioFin is less than or equal to
        defaultVersionFiltering("anioFin.lessThanOrEqual=" + DEFAULT_ANIO_FIN, "anioFin.lessThanOrEqual=" + SMALLER_ANIO_FIN);
    }

    @Test
    @Transactional
    void getAllVersionsByAnioFinIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioFin is less than
        defaultVersionFiltering("anioFin.lessThan=" + UPDATED_ANIO_FIN, "anioFin.lessThan=" + DEFAULT_ANIO_FIN);
    }

    @Test
    @Transactional
    void getAllVersionsByAnioFinIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedVersion = versionRepository.saveAndFlush(version);

        // Get all the versionList where anioFin is greater than
        defaultVersionFiltering("anioFin.greaterThan=" + SMALLER_ANIO_FIN, "anioFin.greaterThan=" + DEFAULT_ANIO_FIN);
    }

    @Test
    @Transactional
    void getAllVersionsByMotoresIsEqualToSomething() throws Exception {
        Motor motores;
        if (TestUtil.findAll(em, Motor.class).isEmpty()) {
            versionRepository.saveAndFlush(version);
            motores = MotorResourceIT.createEntity();
        } else {
            motores = TestUtil.findAll(em, Motor.class).get(0);
        }
        em.persist(motores);
        em.flush();
        version.addMotores(motores);
        versionRepository.saveAndFlush(version);
        Long motoresId = motores.getId();
        // Get all the versionList where motores equals to motoresId
        defaultVersionShouldBeFound("motoresId.equals=" + motoresId);

        // Get all the versionList where motores equals to (motoresId + 1)
        defaultVersionShouldNotBeFound("motoresId.equals=" + (motoresId + 1));
    }

    @Test
    @Transactional
    void getAllVersionsByModelosIsEqualToSomething() throws Exception {
        Modelo modelos;
        if (TestUtil.findAll(em, Modelo.class).isEmpty()) {
            versionRepository.saveAndFlush(version);
            modelos = ModeloResourceIT.createEntity();
        } else {
            modelos = TestUtil.findAll(em, Modelo.class).get(0);
        }
        em.persist(modelos);
        em.flush();
        version.addModelos(modelos);
        versionRepository.saveAndFlush(version);
        Long modelosId = modelos.getId();
        // Get all the versionList where modelos equals to modelosId
        defaultVersionShouldBeFound("modelosId.equals=" + modelosId);

        // Get all the versionList where modelos equals to (modelosId + 1)
        defaultVersionShouldNotBeFound("modelosId.equals=" + (modelosId + 1));
    }

    private void defaultVersionFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultVersionShouldBeFound(shouldBeFound);
        defaultVersionShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultVersionShouldBeFound(String filter) throws Exception {
        restVersionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(version.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].anioInicio").value(hasItem(DEFAULT_ANIO_INICIO)))
            .andExpect(jsonPath("$.[*].anioFin").value(hasItem(DEFAULT_ANIO_FIN)));

        // Check, that the count call also returns 1
        restVersionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultVersionShouldNotBeFound(String filter) throws Exception {
        restVersionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restVersionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
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
