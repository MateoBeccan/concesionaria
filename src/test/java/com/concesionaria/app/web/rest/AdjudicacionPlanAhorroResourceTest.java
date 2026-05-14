package com.concesionaria.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.concesionaria.app.service.AdjudicacionPlanAhorroService;
import com.concesionaria.app.service.dto.AdjudicacionPlanAhorroDTO;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AdjudicacionPlanAhorroResourceTest {

    @Mock
    private AdjudicacionPlanAhorroService adjudicacionService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAllComoAdminUsaFindAll() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest("GET", "/api/adjudicaciones-plan-ahorro")));
        AdjudicacionPlanAhorroResource resource = new AdjudicacionPlanAhorroResource(adjudicacionService);
        var pageable = PageRequest.of(0, 20);
        var dto = new AdjudicacionPlanAhorroDTO();
        dto.setId(1L);
        when(adjudicacionService.findAll(pageable)).thenReturn(new PageImpl<>(List.of(dto), pageable, 1));

        var response = resource.getAll(pageable);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);
        verify(adjudicacionService).findAll(pageable);
    }

    @Test
    void getAllComoUserUsaFindAllCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", "n/a", "ROLE_USER"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest("GET", "/api/adjudicaciones-plan-ahorro")));
        AdjudicacionPlanAhorroResource resource = new AdjudicacionPlanAhorroResource(adjudicacionService);
        var pageable = PageRequest.of(0, 20);
        var dto = new AdjudicacionPlanAhorroDTO();
        dto.setId(2L);
        when(adjudicacionService.findAllCurrentUser(pageable)).thenReturn(new PageImpl<>(List.of(dto), pageable, 1));

        var response = resource.getAll(pageable);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);
        verify(adjudicacionService).findAllCurrentUser(pageable);
    }
}
