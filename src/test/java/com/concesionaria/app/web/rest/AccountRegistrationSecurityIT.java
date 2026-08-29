package com.concesionaria.app.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.concesionaria.app.IntegrationTest;
import com.concesionaria.app.web.rest.vm.ManagedUserVM;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class AccountRegistrationSecurityIT {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc restAccountMockMvc;

    @Test
    void registerIsNotPublicByDefault() throws Exception {
        ManagedUserVM user = new ManagedUserVM();
        user.setLogin("public-registration-disabled");
        user.setPassword("password");
        user.setFirstName("Public");
        user.setLastName("Disabled");
        user.setEmail("public-registration-disabled@localhost");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(user)))
            .andExpect(status().isUnauthorized());
    }
}
