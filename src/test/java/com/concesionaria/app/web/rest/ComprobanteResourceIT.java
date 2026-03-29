package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.ComprobanteAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static com.concesionaria.app.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.service.dto.ComprobanteDTO;
import com.concesionaria.app.service.mapper.ComprobanteMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 * Integration tests for the {@link ComprobanteResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ComprobanteResourceIT {

    private static final String DEFAULT_NUMERO_COMPROBANTE = "AAAAAAAAAA";
    private static final String UPDATED_NUMERO_COMPROBANTE = "BBBBBBBBBB";

    private static final Instant DEFAULT_FECHA_EMISION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_EMISION = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final BigDecimal DEFAULT_IMPORTE_NETO = new BigDecimal(0);
    private static final BigDecimal UPDATED_IMPORTE_NETO = new BigDecimal(1);

    private static final BigDecimal DEFAULT_IMPUESTO = new BigDecimal(0);
    private static final BigDecimal UPDATED_IMPUESTO = new BigDecimal(1);

    private static final BigDecimal DEFAULT_TOTAL = new BigDecimal(0);
    private static final BigDecimal UPDATED_TOTAL = new BigDecimal(1);

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/comprobantes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ComprobanteRepository comprobanteRepository;

    @Autowired
    private ComprobanteMapper comprobanteMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restComprobanteMockMvc;

    private Comprobante comprobante;

    private Comprobante insertedComprobante;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Comprobante createEntity() {
        return new Comprobante()
            .numeroComprobante(DEFAULT_NUMERO_COMPROBANTE)
            .fechaEmision(DEFAULT_FECHA_EMISION)
            .importeNeto(DEFAULT_IMPORTE_NETO)
            .impuesto(DEFAULT_IMPUESTO)
            .total(DEFAULT_TOTAL)
            .createdDate(DEFAULT_CREATED_DATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Comprobante createUpdatedEntity() {
        return new Comprobante()
            .numeroComprobante(UPDATED_NUMERO_COMPROBANTE)
            .fechaEmision(UPDATED_FECHA_EMISION)
            .importeNeto(UPDATED_IMPORTE_NETO)
            .impuesto(UPDATED_IMPUESTO)
            .total(UPDATED_TOTAL)
            .createdDate(UPDATED_CREATED_DATE);
    }

    @BeforeEach
    void initTest() {
        comprobante = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedComprobante != null) {
            comprobanteRepository.delete(insertedComprobante);
            insertedComprobante = null;
        }
    }

    @Test
    @Transactional
    void createComprobante() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Comprobante
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);
        var returnedComprobanteDTO = om.readValue(
            restComprobanteMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(comprobanteDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ComprobanteDTO.class
        );

        // Validate the Comprobante in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedComprobante = comprobanteMapper.toEntity(returnedComprobanteDTO);
        assertComprobanteUpdatableFieldsEquals(returnedComprobante, getPersistedComprobante(returnedComprobante));

        insertedComprobante = returnedComprobante;
    }

    @Test
    @Transactional
    void createComprobanteWithExistingId() throws Exception {
        // Create the Comprobante with an existing ID
        comprobante.setId(1L);
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restComprobanteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(comprobanteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Comprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNumeroComprobanteIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        comprobante.setNumeroComprobante(null);

        // Create the Comprobante, which fails.
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        restComprobanteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(comprobanteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFechaEmisionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        comprobante.setFechaEmision(null);

        // Create the Comprobante, which fails.
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        restComprobanteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(comprobanteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkImporteNetoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        comprobante.setImporteNeto(null);

        // Create the Comprobante, which fails.
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        restComprobanteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(comprobanteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkImpuestoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        comprobante.setImpuesto(null);

        // Create the Comprobante, which fails.
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        restComprobanteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(comprobanteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTotalIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        comprobante.setTotal(null);

        // Create the Comprobante, which fails.
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        restComprobanteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(comprobanteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllComprobantes() throws Exception {
        // Initialize the database
        insertedComprobante = comprobanteRepository.saveAndFlush(comprobante);

        // Get all the comprobanteList
        restComprobanteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(comprobante.getId().intValue())))
            .andExpect(jsonPath("$.[*].numeroComprobante").value(hasItem(DEFAULT_NUMERO_COMPROBANTE)))
            .andExpect(jsonPath("$.[*].fechaEmision").value(hasItem(DEFAULT_FECHA_EMISION.toString())))
            .andExpect(jsonPath("$.[*].importeNeto").value(hasItem(sameNumber(DEFAULT_IMPORTE_NETO))))
            .andExpect(jsonPath("$.[*].impuesto").value(hasItem(sameNumber(DEFAULT_IMPUESTO))))
            .andExpect(jsonPath("$.[*].total").value(hasItem(sameNumber(DEFAULT_TOTAL))))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())));
    }

    @Test
    @Transactional
    void getComprobante() throws Exception {
        // Initialize the database
        insertedComprobante = comprobanteRepository.saveAndFlush(comprobante);

        // Get the comprobante
        restComprobanteMockMvc
            .perform(get(ENTITY_API_URL_ID, comprobante.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(comprobante.getId().intValue()))
            .andExpect(jsonPath("$.numeroComprobante").value(DEFAULT_NUMERO_COMPROBANTE))
            .andExpect(jsonPath("$.fechaEmision").value(DEFAULT_FECHA_EMISION.toString()))
            .andExpect(jsonPath("$.importeNeto").value(sameNumber(DEFAULT_IMPORTE_NETO)))
            .andExpect(jsonPath("$.impuesto").value(sameNumber(DEFAULT_IMPUESTO)))
            .andExpect(jsonPath("$.total").value(sameNumber(DEFAULT_TOTAL)))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingComprobante() throws Exception {
        // Get the comprobante
        restComprobanteMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingComprobante() throws Exception {
        // Initialize the database
        insertedComprobante = comprobanteRepository.saveAndFlush(comprobante);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the comprobante
        Comprobante updatedComprobante = comprobanteRepository.findById(comprobante.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedComprobante are not directly saved in db
        em.detach(updatedComprobante);
        updatedComprobante
            .numeroComprobante(UPDATED_NUMERO_COMPROBANTE)
            .fechaEmision(UPDATED_FECHA_EMISION)
            .importeNeto(UPDATED_IMPORTE_NETO)
            .impuesto(UPDATED_IMPUESTO)
            .total(UPDATED_TOTAL)
            .createdDate(UPDATED_CREATED_DATE);
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(updatedComprobante);

        restComprobanteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, comprobanteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(comprobanteDTO))
            )
            .andExpect(status().isOk());

        // Validate the Comprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedComprobanteToMatchAllProperties(updatedComprobante);
    }

    @Test
    @Transactional
    void putNonExistingComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comprobante.setId(longCount.incrementAndGet());

        // Create the Comprobante
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restComprobanteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, comprobanteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(comprobanteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comprobante.setId(longCount.incrementAndGet());

        // Create the Comprobante
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restComprobanteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(comprobanteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comprobante.setId(longCount.incrementAndGet());

        // Create the Comprobante
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restComprobanteMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(comprobanteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Comprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateComprobanteWithPatch() throws Exception {
        // Initialize the database
        insertedComprobante = comprobanteRepository.saveAndFlush(comprobante);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the comprobante using partial update
        Comprobante partialUpdatedComprobante = new Comprobante();
        partialUpdatedComprobante.setId(comprobante.getId());

        partialUpdatedComprobante.fechaEmision(UPDATED_FECHA_EMISION).impuesto(UPDATED_IMPUESTO);

        restComprobanteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedComprobante.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedComprobante))
            )
            .andExpect(status().isOk());

        // Validate the Comprobante in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertComprobanteUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedComprobante, comprobante),
            getPersistedComprobante(comprobante)
        );
    }

    @Test
    @Transactional
    void fullUpdateComprobanteWithPatch() throws Exception {
        // Initialize the database
        insertedComprobante = comprobanteRepository.saveAndFlush(comprobante);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the comprobante using partial update
        Comprobante partialUpdatedComprobante = new Comprobante();
        partialUpdatedComprobante.setId(comprobante.getId());

        partialUpdatedComprobante
            .numeroComprobante(UPDATED_NUMERO_COMPROBANTE)
            .fechaEmision(UPDATED_FECHA_EMISION)
            .importeNeto(UPDATED_IMPORTE_NETO)
            .impuesto(UPDATED_IMPUESTO)
            .total(UPDATED_TOTAL)
            .createdDate(UPDATED_CREATED_DATE);

        restComprobanteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedComprobante.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedComprobante))
            )
            .andExpect(status().isOk());

        // Validate the Comprobante in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertComprobanteUpdatableFieldsEquals(partialUpdatedComprobante, getPersistedComprobante(partialUpdatedComprobante));
    }

    @Test
    @Transactional
    void patchNonExistingComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comprobante.setId(longCount.incrementAndGet());

        // Create the Comprobante
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restComprobanteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, comprobanteDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(comprobanteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comprobante.setId(longCount.incrementAndGet());

        // Create the Comprobante
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restComprobanteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(comprobanteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamComprobante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comprobante.setId(longCount.incrementAndGet());

        // Create the Comprobante
        ComprobanteDTO comprobanteDTO = comprobanteMapper.toDto(comprobante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restComprobanteMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(comprobanteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Comprobante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteComprobante() throws Exception {
        // Initialize the database
        insertedComprobante = comprobanteRepository.saveAndFlush(comprobante);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the comprobante
        restComprobanteMockMvc
            .perform(delete(ENTITY_API_URL_ID, comprobante.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return comprobanteRepository.count();
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

    protected Comprobante getPersistedComprobante(Comprobante comprobante) {
        return comprobanteRepository.findById(comprobante.getId()).orElseThrow();
    }

    protected void assertPersistedComprobanteToMatchAllProperties(Comprobante expectedComprobante) {
        assertComprobanteAllPropertiesEquals(expectedComprobante, getPersistedComprobante(expectedComprobante));
    }

    protected void assertPersistedComprobanteToMatchUpdatableProperties(Comprobante expectedComprobante) {
        assertComprobanteAllUpdatablePropertiesEquals(expectedComprobante, getPersistedComprobante(expectedComprobante));
    }
}
