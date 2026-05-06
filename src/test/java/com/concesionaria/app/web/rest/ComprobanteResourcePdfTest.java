package com.concesionaria.app.web.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.service.ComprobanteService;
import com.concesionaria.app.service.PdfComprobanteService;
import com.concesionaria.app.service.dto.ComprobantePdfResult;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ComprobanteResourcePdfTest {

    private PdfComprobanteService pdfComprobanteService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ComprobanteService comprobanteService = mock(ComprobanteService.class);
        ComprobanteRepository comprobanteRepository = mock(ComprobanteRepository.class);
        pdfComprobanteService = mock(PdfComprobanteService.class);

        ComprobanteResource resource = new ComprobanteResource(comprobanteService, comprobanteRepository, pdfComprobanteService);
        mockMvc = MockMvcBuilders.standaloneSetup(resource).build();
    }

    @Test
    void endpointPdfDevuelveContenidoYHeadersCorrectos() throws Exception {
        byte[] bytes = "PDF-DATA".getBytes(StandardCharsets.UTF_8);
        when(pdfComprobanteService.generarPdfComprobante(1L)).thenReturn(Optional.of(new ComprobantePdfResult(bytes, "FAC-000001.pdf")));

        mockMvc
            .perform(get("/api/comprobantes/1/pdf"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/pdf"))
            .andExpect(header().string("Content-Disposition", "inline; filename=\"FAC-000001.pdf\""))
            .andExpect(content().bytes(bytes));
    }

    @Test
    void endpointPdfRetorna404CuandoNoExiste() throws Exception {
        when(pdfComprobanteService.generarPdfComprobante(404L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/comprobantes/404/pdf")).andExpect(status().isNotFound());
    }
}

