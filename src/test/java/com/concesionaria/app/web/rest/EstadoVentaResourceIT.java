package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.EstadoVentaAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.EstadoVenta;
import com.concesionaria.app.repository.EstadoVentaRepository;
import com.concesionaria.app.service.dto.EstadoVentaDTO;
import com.concesionaria.app.service.mapper.EstadoVentaMapper;
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
 * Integration tests for the {@link EstadoVentaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EstadoVentaResourceIT {

    private static final String DEFAULT_CODIGO = "AAAAAAAAAA";
    private static final String UPDATED_CODIGO = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/estado-ventas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EstadoVentaRepository estadoVentaRepository;

    @Autowired
    private EstadoVentaMapper estadoVentaMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEstadoVentaMockMvc;

    private EstadoVenta estadoVenta;

    private EstadoVenta insertedEstadoVenta;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EstadoVenta createEntity() {
        return new EstadoVenta().codigo(DEFAULT_CODIGO).descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EstadoVenta createUpdatedEntity() {
        return new EstadoVenta().codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        estadoVenta = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedEstadoVenta != null) {
            estadoVentaRepository.delete(insertedEstadoVenta);
            insertedEstadoVenta = null;
        }
    }

    @Test
    @Transactional
    void createEstadoVenta() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the EstadoVenta
        EstadoVentaDTO estadoVentaDTO = estadoVentaMapper.toDto(estadoVenta);
        var returnedEstadoVentaDTO = om.readValue(
            restEstadoVentaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(estadoVentaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EstadoVentaDTO.class
        );

        // Validate the EstadoVenta in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedEstadoVenta = estadoVentaMapper.toEntity(returnedEstadoVentaDTO);
        assertEstadoVentaUpdatableFieldsEquals(returnedEstadoVenta, getPersistedEstadoVenta(returnedEstadoVenta));

        insertedEstadoVenta = returnedEstadoVenta;
    }

    @Test
    @Transactional
    void createEstadoVentaWithExistingId() throws Exception {
        // Create the EstadoVenta with an existing ID
        estadoVenta.setId(1L);
        EstadoVentaDTO estadoVentaDTO = estadoVentaMapper.toDto(estadoVenta);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEstadoVentaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(estadoVentaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the EstadoVenta in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodigoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        estadoVenta.setCodigo(null);

        // Create the EstadoVenta, which fails.
        EstadoVentaDTO estadoVentaDTO = estadoVentaMapper.toDto(estadoVenta);

        restEstadoVentaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(estadoVentaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEstadoVentas() throws Exception {
        // Initialize the database
        insertedEstadoVenta = estadoVentaRepository.saveAndFlush(estadoVenta);

        // Get all the estadoVentaList
        restEstadoVentaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(estadoVenta.getId().intValue())))
            .andExpect(jsonPath("$.[*].codigo").value(hasItem(DEFAULT_CODIGO)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @Test
    @Transactional
    void getEstadoVenta() throws Exception {
        // Initialize the database
        insertedEstadoVenta = estadoVentaRepository.saveAndFlush(estadoVenta);

        // Get the estadoVenta
        restEstadoVentaMockMvc
            .perform(get(ENTITY_API_URL_ID, estadoVenta.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(estadoVenta.getId().intValue()))
            .andExpect(jsonPath("$.codigo").value(DEFAULT_CODIGO))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getNonExistingEstadoVenta() throws Exception {
        // Get the estadoVenta
        restEstadoVentaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEstadoVenta() throws Exception {
        // Initialize the database
        insertedEstadoVenta = estadoVentaRepository.saveAndFlush(estadoVenta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the estadoVenta
        EstadoVenta updatedEstadoVenta = estadoVentaRepository.findById(estadoVenta.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEstadoVenta are not directly saved in db
        em.detach(updatedEstadoVenta);
        updatedEstadoVenta.codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);
        EstadoVentaDTO estadoVentaDTO = estadoVentaMapper.toDto(updatedEstadoVenta);

        restEstadoVentaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, estadoVentaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(estadoVentaDTO))
            )
            .andExpect(status().isOk());

        // Validate the EstadoVenta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEstadoVentaToMatchAllProperties(updatedEstadoVenta);
    }

    @Test
    @Transactional
    void putNonExistingEstadoVenta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        estadoVenta.setId(longCount.incrementAndGet());

        // Create the EstadoVenta
        EstadoVentaDTO estadoVentaDTO = estadoVentaMapper.toDto(estadoVenta);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEstadoVentaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, estadoVentaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(estadoVentaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EstadoVenta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEstadoVenta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        estadoVenta.setId(longCount.incrementAndGet());

        // Create the EstadoVenta
        EstadoVentaDTO estadoVentaDTO = estadoVentaMapper.toDto(estadoVenta);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEstadoVentaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(estadoVentaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EstadoVenta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEstadoVenta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        estadoVenta.setId(longCount.incrementAndGet());

        // Create the EstadoVenta
        EstadoVentaDTO estadoVentaDTO = estadoVentaMapper.toDto(estadoVenta);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEstadoVentaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(estadoVentaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the EstadoVenta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEstadoVentaWithPatch() throws Exception {
        // Initialize the database
        insertedEstadoVenta = estadoVentaRepository.saveAndFlush(estadoVenta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the estadoVenta using partial update
        EstadoVenta partialUpdatedEstadoVenta = new EstadoVenta();
        partialUpdatedEstadoVenta.setId(estadoVenta.getId());

        partialUpdatedEstadoVenta.descripcion(UPDATED_DESCRIPCION);

        restEstadoVentaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEstadoVenta.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEstadoVenta))
            )
            .andExpect(status().isOk());

        // Validate the EstadoVenta in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEstadoVentaUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedEstadoVenta, estadoVenta),
            getPersistedEstadoVenta(estadoVenta)
        );
    }

    @Test
    @Transactional
    void fullUpdateEstadoVentaWithPatch() throws Exception {
        // Initialize the database
        insertedEstadoVenta = estadoVentaRepository.saveAndFlush(estadoVenta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the estadoVenta using partial update
        EstadoVenta partialUpdatedEstadoVenta = new EstadoVenta();
        partialUpdatedEstadoVenta.setId(estadoVenta.getId());

        partialUpdatedEstadoVenta.codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION);

        restEstadoVentaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEstadoVenta.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEstadoVenta))
            )
            .andExpect(status().isOk());

        // Validate the EstadoVenta in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEstadoVentaUpdatableFieldsEquals(partialUpdatedEstadoVenta, getPersistedEstadoVenta(partialUpdatedEstadoVenta));
    }

    @Test
    @Transactional
    void patchNonExistingEstadoVenta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        estadoVenta.setId(longCount.incrementAndGet());

        // Create the EstadoVenta
        EstadoVentaDTO estadoVentaDTO = estadoVentaMapper.toDto(estadoVenta);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEstadoVentaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, estadoVentaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(estadoVentaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EstadoVenta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEstadoVenta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        estadoVenta.setId(longCount.incrementAndGet());

        // Create the EstadoVenta
        EstadoVentaDTO estadoVentaDTO = estadoVentaMapper.toDto(estadoVenta);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEstadoVentaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(estadoVentaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EstadoVenta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEstadoVenta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        estadoVenta.setId(longCount.incrementAndGet());

        // Create the EstadoVenta
        EstadoVentaDTO estadoVentaDTO = estadoVentaMapper.toDto(estadoVenta);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEstadoVentaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(estadoVentaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the EstadoVenta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEstadoVenta() throws Exception {
        // Initialize the database
        insertedEstadoVenta = estadoVentaRepository.saveAndFlush(estadoVenta);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the estadoVenta
        restEstadoVentaMockMvc
            .perform(delete(ENTITY_API_URL_ID, estadoVenta.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return estadoVentaRepository.count();
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

    protected EstadoVenta getPersistedEstadoVenta(EstadoVenta estadoVenta) {
        return estadoVentaRepository.findById(estadoVenta.getId()).orElseThrow();
    }

    protected void assertPersistedEstadoVentaToMatchAllProperties(EstadoVenta expectedEstadoVenta) {
        assertEstadoVentaAllPropertiesEquals(expectedEstadoVenta, getPersistedEstadoVenta(expectedEstadoVenta));
    }

    protected void assertPersistedEstadoVentaToMatchUpdatableProperties(EstadoVenta expectedEstadoVenta) {
        assertEstadoVentaAllUpdatablePropertiesEquals(expectedEstadoVenta, getPersistedEstadoVenta(expectedEstadoVenta));
    }
}
