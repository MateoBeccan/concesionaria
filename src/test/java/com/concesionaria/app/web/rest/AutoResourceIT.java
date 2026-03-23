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

    private static final LocalDate DEFAULT_FECHA_INGRESO = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_FECHA_INGRESO = LocalDate.now(ZoneId.systemDefault());

    private static final Integer DEFAULT_KM = 0;
    private static final Integer UPDATED_KM = 1;

    private static final String DEFAULT_PATENTE = "AAAAAAAAAA";
    private static final String UPDATED_PATENTE = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRECIO = new BigDecimal(0);
    private static final BigDecimal UPDATED_PRECIO = new BigDecimal(1);

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
            .fechaIngreso(DEFAULT_FECHA_INGRESO)
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
            .fechaIngreso(UPDATED_FECHA_INGRESO)
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
    void checkPatenteIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        auto.setPatente(null);

        // Create the Auto, which fails.
        AutoDTO autoDTO = autoMapper.toDto(auto);

        restAutoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(autoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPrecioIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        auto.setPrecio(null);

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
            .andExpect(jsonPath("$.[*].fechaIngreso").value(hasItem(DEFAULT_FECHA_INGRESO.toString())))
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
            .andExpect(jsonPath("$.fechaIngreso").value(DEFAULT_FECHA_INGRESO.toString()))
            .andExpect(jsonPath("$.km").value(DEFAULT_KM))
            .andExpect(jsonPath("$.patente").value(DEFAULT_PATENTE))
            .andExpect(jsonPath("$.precio").value(sameNumber(DEFAULT_PRECIO)));
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
            .fechaIngreso(UPDATED_FECHA_INGRESO)
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

        partialUpdatedAuto.estado(UPDATED_ESTADO).condicion(UPDATED_CONDICION).fechaIngreso(UPDATED_FECHA_INGRESO).precio(UPDATED_PRECIO);

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
            .fechaIngreso(UPDATED_FECHA_INGRESO)
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
