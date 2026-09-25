package com.concesionaria.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "comprobante_correlativo")
public class ComprobanteCorrelativo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "tipo_comprobante_id")
    private Long tipoComprobanteId;

    @NotNull
    @Column(name = "ultimo_numero", nullable = false)
    private Long ultimoNumero;

    public Long getTipoComprobanteId() {
        return tipoComprobanteId;
    }

    public void setTipoComprobanteId(Long tipoComprobanteId) {
        this.tipoComprobanteId = tipoComprobanteId;
    }

    public Long getUltimoNumero() {
        return ultimoNumero;
    }

    public void setUltimoNumero(Long ultimoNumero) {
        this.ultimoNumero = ultimoNumero;
    }
}
