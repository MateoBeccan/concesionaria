package com.concesionaria.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.concesionaria.app.domain.MetodoPago;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetodoPagoPolicyTest {

    private MetodoPagoPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new MetodoPagoPolicy(null, new PagoTextNormalizer());
    }

    @Test
    void contadoEsMonetarioImpactaCajaYEmiteRecibo() {
        MetodoPago contado = metodo("CONTADO");

        assertThat(policy.requiereEntidadFinanciera(contado)).isFalse();
        assertThat(policy.esMonetario(contado)).isTrue();
        assertThat(policy.impactaCaja(contado)).isTrue();
        assertThat(policy.generaMovimientoInformativo(contado)).isFalse();
        assertThat(policy.permiteComprobanteAutomatico(contado)).isTrue();
        assertThat(policy.resolverTipoComprobantePago(contado)).isEqualTo("REC");
    }

    @Test
    void mediosBancarizadosRequierenEntidadFinancieraYSonMonetarios() {
        List.of("TRANSFERENCIA", "DEPOSITO", "CHEQUE", "TARJETA").forEach(codigo -> {
            MetodoPago metodoPago = metodo(codigo);

            assertThat(policy.requiereEntidadFinanciera(metodoPago)).as(codigo).isTrue();
            assertThat(policy.esMonetario(metodoPago)).as(codigo).isTrue();
            assertThat(policy.generaMovimientoInformativo(metodoPago)).as(codigo).isFalse();
            assertThat(policy.permiteComprobanteAutomatico(metodoPago)).as(codigo).isTrue();
            assertThat(policy.resolverTipoComprobantePago(metodoPago)).as(codigo).isEqualTo("REC");
        });
    }

    @Test
    void entregaUsadoEsNoMonetarioInformativoYPuedeEmitirReciboEnVenta() {
        MetodoPago entregaUsado = metodo("ENTREGA_USADO");

        assertThat(policy.esEntregaUsado(entregaUsado)).isTrue();
        assertThat(policy.esPlanAhorro(entregaUsado)).isFalse();
        assertThat(policy.esMonetario(entregaUsado)).isFalse();
        assertThat(policy.generaMovimientoInformativo(entregaUsado)).isTrue();
        assertThat(policy.permiteComprobanteAutomatico(entregaUsado)).isTrue();
        assertThat(policy.resolverTipoComprobantePago(entregaUsado)).isEqualTo("REC");
    }

    @Test
    void planAhorroEsNoMonetarioInformativoYNoEmiteComprobanteNormal() {
        MetodoPago planAhorro = metodo("PLAN_AHORRO");

        assertThat(policy.esPlanAhorro(planAhorro)).isTrue();
        assertThat(policy.esEntregaUsado(planAhorro)).isFalse();
        assertThat(policy.esMonetario(planAhorro)).isFalse();
        assertThat(policy.generaMovimientoInformativo(planAhorro)).isTrue();
        assertThat(policy.permiteComprobanteAutomatico(planAhorro)).isFalse();
        assertThat(policy.resolverTipoComprobantePago(planAhorro)).isNull();
    }

    @Test
    void seniaEsMonetariaYUsaTipoComprobanteSen() {
        MetodoPago senia = metodo("SENIA");

        assertThat(policy.requiereEntidadFinanciera(senia)).isFalse();
        assertThat(policy.esMonetario(senia)).isTrue();
        assertThat(policy.generaMovimientoInformativo(senia)).isFalse();
        assertThat(policy.permiteComprobanteAutomatico(senia)).isTrue();
        assertThat(policy.resolverTipoComprobantePago(senia)).isEqualTo("SEN");
    }

    private MetodoPago metodo(String codigo) {
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setCodigo(codigo);
        return metodoPago;
    }
}
