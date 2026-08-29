package com.concesionaria.app.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.concesionaria.app.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class VentaSecurityIT {

    @Autowired
    private MockMvc restMockMvc;

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void userCannotDeleteVentas() throws Exception {
        restMockMvc.perform(delete("/api/ventas/{id}", 1L)).andExpect(status().isForbidden());
    }
}
