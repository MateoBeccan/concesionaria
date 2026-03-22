package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.AutoAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static com.concesionaria.app.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Auto;
import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.domain.Motor;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.domain.enumeration.CondicionAuto;
import com.concesionaria.app.domain.enumeration.EstadoAuto;
import com.concesionaria.app.repository.AutoRepository;
import com.concesionaria.app.service.dto.AutoDTO;
import com.concesionaria.app.service.mapper.AutoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link AutoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AutoResourceIT {

    private static final EstadoAuto DEFAULT_ESTADO = EstadoAuto.NUEVO;
    private static final EstadoAuto UPDATED_ESTADO = EstadoAuto.USADO;

    private static final CondicionAuto DEFAULT_CONDICION = CondicionAuto.EN_VENTA;
    private static final CondicionAuto UPDATED_CONDICION = CondicionAuto.RESERVADO;

    private static final LocalDate DEFAULT_FECHA_FABRICACION = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_FECHA_FABRICACION = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_FECHA_FABRICACION = LocalDate.ofEpochDay(-1L);

    private static final Integer DEFAULT_KM = 1;
    private static final Integer UPDATED_KM = 2;
    private static final Integer SMALLER_KM = 1 - 1;

    private static final String DEFAULT_PATENTE = "AAAAAAAAAA";
    private static final String UPDATED_PATENTE = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRECIO = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRECIO = new BigDecimal(2);
    private static final BigDecimal SMALLER_PRECIO = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/autos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AutoRepository autoRepository;

    @Autowired
    private AutoMapper autoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAutoMockMvc;

    private Auto auto;

    private Auto insertedAuto;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Auto createEntity() {
        return new Auto()
            .estado(DEFAULT_ESTADO)
            .condicion(DEFAULT_CONDICION)
            .fechaFabricacion(DEFAULT_FECHA_FABRICACION)
            .km(DEFAULT_KM)
            .patente(DEFAULT_PATENTE)
            .precio(DEFAULT_PRECIO);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Auto createUpdatedEntity() {
        return new Auto()
            .estado(UPDATED_ESTADO)
            .condicion(UPDATED_CONDICION)
            .fechaFabricacion(UPDATED_FECHA_FABRICACION)
            .km(UPDATED_KM)
            .patente(UPDATED_PATENTE)
            .precio(UPDATED_PRECIO);
    }

    @BeforeEach
    void initTest() {
        auto = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAuto != null) {
            autoRepository.delete(insertedAuto);
            insertedAuto = null;
        }
    }

    @Test
    @Transactional
    void createAuto() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Auto
        AutoDTO autoDTO = autoMapper.toDto(auto);
        var returnedAutoDTO = om.readValue(
            restAutoMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(autoDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AutoDTO.class
        );

        // Validate the Auto in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAuto = autoMapper.toEntity(returnedAutoDTO);
        assertAutoUpdatableFieldsEquals(returnedAuto, getPersistedAuto(returnedAuto));

        insertedAuto = returnedAuto;
    }

    @Test
    @Transactional
    void createAutoWithExistingId() throws Exception {
        // Create the Auto with an existing ID
        auto.setId(1L);
        AutoDTO autoDTO = autoMapper.toDto(auto);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAutoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(autoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Auto in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkEstadoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        auto.setEstado(null);

        // Create the Auto, which fails.
        AutoDTO autoDTO = autoMapper.toDto(auto);

        restAutoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(autoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCondicionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        auto.setCondicion(null);

        // Create the Auto, which fails.
        AutoDTO autoDTO = autoMapper.toDto(auto);

        restAutoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(autoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAutos() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList
        restAutoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(auto.getId().intValue())))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].condicion").value(hasItem(DEFAULT_CONDICION.toString())))
            .andExpect(jsonPath("$.[*].fechaFabricacion").value(hasItem(DEFAULT_FECHA_FABRICACION.toString())))
            .andExpect(jsonPath("$.[*].km").value(hasItem(DEFAULT_KM)))
            .andExpect(jsonPath("$.[*].patente").value(hasItem(DEFAULT_PATENTE)))
            .andExpect(jsonPath("$.[*].precio").value(hasItem(sameNumber(DEFAULT_PRECIO))));
    }

    @Test
    @Transactional
    void getAuto() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get the auto
        restAutoMockMvc
            .perform(get(ENTITY_API_URL_ID, auto.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(auto.getId().intValue()))
            .andExpect(jsonPath("$.estado").value(DEFAULT_ESTADO.toString()))
            .andExpect(jsonPath("$.condicion").value(DEFAULT_CONDICION.toString()))
            .andExpect(jsonPath("$.fechaFabricacion").value(DEFAULT_FECHA_FABRICACION.toString()))
            .andExpect(jsonPath("$.km").value(DEFAULT_KM))
            .andExpect(jsonPath("$.patente").value(DEFAULT_PATENTE))
            .andExpect(jsonPath("$.precio").value(sameNumber(DEFAULT_PRECIO)));
    }

    @Test
    @Transactional
    void getAutosByIdFiltering() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        Long id = auto.getId();

        defaultAutoFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultAutoFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultAutoFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAutosByEstadoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where estado equals to
        defaultAutoFiltering("estado.equals=" + DEFAULT_ESTADO, "estado.equals=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllAutosByEstadoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where estado in
        defaultAutoFiltering("estado.in=" + DEFAULT_ESTADO + "," + UPDATED_ESTADO, "estado.in=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllAutosByEstadoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where estado is not null
        defaultAutoFiltering("estado.specified=true", "estado.specified=false");
    }

    @Test
    @Transactional
    void getAllAutosByCondicionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where condicion equals to
        defaultAutoFiltering("condicion.equals=" + DEFAULT_CONDICION, "condicion.equals=" + UPDATED_CONDICION);
    }

    @Test
    @Transactional
    void getAllAutosByCondicionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where condicion in
        defaultAutoFiltering("condicion.in=" + DEFAULT_CONDICION + "," + UPDATED_CONDICION, "condicion.in=" + UPDATED_CONDICION);
    }

    @Test
    @Transactional
    void getAllAutosByCondicionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where condicion is not null
        defaultAutoFiltering("condicion.specified=true", "condicion.specified=false");
    }

    @Test
    @Transactional
    void getAllAutosByFechaFabricacionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where fechaFabricacion equals to
        defaultAutoFiltering(
            "fechaFabricacion.equals=" + DEFAULT_FECHA_FABRICACION,
            "fechaFabricacion.equals=" + UPDATED_FECHA_FABRICACION
        );
    }

    @Test
    @Transactional
    void getAllAutosByFechaFabricacionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where fechaFabricacion in
        defaultAutoFiltering(
            "fechaFabricacion.in=" + DEFAULT_FECHA_FABRICACION + "," + UPDATED_FECHA_FABRICACION,
            "fechaFabricacion.in=" + UPDATED_FECHA_FABRICACION
        );
    }

    @Test
    @Transactional
    void getAllAutosByFechaFabricacionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where fechaFabricacion is not null
        defaultAutoFiltering("fechaFabricacion.specified=true", "fechaFabricacion.specified=false");
    }

    @Test
    @Transactional
    void getAllAutosByFechaFabricacionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where fechaFabricacion is greater than or equal to
        defaultAutoFiltering(
            "fechaFabricacion.greaterThanOrEqual=" + DEFAULT_FECHA_FABRICACION,
            "fechaFabricacion.greaterThanOrEqual=" + UPDATED_FECHA_FABRICACION
        );
    }

    @Test
    @Transactional
    void getAllAutosByFechaFabricacionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where fechaFabricacion is less than or equal to
        defaultAutoFiltering(
            "fechaFabricacion.lessThanOrEqual=" + DEFAULT_FECHA_FABRICACION,
            "fechaFabricacion.lessThanOrEqual=" + SMALLER_FECHA_FABRICACION
        );
    }

    @Test
    @Transactional
    void getAllAutosByFechaFabricacionIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where fechaFabricacion is less than
        defaultAutoFiltering(
            "fechaFabricacion.lessThan=" + UPDATED_FECHA_FABRICACION,
            "fechaFabricacion.lessThan=" + DEFAULT_FECHA_FABRICACION
        );
    }

    @Test
    @Transactional
    void getAllAutosByFechaFabricacionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where fechaFabricacion is greater than
        defaultAutoFiltering(
            "fechaFabricacion.greaterThan=" + SMALLER_FECHA_FABRICACION,
            "fechaFabricacion.greaterThan=" + DEFAULT_FECHA_FABRICACION
        );
    }

    @Test
    @Transactional
    void getAllAutosByKmIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where km equals to
        defaultAutoFiltering("km.equals=" + DEFAULT_KM, "km.equals=" + UPDATED_KM);
    }

    @Test
    @Transactional
    void getAllAutosByKmIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where km in
        defaultAutoFiltering("km.in=" + DEFAULT_KM + "," + UPDATED_KM, "km.in=" + UPDATED_KM);
    }

    @Test
    @Transactional
    void getAllAutosByKmIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where km is not null
        defaultAutoFiltering("km.specified=true", "km.specified=false");
    }

    @Test
    @Transactional
    void getAllAutosByKmIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where km is greater than or equal to
        defaultAutoFiltering("km.greaterThanOrEqual=" + DEFAULT_KM, "km.greaterThanOrEqual=" + UPDATED_KM);
    }

    @Test
    @Transactional
    void getAllAutosByKmIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where km is less than or equal to
        defaultAutoFiltering("km.lessThanOrEqual=" + DEFAULT_KM, "km.lessThanOrEqual=" + SMALLER_KM);
    }

    @Test
    @Transactional
    void getAllAutosByKmIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where km is less than
        defaultAutoFiltering("km.lessThan=" + UPDATED_KM, "km.lessThan=" + DEFAULT_KM);
    }

    @Test
    @Transactional
    void getAllAutosByKmIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where km is greater than
        defaultAutoFiltering("km.greaterThan=" + SMALLER_KM, "km.greaterThan=" + DEFAULT_KM);
    }

    @Test
    @Transactional
    void getAllAutosByPatenteIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where patente equals to
        defaultAutoFiltering("patente.equals=" + DEFAULT_PATENTE, "patente.equals=" + UPDATED_PATENTE);
    }

    @Test
    @Transactional
    void getAllAutosByPatenteIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where patente in
        defaultAutoFiltering("patente.in=" + DEFAULT_PATENTE + "," + UPDATED_PATENTE, "patente.in=" + UPDATED_PATENTE);
    }

    @Test
    @Transactional
    void getAllAutosByPatenteIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where patente is not null
        defaultAutoFiltering("patente.specified=true", "patente.specified=false");
    }

    @Test
    @Transactional
    void getAllAutosByPatenteContainsSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where patente contains
        defaultAutoFiltering("patente.contains=" + DEFAULT_PATENTE, "patente.contains=" + UPDATED_PATENTE);
    }

    @Test
    @Transactional
    void getAllAutosByPatenteNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where patente does not contain
        defaultAutoFiltering("patente.doesNotContain=" + UPDATED_PATENTE, "patente.doesNotContain=" + DEFAULT_PATENTE);
    }

    @Test
    @Transactional
    void getAllAutosByPrecioIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where precio equals to
        defaultAutoFiltering("precio.equals=" + DEFAULT_PRECIO, "precio.equals=" + UPDATED_PRECIO);
    }

    @Test
    @Transactional
    void getAllAutosByPrecioIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where precio in
        defaultAutoFiltering("precio.in=" + DEFAULT_PRECIO + "," + UPDATED_PRECIO, "precio.in=" + UPDATED_PRECIO);
    }

    @Test
    @Transactional
    void getAllAutosByPrecioIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where precio is not null
        defaultAutoFiltering("precio.specified=true", "precio.specified=false");
    }

    @Test
    @Transactional
    void getAllAutosByPrecioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where precio is greater than or equal to
        defaultAutoFiltering("precio.greaterThanOrEqual=" + DEFAULT_PRECIO, "precio.greaterThanOrEqual=" + UPDATED_PRECIO);
    }

    @Test
    @Transactional
    void getAllAutosByPrecioIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where precio is less than or equal to
        defaultAutoFiltering("precio.lessThanOrEqual=" + DEFAULT_PRECIO, "precio.lessThanOrEqual=" + SMALLER_PRECIO);
    }

    @Test
    @Transactional
    void getAllAutosByPrecioIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where precio is less than
        defaultAutoFiltering("precio.lessThan=" + UPDATED_PRECIO, "precio.lessThan=" + DEFAULT_PRECIO);
    }

    @Test
    @Transactional
    void getAllAutosByPrecioIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        // Get all the autoList where precio is greater than
        defaultAutoFiltering("precio.greaterThan=" + SMALLER_PRECIO, "precio.greaterThan=" + DEFAULT_PRECIO);
    }

    @Test
    @Transactional
    void getAllAutosByMarcaIsEqualToSomething() throws Exception {
        Marca marca;
        if (TestUtil.findAll(em, Marca.class).isEmpty()) {
            autoRepository.saveAndFlush(auto);
            marca = MarcaResourceIT.createEntity();
        } else {
            marca = TestUtil.findAll(em, Marca.class).get(0);
        }
        em.persist(marca);
        em.flush();
        auto.setMarca(marca);
        autoRepository.saveAndFlush(auto);
        Long marcaId = marca.getId();
        // Get all the autoList where marca equals to marcaId
        defaultAutoShouldBeFound("marcaId.equals=" + marcaId);

        // Get all the autoList where marca equals to (marcaId + 1)
        defaultAutoShouldNotBeFound("marcaId.equals=" + (marcaId + 1));
    }

    @Test
    @Transactional
    void getAllAutosByModeloIsEqualToSomething() throws Exception {
        Modelo modelo;
        if (TestUtil.findAll(em, Modelo.class).isEmpty()) {
            autoRepository.saveAndFlush(auto);
            modelo = ModeloResourceIT.createEntity();
        } else {
            modelo = TestUtil.findAll(em, Modelo.class).get(0);
        }
        em.persist(modelo);
        em.flush();
        auto.setModelo(modelo);
        autoRepository.saveAndFlush(auto);
        Long modeloId = modelo.getId();
        // Get all the autoList where modelo equals to modeloId
        defaultAutoShouldBeFound("modeloId.equals=" + modeloId);

        // Get all the autoList where modelo equals to (modeloId + 1)
        defaultAutoShouldNotBeFound("modeloId.equals=" + (modeloId + 1));
    }

    @Test
    @Transactional
    void getAllAutosByVersionIsEqualToSomething() throws Exception {
        Version version;
        if (TestUtil.findAll(em, Version.class).isEmpty()) {
            autoRepository.saveAndFlush(auto);
            version = VersionResourceIT.createEntity();
        } else {
            version = TestUtil.findAll(em, Version.class).get(0);
        }
        em.persist(version);
        em.flush();
        auto.setVersion(version);
        autoRepository.saveAndFlush(auto);
        Long versionId = version.getId();
        // Get all the autoList where version equals to versionId
        defaultAutoShouldBeFound("versionId.equals=" + versionId);

        // Get all the autoList where version equals to (versionId + 1)
        defaultAutoShouldNotBeFound("versionId.equals=" + (versionId + 1));
    }

    @Test
    @Transactional
    void getAllAutosByMotorIsEqualToSomething() throws Exception {
        Motor motor;
        if (TestUtil.findAll(em, Motor.class).isEmpty()) {
            autoRepository.saveAndFlush(auto);
            motor = MotorResourceIT.createEntity();
        } else {
            motor = TestUtil.findAll(em, Motor.class).get(0);
        }
        em.persist(motor);
        em.flush();
        auto.setMotor(motor);
        autoRepository.saveAndFlush(auto);
        Long motorId = motor.getId();
        // Get all the autoList where motor equals to motorId
        defaultAutoShouldBeFound("motorId.equals=" + motorId);

        // Get all the autoList where motor equals to (motorId + 1)
        defaultAutoShouldNotBeFound("motorId.equals=" + (motorId + 1));
    }

    private void defaultAutoFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAutoShouldBeFound(shouldBeFound);
        defaultAutoShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAutoShouldBeFound(String filter) throws Exception {
        restAutoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(auto.getId().intValue())))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].condicion").value(hasItem(DEFAULT_CONDICION.toString())))
            .andExpect(jsonPath("$.[*].fechaFabricacion").value(hasItem(DEFAULT_FECHA_FABRICACION.toString())))
            .andExpect(jsonPath("$.[*].km").value(hasItem(DEFAULT_KM)))
            .andExpect(jsonPath("$.[*].patente").value(hasItem(DEFAULT_PATENTE)))
            .andExpect(jsonPath("$.[*].precio").value(hasItem(sameNumber(DEFAULT_PRECIO))));

        // Check, that the count call also returns 1
        restAutoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAutoShouldNotBeFound(String filter) throws Exception {
        restAutoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAutoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAuto() throws Exception {
        // Get the auto
        restAutoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAuto() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the auto
        Auto updatedAuto = autoRepository.findById(auto.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAuto are not directly saved in db
        em.detach(updatedAuto);
        updatedAuto
            .estado(UPDATED_ESTADO)
            .condicion(UPDATED_CONDICION)
            .fechaFabricacion(UPDATED_FECHA_FABRICACION)
            .km(UPDATED_KM)
            .patente(UPDATED_PATENTE)
            .precio(UPDATED_PRECIO);
        AutoDTO autoDTO = autoMapper.toDto(updatedAuto);

        restAutoMockMvc
            .perform(put(ENTITY_API_URL_ID, autoDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(autoDTO)))
            .andExpect(status().isOk());

        // Validate the Auto in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAutoToMatchAllProperties(updatedAuto);
    }

    @Test
    @Transactional
    void putNonExistingAuto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auto.setId(longCount.incrementAndGet());

        // Create the Auto
        AutoDTO autoDTO = autoMapper.toDto(auto);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAutoMockMvc
            .perform(put(ENTITY_API_URL_ID, autoDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(autoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Auto in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAuto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auto.setId(longCount.incrementAndGet());

        // Create the Auto
        AutoDTO autoDTO = autoMapper.toDto(auto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAutoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(autoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Auto in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAuto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auto.setId(longCount.incrementAndGet());

        // Create the Auto
        AutoDTO autoDTO = autoMapper.toDto(auto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAutoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(autoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Auto in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAutoWithPatch() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the auto using partial update
        Auto partialUpdatedAuto = new Auto();
        partialUpdatedAuto.setId(auto.getId());

        partialUpdatedAuto.estado(UPDATED_ESTADO).condicion(UPDATED_CONDICION).km(UPDATED_KM);

        restAutoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuto.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAuto))
            )
            .andExpect(status().isOk());

        // Validate the Auto in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAutoUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedAuto, auto), getPersistedAuto(auto));
    }

    @Test
    @Transactional
    void fullUpdateAutoWithPatch() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the auto using partial update
        Auto partialUpdatedAuto = new Auto();
        partialUpdatedAuto.setId(auto.getId());

        partialUpdatedAuto
            .estado(UPDATED_ESTADO)
            .condicion(UPDATED_CONDICION)
            .fechaFabricacion(UPDATED_FECHA_FABRICACION)
            .km(UPDATED_KM)
            .patente(UPDATED_PATENTE)
            .precio(UPDATED_PRECIO);

        restAutoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuto.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAuto))
            )
            .andExpect(status().isOk());

        // Validate the Auto in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAutoUpdatableFieldsEquals(partialUpdatedAuto, getPersistedAuto(partialUpdatedAuto));
    }

    @Test
    @Transactional
    void patchNonExistingAuto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auto.setId(longCount.incrementAndGet());

        // Create the Auto
        AutoDTO autoDTO = autoMapper.toDto(auto);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAutoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, autoDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(autoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Auto in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAuto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auto.setId(longCount.incrementAndGet());

        // Create the Auto
        AutoDTO autoDTO = autoMapper.toDto(auto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAutoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(autoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Auto in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAuto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auto.setId(longCount.incrementAndGet());

        // Create the Auto
        AutoDTO autoDTO = autoMapper.toDto(auto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAutoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(autoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Auto in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAuto() throws Exception {
        // Initialize the database
        insertedAuto = autoRepository.saveAndFlush(auto);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the auto
        restAutoMockMvc
            .perform(delete(ENTITY_API_URL_ID, auto.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return autoRepository.count();
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

    protected Auto getPersistedAuto(Auto auto) {
        return autoRepository.findById(auto.getId()).orElseThrow();
    }

    protected void assertPersistedAutoToMatchAllProperties(Auto expectedAuto) {
        assertAutoAllPropertiesEquals(expectedAuto, getPersistedAuto(expectedAuto));
    }

    protected void assertPersistedAutoToMatchUpdatableProperties(Auto expectedAuto) {
        assertAutoAllUpdatablePropertiesEquals(expectedAuto, getPersistedAuto(expectedAuto));
    }
}
