package com.concesionaria.app.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.repository.MarcaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class AdminManagedApiSecurityIT {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private MarcaRepository marcaRepository;

    private Long createdMarcaId;

    @AfterEach
    void cleanup() {
        if (createdMarcaId != null) {
            marcaRepository.deleteById(createdMarcaId);
            createdMarcaId = null;
        }
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void userCannotWriteAdminManagedCatalogs() throws Exception {
        Marca marca = new Marca().nombre("Marca restringida");

        restMockMvc
            .perform(post("/api/marcas").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marca)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void adminCanWriteAdminManagedCatalogs() throws Exception {
        Marca marca = new Marca().nombre("Marca permitida admin");

        String response = restMockMvc
            .perform(post("/api/marcas").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marca)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        createdMarcaId = om.readValue(response, Marca.class).getId();
    }
}
