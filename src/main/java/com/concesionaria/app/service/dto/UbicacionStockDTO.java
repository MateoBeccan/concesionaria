package com.concesionaria.app.service.dto;

import com.concesionaria.app.domain.enumeration.TipoUbicacionStock;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

public class UbicacionStockDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 30)
    private String codigo;

    @NotNull
    @Size(max = 100)
    private String nombre;

    @NotNull
    private TipoUbicacionStock tipoUbicacion;

    @Size(max = 255)
    private String direccion;

    @Size(max = 500)
    private String descripcion;

    @NotNull
    private Boolean activa;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoUbicacionStock getTipoUbicacion() {
        return tipoUbicacion;
    }

    public void setTipoUbicacion(TipoUbicacionStock tipoUbicacion) {
        this.tipoUbicacion = tipoUbicacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UbicacionStockDTO that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
