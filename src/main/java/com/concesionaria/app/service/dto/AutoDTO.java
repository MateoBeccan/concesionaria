package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.CondicionAuto;
import com.concesionaria.app.domain.enumeration.EstadoAuto;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.concesionaria.app.domain.Auto} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AutoDTO implements Serializable {

    private Long id;

    @NotNull
    private EstadoAuto estado;

    @NotNull
    private CondicionAuto condicion;

    private LocalDate fechaFabricacion;

    private Integer km;

    private String patente;

    private BigDecimal precio;

    private ConfiguracionAutoDTO configuracion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EstadoAuto getEstado() {
        return estado;
    }

    public void setEstado(EstadoAuto estado) {
        this.estado = estado;
    }

    public CondicionAuto getCondicion() {
        return condicion;
    }

    public void setCondicion(CondicionAuto condicion) {
        this.condicion = condicion;
    }

    public LocalDate getFechaFabricacion() {
        return fechaFabricacion;
    }

    public void setFechaFabricacion(LocalDate fechaFabricacion) {
        this.fechaFabricacion = fechaFabricacion;
    }

    public Integer getKm() {
        return km;
    }

    public void setKm(Integer km) {
        this.km = km;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public ConfiguracionAutoDTO getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(ConfiguracionAutoDTO configuracion) {
        this.configuracion = configuracion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AutoDTO)) {
            return false;
        }

        AutoDTO autoDTO = (AutoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, autoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AutoDTO{" +
            "id=" + getId() +
            ", estado='" + getEstado() + "'" +
            ", condicion='" + getCondicion() + "'" +
            ", fechaFabricacion='" + getFechaFabricacion() + "'" +
            ", km=" + getKm() +
            ", patente='" + getPatente() + "'" +
            ", precio=" + getPrecio() +
            ", configuracion=" + getConfiguracion() +
            "}";
    }
}
