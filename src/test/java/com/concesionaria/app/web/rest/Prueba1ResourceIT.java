package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.Prueba1Asserts.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Prueba1;
import com.concesionaria.app.repository.Prueba1Repository;
import com.concesionaria.app.service.dto.Prueba1DTO;
import com.concesionaria.app.service.mapper.Prueba1Mapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link Prueba1Resource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class Prueba1ResourceIT {

    private static final String ENTITY_API_URL = "/api/prueba-1-s";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Prueba1Repository prueba1Repository;

    @Autowired
    private Prueba1Mapper prueba1Mapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPrueba1MockMvc;

    private Prueba1 prueba1;

    private Prueba1 insertedPrueba1;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prueba1 createEntity() {
        return new Prueba1();
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prueba1 createUpdatedEntity() {
        return new Prueba1();
    }

    @BeforeEach
    void initTest() {
        prueba1 = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPrueba1 != null) {
            prueba1Repository.delete(insertedPrueba1);
            insertedPrueba1 = null;
        }
    }

    @Test
    @Transactional
    void createPrueba1() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Prueba1
        Prueba1DTO prueba1DTO = prueba1Mapper.toDto(prueba1);
        var returnedPrueba1DTO = om.readValue(
            restPrueba1MockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prueba1DTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Prueba1DTO.class
        );

        // Validate the Prueba1 in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPrueba1 = prueba1Mapper.toEntity(returnedPrueba1DTO);
        assertPrueba1UpdatableFieldsEquals(returnedPrueba1, getPersistedPrueba1(returnedPrueba1));

        insertedPrueba1 = returnedPrueba1;
    }

    @Test
    @Transactional
    void createPrueba1WithExistingId() throws Exception {
        // Create the Prueba1 with an existing ID
        prueba1.setId(1L);
        Prueba1DTO prueba1DTO = prueba1Mapper.toDto(prueba1);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPrueba1MockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prueba1DTO)))
            .andExpect(status().isBadRequest());

        // Validate the Prueba1 in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllPrueba1s() throws Exception {
        // Initialize the database
        insertedPrueba1 = prueba1Repository.saveAndFlush(prueba1);

        // Get all the prueba1List
        restPrueba1MockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prueba1.getId().intValue())));
    }

    @Test
    @Transactional
    void getPrueba1() throws Exception {
        // Initialize the database
        insertedPrueba1 = prueba1Repository.saveAndFlush(prueba1);

        // Get the prueba1
        restPrueba1MockMvc
            .perform(get(ENTITY_API_URL_ID, prueba1.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(prueba1.getId().intValue()));
    }

    @Test
    @Transactional
    void getPrueba1sByIdFiltering() throws Exception {
        // Initialize the database
        insertedPrueba1 = prueba1Repository.saveAndFlush(prueba1);

        Long id = prueba1.getId();

        defaultPrueba1Filtering("id.equals=" + id, "id.notEquals=" + id);

        defaultPrueba1Filtering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultPrueba1Filtering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    private void defaultPrueba1Filtering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultPrueba1ShouldBeFound(shouldBeFound);
        defaultPrueba1ShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPrueba1ShouldBeFound(String filter) throws Exception {
        restPrueba1MockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prueba1.getId().intValue())));

        // Check, that the count call also returns 1
        restPrueba1MockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPrueba1ShouldNotBeFound(String filter) throws Exception {
        restPrueba1MockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPrueba1MockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingPrueba1() throws Exception {
        // Get the prueba1
        restPrueba1MockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void deletePrueba1() throws Exception {
        // Initialize the database
        insertedPrueba1 = prueba1Repository.saveAndFlush(prueba1);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the prueba1
        restPrueba1MockMvc
            .perform(delete(ENTITY_API_URL_ID, prueba1.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return prueba1Repository.count();
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

    protected Prueba1 getPersistedPrueba1(Prueba1 prueba1) {
        return prueba1Repository.findById(prueba1.getId()).orElseThrow();
    }

    protected void assertPersistedPrueba1ToMatchAllProperties(Prueba1 expectedPrueba1) {
        assertPrueba1AllPropertiesEquals(expectedPrueba1, getPersistedPrueba1(expectedPrueba1));
    }

    protected void assertPersistedPrueba1ToMatchUpdatableProperties(Prueba1 expectedPrueba1) {
        assertPrueba1AllUpdatablePropertiesEquals(expectedPrueba1, getPersistedPrueba1(expectedPrueba1));
    }
}
