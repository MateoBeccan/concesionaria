package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.MonedaAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.repository.MonedaRepository;
import com.concesionaria.app.service.dto.MonedaDTO;
import com.concesionaria.app.service.mapper.MonedaMapper;
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
 * Integration tests for the {@link MonedaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MonedaResourceIT {

    private static final String DEFAULT_CODIGO = "AAAAAAAAAA";
    private static final String UPDATED_CODIGO = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String DEFAULT_SIMBOLO = "AAAAA";
    private static final String UPDATED_SIMBOLO = "BBBBB";

    private static final Boolean DEFAULT_ACTIVO = false;
    private static final Boolean UPDATED_ACTIVO = true;

    private static final String ENTITY_API_URL = "/api/monedas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MonedaRepository monedaRepository;

    @Autowired
    private MonedaMapper monedaMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMonedaMockMvc;

    private Moneda moneda;

    private Moneda insertedMoneda;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Moneda createEntity() {
        return new Moneda().codigo(DEFAULT_CODIGO).descripcion(DEFAULT_DESCRIPCION).simbolo(DEFAULT_SIMBOLO).activo(DEFAULT_ACTIVO);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Moneda createUpdatedEntity() {
        return new Moneda().codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION).simbolo(UPDATED_SIMBOLO).activo(UPDATED_ACTIVO);
    }

    @BeforeEach
    void initTest() {
        moneda = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMoneda != null) {
            monedaRepository.delete(insertedMoneda);
            insertedMoneda = null;
        }
    }

    @Test
    @Transactional
    void createMoneda() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Moneda
        MonedaDTO monedaDTO = monedaMapper.toDto(moneda);
        var returnedMonedaDTO = om.readValue(
            restMonedaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monedaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MonedaDTO.class
        );

        // Validate the Moneda in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMoneda = monedaMapper.toEntity(returnedMonedaDTO);
        assertMonedaUpdatableFieldsEquals(returnedMoneda, getPersistedMoneda(returnedMoneda));

        insertedMoneda = returnedMoneda;
    }

    @Test
    @Transactional
    void createMonedaWithExistingId() throws Exception {
        // Create the Moneda with an existing ID
        moneda.setId(1L);
        MonedaDTO monedaDTO = monedaMapper.toDto(moneda);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMonedaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monedaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Moneda in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodigoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        moneda.setCodigo(null);

        // Create the Moneda, which fails.
        MonedaDTO monedaDTO = monedaMapper.toDto(moneda);

        restMonedaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monedaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActivoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        moneda.setActivo(null);

        // Create the Moneda, which fails.
        MonedaDTO monedaDTO = monedaMapper.toDto(moneda);

        restMonedaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monedaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMonedas() throws Exception {
        // Initialize the database
        insertedMoneda = monedaRepository.saveAndFlush(moneda);

        // Get all the monedaList
        restMonedaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(moneda.getId().intValue())))
            .andExpect(jsonPath("$.[*].codigo").value(hasItem(DEFAULT_CODIGO)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].simbolo").value(hasItem(DEFAULT_SIMBOLO)))
            .andExpect(jsonPath("$.[*].activo").value(hasItem(DEFAULT_ACTIVO)));
    }

    @Test
    @Transactional
    void getMoneda() throws Exception {
        // Initialize the database
        insertedMoneda = monedaRepository.saveAndFlush(moneda);

        // Get the moneda
        restMonedaMockMvc
            .perform(get(ENTITY_API_URL_ID, moneda.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(moneda.getId().intValue()))
            .andExpect(jsonPath("$.codigo").value(DEFAULT_CODIGO))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION))
            .andExpect(jsonPath("$.simbolo").value(DEFAULT_SIMBOLO))
            .andExpect(jsonPath("$.activo").value(DEFAULT_ACTIVO));
    }

    @Test
    @Transactional
    void getNonExistingMoneda() throws Exception {
        // Get the moneda
        restMonedaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMoneda() throws Exception {
        // Initialize the database
        insertedMoneda = monedaRepository.saveAndFlush(moneda);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the moneda
        Moneda updatedMoneda = monedaRepository.findById(moneda.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMoneda are not directly saved in db
        em.detach(updatedMoneda);
        updatedMoneda.codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION).simbolo(UPDATED_SIMBOLO).activo(UPDATED_ACTIVO);
        MonedaDTO monedaDTO = monedaMapper.toDto(updatedMoneda);

        restMonedaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, monedaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monedaDTO))
            )
            .andExpect(status().isOk());

        // Validate the Moneda in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMonedaToMatchAllProperties(updatedMoneda);
    }

    @Test
    @Transactional
    void putNonExistingMoneda() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moneda.setId(longCount.incrementAndGet());

        // Create the Moneda
        MonedaDTO monedaDTO = monedaMapper.toDto(moneda);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMonedaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, monedaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monedaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Moneda in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMoneda() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moneda.setId(longCount.incrementAndGet());

        // Create the Moneda
        MonedaDTO monedaDTO = monedaMapper.toDto(moneda);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMonedaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(monedaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Moneda in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMoneda() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moneda.setId(longCount.incrementAndGet());

        // Create the Moneda
        MonedaDTO monedaDTO = monedaMapper.toDto(moneda);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMonedaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(monedaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Moneda in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMonedaWithPatch() throws Exception {
        // Initialize the database
        insertedMoneda = monedaRepository.saveAndFlush(moneda);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the moneda using partial update
        Moneda partialUpdatedMoneda = new Moneda();
        partialUpdatedMoneda.setId(moneda.getId());

        partialUpdatedMoneda.descripcion(UPDATED_DESCRIPCION);

        restMonedaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMoneda.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMoneda))
            )
            .andExpect(status().isOk());

        // Validate the Moneda in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMonedaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedMoneda, moneda), getPersistedMoneda(moneda));
    }

    @Test
    @Transactional
    void fullUpdateMonedaWithPatch() throws Exception {
        // Initialize the database
        insertedMoneda = monedaRepository.saveAndFlush(moneda);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the moneda using partial update
        Moneda partialUpdatedMoneda = new Moneda();
        partialUpdatedMoneda.setId(moneda.getId());

        partialUpdatedMoneda.codigo(UPDATED_CODIGO).descripcion(UPDATED_DESCRIPCION).simbolo(UPDATED_SIMBOLO).activo(UPDATED_ACTIVO);

        restMonedaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMoneda.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMoneda))
            )
            .andExpect(status().isOk());

        // Validate the Moneda in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMonedaUpdatableFieldsEquals(partialUpdatedMoneda, getPersistedMoneda(partialUpdatedMoneda));
    }

    @Test
    @Transactional
    void patchNonExistingMoneda() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moneda.setId(longCount.incrementAndGet());

        // Create the Moneda
        MonedaDTO monedaDTO = monedaMapper.toDto(moneda);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMonedaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, monedaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(monedaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Moneda in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMoneda() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moneda.setId(longCount.incrementAndGet());

        // Create the Moneda
        MonedaDTO monedaDTO = monedaMapper.toDto(moneda);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMonedaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(monedaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Moneda in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMoneda() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        moneda.setId(longCount.incrementAndGet());

        // Create the Moneda
        MonedaDTO monedaDTO = monedaMapper.toDto(moneda);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMonedaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(monedaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Moneda in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMoneda() throws Exception {
        // Initialize the database
        insertedMoneda = monedaRepository.saveAndFlush(moneda);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the moneda
        restMonedaMockMvc
            .perform(delete(ENTITY_API_URL_ID, moneda.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return monedaRepository.count();
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

    protected Moneda getPersistedMoneda(Moneda moneda) {
        return monedaRepository.findById(moneda.getId()).orElseThrow();
    }

    protected void assertPersistedMonedaToMatchAllProperties(Moneda expectedMoneda) {
        assertMonedaAllPropertiesEquals(expectedMoneda, getPersistedMoneda(expectedMoneda));
    }

    protected void assertPersistedMonedaToMatchUpdatableProperties(Moneda expectedMoneda) {
        assertMonedaAllUpdatablePropertiesEquals(expectedMoneda, getPersistedMoneda(expectedMoneda));
    }
}
