package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.ModeloAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.repository.ModeloRepository;
import com.concesionaria.app.service.ModeloService;
import com.concesionaria.app.service.dto.ModeloDTO;
import com.concesionaria.app.service.mapper.ModeloMapper;
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
 * Integration tests for the {@link ModeloResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ModeloResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final Integer DEFAULT_ANIO_LANZAMIENTO = 1;
    private static final Integer UPDATED_ANIO_LANZAMIENTO = 2;
    private static final Integer SMALLER_ANIO_LANZAMIENTO = 1 - 1;

    private static final String DEFAULT_CARROCERIA = "AAAAAAAAAA";
    private static final String UPDATED_CARROCERIA = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/modelos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModeloRepository modeloRepository;

    @Mock
    private ModeloRepository modeloRepositoryMock;

    @Autowired
    private ModeloMapper modeloMapper;

    @Mock
    private ModeloService modeloServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restModeloMockMvc;

    private Modelo modelo;

    private Modelo insertedModelo;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Modelo createEntity() {
        return new Modelo().nombre(DEFAULT_NOMBRE).anioLanzamiento(DEFAULT_ANIO_LANZAMIENTO).carroceria(DEFAULT_CARROCERIA);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Modelo createUpdatedEntity() {
        return new Modelo().nombre(UPDATED_NOMBRE).anioLanzamiento(UPDATED_ANIO_LANZAMIENTO).carroceria(UPDATED_CARROCERIA);
    }

    @BeforeEach
    void initTest() {
        modelo = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedModelo != null) {
            modeloRepository.delete(insertedModelo);
            insertedModelo = null;
        }
    }

    @Test
    @Transactional
    void createModelo() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Modelo
        ModeloDTO modeloDTO = modeloMapper.toDto(modelo);
        var returnedModeloDTO = om.readValue(
            restModeloMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(modeloDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ModeloDTO.class
        );

        // Validate the Modelo in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedModelo = modeloMapper.toEntity(returnedModeloDTO);
        assertModeloUpdatableFieldsEquals(returnedModelo, getPersistedModelo(returnedModelo));

        insertedModelo = returnedModelo;
    }

    @Test
    @Transactional
    void createModeloWithExistingId() throws Exception {
        // Create the Modelo with an existing ID
        modelo.setId(1L);
        ModeloDTO modeloDTO = modeloMapper.toDto(modelo);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restModeloMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(modeloDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Modelo in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        modelo.setNombre(null);

        // Create the Modelo, which fails.
        ModeloDTO modeloDTO = modeloMapper.toDto(modelo);

        restModeloMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(modeloDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllModelos() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList
        restModeloMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(modelo.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].anioLanzamiento").value(hasItem(DEFAULT_ANIO_LANZAMIENTO)))
            .andExpect(jsonPath("$.[*].carroceria").value(hasItem(DEFAULT_CARROCERIA)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllModelosWithEagerRelationshipsIsEnabled() throws Exception {
        when(modeloServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restModeloMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(modeloServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllModelosWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(modeloServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restModeloMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(modeloRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getModelo() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get the modelo
        restModeloMockMvc
            .perform(get(ENTITY_API_URL_ID, modelo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(modelo.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.anioLanzamiento").value(DEFAULT_ANIO_LANZAMIENTO))
            .andExpect(jsonPath("$.carroceria").value(DEFAULT_CARROCERIA));
    }

    @Test
    @Transactional
    void getModelosByIdFiltering() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        Long id = modelo.getId();

        defaultModeloFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultModeloFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultModeloFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllModelosByNombreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where nombre equals to
        defaultModeloFiltering("nombre.equals=" + DEFAULT_NOMBRE, "nombre.equals=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllModelosByNombreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where nombre in
        defaultModeloFiltering("nombre.in=" + DEFAULT_NOMBRE + "," + UPDATED_NOMBRE, "nombre.in=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllModelosByNombreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where nombre is not null
        defaultModeloFiltering("nombre.specified=true", "nombre.specified=false");
    }

    @Test
    @Transactional
    void getAllModelosByNombreContainsSomething() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where nombre contains
        defaultModeloFiltering("nombre.contains=" + DEFAULT_NOMBRE, "nombre.contains=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllModelosByNombreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where nombre does not contain
        defaultModeloFiltering("nombre.doesNotContain=" + UPDATED_NOMBRE, "nombre.doesNotContain=" + DEFAULT_NOMBRE);
    }

    @Test
    @Transactional
    void getAllModelosByAnioLanzamientoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where anioLanzamiento equals to
        defaultModeloFiltering("anioLanzamiento.equals=" + DEFAULT_ANIO_LANZAMIENTO, "anioLanzamiento.equals=" + UPDATED_ANIO_LANZAMIENTO);
    }

    @Test
    @Transactional
    void getAllModelosByAnioLanzamientoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where anioLanzamiento in
        defaultModeloFiltering(
            "anioLanzamiento.in=" + DEFAULT_ANIO_LANZAMIENTO + "," + UPDATED_ANIO_LANZAMIENTO,
            "anioLanzamiento.in=" + UPDATED_ANIO_LANZAMIENTO
        );
    }

    @Test
    @Transactional
    void getAllModelosByAnioLanzamientoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where anioLanzamiento is not null
        defaultModeloFiltering("anioLanzamiento.specified=true", "anioLanzamiento.specified=false");
    }

    @Test
    @Transactional
    void getAllModelosByAnioLanzamientoIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where anioLanzamiento is greater than or equal to
        defaultModeloFiltering(
            "anioLanzamiento.greaterThanOrEqual=" + DEFAULT_ANIO_LANZAMIENTO,
            "anioLanzamiento.greaterThanOrEqual=" + UPDATED_ANIO_LANZAMIENTO
        );
    }

    @Test
    @Transactional
    void getAllModelosByAnioLanzamientoIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where anioLanzamiento is less than or equal to
        defaultModeloFiltering(
            "anioLanzamiento.lessThanOrEqual=" + DEFAULT_ANIO_LANZAMIENTO,
            "anioLanzamiento.lessThanOrEqual=" + SMALLER_ANIO_LANZAMIENTO
        );
    }

    @Test
    @Transactional
    void getAllModelosByAnioLanzamientoIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where anioLanzamiento is less than
        defaultModeloFiltering(
            "anioLanzamiento.lessThan=" + UPDATED_ANIO_LANZAMIENTO,
            "anioLanzamiento.lessThan=" + DEFAULT_ANIO_LANZAMIENTO
        );
    }

    @Test
    @Transactional
    void getAllModelosByAnioLanzamientoIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where anioLanzamiento is greater than
        defaultModeloFiltering(
            "anioLanzamiento.greaterThan=" + SMALLER_ANIO_LANZAMIENTO,
            "anioLanzamiento.greaterThan=" + DEFAULT_ANIO_LANZAMIENTO
        );
    }

    @Test
    @Transactional
    void getAllModelosByCarroceriaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where carroceria equals to
        defaultModeloFiltering("carroceria.equals=" + DEFAULT_CARROCERIA, "carroceria.equals=" + UPDATED_CARROCERIA);
    }

    @Test
    @Transactional
    void getAllModelosByCarroceriaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where carroceria in
        defaultModeloFiltering("carroceria.in=" + DEFAULT_CARROCERIA + "," + UPDATED_CARROCERIA, "carroceria.in=" + UPDATED_CARROCERIA);
    }

    @Test
    @Transactional
    void getAllModelosByCarroceriaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where carroceria is not null
        defaultModeloFiltering("carroceria.specified=true", "carroceria.specified=false");
    }

    @Test
    @Transactional
    void getAllModelosByCarroceriaContainsSomething() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where carroceria contains
        defaultModeloFiltering("carroceria.contains=" + DEFAULT_CARROCERIA, "carroceria.contains=" + UPDATED_CARROCERIA);
    }

    @Test
    @Transactional
    void getAllModelosByCarroceriaNotContainsSomething() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        // Get all the modeloList where carroceria does not contain
        defaultModeloFiltering("carroceria.doesNotContain=" + UPDATED_CARROCERIA, "carroceria.doesNotContain=" + DEFAULT_CARROCERIA);
    }

    @Test
    @Transactional
    void getAllModelosByMarcaIsEqualToSomething() throws Exception {
        Marca marca;
        if (TestUtil.findAll(em, Marca.class).isEmpty()) {
            modeloRepository.saveAndFlush(modelo);
            marca = MarcaResourceIT.createEntity();
        } else {
            marca = TestUtil.findAll(em, Marca.class).get(0);
        }
        em.persist(marca);
        em.flush();
        modelo.setMarca(marca);
        modeloRepository.saveAndFlush(modelo);
        Long marcaId = marca.getId();
        // Get all the modeloList where marca equals to marcaId
        defaultModeloShouldBeFound("marcaId.equals=" + marcaId);

        // Get all the modeloList where marca equals to (marcaId + 1)
        defaultModeloShouldNotBeFound("marcaId.equals=" + (marcaId + 1));
    }

    @Test
    @Transactional
    void getAllModelosByVersionesIsEqualToSomething() throws Exception {
        Version versiones;
        if (TestUtil.findAll(em, Version.class).isEmpty()) {
            modeloRepository.saveAndFlush(modelo);
            versiones = VersionResourceIT.createEntity();
        } else {
            versiones = TestUtil.findAll(em, Version.class).get(0);
        }
        em.persist(versiones);
        em.flush();
        modelo.addVersiones(versiones);
        modeloRepository.saveAndFlush(modelo);
        Long versionesId = versiones.getId();
        // Get all the modeloList where versiones equals to versionesId
        defaultModeloShouldBeFound("versionesId.equals=" + versionesId);

        // Get all the modeloList where versiones equals to (versionesId + 1)
        defaultModeloShouldNotBeFound("versionesId.equals=" + (versionesId + 1));
    }

    private void defaultModeloFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultModeloShouldBeFound(shouldBeFound);
        defaultModeloShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultModeloShouldBeFound(String filter) throws Exception {
        restModeloMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(modelo.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].anioLanzamiento").value(hasItem(DEFAULT_ANIO_LANZAMIENTO)))
            .andExpect(jsonPath("$.[*].carroceria").value(hasItem(DEFAULT_CARROCERIA)));

        // Check, that the count call also returns 1
        restModeloMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultModeloShouldNotBeFound(String filter) throws Exception {
        restModeloMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restModeloMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingModelo() throws Exception {
        // Get the modelo
        restModeloMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingModelo() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the modelo
        Modelo updatedModelo = modeloRepository.findById(modelo.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedModelo are not directly saved in db
        em.detach(updatedModelo);
        updatedModelo.nombre(UPDATED_NOMBRE).anioLanzamiento(UPDATED_ANIO_LANZAMIENTO).carroceria(UPDATED_CARROCERIA);
        ModeloDTO modeloDTO = modeloMapper.toDto(updatedModelo);

        restModeloMockMvc
            .perform(
                put(ENTITY_API_URL_ID, modeloDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(modeloDTO))
            )
            .andExpect(status().isOk());

        // Validate the Modelo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedModeloToMatchAllProperties(updatedModelo);
    }

    @Test
    @Transactional
    void putNonExistingModelo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modelo.setId(longCount.incrementAndGet());

        // Create the Modelo
        ModeloDTO modeloDTO = modeloMapper.toDto(modelo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restModeloMockMvc
            .perform(
                put(ENTITY_API_URL_ID, modeloDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(modeloDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Modelo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchModelo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modelo.setId(longCount.incrementAndGet());

        // Create the Modelo
        ModeloDTO modeloDTO = modeloMapper.toDto(modelo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModeloMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(modeloDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Modelo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamModelo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modelo.setId(longCount.incrementAndGet());

        // Create the Modelo
        ModeloDTO modeloDTO = modeloMapper.toDto(modelo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModeloMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(modeloDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Modelo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateModeloWithPatch() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the modelo using partial update
        Modelo partialUpdatedModelo = new Modelo();
        partialUpdatedModelo.setId(modelo.getId());

        partialUpdatedModelo.nombre(UPDATED_NOMBRE).anioLanzamiento(UPDATED_ANIO_LANZAMIENTO);

        restModeloMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedModelo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedModelo))
            )
            .andExpect(status().isOk());

        // Validate the Modelo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertModeloUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedModelo, modelo), getPersistedModelo(modelo));
    }

    @Test
    @Transactional
    void fullUpdateModeloWithPatch() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the modelo using partial update
        Modelo partialUpdatedModelo = new Modelo();
        partialUpdatedModelo.setId(modelo.getId());

        partialUpdatedModelo.nombre(UPDATED_NOMBRE).anioLanzamiento(UPDATED_ANIO_LANZAMIENTO).carroceria(UPDATED_CARROCERIA);

        restModeloMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedModelo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedModelo))
            )
            .andExpect(status().isOk());

        // Validate the Modelo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertModeloUpdatableFieldsEquals(partialUpdatedModelo, getPersistedModelo(partialUpdatedModelo));
    }

    @Test
    @Transactional
    void patchNonExistingModelo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modelo.setId(longCount.incrementAndGet());

        // Create the Modelo
        ModeloDTO modeloDTO = modeloMapper.toDto(modelo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restModeloMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, modeloDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(modeloDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Modelo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchModelo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modelo.setId(longCount.incrementAndGet());

        // Create the Modelo
        ModeloDTO modeloDTO = modeloMapper.toDto(modelo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModeloMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(modeloDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Modelo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamModelo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modelo.setId(longCount.incrementAndGet());

        // Create the Modelo
        ModeloDTO modeloDTO = modeloMapper.toDto(modelo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModeloMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(modeloDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Modelo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteModelo() throws Exception {
        // Initialize the database
        insertedModelo = modeloRepository.saveAndFlush(modelo);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the modelo
        restModeloMockMvc
            .perform(delete(ENTITY_API_URL_ID, modelo.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return modeloRepository.count();
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

    protected Modelo getPersistedModelo(Modelo modelo) {
        return modeloRepository.findById(modelo.getId()).orElseThrow();
    }

    protected void assertPersistedModeloToMatchAllProperties(Modelo expectedModelo) {
        assertModeloAllPropertiesEquals(expectedModelo, getPersistedModelo(expectedModelo));
    }

    protected void assertPersistedModeloToMatchUpdatableProperties(Modelo expectedModelo) {
        assertModeloAllUpdatablePropertiesEquals(expectedModelo, getPersistedModelo(expectedModelo));
    }
}
