package com.concesionaria.app.domain;

import com.concesionaria.app.domain.enumeration.TipoUbicacionStock;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "ubicacion_stock")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class UbicacionStock implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 30)
    @Column(name = "codigo", nullable = false, length = 30, unique = true)
    private String codigo;

    @NotNull
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ubicacion", nullable = false)
    private TipoUbicacionStock tipoUbicacion;

    @Size(max = 255)
    @Column(name = "direccion", length = 255)
    private String direccion;

    @Size(max = 500)
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull
    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

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
}
