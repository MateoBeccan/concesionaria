package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.concesionaria.app.domain.Cliente;
import com.concesionaria.app.domain.Comprobante;
import com.concesionaria.app.domain.CondicionIva;
import com.concesionaria.app.domain.Marca;
import com.concesionaria.app.domain.Modelo;
import com.concesionaria.app.domain.Moneda;
import com.concesionaria.app.domain.TipoComprobante;
import com.concesionaria.app.domain.Vehiculo;
import com.concesionaria.app.domain.Venta;
import com.concesionaria.app.domain.Version;
import com.concesionaria.app.domain.enumeration.EstadoComprobante;
import com.concesionaria.app.domain.enumeration.EstadoVehiculo;
import com.concesionaria.app.repository.ComprobanteRepository;
import com.concesionaria.app.repository.PagoRepository;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PdfComprobanteServiceImplTest {

    @Mock
    private ComprobanteRepository comprobanteRepository;

    @Mock
    private PagoRepository pagoRepository;

    private PdfComprobanteServiceImpl pdfComprobanteService;

    @BeforeEach
    void setUp() {
        pdfComprobanteService = new PdfComprobanteServiceImpl(
            comprobanteRepository,
            pagoRepository,
            "Concesionaria MB",
            "Av. Siempre Viva 1234 - CABA",
            "+54 11 4000-1234",
            "ventas@concesionariamb.com",
            "30-12345678-9",
            "src/main/webapp/content/images/branding/logo.png"
        );
    }

    @Test
    void generaPdfDeComprobanteValido() throws Exception {
        Comprobante comprobante = comprobanteBase(EstadoComprobante.EMITIDO);
        when(comprobanteRepository.findOneForPdf(1L)).thenReturn(Optional.of(comprobante));

        var result = pdfComprobanteService.generarPdfComprobante(1L);

        assertThat(result).isPresent();
        assertThat(result.get().content()).isNotEmpty();
        assertThat(result.get().fileName()).isEqualTo("FAC-000001.pdf");
        String pdfText = extractText(result.get().content());
        assertThat(pdfText).contains("Concesionaria MB");
        assertThat(pdfText).contains("FAC-000001");
        assertThat(pdfText).contains("Documento generado automaticamente");
    }

    @Test
    void comprobanteAnuladoIncluyeWatermarkAnulado() throws Exception {
        Comprobante comprobante = comprobanteBase(EstadoComprobante.ANULADO);
        when(comprobanteRepository.findOneForPdf(2L)).thenReturn(Optional.of(comprobante));

        var result = pdfComprobanteService.generarPdfComprobante(2L);

        assertThat(result).isPresent();
        String pdfText = extractText(result.get().content());
        assertThat(pdfText).contains("ANULADO");
    }

    @Test
    void retornaVacioCuandoNoExisteComprobante() {
        when(comprobanteRepository.findOneForPdf(99L)).thenReturn(Optional.empty());

        var result = pdfComprobanteService.generarPdfComprobante(99L);

        assertThat(result).isEmpty();
    }

    private String extractText(byte[] pdfBytes) throws IOException {
        PdfReader reader = new PdfReader(pdfBytes);
        StringBuilder sb = new StringBuilder();
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        int pages = reader.getNumberOfPages();
        for (int i = 1; i <= pages; i++) {
            sb.append(extractor.getTextFromPage(i)).append('\n');
        }
        reader.close();
        return sb.toString();
    }

    private Comprobante comprobanteBase(EstadoComprobante estado) {
        Moneda moneda = new Moneda();
        moneda.setId(1L);
        moneda.setCodigo("ARS");
        moneda.setSimbolo("$");

        CondicionIva condicionIva = new CondicionIva();
        condicionIva.setCodigo("CF");
        condicionIva.setDescripcion("Consumidor Final");

        Cliente cliente = new Cliente();
        cliente.setNombre("Diego");
        cliente.setApellido("Perez");
        cliente.setNroDocumento("33444555");
        cliente.setTelefono("1122334455");
        cliente.setEmail("diego@cliente.com");
        cliente.setDireccion("Calle 123");
        cliente.setCondicionIva(condicionIva);

        Marca marca = new Marca();
        marca.setNombre("Toyota");
        Modelo modelo = new Modelo();
        modelo.setNombre("Corolla");
        modelo.setMarca(marca);
        Version version = new Version();
        version.setNombre("XEI");
        version.setModelo(modelo);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setVersion(version);
        vehiculo.setEstado(EstadoVehiculo.NUEVO);
        vehiculo.setPatente("AB123CD");
        vehiculo.setVinChasis("8A1ABC12345678901");
        vehiculo.setColor("Gris");
        vehiculo.setFechaFabricacion(LocalDate.of(2024, 1, 1));

        Venta venta = new Venta();
        venta.setId(10L);
        venta.setCliente(cliente);
        venta.setVehiculo(vehiculo);
        venta.setTotalPagado(new BigDecimal("5000000"));
        venta.setSaldo(new BigDecimal("0"));

        TipoComprobante tipoComprobante = new TipoComprobante();
        tipoComprobante.setCodigo("FAC");

        Comprobante comprobante = new Comprobante();
        comprobante.setId(1L);
        comprobante.setNumeroComprobante("FAC-000001");
        comprobante.setFechaEmision(Instant.now());
        comprobante.setEstado(estado);
        comprobante.setImporteNeto(new BigDecimal("4132231.40"));
        comprobante.setImpuesto(new BigDecimal("867768.60"));
        comprobante.setTotal(new BigDecimal("5000000.00"));
        comprobante.setUsuarioEmision("admin");
        comprobante.setMoneda(moneda);
        comprobante.setVenta(venta);
        comprobante.setTipoComprobante(tipoComprobante);
        return comprobante;
    }
}

