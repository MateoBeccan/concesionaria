package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.MotorAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.repository.MotorRepository;
import com.concesionaria.app.service.dto.MotorDTO;
import com.concesionaria.app.service.mapper.MotorMapper;
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
 * Integration tests for the {@link MotorResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MotorResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final Integer DEFAULT_CILINDRADA_CC = 1;
    private static final Integer UPDATED_CILINDRADA_CC = 2;
    private static final Integer SMALLER_CILINDRADA_CC = 1 - 1;

    private static final Integer DEFAULT_CILINDRO_CANT = 1;
    private static final Integer UPDATED_CILINDRO_CANT = 2;
    private static final Integer SMALLER_CILINDRO_CANT = 1 - 1;

    private static final Integer DEFAULT_POTENCIA_HP = 1;
    private static final Integer UPDATED_POTENCIA_HP = 2;
    private static final Integer SMALLER_POTENCIA_HP = 1 - 1;

    private static final Boolean DEFAULT_TURBO = false;
    private static final Boolean UPDATED_TURBO = true;

    private static final String ENTITY_API_URL = "/api/motors";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MotorRepository motorRepository;

    @Autowired
    private MotorMapper motorMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMotorMockMvc;

    private Motor motor;

    private Motor insertedMotor;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Motor createEntity() {
        return new Motor()
            .nombre(DEFAULT_NOMBRE)
            .cilindradaCc(DEFAULT_CILINDRADA_CC)
            .cilindroCant(DEFAULT_CILINDRO_CANT)
            .potenciaHp(DEFAULT_POTENCIA_HP)
            .turbo(DEFAULT_TURBO);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Motor createUpdatedEntity() {
        return new Motor()
            .nombre(UPDATED_NOMBRE)
            .cilindradaCc(UPDATED_CILINDRADA_CC)
            .cilindroCant(UPDATED_CILINDRO_CANT)
            .potenciaHp(UPDATED_POTENCIA_HP)
            .turbo(UPDATED_TURBO);
    }

    @BeforeEach
    void initTest() {
        motor = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMotor != null) {
            motorRepository.delete(insertedMotor);
            insertedMotor = null;
        }
    }

    @Test
    @Transactional
    void createMotor() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Motor
        MotorDTO motorDTO = motorMapper.toDto(motor);
        var returnedMotorDTO = om.readValue(
            restMotorMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(motorDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MotorDTO.class
        );

        // Validate the Motor in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMotor = motorMapper.toEntity(returnedMotorDTO);
        assertMotorUpdatableFieldsEquals(returnedMotor, getPersistedMotor(returnedMotor));

        insertedMotor = returnedMotor;
    }

    @Test
    @Transactional
    void createMotorWithExistingId() throws Exception {
        // Create the Motor with an existing ID
        motor.setId(1L);
        MotorDTO motorDTO = motorMapper.toDto(motor);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMotorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(motorDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Motor in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        motor.setNombre(null);

        // Create the Motor, which fails.
        MotorDTO motorDTO = motorMapper.toDto(motor);

        restMotorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(motorDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMotors() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList
        restMotorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(motor.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].cilindradaCc").value(hasItem(DEFAULT_CILINDRADA_CC)))
            .andExpect(jsonPath("$.[*].cilindroCant").value(hasItem(DEFAULT_CILINDRO_CANT)))
            .andExpect(jsonPath("$.[*].potenciaHp").value(hasItem(DEFAULT_POTENCIA_HP)))
            .andExpect(jsonPath("$.[*].turbo").value(hasItem(DEFAULT_TURBO)));
    }

    @Test
    @Transactional
    void getMotor() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get the motor
        restMotorMockMvc
            .perform(get(ENTITY_API_URL_ID, motor.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(motor.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.cilindradaCc").value(DEFAULT_CILINDRADA_CC))
            .andExpect(jsonPath("$.cilindroCant").value(DEFAULT_CILINDRO_CANT))
            .andExpect(jsonPath("$.potenciaHp").value(DEFAULT_POTENCIA_HP))
            .andExpect(jsonPath("$.turbo").value(DEFAULT_TURBO));
    }

    @Test
    @Transactional
    void getMotorsByIdFiltering() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        Long id = motor.getId();

        defaultMotorFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultMotorFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultMotorFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllMotorsByNombreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where nombre equals to
        defaultMotorFiltering("nombre.equals=" + DEFAULT_NOMBRE, "nombre.equals=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllMotorsByNombreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where nombre in
        defaultMotorFiltering("nombre.in=" + DEFAULT_NOMBRE + "," + UPDATED_NOMBRE, "nombre.in=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllMotorsByNombreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where nombre is not null
        defaultMotorFiltering("nombre.specified=true", "nombre.specified=false");
    }

    @Test
    @Transactional
    void getAllMotorsByNombreContainsSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where nombre contains
        defaultMotorFiltering("nombre.contains=" + DEFAULT_NOMBRE, "nombre.contains=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllMotorsByNombreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where nombre does not contain
        defaultMotorFiltering("nombre.doesNotContain=" + UPDATED_NOMBRE, "nombre.doesNotContain=" + DEFAULT_NOMBRE);
    }

    @Test
    @Transactional
    void getAllMotorsByCilindradaCcIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindradaCc equals to
        defaultMotorFiltering("cilindradaCc.equals=" + DEFAULT_CILINDRADA_CC, "cilindradaCc.equals=" + UPDATED_CILINDRADA_CC);
    }

    @Test
    @Transactional
    void getAllMotorsByCilindradaCcIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindradaCc in
        defaultMotorFiltering(
            "cilindradaCc.in=" + DEFAULT_CILINDRADA_CC + "," + UPDATED_CILINDRADA_CC,
            "cilindradaCc.in=" + UPDATED_CILINDRADA_CC
        );
    }

    @Test
    @Transactional
    void getAllMotorsByCilindradaCcIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindradaCc is not null
        defaultMotorFiltering("cilindradaCc.specified=true", "cilindradaCc.specified=false");
    }

    @Test
    @Transactional
    void getAllMotorsByCilindradaCcIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindradaCc is greater than or equal to
        defaultMotorFiltering(
            "cilindradaCc.greaterThanOrEqual=" + DEFAULT_CILINDRADA_CC,
            "cilindradaCc.greaterThanOrEqual=" + UPDATED_CILINDRADA_CC
        );
    }

    @Test
    @Transactional
    void getAllMotorsByCilindradaCcIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindradaCc is less than or equal to
        defaultMotorFiltering(
            "cilindradaCc.lessThanOrEqual=" + DEFAULT_CILINDRADA_CC,
            "cilindradaCc.lessThanOrEqual=" + SMALLER_CILINDRADA_CC
        );
    }

    @Test
    @Transactional
    void getAllMotorsByCilindradaCcIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindradaCc is less than
        defaultMotorFiltering("cilindradaCc.lessThan=" + UPDATED_CILINDRADA_CC, "cilindradaCc.lessThan=" + DEFAULT_CILINDRADA_CC);
    }

    @Test
    @Transactional
    void getAllMotorsByCilindradaCcIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindradaCc is greater than
        defaultMotorFiltering("cilindradaCc.greaterThan=" + SMALLER_CILINDRADA_CC, "cilindradaCc.greaterThan=" + DEFAULT_CILINDRADA_CC);
    }

    @Test
    @Transactional
    void getAllMotorsByCilindroCantIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindroCant equals to
        defaultMotorFiltering("cilindroCant.equals=" + DEFAULT_CILINDRO_CANT, "cilindroCant.equals=" + UPDATED_CILINDRO_CANT);
    }

    @Test
    @Transactional
    void getAllMotorsByCilindroCantIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindroCant in
        defaultMotorFiltering(
            "cilindroCant.in=" + DEFAULT_CILINDRO_CANT + "," + UPDATED_CILINDRO_CANT,
            "cilindroCant.in=" + UPDATED_CILINDRO_CANT
        );
    }

    @Test
    @Transactional
    void getAllMotorsByCilindroCantIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindroCant is not null
        defaultMotorFiltering("cilindroCant.specified=true", "cilindroCant.specified=false");
    }

    @Test
    @Transactional
    void getAllMotorsByCilindroCantIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindroCant is greater than or equal to
        defaultMotorFiltering(
            "cilindroCant.greaterThanOrEqual=" + DEFAULT_CILINDRO_CANT,
            "cilindroCant.greaterThanOrEqual=" + UPDATED_CILINDRO_CANT
        );
    }

    @Test
    @Transactional
    void getAllMotorsByCilindroCantIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindroCant is less than or equal to
        defaultMotorFiltering(
            "cilindroCant.lessThanOrEqual=" + DEFAULT_CILINDRO_CANT,
            "cilindroCant.lessThanOrEqual=" + SMALLER_CILINDRO_CANT
        );
    }

    @Test
    @Transactional
    void getAllMotorsByCilindroCantIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindroCant is less than
        defaultMotorFiltering("cilindroCant.lessThan=" + UPDATED_CILINDRO_CANT, "cilindroCant.lessThan=" + DEFAULT_CILINDRO_CANT);
    }

    @Test
    @Transactional
    void getAllMotorsByCilindroCantIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where cilindroCant is greater than
        defaultMotorFiltering("cilindroCant.greaterThan=" + SMALLER_CILINDRO_CANT, "cilindroCant.greaterThan=" + DEFAULT_CILINDRO_CANT);
    }

    @Test
    @Transactional
    void getAllMotorsByPotenciaHpIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where potenciaHp equals to
        defaultMotorFiltering("potenciaHp.equals=" + DEFAULT_POTENCIA_HP, "potenciaHp.equals=" + UPDATED_POTENCIA_HP);
    }

    @Test
    @Transactional
    void getAllMotorsByPotenciaHpIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where potenciaHp in
        defaultMotorFiltering("potenciaHp.in=" + DEFAULT_POTENCIA_HP + "," + UPDATED_POTENCIA_HP, "potenciaHp.in=" + UPDATED_POTENCIA_HP);
    }

    @Test
    @Transactional
    void getAllMotorsByPotenciaHpIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where potenciaHp is not null
        defaultMotorFiltering("potenciaHp.specified=true", "potenciaHp.specified=false");
    }

    @Test
    @Transactional
    void getAllMotorsByPotenciaHpIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where potenciaHp is greater than or equal to
        defaultMotorFiltering(
            "potenciaHp.greaterThanOrEqual=" + DEFAULT_POTENCIA_HP,
            "potenciaHp.greaterThanOrEqual=" + UPDATED_POTENCIA_HP
        );
    }

    @Test
    @Transactional
    void getAllMotorsByPotenciaHpIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where potenciaHp is less than or equal to
        defaultMotorFiltering("potenciaHp.lessThanOrEqual=" + DEFAULT_POTENCIA_HP, "potenciaHp.lessThanOrEqual=" + SMALLER_POTENCIA_HP);
    }

    @Test
    @Transactional
    void getAllMotorsByPotenciaHpIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where potenciaHp is less than
        defaultMotorFiltering("potenciaHp.lessThan=" + UPDATED_POTENCIA_HP, "potenciaHp.lessThan=" + DEFAULT_POTENCIA_HP);
    }

    @Test
    @Transactional
    void getAllMotorsByPotenciaHpIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where potenciaHp is greater than
        defaultMotorFiltering("potenciaHp.greaterThan=" + SMALLER_POTENCIA_HP, "potenciaHp.greaterThan=" + DEFAULT_POTENCIA_HP);
    }

    @Test
    @Transactional
    void getAllMotorsByTurboIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where turbo equals to
        defaultMotorFiltering("turbo.equals=" + DEFAULT_TURBO, "turbo.equals=" + UPDATED_TURBO);
    }

    @Test
    @Transactional
    void getAllMotorsByTurboIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where turbo in
        defaultMotorFiltering("turbo.in=" + DEFAULT_TURBO + "," + UPDATED_TURBO, "turbo.in=" + UPDATED_TURBO);
    }

    @Test
    @Transactional
    void getAllMotorsByTurboIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        // Get all the motorList where turbo is not null
        defaultMotorFiltering("turbo.specified=true", "turbo.specified=false");
    }

    @Test
    @Transactional
    void getAllMotorsByVersionesIsEqualToSomething() throws Exception {
        Version versiones;
        if (TestUtil.findAll(em, Version.class).isEmpty()) {
            motorRepository.saveAndFlush(motor);
            versiones = VersionResourceIT.createEntity();
        } else {
            versiones = TestUtil.findAll(em, Version.class).get(0);
        }
        em.persist(versiones);
        em.flush();
        motor.addVersiones(versiones);
        motorRepository.saveAndFlush(motor);
        Long versionesId = versiones.getId();
        // Get all the motorList where versiones equals to versionesId
        defaultMotorShouldBeFound("versionesId.equals=" + versionesId);

        // Get all the motorList where versiones equals to (versionesId + 1)
        defaultMotorShouldNotBeFound("versionesId.equals=" + (versionesId + 1));
    }

    private void defaultMotorFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultMotorShouldBeFound(shouldBeFound);
        defaultMotorShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultMotorShouldBeFound(String filter) throws Exception {
        restMotorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(motor.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].cilindradaCc").value(hasItem(DEFAULT_CILINDRADA_CC)))
            .andExpect(jsonPath("$.[*].cilindroCant").value(hasItem(DEFAULT_CILINDRO_CANT)))
            .andExpect(jsonPath("$.[*].potenciaHp").value(hasItem(DEFAULT_POTENCIA_HP)))
            .andExpect(jsonPath("$.[*].turbo").value(hasItem(DEFAULT_TURBO)));

        // Check, that the count call also returns 1
        restMotorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultMotorShouldNotBeFound(String filter) throws Exception {
        restMotorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMotorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMotor() throws Exception {
        // Get the motor
        restMotorMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMotor() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the motor
        Motor updatedMotor = motorRepository.findById(motor.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMotor are not directly saved in db
        em.detach(updatedMotor);
        updatedMotor
            .nombre(UPDATED_NOMBRE)
            .cilindradaCc(UPDATED_CILINDRADA_CC)
            .cilindroCant(UPDATED_CILINDRO_CANT)
            .potenciaHp(UPDATED_POTENCIA_HP)
            .turbo(UPDATED_TURBO);
        MotorDTO motorDTO = motorMapper.toDto(updatedMotor);

        restMotorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, motorDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(motorDTO))
            )
            .andExpect(status().isOk());

        // Validate the Motor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMotorToMatchAllProperties(updatedMotor);
    }

    @Test
    @Transactional
    void putNonExistingMotor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        motor.setId(longCount.incrementAndGet());

        // Create the Motor
        MotorDTO motorDTO = motorMapper.toDto(motor);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMotorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, motorDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(motorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Motor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMotor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        motor.setId(longCount.incrementAndGet());

        // Create the Motor
        MotorDTO motorDTO = motorMapper.toDto(motor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMotorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(motorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Motor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMotor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        motor.setId(longCount.incrementAndGet());

        // Create the Motor
        MotorDTO motorDTO = motorMapper.toDto(motor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMotorMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(motorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Motor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMotorWithPatch() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the motor using partial update
        Motor partialUpdatedMotor = new Motor();
        partialUpdatedMotor.setId(motor.getId());

        partialUpdatedMotor
            .nombre(UPDATED_NOMBRE)
            .cilindradaCc(UPDATED_CILINDRADA_CC)
            .cilindroCant(UPDATED_CILINDRO_CANT)
            .turbo(UPDATED_TURBO);

        restMotorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMotor.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMotor))
            )
            .andExpect(status().isOk());

        // Validate the Motor in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMotorUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedMotor, motor), getPersistedMotor(motor));
    }

    @Test
    @Transactional
    void fullUpdateMotorWithPatch() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the motor using partial update
        Motor partialUpdatedMotor = new Motor();
        partialUpdatedMotor.setId(motor.getId());

        partialUpdatedMotor
            .nombre(UPDATED_NOMBRE)
            .cilindradaCc(UPDATED_CILINDRADA_CC)
            .cilindroCant(UPDATED_CILINDRO_CANT)
            .potenciaHp(UPDATED_POTENCIA_HP)
            .turbo(UPDATED_TURBO);

        restMotorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMotor.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMotor))
            )
            .andExpect(status().isOk());

        // Validate the Motor in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMotorUpdatableFieldsEquals(partialUpdatedMotor, getPersistedMotor(partialUpdatedMotor));
    }

    @Test
    @Transactional
    void patchNonExistingMotor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        motor.setId(longCount.incrementAndGet());

        // Create the Motor
        MotorDTO motorDTO = motorMapper.toDto(motor);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMotorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, motorDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(motorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Motor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMotor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        motor.setId(longCount.incrementAndGet());

        // Create the Motor
        MotorDTO motorDTO = motorMapper.toDto(motor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMotorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(motorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Motor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMotor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        motor.setId(longCount.incrementAndGet());

        // Create the Motor
        MotorDTO motorDTO = motorMapper.toDto(motor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMotorMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(motorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Motor in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMotor() throws Exception {
        // Initialize the database
        insertedMotor = motorRepository.saveAndFlush(motor);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the motor
        restMotorMockMvc
            .perform(delete(ENTITY_API_URL_ID, motor.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return motorRepository.count();
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

    protected Motor getPersistedMotor(Motor motor) {
        return motorRepository.findById(motor.getId()).orElseThrow();
    }

    protected void assertPersistedMotorToMatchAllProperties(Motor expectedMotor) {
        assertMotorAllPropertiesEquals(expectedMotor, getPersistedMotor(expectedMotor));
    }

    protected void assertPersistedMotorToMatchUpdatableProperties(Motor expectedMotor) {
        assertMotorAllUpdatablePropertiesEquals(expectedMotor, getPersistedMotor(expectedMotor));
    }
}
