package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.CarroceriaAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Carroceria;
import com.concesionaria.app.repository.CarroceriaRepository;
import com.concesionaria.app.service.dto.CarroceriaDTO;
import com.concesionaria.app.service.mapper.CarroceriaMapper;
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
 * Integration tests for the {@link CarroceriaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CarroceriaResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/carrocerias";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CarroceriaRepository carroceriaRepository;

    @Autowired
    private CarroceriaMapper carroceriaMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCarroceriaMockMvc;

    private Carroceria carroceria;

    private Carroceria insertedCarroceria;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Carroceria createEntity() {
        return new Carroceria().nombre(DEFAULT_NOMBRE).descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Carroceria createUpdatedEntity() {
        return new Carroceria().nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        carroceria = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCarroceria != null) {
            carroceriaRepository.delete(insertedCarroceria);
            insertedCarroceria = null;
        }
    }

    @Test
    @Transactional
    void createCarroceria() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Carroceria
        CarroceriaDTO carroceriaDTO = carroceriaMapper.toDto(carroceria);
        var returnedCarroceriaDTO = om.readValue(
            restCarroceriaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(carroceriaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CarroceriaDTO.class
        );

        // Validate the Carroceria in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCarroceria = carroceriaMapper.toEntity(returnedCarroceriaDTO);
        assertCarroceriaUpdatableFieldsEquals(returnedCarroceria, getPersistedCarroceria(returnedCarroceria));

        insertedCarroceria = returnedCarroceria;
    }

    @Test
    @Transactional
    void createCarroceriaWithExistingId() throws Exception {
        // Create the Carroceria with an existing ID
        carroceria.setId(1L);
        CarroceriaDTO carroceriaDTO = carroceriaMapper.toDto(carroceria);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCarroceriaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(carroceriaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Carroceria in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        carroceria.setNombre(null);

        // Create the Carroceria, which fails.
        CarroceriaDTO carroceriaDTO = carroceriaMapper.toDto(carroceria);

        restCarroceriaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(carroceriaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCarrocerias() throws Exception {
        // Initialize the database
        insertedCarroceria = carroceriaRepository.saveAndFlush(carroceria);

        // Get all the carroceriaList
        restCarroceriaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(carroceria.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @Test
    @Transactional
    void getCarroceria() throws Exception {
        // Initialize the database
        insertedCarroceria = carroceriaRepository.saveAndFlush(carroceria);

        // Get the carroceria
        restCarroceriaMockMvc
            .perform(get(ENTITY_API_URL_ID, carroceria.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(carroceria.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getNonExistingCarroceria() throws Exception {
        // Get the carroceria
        restCarroceriaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCarroceria() throws Exception {
        // Initialize the database
        insertedCarroceria = carroceriaRepository.saveAndFlush(carroceria);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the carroceria
        Carroceria updatedCarroceria = carroceriaRepository.findById(carroceria.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCarroceria are not directly saved in db
        em.detach(updatedCarroceria);
        updatedCarroceria.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
        CarroceriaDTO carroceriaDTO = carroceriaMapper.toDto(updatedCarroceria);

        restCarroceriaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, carroceriaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(carroceriaDTO))
            )
            .andExpect(status().isOk());

        // Validate the Carroceria in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCarroceriaToMatchAllProperties(updatedCarroceria);
    }

    @Test
    @Transactional
    void putNonExistingCarroceria() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        carroceria.setId(longCount.incrementAndGet());

        // Create the Carroceria
        CarroceriaDTO carroceriaDTO = carroceriaMapper.toDto(carroceria);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarroceriaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, carroceriaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(carroceriaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Carroceria in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCarroceria() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        carroceria.setId(longCount.incrementAndGet());

        // Create the Carroceria
        CarroceriaDTO carroceriaDTO = carroceriaMapper.toDto(carroceria);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarroceriaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(carroceriaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Carroceria in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCarroceria() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        carroceria.setId(longCount.incrementAndGet());

        // Create the Carroceria
        CarroceriaDTO carroceriaDTO = carroceriaMapper.toDto(carroceria);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarroceriaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(carroceriaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Carroceria in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCarroceriaWithPatch() throws Exception {
        // Initialize the database
        insertedCarroceria = carroceriaRepository.saveAndFlush(carroceria);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the carroceria using partial update
        Carroceria partialUpdatedCarroceria = new Carroceria();
        partialUpdatedCarroceria.setId(carroceria.getId());

        partialUpdatedCarroceria.descripcion(UPDATED_DESCRIPCION);

        restCarroceriaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCarroceria.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCarroceria))
            )
            .andExpect(status().isOk());

        // Validate the Carroceria in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCarroceriaUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCarroceria, carroceria),
            getPersistedCarroceria(carroceria)
        );
    }

    @Test
    @Transactional
    void fullUpdateCarroceriaWithPatch() throws Exception {
        // Initialize the database
        insertedCarroceria = carroceriaRepository.saveAndFlush(carroceria);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the carroceria using partial update
        Carroceria partialUpdatedCarroceria = new Carroceria();
        partialUpdatedCarroceria.setId(carroceria.getId());

        partialUpdatedCarroceria.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);

        restCarroceriaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCarroceria.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCarroceria))
            )
            .andExpect(status().isOk());

        // Validate the Carroceria in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCarroceriaUpdatableFieldsEquals(partialUpdatedCarroceria, getPersistedCarroceria(partialUpdatedCarroceria));
    }

    @Test
    @Transactional
    void patchNonExistingCarroceria() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        carroceria.setId(longCount.incrementAndGet());

        // Create the Carroceria
        CarroceriaDTO carroceriaDTO = carroceriaMapper.toDto(carroceria);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarroceriaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, carroceriaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(carroceriaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Carroceria in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCarroceria() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        carroceria.setId(longCount.incrementAndGet());

        // Create the Carroceria
        CarroceriaDTO carroceriaDTO = carroceriaMapper.toDto(carroceria);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarroceriaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(carroceriaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Carroceria in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCarroceria() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        carroceria.setId(longCount.incrementAndGet());

        // Create the Carroceria
        CarroceriaDTO carroceriaDTO = carroceriaMapper.toDto(carroceria);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCarroceriaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(carroceriaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Carroceria in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCarroceria() throws Exception {
        // Initialize the database
        insertedCarroceria = carroceriaRepository.saveAndFlush(carroceria);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the carroceria
        restCarroceriaMockMvc
            .perform(delete(ENTITY_API_URL_ID, carroceria.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return carroceriaRepository.count();
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

    protected Carroceria getPersistedCarroceria(Carroceria carroceria) {
        return carroceriaRepository.findById(carroceria.getId()).orElseThrow();
    }

    protected void assertPersistedCarroceriaToMatchAllProperties(Carroceria expectedCarroceria) {
        assertCarroceriaAllPropertiesEquals(expectedCarroceria, getPersistedCarroceria(expectedCarroceria));
    }

    protected void assertPersistedCarroceriaToMatchUpdatableProperties(Carroceria expectedCarroceria) {
        assertCarroceriaAllUpdatablePropertiesEquals(expectedCarroceria, getPersistedCarroceria(expectedCarroceria));
    }
}
