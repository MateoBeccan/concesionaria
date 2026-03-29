package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.MetodoPagoAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.MetodoPago;
import com.concesionaria.app.repository.MetodoPagoRepository;
import com.concesionaria.app.service.dto.MetodoPagoDTO;
import com.concesionaria.app.service.mapper.MetodoPagoMapper;
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
 * Integration tests for the {@link MetodoPagoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MetodoPagoResourceIT {

    private static final String DEFAULT_CODIGO = "AAAAAAAAAA";
    private static final String UPDATED_CODIGO = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVO = false;
    private static final Boolean UPDATED_ACTIVO = true;

    private static final Boolean DEFAULT_REQUIERE_REFERENCIA = false;
    private static final Boolean UPDATED_REQUIERE_REFERENCIA = true;

    private static final String ENTITY_API_URL = "/api/metodo-pagos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private MetodoPagoMapper metodoPagoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMetodoPagoMockMvc;

    private MetodoPago metodoPago;

    private MetodoPago insertedMetodoPago;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MetodoPago createEntity() {
        return new MetodoPago()
            .codigo(DEFAULT_CODIGO)
            .descripcion(DEFAULT_DESCRIPCION)
            .activo(DEFAULT_ACTIVO)
            .requiereReferencia(DEFAULT_REQUIERE_REFERENCIA);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MetodoPago createUpdatedEntity() {
        return new MetodoPago()
            .codigo(UPDATED_CODIGO)
            .descripcion(UPDATED_DESCRIPCION)
            .activo(UPDATED_ACTIVO)
            .requiereReferencia(UPDATED_REQUIERE_REFERENCIA);
    }

    @BeforeEach
    void initTest() {
        metodoPago = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMetodoPago != null) {
            metodoPagoRepository.delete(insertedMetodoPago);
            insertedMetodoPago = null;
        }
    }

    @Test
    @Transactional
    void createMetodoPago() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the MetodoPago
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(metodoPago);
        var returnedMetodoPagoDTO = om.readValue(
            restMetodoPagoMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metodoPagoDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MetodoPagoDTO.class
        );

        // Validate the MetodoPago in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMetodoPago = metodoPagoMapper.toEntity(returnedMetodoPagoDTO);
        assertMetodoPagoUpdatableFieldsEquals(returnedMetodoPago, getPersistedMetodoPago(returnedMetodoPago));

        insertedMetodoPago = returnedMetodoPago;
    }

    @Test
    @Transactional
    void createMetodoPagoWithExistingId() throws Exception {
        // Create the MetodoPago with an existing ID
        metodoPago.setId(1L);
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(metodoPago);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMetodoPagoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metodoPagoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MetodoPago in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodigoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        metodoPago.setCodigo(null);

        // Create the MetodoPago, which fails.
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(metodoPago);

        restMetodoPagoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metodoPagoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActivoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        metodoPago.setActivo(null);

        // Create the MetodoPago, which fails.
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(metodoPago);

        restMetodoPagoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metodoPagoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRequiereReferenciaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        metodoPago.setRequiereReferencia(null);

        // Create the MetodoPago, which fails.
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(metodoPago);

        restMetodoPagoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metodoPagoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMetodoPagos() throws Exception {
        // Initialize the database
        insertedMetodoPago = metodoPagoRepository.saveAndFlush(metodoPago);

        // Get all the metodoPagoList
        restMetodoPagoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(metodoPago.getId().intValue())))
            .andExpect(jsonPath("$.[*].codigo").value(hasItem(DEFAULT_CODIGO)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].activo").value(hasItem(DEFAULT_ACTIVO)))
            .andExpect(jsonPath("$.[*].requiereReferencia").value(hasItem(DEFAULT_REQUIERE_REFERENCIA)));
    }

    @Test
    @Transactional
    void getMetodoPago() throws Exception {
        // Initialize the database
        insertedMetodoPago = metodoPagoRepository.saveAndFlush(metodoPago);

        // Get the metodoPago
        restMetodoPagoMockMvc
            .perform(get(ENTITY_API_URL_ID, metodoPago.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(metodoPago.getId().intValue()))
            .andExpect(jsonPath("$.codigo").value(DEFAULT_CODIGO))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION))
            .andExpect(jsonPath("$.activo").value(DEFAULT_ACTIVO))
            .andExpect(jsonPath("$.requiereReferencia").value(DEFAULT_REQUIERE_REFERENCIA));
    }

    @Test
    @Transactional
    void getNonExistingMetodoPago() throws Exception {
        // Get the metodoPago
        restMetodoPagoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMetodoPago() throws Exception {
        // Initialize the database
        insertedMetodoPago = metodoPagoRepository.saveAndFlush(metodoPago);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the metodoPago
        MetodoPago updatedMetodoPago = metodoPagoRepository.findById(metodoPago.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMetodoPago are not directly saved in db
        em.detach(updatedMetodoPago);
        updatedMetodoPago
            .codigo(UPDATED_CODIGO)
            .descripcion(UPDATED_DESCRIPCION)
            .activo(UPDATED_ACTIVO)
            .requiereReferencia(UPDATED_REQUIERE_REFERENCIA);
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(updatedMetodoPago);

        restMetodoPagoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, metodoPagoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metodoPagoDTO))
            )
            .andExpect(status().isOk());

        // Validate the MetodoPago in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMetodoPagoToMatchAllProperties(updatedMetodoPago);
    }

    @Test
    @Transactional
    void putNonExistingMetodoPago() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metodoPago.setId(longCount.incrementAndGet());

        // Create the MetodoPago
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(metodoPago);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMetodoPagoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, metodoPagoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metodoPagoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MetodoPago in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMetodoPago() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metodoPago.setId(longCount.incrementAndGet());

        // Create the MetodoPago
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(metodoPago);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetodoPagoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metodoPagoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MetodoPago in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMetodoPago() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metodoPago.setId(longCount.incrementAndGet());

        // Create the MetodoPago
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(metodoPago);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetodoPagoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metodoPagoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MetodoPago in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMetodoPagoWithPatch() throws Exception {
        // Initialize the database
        insertedMetodoPago = metodoPagoRepository.saveAndFlush(metodoPago);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the metodoPago using partial update
        MetodoPago partialUpdatedMetodoPago = new MetodoPago();
        partialUpdatedMetodoPago.setId(metodoPago.getId());

        partialUpdatedMetodoPago.codigo(UPDATED_CODIGO);

        restMetodoPagoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMetodoPago.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMetodoPago))
            )
            .andExpect(status().isOk());

        // Validate the MetodoPago in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMetodoPagoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMetodoPago, metodoPago),
            getPersistedMetodoPago(metodoPago)
        );
    }

    @Test
    @Transactional
    void fullUpdateMetodoPagoWithPatch() throws Exception {
        // Initialize the database
        insertedMetodoPago = metodoPagoRepository.saveAndFlush(metodoPago);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the metodoPago using partial update
        MetodoPago partialUpdatedMetodoPago = new MetodoPago();
        partialUpdatedMetodoPago.setId(metodoPago.getId());

        partialUpdatedMetodoPago
            .codigo(UPDATED_CODIGO)
            .descripcion(UPDATED_DESCRIPCION)
            .activo(UPDATED_ACTIVO)
            .requiereReferencia(UPDATED_REQUIERE_REFERENCIA);

        restMetodoPagoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMetodoPago.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMetodoPago))
            )
            .andExpect(status().isOk());

        // Validate the MetodoPago in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMetodoPagoUpdatableFieldsEquals(partialUpdatedMetodoPago, getPersistedMetodoPago(partialUpdatedMetodoPago));
    }

    @Test
    @Transactional
    void patchNonExistingMetodoPago() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metodoPago.setId(longCount.incrementAndGet());

        // Create the MetodoPago
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(metodoPago);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMetodoPagoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, metodoPagoDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(metodoPagoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MetodoPago in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMetodoPago() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metodoPago.setId(longCount.incrementAndGet());

        // Create the MetodoPago
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(metodoPago);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetodoPagoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(metodoPagoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MetodoPago in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMetodoPago() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metodoPago.setId(longCount.incrementAndGet());

        // Create the MetodoPago
        MetodoPagoDTO metodoPagoDTO = metodoPagoMapper.toDto(metodoPago);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetodoPagoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(metodoPagoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MetodoPago in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMetodoPago() throws Exception {
        // Initialize the database
        insertedMetodoPago = metodoPagoRepository.saveAndFlush(metodoPago);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the metodoPago
        restMetodoPagoMockMvc
            .perform(delete(ENTITY_API_URL_ID, metodoPago.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return metodoPagoRepository.count();
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

    protected MetodoPago getPersistedMetodoPago(MetodoPago metodoPago) {
        return metodoPagoRepository.findById(metodoPago.getId()).orElseThrow();
    }

    protected void assertPersistedMetodoPagoToMatchAllProperties(MetodoPago expectedMetodoPago) {
        assertMetodoPagoAllPropertiesEquals(expectedMetodoPago, getPersistedMetodoPago(expectedMetodoPago));
    }

    protected void assertPersistedMetodoPagoToMatchUpdatableProperties(MetodoPago expectedMetodoPago) {
        assertMetodoPagoAllUpdatablePropertiesEquals(expectedMetodoPago, getPersistedMetodoPago(expectedMetodoPago));
    }
}
