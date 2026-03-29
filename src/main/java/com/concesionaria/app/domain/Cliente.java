package com.concesionaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Cliente.
 */
@Entity
@Table(name = "cliente")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Cliente implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @NotNull
    @Size(min = 2, max = 100)
    @Column(name = "apellido", length = 100, nullable = false)
    private String apellido;

    @NotNull
    @Pattern(regexp = "^[0-9]{7,11}$")
    @Column(name = "nro_documento", nullable = false)
    private String nroDocumento;

    @Pattern(regexp = "^[0-9+\\-\\s]{6,20}$")
    @Column(name = "telefono")
    private String telefono;

    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 255)
    @Column(name = "direccion", length = 255)
    private String direccion;

    @Size(max = 100)
    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Size(max = 100)
    @Column(name = "provincia", length = 100)
    private String provincia;

    @Size(max = 100)
    @Column(name = "pais", length = 100)
    private String pais;

    @NotNull
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @NotNull
    @Column(name = "fecha_alta", nullable = false)
    private Instant fechaAlta;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private CondicionIva condicionIva;

    @ManyToOne(fetch = FetchType.LAZY)
    private TipoDocumento tipoDocumento;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Cliente id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Cliente nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return this.apellido;
    }

    public Cliente apellido(String apellido) {
        this.setApellido(apellido);
        return this;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNroDocumento() {
        return this.nroDocumento;
    }

    public Cliente nroDocumento(String nroDocumento) {
        this.setNroDocumento(nroDocumento);
        return this;
    }

    public void setNroDocumento(String nroDocumento) {
        this.nroDocumento = nroDocumento;
    }

    public String getTelefono() {
        return this.telefono;
    }

    public Cliente telefono(String telefono) {
        this.setTelefono(telefono);
        return this;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return this.email;
    }

    public Cliente email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return this.direccion;
    }

    public Cliente direccion(String direccion) {
        this.setDireccion(direccion);
        return this;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return this.ciudad;
    }

    public Cliente ciudad(String ciudad) {
        this.setCiudad(ciudad);
        return this;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getProvincia() {
        return this.provincia;
    }

    public Cliente provincia(String provincia) {
        this.setProvincia(provincia);
        return this;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getPais() {
        return this.pais;
    }

    public Cliente pais(String pais) {
        this.setPais(pais);
        return this;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Boolean getActivo() {
        return this.activo;
    }

    public Cliente activo(Boolean activo) {
        this.setActivo(activo);
        return this;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Instant getFechaAlta() {
        return this.fechaAlta;
    }

    public Cliente fechaAlta(Instant fechaAlta) {
        this.setFechaAlta(fechaAlta);
        return this;
    }

    public void setFechaAlta(Instant fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Cliente createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public Cliente lastModifiedDate(Instant lastModifiedDate) {
        this.setLastModifiedDate(lastModifiedDate);
        return this;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public CondicionIva getCondicionIva() {
        return this.condicionIva;
    }

    public void setCondicionIva(CondicionIva condicionIva) {
        this.condicionIva = condicionIva;
    }

    public Cliente condicionIva(CondicionIva condicionIva) {
        this.setCondicionIva(condicionIva);
        return this;
    }

    public TipoDocumento getTipoDocumento() {
        return this.tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public Cliente tipoDocumento(TipoDocumento tipoDocumento) {
        this.setTipoDocumento(tipoDocumento);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cliente)) {
            return false;
        }
        return getId() != null && getId().equals(((Cliente) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Cliente{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", apellido='" + getApellido() + "'" +
            ", nroDocumento='" + getNroDocumento() + "'" +
            ", telefono='" + getTelefono() + "'" +
            ", email='" + getEmail() + "'" +
            ", direccion='" + getDireccion() + "'" +
            ", ciudad='" + getCiudad() + "'" +
            ", provincia='" + getProvincia() + "'" +
            ", pais='" + getPais() + "'" +
            ", activo='" + getActivo() + "'" +
            ", fechaAlta='" + getFechaAlta() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            "}";
    }
}
