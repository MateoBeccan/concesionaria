package com.concesionaria.app.web.rest;

import static com.concesionaria.app.domain.InventarioAsserts.*;
import static com.concesionaria.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Inventario;
import com.concesionaria.app.domain.enumeration.EstadoInventario;
import com.concesionaria.app.repository.InventarioRepository;
import com.concesionaria.app.service.dto.InventarioDTO;
import com.concesionaria.app.service.mapper.InventarioMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link InventarioResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class InventarioResourceIT {

    private static final Instant DEFAULT_FECHA_INGRESO = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_INGRESO = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_UBICACION = "AAAAAAAAAA";
    private static final String UPDATED_UBICACION = "BBBBBBBBBB";

    private static final EstadoInventario DEFAULT_ESTADO_INVENTARIO = EstadoInventario.DISPONIBLE;
    private static final EstadoInventario UPDATED_ESTADO_INVENTARIO = EstadoInventario.RESERVADO;

    private static final String DEFAULT_OBSERVACIONES = "AAAAAAAAAA";
    private static final String UPDATED_OBSERVACIONES = "BBBBBBBBBB";

    private static final Boolean DEFAULT_DISPONIBLE = false;
    private static final Boolean UPDATED_DISPONIBLE = true;

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_LAST_MODIFIED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_MODIFIED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_FECHA_RESERVA = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_RESERVA = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_FECHA_VENCIMIENTO_RESERVA = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_FECHA_VENCIMIENTO_RESERVA = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/inventarios";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private InventarioMapper inventarioMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restInventarioMockMvc;

    private Inventario inventario;

    private Inventario insertedInventario;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Inventario createEntity() {
        return new Inventario()
            .fechaIngreso(DEFAULT_FECHA_INGRESO)
            .ubicacion(DEFAULT_UBICACION)
            .estadoInventario(DEFAULT_ESTADO_INVENTARIO)
            .observaciones(DEFAULT_OBSERVACIONES)
            .disponible(DEFAULT_DISPONIBLE)
            .createdDate(DEFAULT_CREATED_DATE)
            .lastModifiedDate(DEFAULT_LAST_MODIFIED_DATE)
            .fechaReserva(DEFAULT_FECHA_RESERVA)
            .fechaVencimientoReserva(DEFAULT_FECHA_VENCIMIENTO_RESERVA);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Inventario createUpdatedEntity() {
        return new Inventario()
            .fechaIngreso(UPDATED_FECHA_INGRESO)
            .ubicacion(UPDATED_UBICACION)
            .estadoInventario(UPDATED_ESTADO_INVENTARIO)
            .observaciones(UPDATED_OBSERVACIONES)
            .disponible(UPDATED_DISPONIBLE)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE)
            .fechaReserva(UPDATED_FECHA_RESERVA)
            .fechaVencimientoReserva(UPDATED_FECHA_VENCIMIENTO_RESERVA);
    }

    @BeforeEach
    void initTest() {
        inventario = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedInventario != null) {
            inventarioRepository.delete(insertedInventario);
            insertedInventario = null;
        }
    }

    @Test
    @Transactional
    void createInventario() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Inventario
        InventarioDTO inventarioDTO = inventarioMapper.toDto(inventario);
        var returnedInventarioDTO = om.readValue(
            restInventarioMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(inventarioDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            InventarioDTO.class
        );

        // Validate the Inventario in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedInventario = inventarioMapper.toEntity(returnedInventarioDTO);
        assertInventarioUpdatableFieldsEquals(returnedInventario, getPersistedInventario(returnedInventario));

        insertedInventario = returnedInventario;
    }

    @Test
    @Transactional
    void createInventarioWithExistingId() throws Exception {
        // Create the Inventario with an existing ID
        inventario.setId(1L);
        InventarioDTO inventarioDTO = inventarioMapper.toDto(inventario);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restInventarioMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(inventarioDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Inventario in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkFechaIngresoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        inventario.setFechaIngreso(null);

        // Create the Inventario, which fails.
        InventarioDTO inventarioDTO = inventarioMapper.toDto(inventario);

        restInventarioMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(inventarioDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEstadoInventarioIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        inventario.setEstadoInventario(null);

        // Create the Inventario, which fails.
        InventarioDTO inventarioDTO = inventarioMapper.toDto(inventario);

        restInventarioMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(inventarioDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDisponibleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        inventario.setDisponible(null);

        // Create the Inventario, which fails.
        InventarioDTO inventarioDTO = inventarioMapper.toDto(inventario);

        restInventarioMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(inventarioDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllInventarios() throws Exception {
        // Initialize the database
        insertedInventario = inventarioRepository.saveAndFlush(inventario);

        // Get all the inventarioList
        restInventarioMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(inventario.getId().intValue())))
            .andExpect(jsonPath("$.[*].fechaIngreso").value(hasItem(DEFAULT_FECHA_INGRESO.toString())))
            .andExpect(jsonPath("$.[*].ubicacion").value(hasItem(DEFAULT_UBICACION)))
            .andExpect(jsonPath("$.[*].estadoInventario").value(hasItem(DEFAULT_ESTADO_INVENTARIO.toString())))
            .andExpect(jsonPath("$.[*].observaciones").value(hasItem(DEFAULT_OBSERVACIONES)))
            .andExpect(jsonPath("$.[*].disponible").value(hasItem(DEFAULT_DISPONIBLE)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(DEFAULT_LAST_MODIFIED_DATE.toString())))
            .andExpect(jsonPath("$.[*].fechaReserva").value(hasItem(DEFAULT_FECHA_RESERVA.toString())))
            .andExpect(jsonPath("$.[*].fechaVencimientoReserva").value(hasItem(DEFAULT_FECHA_VENCIMIENTO_RESERVA.toString())));
    }

    @Test
    @Transactional
    void getInventario() throws Exception {
        // Initialize the database
        insertedInventario = inventarioRepository.saveAndFlush(inventario);

        // Get the inventario
        restInventarioMockMvc
            .perform(get(ENTITY_API_URL_ID, inventario.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(inventario.getId().intValue()))
            .andExpect(jsonPath("$.fechaIngreso").value(DEFAULT_FECHA_INGRESO.toString()))
            .andExpect(jsonPath("$.ubicacion").value(DEFAULT_UBICACION))
            .andExpect(jsonPath("$.estadoInventario").value(DEFAULT_ESTADO_INVENTARIO.toString()))
            .andExpect(jsonPath("$.observaciones").value(DEFAULT_OBSERVACIONES))
            .andExpect(jsonPath("$.disponible").value(DEFAULT_DISPONIBLE))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.lastModifiedDate").value(DEFAULT_LAST_MODIFIED_DATE.toString()))
            .andExpect(jsonPath("$.fechaReserva").value(DEFAULT_FECHA_RESERVA.toString()))
            .andExpect(jsonPath("$.fechaVencimientoReserva").value(DEFAULT_FECHA_VENCIMIENTO_RESERVA.toString()));
    }

    @Test
    @Transactional
    void getNonExistingInventario() throws Exception {
        // Get the inventario
        restInventarioMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingInventario() throws Exception {
        // Initialize the database
        insertedInventario = inventarioRepository.saveAndFlush(inventario);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the inventario
        Inventario updatedInventario = inventarioRepository.findById(inventario.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedInventario are not directly saved in db
        em.detach(updatedInventario);
        updatedInventario
            .fechaIngreso(UPDATED_FECHA_INGRESO)
            .ubicacion(UPDATED_UBICACION)
            .estadoInventario(UPDATED_ESTADO_INVENTARIO)
            .observaciones(UPDATED_OBSERVACIONES)
            .disponible(UPDATED_DISPONIBLE)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE)
            .fechaReserva(UPDATED_FECHA_RESERVA)
            .fechaVencimientoReserva(UPDATED_FECHA_VENCIMIENTO_RESERVA);
        InventarioDTO inventarioDTO = inventarioMapper.toDto(updatedInventario);

        restInventarioMockMvc
            .perform(
                put(ENTITY_API_URL_ID, inventarioDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(inventarioDTO))
            )
            .andExpect(status().isOk());

        // Validate the Inventario in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedInventarioToMatchAllProperties(updatedInventario);
    }

    @Test
    @Transactional
    void putNonExistingInventario() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inventario.setId(longCount.incrementAndGet());

        // Create the Inventario
        InventarioDTO inventarioDTO = inventarioMapper.toDto(inventario);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInventarioMockMvc
            .perform(
                put(ENTITY_API_URL_ID, inventarioDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(inventarioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Inventario in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchInventario() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inventario.setId(longCount.incrementAndGet());

        // Create the Inventario
        InventarioDTO inventarioDTO = inventarioMapper.toDto(inventario);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInventarioMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(inventarioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Inventario in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamInventario() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inventario.setId(longCount.incrementAndGet());

        // Create the Inventario
        InventarioDTO inventarioDTO = inventarioMapper.toDto(inventario);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInventarioMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(inventarioDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Inventario in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateInventarioWithPatch() throws Exception {
        // Initialize the database
        insertedInventario = inventarioRepository.saveAndFlush(inventario);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the inventario using partial update
        Inventario partialUpdatedInventario = new Inventario();
        partialUpdatedInventario.setId(inventario.getId());

        partialUpdatedInventario
            .fechaIngreso(UPDATED_FECHA_INGRESO)
            .observaciones(UPDATED_OBSERVACIONES)
            .disponible(UPDATED_DISPONIBLE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE)
            .fechaReserva(UPDATED_FECHA_RESERVA)
            .fechaVencimientoReserva(UPDATED_FECHA_VENCIMIENTO_RESERVA);

        restInventarioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInventario.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedInventario))
            )
            .andExpect(status().isOk());

        // Validate the Inventario in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertInventarioUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedInventario, inventario),
            getPersistedInventario(inventario)
        );
    }

    @Test
    @Transactional
    void fullUpdateInventarioWithPatch() throws Exception {
        // Initialize the database
        insertedInventario = inventarioRepository.saveAndFlush(inventario);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the inventario using partial update
        Inventario partialUpdatedInventario = new Inventario();
        partialUpdatedInventario.setId(inventario.getId());

        partialUpdatedInventario
            .fechaIngreso(UPDATED_FECHA_INGRESO)
            .ubicacion(UPDATED_UBICACION)
            .estadoInventario(UPDATED_ESTADO_INVENTARIO)
            .observaciones(UPDATED_OBSERVACIONES)
            .disponible(UPDATED_DISPONIBLE)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE)
            .fechaReserva(UPDATED_FECHA_RESERVA)
            .fechaVencimientoReserva(UPDATED_FECHA_VENCIMIENTO_RESERVA);

        restInventarioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInventario.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedInventario))
            )
            .andExpect(status().isOk());

        // Validate the Inventario in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertInventarioUpdatableFieldsEquals(partialUpdatedInventario, getPersistedInventario(partialUpdatedInventario));
    }

    @Test
    @Transactional
    void patchNonExistingInventario() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inventario.setId(longCount.incrementAndGet());

        // Create the Inventario
        InventarioDTO inventarioDTO = inventarioMapper.toDto(inventario);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInventarioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, inventarioDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(inventarioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Inventario in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchInventario() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inventario.setId(longCount.incrementAndGet());

        // Create the Inventario
        InventarioDTO inventarioDTO = inventarioMapper.toDto(inventario);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInventarioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(inventarioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Inventario in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamInventario() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        inventario.setId(longCount.incrementAndGet());

        // Create the Inventario
        InventarioDTO inventarioDTO = inventarioMapper.toDto(inventario);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInventarioMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(inventarioDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Inventario in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteInventario() throws Exception {
        // Initialize the database
        insertedInventario = inventarioRepository.saveAndFlush(inventario);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the inventario
        restInventarioMockMvc
            .perform(delete(ENTITY_API_URL_ID, inventario.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return inventarioRepository.count();
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

    protected Inventario getPersistedInventario(Inventario inventario) {
        return inventarioRepository.findById(inventario.getId()).orElseThrow();
    }

    protected void assertPersistedInventarioToMatchAllProperties(Inventario expectedInventario) {
        assertInventarioAllPropertiesEquals(expectedInventario, getPersistedInventario(expectedInventario));
    }

    protected void assertPersistedInventarioToMatchUpdatableProperties(Inventario expectedInventario) {
        assertInventarioAllUpdatablePropertiesEquals(expectedInventario, getPersistedInventario(expectedInventario));
    }
}
