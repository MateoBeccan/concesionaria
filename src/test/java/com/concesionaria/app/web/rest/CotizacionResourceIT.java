package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.CotizacionAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static com.concesionaria.app.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Cotizacion;
import com.concesionaria.app.repository.CotizacionRepository;
import com.concesionaria.app.service.dto.CotizacionDTO;
import com.concesionaria.app.service.mapper.CotizacionMapper;
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
 * Integration tests for the {@link CotizacionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CotizacionResourceIT {

    private static final Instant DEFAULT_FECHA = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final BigDecimal DEFAULT_VALOR_COMPRA = new BigDecimal(0);
    private static final BigDecimal UPDATED_VALOR_COMPRA = new BigDecimal(1);

    private static final BigDecimal DEFAULT_VALOR_VENTA = new BigDecimal(0);
    private static final BigDecimal UPDATED_VALOR_VENTA = new BigDecimal(1);

    private static final Boolean DEFAULT_ACTIVO = false;
    private static final Boolean UPDATED_ACTIVO = true;

    private static final String ENTITY_API_URL = "/api/cotizacions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CotizacionRepository cotizacionRepository;

    @Autowired
    private CotizacionMapper cotizacionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCotizacionMockMvc;

    private Cotizacion cotizacion;

    private Cotizacion insertedCotizacion;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cotizacion createEntity() {
        return new Cotizacion()
            .fecha(DEFAULT_FECHA)
            .valorCompra(DEFAULT_VALOR_COMPRA)
            .valorVenta(DEFAULT_VALOR_VENTA)
            .activo(DEFAULT_ACTIVO);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cotizacion createUpdatedEntity() {
        return new Cotizacion()
            .fecha(UPDATED_FECHA)
            .valorCompra(UPDATED_VALOR_COMPRA)
            .valorVenta(UPDATED_VALOR_VENTA)
            .activo(UPDATED_ACTIVO);
    }

    @BeforeEach
    void initTest() {
        cotizacion = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCotizacion != null) {
            cotizacionRepository.delete(insertedCotizacion);
            insertedCotizacion = null;
        }
    }

    @Test
    @Transactional
    void createCotizacion() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Cotizacion
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);
        var returnedCotizacionDTO = om.readValue(
            restCotizacionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cotizacionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CotizacionDTO.class
        );

        // Validate the Cotizacion in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCotizacion = cotizacionMapper.toEntity(returnedCotizacionDTO);
        assertCotizacionUpdatableFieldsEquals(returnedCotizacion, getPersistedCotizacion(returnedCotizacion));

        insertedCotizacion = returnedCotizacion;
    }

    @Test
    @Transactional
    void createCotizacionWithExistingId() throws Exception {
        // Create the Cotizacion with an existing ID
        cotizacion.setId(1L);
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCotizacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cotizacionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Cotizacion in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkFechaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cotizacion.setFecha(null);

        // Create the Cotizacion, which fails.
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);

        restCotizacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cotizacionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkValorCompraIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cotizacion.setValorCompra(null);

        // Create the Cotizacion, which fails.
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);

        restCotizacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cotizacionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkValorVentaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cotizacion.setValorVenta(null);

        // Create the Cotizacion, which fails.
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);

        restCotizacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cotizacionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActivoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cotizacion.setActivo(null);

        // Create the Cotizacion, which fails.
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);

        restCotizacionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cotizacionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCotizacions() throws Exception {
        // Initialize the database
        insertedCotizacion = cotizacionRepository.saveAndFlush(cotizacion);

        // Get all the cotizacionList
        restCotizacionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cotizacion.getId().intValue())))
            .andExpect(jsonPath("$.[*].fecha").value(hasItem(DEFAULT_FECHA.toString())))
            .andExpect(jsonPath("$.[*].valorCompra").value(hasItem(sameNumber(DEFAULT_VALOR_COMPRA))))
            .andExpect(jsonPath("$.[*].valorVenta").value(hasItem(sameNumber(DEFAULT_VALOR_VENTA))))
            .andExpect(jsonPath("$.[*].activo").value(hasItem(DEFAULT_ACTIVO)));
    }

    @Test
    @Transactional
    void getCotizacion() throws Exception {
        // Initialize the database
        insertedCotizacion = cotizacionRepository.saveAndFlush(cotizacion);

        // Get the cotizacion
        restCotizacionMockMvc
            .perform(get(ENTITY_API_URL_ID, cotizacion.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cotizacion.getId().intValue()))
            .andExpect(jsonPath("$.fecha").value(DEFAULT_FECHA.toString()))
            .andExpect(jsonPath("$.valorCompra").value(sameNumber(DEFAULT_VALOR_COMPRA)))
            .andExpect(jsonPath("$.valorVenta").value(sameNumber(DEFAULT_VALOR_VENTA)))
            .andExpect(jsonPath("$.activo").value(DEFAULT_ACTIVO));
    }

    @Test
    @Transactional
    void getNonExistingCotizacion() throws Exception {
        // Get the cotizacion
        restCotizacionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCotizacion() throws Exception {
        // Initialize the database
        insertedCotizacion = cotizacionRepository.saveAndFlush(cotizacion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the cotizacion
        Cotizacion updatedCotizacion = cotizacionRepository.findById(cotizacion.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCotizacion are not directly saved in db
        em.detach(updatedCotizacion);
        updatedCotizacion.fecha(UPDATED_FECHA).valorCompra(UPDATED_VALOR_COMPRA).valorVenta(UPDATED_VALOR_VENTA).activo(UPDATED_ACTIVO);
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(updatedCotizacion);

        restCotizacionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cotizacionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(cotizacionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Cotizacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCotizacionToMatchAllProperties(updatedCotizacion);
    }

    @Test
    @Transactional
    void putNonExistingCotizacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cotizacion.setId(longCount.incrementAndGet());

        // Create the Cotizacion
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCotizacionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cotizacionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(cotizacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cotizacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCotizacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cotizacion.setId(longCount.incrementAndGet());

        // Create the Cotizacion
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCotizacionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(cotizacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cotizacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCotizacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cotizacion.setId(longCount.incrementAndGet());

        // Create the Cotizacion
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCotizacionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cotizacionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Cotizacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCotizacionWithPatch() throws Exception {
        // Initialize the database
        insertedCotizacion = cotizacionRepository.saveAndFlush(cotizacion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the cotizacion using partial update
        Cotizacion partialUpdatedCotizacion = new Cotizacion();
        partialUpdatedCotizacion.setId(cotizacion.getId());

        partialUpdatedCotizacion.valorCompra(UPDATED_VALOR_COMPRA);

        restCotizacionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCotizacion.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCotizacion))
            )
            .andExpect(status().isOk());

        // Validate the Cotizacion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCotizacionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCotizacion, cotizacion),
            getPersistedCotizacion(cotizacion)
        );
    }

    @Test
    @Transactional
    void fullUpdateCotizacionWithPatch() throws Exception {
        // Initialize the database
        insertedCotizacion = cotizacionRepository.saveAndFlush(cotizacion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the cotizacion using partial update
        Cotizacion partialUpdatedCotizacion = new Cotizacion();
        partialUpdatedCotizacion.setId(cotizacion.getId());

        partialUpdatedCotizacion
            .fecha(UPDATED_FECHA)
            .valorCompra(UPDATED_VALOR_COMPRA)
            .valorVenta(UPDATED_VALOR_VENTA)
            .activo(UPDATED_ACTIVO);

        restCotizacionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCotizacion.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCotizacion))
            )
            .andExpect(status().isOk());

        // Validate the Cotizacion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCotizacionUpdatableFieldsEquals(partialUpdatedCotizacion, getPersistedCotizacion(partialUpdatedCotizacion));
    }

    @Test
    @Transactional
    void patchNonExistingCotizacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cotizacion.setId(longCount.incrementAndGet());

        // Create the Cotizacion
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCotizacionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, cotizacionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(cotizacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cotizacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCotizacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cotizacion.setId(longCount.incrementAndGet());

        // Create the Cotizacion
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCotizacionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(cotizacionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cotizacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCotizacion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cotizacion.setId(longCount.incrementAndGet());

        // Create the Cotizacion
        CotizacionDTO cotizacionDTO = cotizacionMapper.toDto(cotizacion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCotizacionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(cotizacionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Cotizacion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCotizacion() throws Exception {
        // Initialize the database
        insertedCotizacion = cotizacionRepository.saveAndFlush(cotizacion);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the cotizacion
        restCotizacionMockMvc
            .perform(delete(ENTITY_API_URL_ID, cotizacion.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return cotizacionRepository.count();
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

    protected Cotizacion getPersistedCotizacion(Cotizacion cotizacion) {
        return cotizacionRepository.findById(cotizacion.getId()).orElseThrow();
    }

    protected void assertPersistedCotizacionToMatchAllProperties(Cotizacion expectedCotizacion) {
        assertCotizacionAllPropertiesEquals(expectedCotizacion, getPersistedCotizacion(expectedCotizacion));
    }

    protected void assertPersistedCotizacionToMatchUpdatableProperties(Cotizacion expectedCotizacion) {
        assertCotizacionAllUpdatablePropertiesEquals(expectedCotizacion, getPersistedCotizacion(expectedCotizacion));
    }
}
