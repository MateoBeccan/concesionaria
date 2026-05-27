package com.concesionaria.app.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.negocio")
public class BusinessProperties {

    @NotBlank
    private String monedaBaseCodigo = "ARS";

    @Valid
    private final Impuesto impuesto = new Impuesto();

    @Valid
    private final Reserva reserva = new Reserva();

    @Valid
    private final Venta venta = new Venta();

    @Valid
    private final Pagos pagos = new Pagos();

    public String getMonedaBaseCodigo() {
        return monedaBaseCodigo;
    }

    public void setMonedaBaseCodigo(String monedaBaseCodigo) {
        this.monedaBaseCodigo = monedaBaseCodigo;
    }

    public Impuesto getImpuesto() {
        return impuesto;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public Venta getVenta() {
        return venta;
    }

    public Pagos getPagos() {
        return pagos;
    }

    public static BusinessProperties defaults() {
        return new BusinessProperties();
    }

    public static class Impuesto {

        @DecimalMin("0.00")
        private BigDecimal porcentaje = new BigDecimal("21.00");

        public BigDecimal getPorcentaje() {
            return porcentaje;
        }

        public void setPorcentaje(BigDecimal porcentaje) {
            this.porcentaje = porcentaje;
        }
    }

    public static class Reserva {

        @DecimalMin("0.00")
        @DecimalMax("1.00")
        private BigDecimal porcentajeMinimo = new BigDecimal("0.10");

        @Min(0)
        private int diasVencimiento = 30;

        public BigDecimal getPorcentajeMinimo() {
            return porcentajeMinimo;
        }

        public void setPorcentajeMinimo(BigDecimal porcentajeMinimo) {
            this.porcentajeMinimo = porcentajeMinimo;
        }

        public int getDiasVencimiento() {
            return diasVencimiento;
        }

        public void setDiasVencimiento(int diasVencimiento) {
            this.diasVencimiento = diasVencimiento;
        }
    }

    public static class Venta {

        @DecimalMin("0.00")
        @DecimalMax("1.00")
        private BigDecimal anticipoMinimo = new BigDecimal("0.10");

        public BigDecimal getAnticipoMinimo() {
            return anticipoMinimo;
        }

        public void setAnticipoMinimo(BigDecimal anticipoMinimo) {
            this.anticipoMinimo = anticipoMinimo;
        }
    }

    public static class Pagos {

        @Min(0)
        private int escalaMonetaria = 2;

        @Min(0)
        private int escalaCotizacion = 8;

        public int getEscalaMonetaria() {
            return escalaMonetaria;
        }

        public void setEscalaMonetaria(int escalaMonetaria) {
            this.escalaMonetaria = escalaMonetaria;
        }

        public int getEscalaCotizacion() {
            return escalaCotizacion;
        }

        public void setEscalaCotizacion(int escalaCotizacion) {
            this.escalaCotizacion = escalaCotizacion;
        }
    }
}
